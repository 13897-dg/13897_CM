package com.a13897.a21_duel.ui.jogo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a13897.a21_duel.data.repository.PartidaRepository
import com.a13897.a21_duel.data.repository.UtilizadorRepository
import com.a13897.a21_duel.game.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay

/** Identificador fixo usado como idJogador2 quando a partida é contra a IA. */
const val ID_JOGADOR_IA = "IA"

class JogoViewModel(
    private val _meuJogadorInterno: MutableStateFlow<String> = MutableStateFlow("jogador1"),
    val meuJogadorInterno: StateFlow<String> = _meuJogadorInterno,
    private val _mensagemAcaoOponente: MutableStateFlow<String?> = MutableStateFlow<String?>(null),
    val mensagemAcaoOponente: StateFlow<String?> = _mensagemAcaoOponente,
    private val _mensagemFimRonda: MutableStateFlow<String?> = MutableStateFlow<String?>(null),
    val mensagemFimRonda: StateFlow<String?> = _mensagemFimRonda,
    private var timerJob: Job? = null,
    private val partidaRepository: PartidaRepository = PartidaRepository(),
    private val utilizadorRepository: UtilizadorRepository = UtilizadorRepository()
) : ViewModel() {

    private val _estadoRonda = MutableStateFlow(EstadoRonda(numero = 1, aposta = 1))
    val estadoRonda: StateFlow<EstadoRonda> = _estadoRonda

    private val _vidasJogador1 = MutableStateFlow(MotorJogo.VIDAS_INICIAIS)
    val vidasJogador1: StateFlow<Int> = _vidasJogador1

    private val _vidasJogador2 = MutableStateFlow(MotorJogo.VIDAS_INICIAIS)
    val vidasJogador2: StateFlow<Int> = _vidasJogador2

    private val _inventarioJogador1 = MutableStateFlow<List<String>>(emptyList())
    val inventarioJogador1: StateFlow<List<String>> = _inventarioJogador1

    private val _inventarioJogador2 = MutableStateFlow<List<String>>(emptyList())
    val inventarioJogador2: StateFlow<List<String>> = _inventarioJogador2

    private val _fimDeJogo = MutableStateFlow<String?>(null) // "jogador1" ou "jogador2" quando o jogo termina
    val fimDeJogo: StateFlow<String?> = _fimDeJogo

    private var idPartida: String = ""
    private var contraIA: Boolean = false
    private var idJogador1Real: String = ""
    private var idJogador2Real: String = ""

    /** Converte "jogador1"/"jogador2" (usado internamente) para o UID real correspondente. */
    private fun idRealDe(jogador: String): String {
        return if (jogador == "jogador1") idJogador1Real else idJogador2Real
    }

    /** Chamado pelo Screen ao entrar — prepara a partida (local para IA, observa Firestore se online). */
    fun iniciar(idPartidaRecebido: String, idJogador1: String, idJogador2: String, ehContraIA: Boolean) {
        viewModelScope.launch {
            // 1. Garantir que temos um documento no Firestore (resolve o ecrã de resultados infinito)
            if (idPartidaRecebido.isEmpty()) {
                val resultado = partidaRepository.criarPartida(idJogador1, idJogador2)
                resultado.onSuccess { novoId ->
                    idPartida = novoId
                }
            } else {
                idPartida = idPartidaRecebido
            }

            contraIA = ehContraIA

            // 2. Configurar a partida com base no modo de jogo
            if (!ehContraIA && idJogador1 == "online") {
                // --- MODO MULTIPLAYER ONLINE ---
                partidaRepository.observarPartida(idPartida).collect { partida ->
                    if (partida != null && idJogador1Real.isEmpty()) {
                        idJogador1Real = partida.idJogador1
                        idJogador2Real = partida.idJogador2

                        // Descobrir em que cadeira me sento (para a UI e permissões)
                        val meuUid = utilizadorRepository.utilizadorActualId()
                        _meuJogadorInterno.value = if (meuUid == partida.idJogador1) "jogador1" else "jogador2"

                        // APENAS O HOST (Jogador 1) gera o baralho inicial e distribui as cartas
                        if (_meuJogadorInterno.value == "jogador1") {
                            iniciarPrimeiraRonda()
                        }

                        observarPartidaOnline()
                    }
                }
            } else {
                // --- MODO CONTRA A IA ---
                idJogador1Real = idJogador1
                idJogador2Real = idJogador2
                _meuJogadorInterno.value = "jogador1" // Contra a IA és sempre o jogador 1

                iniciarPrimeiraRonda() // Como estás sozinho, inicias tu a ronda
            }
        }
    }

    private fun iniciarPrimeiraRonda(numeroRonda: Int = 1, aposta: Int = 1) {
        val baralhoInicial = (1..11).toList()
        val (mao1, baralhoApos1) = distribuirMaoInicial(baralhoInicial)
        val (mao2, baralhoApos2) = distribuirMaoInicial(baralhoApos1)

        _estadoRonda.value = EstadoRonda(
            numero = numeroRonda,
            aposta = aposta,
            maoJogador1 = mao1,
            maoJogador2 = mao2,
            cartasDisponiveis = baralhoApos2,
            turnoAtual = "jogador1",
            tempoRestanteSegundos = MotorJogo.TIMER_RONDA_SEGUNDOS
        )
        iniciarTimer()
    }

    private fun iniciarTimer() {
        timerJob?.cancel() // Cancela o timer anterior se existir
        timerJob = viewModelScope.launch {
            while (_estadoRonda.value.tempoRestanteSegundos > 0 && !_estadoRonda.value.rondaTerminou()) {
                delay(1000L)
                val estado = _estadoRonda.value
                _estadoRonda.value = estado.copy(tempoRestanteSegundos = estado.tempoRestanteSegundos - 1)

                // Se o tempo chegar a 0, dá stay automático
                if (_estadoRonda.value.tempoRestanteSegundos <= 0) {
                    ficar(_estadoRonda.value.turnoAtual)
                }
            }
        }
    }

    private fun distribuirMaoInicial(baralho: List<Int>): Pair<List<CartaMao>, List<Int>> {
        var baralhoRestante = baralho
        val cartaVisivel = baralhoRestante.random()
        baralhoRestante = MotorJogo.removerCartaDoBaralho(baralhoRestante, cartaVisivel)
        val cartaOculta = baralhoRestante.random()
        baralhoRestante = MotorJogo.removerCartaDoBaralho(baralhoRestante, cartaOculta)

        val mao = listOf(
            CartaMao(valor = cartaVisivel, visivel = true),
            CartaMao(valor = cartaOculta, visivel = false)
        )
        return Pair(mao, baralhoRestante)
    }

    private fun observarPartidaOnline() {
        viewModelScope.launch {
            partidaRepository.observarPartida(idPartida).collect { partida ->
                if (partida != null) {
                    _vidasJogador1.value = partida.vidasJogador1
                    _vidasJogador2.value = partida.vidasJogador2
                }
            }
        }
    }

    // -----------------------------------------------------------------------
    // Acções do jogador
    // -----------------------------------------------------------------------

    /** O jogador pede carta... */
    fun pedirCarta(jogador: String) {
        val estado = _estadoRonda.value
        if (!MotorJogo.baralhoTemCartas(estado.cartasDisponiveis)) return

        val carta = estado.cartasDisponiveis.random()
        val novoBaralho = MotorJogo.removerCartaDoBaralho(estado.cartasDisponiveis, carta)
        val novaCarta = CartaMao(valor = carta, visivel = true)

        // CORREÇÃO: Qualquer jogada limpa os stays anteriores de ambos!
        val estadoAposCarta = adicionarCartaAMao(estado, jogador, novaCarta).copy(
            cartasDisponiveis = novoBaralho,
            stayJogador1 = false,
            stayJogador2 = false
        )
        _estadoRonda.value = estadoAposCarta

        depoisDaJogada(jogador)
    }

    /** O jogador dá stay. */
    fun ficar(jogador: String) {
        val estado = _estadoRonda.value
        _estadoRonda.value = if (jogador == "jogador1") {
            estado.copy(stayJogador1 = true)
        } else {
            estado.copy(stayJogador2 = true)
        }

        // CORREÇÃO: Se ambos deram stay, a ronda termina. Senão, só passa o turno.
        if (_estadoRonda.value.rondaTerminou()) {
            verificarFimDaRonda()
        } else {
            depoisDaJogada(jogador)
        }
    }

    /**
     * O jogador joga uma carta especial do inventário.
     * Recalcula aposta/objectivo a partir de TODAS as cartas activas em campo
     * (ver decisão tomada com o utilizador sobre a carta "Destroi").
     */
    fun jogarCartaEspecial(jogador: String, nomeCarta: String) {
        val carta = RegistoCartasEspeciais.porNome(nomeCarta) ?: return
        var estado = _estadoRonda.value

        estado = carta.aplicar(estado, jogador)

        if (carta.tipo == TipoEfeitoCarta.PASSIVO) {
            estado = estado.copy(cartasEspeciaisEmCampo = estado.cartasEspeciaisEmCampo + nomeCarta)
        }

        // CORREÇÃO: Jogar uma carta especial também limpa os stays
        estado = estado.copy(stayJogador1 = false, stayJogador2 = false)

        _estadoRonda.value = estado
        removerCartaDoInventario(jogador, nomeCarta)

        // (Lógica de inventário mantida igual)
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
     * Recalcula aposta e objectivo a partir de TODAS as cartas passivas actualmente em campo,
     * em vez de aplicar deltas isolados — assim destruir uma carta (Destroi) remove
     * exactamente o efeito dela, sem afectar as restantes.
     */
    private fun recalcularApostaEObjectivoApartirDoCampo() {
        val estado = _estadoRonda.value
        var apostaBase = estado.numero // valor inicial da ronda antes de cartas especiais
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
                "Até 17" -> { objectivoBase = 17; cartaAteXActiva = nomeCarta }
                "Até 24" -> { objectivoBase = 24; cartaAteXActiva = nomeCarta }
                "Até 27" -> { objectivoBase = 27; cartaAteXActiva = nomeCarta }
                "Mais um" -> ajusteObjectivo += 1
                "Menos um" -> ajusteObjectivo -= 1
            }
        }

        _estadoRonda.value = estado.copy(
            aposta = apostaBase.coerceAtLeast(0),
            objectivoBase = objectivoBase,
            ajusteObjectivo = ajusteObjectivo,
            cartaAteXActiva = cartaAteXActiva
        )
    }

    private fun removerCartaDoInventario(jogador: String, nomeCarta: String) {
        if (jogador == "jogador1") {
            _inventarioJogador1.value = _inventarioJogador1.value.toMutableList().apply { remove(nomeCarta) }
        } else {
            _inventarioJogador2.value = _inventarioJogador2.value.toMutableList().apply { remove(nomeCarta) }
        }
    }

    private fun adicionarCartaAoInventario(jogador: String, nomeCarta: String) {
        if (jogador == "jogador1") {
            _inventarioJogador1.value = _inventarioJogador1.value + nomeCarta
        } else {
            _inventarioJogador2.value = _inventarioJogador2.value + nomeCarta
        }
    }

    /** Depois de cada jogada, se for vs IA e for a vez da IA, dispara a acção dela. */
    private fun depoisDaJogada(quemAcabouDeJogar: String) {
        val proximo = if (quemAcabouDeJogar == "jogador1") "jogador2" else "jogador1"

        // Limpa a mensagem do oponente quando a vez volta para ti
        if (proximo == "jogador1") _mensagemAcaoOponente.value = null

        _estadoRonda.value = _estadoRonda.value.copy(
            turnoAtual = proximo,
            tempoRestanteSegundos = MotorJogo.TIMER_RONDA_SEGUNDOS
        )
        iniciarTimer()

        if (contraIA && proximo == "jogador2" && !_estadoRonda.value.rondaTerminou()) {
            viewModelScope.launch {
                _mensagemAcaoOponente.value = "A pensar..."
                delay(2500) // MAIS TEMPO: Espera 2.5s antes da IA decidir o que fazer
                jogadaDaIA()
            }
        }
    }

    private suspend fun jogadaDaIA() {
        val estado = _estadoRonda.value
        val pontuacaoIA = estado.pontuacaoJogador2()

        val cartaEspecialIA = IAJogador.escolherCartaEspecial(_inventarioJogador2.value)
        if (cartaEspecialIA != null) {
            _mensagemAcaoOponente.value = "Usou carta: $cartaEspecialIA"
            delay(2000) // MAIS TEMPO: Dá 2s para veres que carta ele usou
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

    // -----------------------------------------------------------------------
    // Fim de ronda / fim de jogo
    // -----------------------------------------------------------------------

    private fun verificarFimDaRonda() {
        val estado = _estadoRonda.value
        if (!estado.rondaTerminou()) return

        // Pára o timer enquanto a ronda está em pausa a mostrar os resultados
        timerJob?.cancel()

        val resultado = MotorJogo.decidirVencedorRonda(
            estado.pontuacaoJogador1(), estado.pontuacaoJogador2(), estado.objectivoActual
        )

        val (mensagem, perdedor) = when (resultado) {
            is ResultadoRonda.VenceJogador1 -> "Ganhaste a ronda!" to "jogador2"
            is ResultadoRonda.VenceJogador2 -> "O oponente ganhou a ronda." to "jogador1"
            is ResultadoRonda.Empate -> "Empate! Ninguém perde vidas." to null
        }

        // Mostra a mensagem no ecrã
        _mensagemFimRonda.value = mensagem

        // Lança uma coroutine para fazer a pausa sem bloquear a app
        viewModelScope.launch {
            delay(3000) // Espera 3 segundos (como previsto no ADD)

            _mensagemFimRonda.value = null // Limpa a mensagem

            if (perdedor != null) {
                aplicarResultadoRonda(perdedor)
            } else {
                proximaRonda()
            }
        }
    }

    private fun aplicarResultadoRonda(perdedor: String) {
        val estado = _estadoRonda.value
        var aposta = estado.aposta

        // Super 8: se o vencedor tiver o 8 na mão, +2 de dano extra
        val vencedor = oponenteDe(perdedor)
        val maoVencedor = if (vencedor == "jogador1") estado.maoJogador1 else estado.maoJogador2
        val inventarioVencedor = if (vencedor == "jogador1") _inventarioJogador1.value else _inventarioJogador2.value
        if (maoVencedor.any { it.valor == 8 } && inventarioVencedor.contains("Super 8")) {
            aposta += 2
        }

        var vidasPerdedor = if (perdedor == "jogador1") _vidasJogador1.value else _vidasJogador2.value
        vidasPerdedor = MotorJogo.aplicarPerdaVidas(vidasPerdedor, aposta)

        // Resistente: se o perdedor chegar a 0 e tiver esta carta activa, recupera 1 vida
        val inventarioPerdedor = if (perdedor == "jogador1") _inventarioJogador1.value else _inventarioJogador2.value
        if (vidasPerdedor <= 0 && inventarioPerdedor.contains("Resistente")) {
            vidasPerdedor = 1
        }

        if (perdedor == "jogador1") _vidasJogador1.value = vidasPerdedor else _vidasJogador2.value = vidasPerdedor

        viewModelScope.launch {
            partidaRepository.actualizarVidas(idPartida, _vidasJogador1.value, _vidasJogador2.value)
        }

        if (MotorJogo.jogoTerminou(vidasPerdedor)) {
            terminarJogo(vencedor = oponenteDe(perdedor))
        } else {
            proximaRonda()
        }
    }

    private fun proximaRonda() {
        val estadoAnterior = _estadoRonda.value
        val proximoNumero = estadoAnterior.numero + 1
        val novaAposta = MotorJogo.calcularProximaAposta(estadoAnterior.aposta)

        // Distribui Cartas Especiais (Regra do ACD: alternar 1 e 2 cartas por ronda a partir da ronda 2)
        if (proximoNumero >= 2) {
            val numCartas = if (proximoNumero % 2 == 0) 1 else 2
            for (i in 1..numCartas) {
                adicionarCartaAoInventario("jogador1", RegistoCartasEspeciais.todas.random().nome)
                adicionarCartaAoInventario("jogador2", RegistoCartasEspeciais.todas.random().nome)
            }
        }

        iniciarPrimeiraRonda(numeroRonda = proximoNumero, aposta = novaAposta)
    }

    private fun terminarJogo(vencedor: String) {
        _fimDeJogo.value = vencedor
        val idVencedorReal = idRealDe(vencedor)
        viewModelScope.launch {
            partidaRepository.terminarPartida(idPartida, idVencedorReal)
            val idUtilizador = utilizadorRepository.utilizadorActualId()
            if (idUtilizador != null) {
                utilizadorRepository.actualizarEstatisticas(idUtilizador, venceu = (idVencedorReal == idUtilizador))
            }
        }
    }
}