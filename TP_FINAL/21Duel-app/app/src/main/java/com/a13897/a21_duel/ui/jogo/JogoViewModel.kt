package com.a13897.a21_duel.ui.jogo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a13897.a21_duel.data.model.EstadoPartida
import com.a13897.a21_duel.data.repository.PartidaRepository
import com.a13897.a21_duel.data.repository.UtilizadorRepository
import com.a13897.a21_duel.game.*
import com.google.gson.Gson
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

/** Identificador fixo usado como idJogador2 quando a partida é contra a IA local. */
const val ID_JOGADOR_IA = "IA"

/**
 * ViewModel principal do ecrã de Jogo.
 *
 * Responsabilidades:
 * - Gerir o estado local da ronda (EstadoRonda) e expô-lo à UI via StateFlow
 * - Sincronizar o estado com o Firestore em modo online (só o Host escreve)
 * - Processar as acções do jogador (pedir carta, ficar, jogar carta especial)
 * - Executar a lógica da IA em modo local
 * - Detectar e processar o fim de ronda e fim de jogo
 *
 * Arquitectura Multiplayer — Fonte de Verdade Única:
 * Apenas o Host (Jogador 1) escreve o estado da ronda no Firestore.
 * O Cliente (Jogador 2) observa via SnapshotListener e aplica o estado recebido localmente.
 * Isto evita conflitos de escrita e race conditions entre os dois clientes.
 */
class JogoViewModel(
    private val _mensagemAcaoOponente: MutableStateFlow<String?> = MutableStateFlow<String?>(null),
    val mensagemAcaoOponente: StateFlow<String?> = _mensagemAcaoOponente,
    private val _mensagemFimRonda: MutableStateFlow<String?> = MutableStateFlow<String?>(null),
    val mensagemFimRonda: StateFlow<String?> = _mensagemFimRonda,
    private var timerJob: Job? = null,
    private val partidaRepository: PartidaRepository = PartidaRepository(),
    private val utilizadorRepository: UtilizadorRepository = UtilizadorRepository()
) : ViewModel() {

    /** Gson usado para serializar/desserializar o EstadoRonda para/de JSON no Firestore. */
    private val gson = Gson()

    /** Estado completo da ronda actual (mãos, baralho, objectivo, aposta, turno, timer). */
    private val _estadoRonda = MutableStateFlow(EstadoRonda(numero = 1, aposta = 1))
    val estadoRonda: StateFlow<EstadoRonda> = _estadoRonda

    /** Vidas actuais de cada jogador — actualizadas no Firestore no fim de cada ronda. */
    private val _vidasJogador1 = MutableStateFlow(MotorJogo.VIDAS_INICIAIS)
    val vidasJogador1: StateFlow<Int> = _vidasJogador1

    private val _vidasJogador2 = MutableStateFlow(MotorJogo.VIDAS_INICIAIS)
    val vidasJogador2: StateFlow<Int> = _vidasJogador2

    /** Inventários de cartas especiais de cada jogador — persistidos no documento da Partida. */
    private val _inventarioJogador1 = MutableStateFlow<List<String>>(emptyList())
    val inventarioJogador1: StateFlow<List<String>> = _inventarioJogador1

    private val _inventarioJogador2 = MutableStateFlow<List<String>>(emptyList())
    val inventarioJogador2: StateFlow<List<String>> = _inventarioJogador2

    /**
     * Sinaliza o fim do jogo — quando não é null, o ecrã navega para Resultados.
     * Contém "jogador1" ou "jogador2" (identificador interno do vencedor).
     */
    private val _fimDeJogo = MutableStateFlow<String?>(null)
    val fimDeJogo: StateFlow<String?> = _fimDeJogo

    /**
     * Identifica qual o papel deste cliente na partida online.
     * "jogador1" = Host (escreve no Firestore)
     * "jogador2" = Cliente (só lê do Firestore)
     * No modo vs IA é sempre "jogador1".
     */
    private val _meuJogadorInterno = MutableStateFlow("jogador1")
    val meuJogadorInterno: StateFlow<String> = _meuJogadorInterno

    private var idPartida: String = ""
    private var contraIA: Boolean = false

    /** UIDs reais dos jogadores no Firebase Authentication. */
    private var idJogador1Real: String = ""
    private var idJogador2Real: String = ""

    /**
     * TRANCA DE SEGURANÇA contra "rondas duplas".
     *
     * Problema: o Firebase pode disparar múltiplos snapshots para o mesmo update
     * devido a latência de rede, fazendo com que verificarFimDaRonda() seja chamada
     * mais do que uma vez para a mesma ronda.
     * Solução: esta flag bloqueia qualquer chamada subsequente após a primeira.
     * É reposta a false no início de cada nova ronda.
     */
    private var aProcessarFimDeRonda = false

    /** Converte o identificador interno ("jogador1"/"jogador2") para o UID real do Firebase. */
    private fun idRealDe(jogador: String): String {
        return if (jogador == "jogador1") idJogador1Real else idJogador2Real
    }

    /**
     * Actualiza o estado local e, se for modo online E este cliente for o Host,
     * serializa o EstadoRonda para JSON e guarda no Firestore.
     * O Cliente nunca chama esta função directamente para escrever — só o Host escreve.
     */
    private fun atualizarEstadoSync(novoEstado: EstadoRonda) {
        _estadoRonda.value = novoEstado
        if (!contraIA) {
            viewModelScope.launch {
                val json = gson.toJson(novoEstado)
                partidaRepository.atualizarEstadoJogo(
                    idPartida, json, _inventarioJogador1.value, _inventarioJogador2.value
                )
            }
        }
    }

    /**
     * Ponto de entrada do ecrã de Jogo — chamado pelo JogoScreen ao entrar.
     *
     * Fluxo online:
     * 1. Determina o papel deste cliente (Host ou Cliente) comparando o UID actual
     *    com o idJogador1 guardado no documento da Partida.
     * 2. Se for Host, gera as cartas iniciais e começa a escrever no Firestore.
     * 3. Se for Cliente, fica apenas a observar o Firestore.
     *
     * Fluxo vs IA:
     * 1. Este cliente é sempre o Host (jogador1).
     * 2. Gera as cartas iniciais e executa a lógica da IA localmente.
     */
    fun iniciar(idPartidaRecebido: String, idJogador1: String, idJogador2: String, ehContraIA: Boolean) {
        viewModelScope.launch {
            if (idPartidaRecebido.isEmpty()) {
                val resultado = partidaRepository.criarPartida(idJogador1, idJogador2)
                resultado.onSuccess { novoId -> idPartida = novoId }
            } else {
                idPartida = idPartidaRecebido
            }

            contraIA = ehContraIA

            if (!ehContraIA && idJogador1 == "online") {
                // Modo online: lê os UIDs reais do documento da Partida e determina o papel
                partidaRepository.observarPartida(idPartida).collect { partida ->
                    if (partida != null && idJogador1Real.isEmpty()) {
                        idJogador1Real = partida.idJogador1
                        idJogador2Real = partida.idJogador2

                        val meuUid = utilizadorRepository.utilizadorActualId()
                        _meuJogadorInterno.value = if (meuUid == partida.idJogador1) "jogador1" else "jogador2"

                        // Só o Host gera as cartas iniciais e escreve o primeiro estado
                        if (_meuJogadorInterno.value == "jogador1") iniciarPrimeiraRonda()
                        observarPartidaOnline()
                    }
                }
            } else {
                // Modo vs IA: sempre Host, UIDs já conhecidos
                idJogador1Real = idJogador1
                idJogador2Real = idJogador2
                _meuJogadorInterno.value = "jogador1"
                iniciarPrimeiraRonda()
            }
        }
    }

    /**
     * Inicializa uma nova ronda — distribui as cartas iniciais, define o turno inicial
     * e guarda o estado no Firestore (via atualizarEstadoSync).
     *
     * Alternância de turnos: rondas ímpares começam no Jogador 1 (Host),
     * rondas pares começam no Jogador 2 (Cliente) — garante equidade ao longo da partida.
     *
     * Se for modo online e este cliente for o Cliente (não o Host), não faz nada —
     * o estado inicial virá do Firestore via observarPartidaOnline().
     */
    private fun iniciarPrimeiraRonda(numeroRonda: Int = 1, aposta: Int = 1) {
        aProcessarFimDeRonda = false // Destrancar a porta para a nova ronda

        val baralhoInicial = (1..11).toList()
        val (mao1, baralhoApos1) = distribuirMaoInicial(baralhoInicial)
        val (mao2, baralhoApos2) = distribuirMaoInicial(baralhoApos1)

        // Rondas ímpares: começa o Jogador 1 | Rondas pares: começa o Jogador 2
        val turnoInicial = if (numeroRonda % 2 != 0) "jogador1" else "jogador2"

        val novoEstado = EstadoRonda(
            numero = numeroRonda,
            aposta = aposta,
            maoJogador1 = mao1,
            maoJogador2 = mao2,
            cartasDisponiveis = baralhoApos2,
            turnoAtual = turnoInicial,
            tempoRestanteSegundos = MotorJogo.TIMER_RONDA_SEGUNDOS
        )

        // O Cliente online não gera estado — aguarda o Firestore
        if (!contraIA && _meuJogadorInterno.value == "jogador2") return

        atualizarEstadoSync(novoEstado)
        iniciarTimer()

        // Se a IA começa a ronda (ronda par), dispara a sua jogada imediatamente
        if (contraIA && turnoInicial == "jogador2") {
            viewModelScope.launch {
                _mensagemAcaoOponente.value = "A pensar..."
                delay(2500)
                jogadaDaIA()
            }
        }
    }

    /**
     * Gere o timer de cada turno (60 segundos).
     * Cancela o timer anterior antes de iniciar um novo (evita timers paralelos).
     * Se o tempo esgotar e for a vez deste jogador, dá stay automático.
     */
    private fun iniciarTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_estadoRonda.value.tempoRestanteSegundos > 0 && !_estadoRonda.value.rondaTerminou()) {
                delay(1000L)
                val estado = _estadoRonda.value
                _estadoRonda.value = estado.copy(tempoRestanteSegundos = estado.tempoRestanteSegundos - 1)

                // Timeout: dá stay automático se o tempo esgotar na vez deste jogador
                if (_estadoRonda.value.tempoRestanteSegundos <= 0 && _estadoRonda.value.turnoAtual == _meuJogadorInterno.value) {
                    ficar(_estadoRonda.value.turnoAtual)
                }
            }
        }
    }

    /**
     * Distribui 2 cartas iniciais a um jogador a partir do baralho fornecido.
     * A primeira carta é visível, a segunda é oculta (só o próprio jogador sabe o valor).
     * Devolve o par (mão, baralho restante).
     */
    private fun distribuirMaoInicial(baralho: List<Int>): Pair<List<CartaMao>, List<Int>> {
        var baralhoRestante = baralho
        val cartaVisivel = baralhoRestante.random()
        baralhoRestante = MotorJogo.removerCartaDoBaralho(baralhoRestante, cartaVisivel)
        val cartaOculta = baralhoRestante.random()
        baralhoRestante = MotorJogo.removerCartaDoBaralho(baralhoRestante, cartaOculta)
        return Pair(listOf(CartaMao(cartaVisivel, true), CartaMao(cartaOculta, false)), baralhoRestante)
    }

    /**
     * Observa o documento da Partida no Firestore em tempo real (modo online).
     *
     * A cada snapshot recebido:
     * 1. Verifica se o jogo terminou (campo "estado" == TERMINADA)
     * 2. Actualiza as vidas locais
     * 3. Desserializa o estadoRondaJson e compara com o estado local
     * 4. Se o estado remoto for diferente do local, infere o que o oponente fez
     *    (pediu carta, deu stay, usou carta especial) e mostra uma mensagem ao jogador
     * 5. Aplica o novo estado e reinicia o timer
     *
     * Nota: o timer é excluído da comparação (copy(tempoRestanteSegundos = 0))
     * para evitar falsos positivos causados pela diferença de tempo entre clientes.
     */
    private fun observarPartidaOnline() {
        viewModelScope.launch {
            partidaRepository.observarPartida(idPartida).collect { partida ->
                if (partida != null) {
                    if (partida.estado == EstadoPartida.TERMINADA && partida.vencedor != null) {
                        _fimDeJogo.value = partida.vencedor
                        return@collect
                    }

                    _vidasJogador1.value = partida.vidasJogador1
                    _vidasJogador2.value = partida.vidasJogador2

                    if (partida.estadoRondaJson.isNotEmpty()) {
                        val estadoRemoto = gson.fromJson(partida.estadoRondaJson, EstadoRonda::class.java)

                        // Ignora estados de rondas anteriores (podem chegar com atraso)
                        if (estadoRemoto.numero < _estadoRonda.value.numero) return@collect

                        // Compara ignorando o timer — diferenças de timer não são jogadas reais
                        val estadoComparacaoRemoto = estadoRemoto.copy(tempoRestanteSegundos = 0)
                        val estadoComparacaoLocal = _estadoRonda.value.copy(tempoRestanteSegundos = 0)

                        if (estadoComparacaoLocal != estadoComparacaoRemoto ||
                            _inventarioJogador1.value != partida.inventarioJogador1 ||
                            _inventarioJogador2.value != partida.inventarioJogador2) {

                            // Se o estado remoto já terminou a ronda mas o local ainda não,
                            // aplica directamente e processa o fim da ronda
                            if (estadoComparacaoRemoto.rondaTerminou() && !estadoComparacaoLocal.rondaTerminou()) {
                                _estadoRonda.value = estadoRemoto
                                _inventarioJogador1.value = partida.inventarioJogador1
                                _inventarioJogador2.value = partida.inventarioJogador2
                                verificarFimDaRonda()
                                return@collect
                            }

                            // Inferir o que o oponente fez para mostrar mensagem ao jogador local
                            val oponenteId = if (_meuJogadorInterno.value == "jogador1") "jogador2" else "jogador1"

                            val maoLocalOponente = if (oponenteId == "jogador1") estadoComparacaoLocal.maoJogador1 else estadoComparacaoLocal.maoJogador2
                            val maoRemotaOponente = if (oponenteId == "jogador1") estadoRemoto.maoJogador1 else estadoRemoto.maoJogador2

                            val stayLocalOponente = if (oponenteId == "jogador1") estadoComparacaoLocal.stayJogador1 else estadoComparacaoLocal.stayJogador2
                            val stayRemotoOponente = if (oponenteId == "jogador1") estadoRemoto.stayJogador1 else estadoRemoto.stayJogador2

                            val invLocalOponente = if (oponenteId == "jogador1") _inventarioJogador1.value else _inventarioJogador2.value
                            val invRemotoOponente = if (oponenteId == "jogador1") partida.inventarioJogador1 else partida.inventarioJogador2

                            var mensagemAcao: String? = null

                            if (estadoComparacaoLocal.numero == estadoRemoto.numero && estadoComparacaoLocal.turnoAtual == oponenteId) {
                                // Compara inventários: se o oponente gastou uma carta, sabe-se qual
                                val cartasGastas = invLocalOponente.toMutableList()
                                invRemotoOponente.forEach { cartasGastas.remove(it) }

                                if (cartasGastas.isNotEmpty()) {
                                    mensagemAcao = "Usou carta: ${cartasGastas.first()}"
                                } else if (maoRemotaOponente.size > maoLocalOponente.size) {
                                    mensagemAcao = "Pediu carta."
                                } else if (!stayLocalOponente && stayRemotoOponente) {
                                    mensagemAcao = "Parou (Stay)."
                                }
                            }

                            if (mensagemAcao != null) {
                                _mensagemAcaoOponente.value = mensagemAcao
                                delay(2000)
                                _mensagemAcaoOponente.value = null
                            }

                            // Aplica o estado remoto e reinicia o timer
                            _estadoRonda.value = estadoRemoto
                            _inventarioJogador1.value = partida.inventarioJogador1
                            _inventarioJogador2.value = partida.inventarioJogador2

                            iniciarTimer()
                        }
                    }
                }
            }
        }
    }

    /**
     * O jogador pede uma carta ao baralho.
     * Remove uma carta aleatória das cartasDisponiveis, adiciona à mão do jogador,
     * e repõe os stays a false (pedir carta cancela um stay anterior).
     */
    fun pedirCarta(jogador: String) {
        val estado = _estadoRonda.value
        if (!MotorJogo.baralhoTemCartas(estado.cartasDisponiveis)) return

        val carta = estado.cartasDisponiveis.random()
        val novoBaralho = MotorJogo.removerCartaDoBaralho(estado.cartasDisponiveis, carta)
        val novaCarta = CartaMao(valor = carta, visivel = true)

        val estadoAposCarta = adicionarCartaAMao(estado, jogador, novaCarta).copy(
            cartasDisponiveis = novoBaralho, stayJogador1 = false, stayJogador2 = false
        )
        _estadoRonda.value = estadoAposCarta
        depoisDaJogada(jogador)
    }

    /**
     * O jogador dá stay (fica sem pedir carta).
     * Se ambos tiverem dado stay, a ronda termina imediatamente —
     * o estado é sincronizado e verificarFimDaRonda() é chamado.
     */
    fun ficar(jogador: String) {
        val estado = _estadoRonda.value
        _estadoRonda.value = if (jogador == "jogador1") estado.copy(stayJogador1 = true) else estado.copy(stayJogador2 = true)

        if (_estadoRonda.value.rondaTerminou()) {
            atualizarEstadoSync(_estadoRonda.value)
            verificarFimDaRonda()
        } else {
            depoisDaJogada(jogador)
        }
    }

    /**
     * O jogador joga uma carta especial do inventário.
     *
     * Fluxo:
     * 1. Obtém a implementação da carta pelo nome (via RegistoCartasEspeciais)
     * 2. Aplica o efeito sobre o EstadoRonda (cada carta sabe aplicar o seu próprio efeito)
     * 3. Se for passiva, regista-a em cartasEspeciaisEmCampo
     * 4. Remove do inventário
     * 5. Trata efeitos de inventário (Massacre, Amizade, Renasce dão cartas extra)
     * 6. Recalcula aposta e objectivo a partir das cartas activas em campo
     */
    fun jogarCartaEspecial(jogador: String, nomeCarta: String) {
        val carta = RegistoCartasEspeciais.porNome(nomeCarta) ?: return
        var estado = _estadoRonda.value

        estado = carta.aplicar(estado, jogador)

        if (carta.tipo == TipoEfeitoCarta.PASSIVO) {
            estado = estado.copy(cartasEspeciaisEmCampo = estado.cartasEspeciaisEmCampo + nomeCarta)
        }

        estado = estado.copy(stayJogador1 = false, stayJogador2 = false)
        _estadoRonda.value = estado

        removerCartaDoInventario(jogador, nomeCarta)

        // Efeitos de inventário — tratados aqui porque o EstadoRonda não gere inventários
        when (nomeCarta) {
            "Massacre" -> adicionarCartaAoInventario(jogador, RegistoCartasEspeciais.todas.random().nome)
            "Amizade" -> {
                adicionarCartaAoInventario("jogador1", RegistoCartasEspeciais.todas.random().nome)
                adicionarCartaAoInventario("jogador1", RegistoCartasEspeciais.todas.random().nome)
                adicionarCartaAoInventario("jogador2", RegistoCartasEspeciais.todas.random().nome)
                adicionarCartaAoInventario("jogador2", RegistoCartasEspeciais.todas.random().nome)
            }
            "Renasce" -> adicionarCartaAoInventario(jogador, RegistoCartasEspeciais.todas.random().nome)
        }

        recalcularApostaEObjectivoApartirDoCampo()
    }

    /**
     * Recalcula aposta e objectivo a partir de ZERO com base nas cartas passivas
     * actualmente em campo — em vez de aplicar deltas individuais.
     *
     * Porquê: se a carta "Destroi" remover uma "Espada" do campo, o +1 de aposta
     * dessa "Espada" tem de desaparecer. Recalcular do zero garante consistência
     * independentemente da ordem em que as cartas foram jogadas ou destruídas.
     *
     * A aposta base começa no número da ronda (ronda 1 = aposta 1, ronda 2 = aposta 2, etc.)
     * e as cartas de aposta ajustam sobre esse valor base.
     */
    private fun recalcularApostaEObjectivoApartirDoCampo() {
        val estado = _estadoRonda.value
        var apostaBase = estado.numero
        var ajusteObjectivo = 0
        var cartaAteXActiva: String? = null
        var objectivoBase = MotorJogo.OBJECTIVO_PADRAO

        estado.cartasEspeciaisEmCampo.forEach { nomeCarta ->
            when (nomeCarta) {
                "Escudo" -> apostaBase -= 1
                "Escudo+" -> apostaBase -= 2
                "Espada" -> apostaBase += 1
                "Espada+" -> apostaBase += 2
                "Massacre" -> apostaBase += 1
                // Cartas "até X": só pode haver uma activa — a última substitui a anterior
                "Até 17" -> { objectivoBase = 17; cartaAteXActiva = nomeCarta }
                "Até 24" -> { objectivoBase = 24; cartaAteXActiva = nomeCarta }
                "Até 27" -> { objectivoBase = 27; cartaAteXActiva = nomeCarta }
                // "Mais um"/"Menos um" acumulam sobre o objectivo actual
                "Mais um" -> ajusteObjectivo += 1
                "Menos um" -> ajusteObjectivo -= 1
            }
        }

        val novoEstado = estado.copy(
            aposta = apostaBase.coerceAtLeast(0), // aposta nunca desce abaixo de 0
            objectivoBase = objectivoBase,
            ajusteObjectivo = ajusteObjectivo,
            cartaAteXActiva = cartaAteXActiva
        )
        atualizarEstadoSync(novoEstado)
    }

    /** Remove uma carta do inventário do jogador (após ser jogada). */
    private fun removerCartaDoInventario(jogador: String, nomeCarta: String) {
        if (jogador == "jogador1") {
            _inventarioJogador1.value = _inventarioJogador1.value.toMutableList().apply { remove(nomeCarta) }
        } else {
            _inventarioJogador2.value = _inventarioJogador2.value.toMutableList().apply { remove(nomeCarta) }
        }
    }

    /** Adiciona uma carta ao inventário do jogador (recebida por Massacre, Amizade, Renasce, ou início de ronda). */
    private fun adicionarCartaAoInventario(jogador: String, nomeCarta: String) {
        if (jogador == "jogador1") {
            _inventarioJogador1.value = _inventarioJogador1.value + nomeCarta
        } else {
            _inventarioJogador2.value = _inventarioJogador2.value + nomeCarta
        }
    }

    /**
     * Chamado após cada jogada (pedir carta, carta especial).
     * Passa o turno para o próximo jogador, repõe o timer e sincroniza com o Firestore.
     * Se for vs IA e for a vez da IA, dispara a sua jogada com um delay visual.
     */
    private fun depoisDaJogada(quemAcabouDeJogar: String) {
        val proximo = if (quemAcabouDeJogar == "jogador1") "jogador2" else "jogador1"
        if (proximo == "jogador1") _mensagemAcaoOponente.value = null

        val novoEstado = _estadoRonda.value.copy(
            turnoAtual = proximo,
            tempoRestanteSegundos = MotorJogo.TIMER_RONDA_SEGUNDOS
        )
        atualizarEstadoSync(novoEstado)
        iniciarTimer()

        if (contraIA && proximo == "jogador2" && !_estadoRonda.value.rondaTerminou()) {
            viewModelScope.launch {
                _mensagemAcaoOponente.value = "A pensar..."
                delay(2500)
                jogadaDaIA()
            }
        }
    }

    /**
     * Executa a jogada da IA (modo vs IA).
     * Primeiro joga uma carta especial aleatória do inventário (se houver),
     * depois decide pedir carta ou ficar com base na lógica definida no ACD:
     * se pontuação ≤ objectivo - 3 → pede carta; caso contrário → fica.
     * Cada acção tem um delay visual de 2 segundos para simular "pensar".
     */
    private suspend fun jogadaDaIA() {
        val estado = _estadoRonda.value
        val pontuacaoIA = estado.pontuacaoJogador2()

        val cartaEspecialIA = IAJogador.escolherCartaEspecial(_inventarioJogador2.value)
        if (cartaEspecialIA != null) {
            _mensagemAcaoOponente.value = "Usou carta: $cartaEspecialIA"
            delay(2000)
            jogarCartaEspecial("jogador2", cartaEspecialIA)
        }

        when (IAJogador.decidirAccao(pontuacaoIA, _estadoRonda.value.objectivoActual)) {
            AccaoIA.PEDIR_CARTA -> {
                _mensagemAcaoOponente.value = "Pediu carta."
                delay(2000)
                pedirCarta("jogador2")
            }
            AccaoIA.FICAR -> {
                _mensagemAcaoOponente.value = "Parou (Stay)."
                delay(2000)
                ficar("jogador2")
            }
        }
    }

    /**
     * Verifica se a ronda terminou e processa o resultado.
     *
     * Protegida pela tranca aProcessarFimDeRonda para evitar processamento duplo
     * causado por snapshots repetidos do Firestore.
     *
     * Só o Host (ou a IA local) aplica o resultado da ronda e inicia a próxima —
     * o Cliente limita-se a mostrar a mensagem e aguarda o Firestore.
     */
    private fun verificarFimDaRonda() {
        if (aProcessarFimDeRonda) return // Tranca activa — já está a ser processado
        val estado = _estadoRonda.value
        if (!estado.rondaTerminou()) return
        aProcessarFimDeRonda = true // Tranca a porta

        timerJob?.cancel()
        val resultado = MotorJogo.decidirVencedorRonda(
            estado.pontuacaoJogador1(), estado.pontuacaoJogador2(), estado.objectivoActual
        )

        val souO1 = _meuJogadorInterno.value == "jogador1"

        val perdedor = when (resultado) {
            is ResultadoRonda.VenceJogador1 -> "jogador2"
            is ResultadoRonda.VenceJogador2 -> "jogador1"
            is ResultadoRonda.Empate -> null
        }

        // Mensagem de fim de ronda personalizada para cada cliente
        val mensagem = when (resultado) {
            is ResultadoRonda.VenceJogador1 -> if (souO1) "Ganhaste a ronda!" else "O oponente ganhou a ronda."
            is ResultadoRonda.VenceJogador2 -> if (!souO1) "Ganhaste a ronda!" else "O oponente ganhou a ronda."
            is ResultadoRonda.Empate -> "Empate! Ninguém perde vidas."
        }

        _mensagemFimRonda.value = mensagem

        viewModelScope.launch {
            delay(3000) // Mostra a mensagem 3 segundos antes de avançar
            _mensagemFimRonda.value = null

            // Só o Host aplica o resultado e inicia a próxima ronda
            if (contraIA || _meuJogadorInterno.value == "jogador1") {
                if (perdedor != null) aplicarResultadoRonda(perdedor) else proximaRonda()
            }
        }
    }

    /**
     * Aplica o resultado da ronda ao perdedor:
     * 1. Verifica se o vencedor tem o Super 8 na mão (bónus de +2 dano)
     * 2. Subtrai vidas ao perdedor (coerceAtLeast(0) garante que não fica negativo)
     * 3. Verifica se o perdedor tem a carta Resistente activa (recupera 1 vida se chegar a 0)
     * 4. Actualiza as vidas no Firestore
     * 5. Se o perdedor ficou com 0 vidas, termina o jogo; caso contrário, inicia nova ronda
     */
    private fun aplicarResultadoRonda(perdedor: String) {
        val estado = _estadoRonda.value
        var aposta = estado.aposta

        // Super 8: se o vencedor tiver o 8 e a carta Super 8, aplica +2 de dano extra
        val vencedor = oponenteDe(perdedor)
        val maoVencedor = if (vencedor == "jogador1") estado.maoJogador1 else estado.maoJogador2
        val inventarioVencedor = if (vencedor == "jogador1") _inventarioJogador1.value else _inventarioJogador2.value
        if (maoVencedor.any { it.valor == 8 } && inventarioVencedor.contains("Super 8")) aposta += 2

        var vidasPerdedor = if (perdedor == "jogador1") _vidasJogador1.value else _vidasJogador2.value
        vidasPerdedor = MotorJogo.aplicarPerdaVidas(vidasPerdedor, aposta)

        // Resistente: se chegou a 0 vidas e tem a carta, recupera 1 vida e a carta é consumida
        val inventarioPerdedor = if (perdedor == "jogador1") _inventarioJogador1.value else _inventarioJogador2.value
        if (vidasPerdedor <= 0 && inventarioPerdedor.contains("Resistente")) {
            vidasPerdedor = 1
            removerCartaDoInventario(perdedor, "Resistente")
        }

        if (perdedor == "jogador1") _vidasJogador1.value = vidasPerdedor else _vidasJogador2.value = vidasPerdedor

        if (contraIA || _meuJogadorInterno.value == "jogador1") {
            viewModelScope.launch {
                partidaRepository.actualizarVidas(idPartida, _vidasJogador1.value, _vidasJogador2.value)
                if (MotorJogo.jogoTerminou(vidasPerdedor)) {
                    terminarJogo(vencedor = oponenteDe(perdedor))
                } else {
                    proximaRonda()
                }
            }
        }
    }

    /**
     * Inicia a próxima ronda:
     * 1. Incrementa o número da ronda e a aposta
     * 2. Distribui cartas especiais a ambos os jogadores
     *    (ronda 2: 1 carta cada | ronda 3: 2 cartas cada | alterna entre 1 e 2)
     * 3. Chama iniciarPrimeiraRonda() com os novos parâmetros
     *
     * Nota: o campo das cartas especiais faz reset automaticamente porque
     * o EstadoRonda é criado de novo em iniciarPrimeiraRonda(); os inventários
     * mantêm-se porque são geridos separadamente nos StateFlow de inventário.
     */
    private fun proximaRonda() {
        val estadoAnterior = _estadoRonda.value
        val proximoNumero = estadoAnterior.numero + 1
        val novaAposta = MotorJogo.calcularProximaAposta(estadoAnterior.aposta)

        if (contraIA || _meuJogadorInterno.value == "jogador1") {
            if (proximoNumero >= 2) {
                // Alterna entre 1 e 2 cartas por ronda (rondas pares = 1, rondas ímpares = 2)
                val numCartas = if (proximoNumero % 2 == 0) 1 else 2
                for (i in 1..numCartas) {
                    adicionarCartaAoInventario("jogador1", RegistoCartasEspeciais.todas.random().nome)
                    adicionarCartaAoInventario("jogador2", RegistoCartasEspeciais.todas.random().nome)
                }
            }
            iniciarPrimeiraRonda(numeroRonda = proximoNumero, aposta = novaAposta)
        }
    }

    /**
     * Termina o jogo:
     * 1. Actualiza o documento da Partida no Firestore (estado = TERMINADA, vencedor = UID real)
     * 2. Actualiza as estatísticas do utilizador actual (vitórias/derrotas)
     * 3. Só depois de a gravação ser confirmada, sinaliza o fim do jogo à UI (_fimDeJogo)
     *
     * A ordem é importante: garantir que o Firestore está actualizado antes de navegar
     * para o ecrã de Resultados evita que o Cliente veja dados inconsistentes.
     */
    private fun terminarJogo(vencedor: String) {
        if (contraIA || _meuJogadorInterno.value == "jogador1") {
            val idVencedorReal = idRealDe(vencedor)
            viewModelScope.launch {
                partidaRepository.terminarPartida(idPartida, idVencedorReal)
                val idUtilizador = utilizadorRepository.utilizadorActualId()
                if (idUtilizador != null) {
                    utilizadorRepository.actualizarEstatisticas(idUtilizador, venceu = (idVencedorReal == idUtilizador))
                }
                // Só navega para Resultados após confirmação de gravação
                _fimDeJogo.value = vencedor
            }
        }
    }
}