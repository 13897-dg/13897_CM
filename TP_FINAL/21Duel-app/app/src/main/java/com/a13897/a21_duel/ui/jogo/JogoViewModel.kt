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

const val ID_JOGADOR_IA = "IA"

class JogoViewModel(
    private val _mensagemAcaoOponente: MutableStateFlow<String?> = MutableStateFlow<String?>(null),
    val mensagemAcaoOponente: StateFlow<String?> = _mensagemAcaoOponente,
    private val _mensagemFimRonda: MutableStateFlow<String?> = MutableStateFlow<String?>(null),
    val mensagemFimRonda: StateFlow<String?> = _mensagemFimRonda,
    private var timerJob: Job? = null,
    private val partidaRepository: PartidaRepository = PartidaRepository(),
    private val utilizadorRepository: UtilizadorRepository = UtilizadorRepository()
) : ViewModel() {

    private val gson = Gson()

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

    private val _fimDeJogo = MutableStateFlow<String?>(null)
    val fimDeJogo: StateFlow<String?> = _fimDeJogo

    private val _meuJogadorInterno = MutableStateFlow("jogador1")
    val meuJogadorInterno: StateFlow<String> = _meuJogadorInterno

    private var idPartida: String = ""
    private var contraIA: Boolean = false
    private var idJogador1Real: String = ""
    private var idJogador2Real: String = ""

    // TRANCA DE SEGURANÇA PARA AS RONDAS DUPLAS
    private var aProcessarFimDeRonda = false

    private fun idRealDe(jogador: String): String {
        return if (jogador == "jogador1") idJogador1Real else idJogador2Real
    }

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
                partidaRepository.observarPartida(idPartida).collect { partida ->
                    if (partida != null && idJogador1Real.isEmpty()) {
                        idJogador1Real = partida.idJogador1
                        idJogador2Real = partida.idJogador2

                        val meuUid = utilizadorRepository.utilizadorActualId()
                        _meuJogadorInterno.value = if (meuUid == partida.idJogador1) "jogador1" else "jogador2"

                        if (_meuJogadorInterno.value == "jogador1") iniciarPrimeiraRonda()
                        observarPartidaOnline()
                    }
                }
            } else {
                idJogador1Real = idJogador1
                idJogador2Real = idJogador2
                _meuJogadorInterno.value = "jogador1"
                iniciarPrimeiraRonda()
            }
        }
    }

    private fun iniciarPrimeiraRonda(numeroRonda: Int = 1, aposta: Int = 1) {
        aProcessarFimDeRonda = false // Destranca a porta ao iniciar uma nova ronda

        val baralhoInicial = (1..11).toList()
        val (mao1, baralhoApos1) = distribuirMaoInicial(baralhoInicial)
        val (mao2, baralhoApos2) = distribuirMaoInicial(baralhoApos1)

        // ALTERNÂNCIA DE TURNOS: Ímpar = Jogador 1 | Par = Jogador 2
        val turnoInicial = if (numeroRonda % 2 != 0) "jogador1" else "jogador2"

        val novoEstado = EstadoRonda(
            numero = numeroRonda,
            aposta = aposta,
            maoJogador1 = mao1,
            maoJogador2 = mao2,
            cartasDisponiveis = baralhoApos2,
            turnoAtual = turnoInicial, // O turno inicial agora é dinâmico
            tempoRestanteSegundos = MotorJogo.TIMER_RONDA_SEGUNDOS
        )

        if (!contraIA && _meuJogadorInterno.value == "jogador2") return

        atualizarEstadoSync(novoEstado)
        iniciarTimer()

        // SE A IA COMEÇA A RONDA, TEMOS DE A MANDAR JOGAR!
        if (contraIA && turnoInicial == "jogador2") {
            viewModelScope.launch {
                _mensagemAcaoOponente.value = "A pensar..."
                delay(2500)
                jogadaDaIA()
            }
        }
    }

    private fun iniciarTimer() {
        timerJob?.cancel()
        timerJob = viewModelScope.launch {
            while (_estadoRonda.value.tempoRestanteSegundos > 0 && !_estadoRonda.value.rondaTerminou()) {
                delay(1000L)
                val estado = _estadoRonda.value
                _estadoRonda.value = estado.copy(tempoRestanteSegundos = estado.tempoRestanteSegundos - 1)

                if (_estadoRonda.value.tempoRestanteSegundos <= 0 && _estadoRonda.value.turnoAtual == _meuJogadorInterno.value) {
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
        return Pair(listOf(CartaMao(cartaVisivel, true), CartaMao(cartaOculta, false)), baralhoRestante)
    }

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

                        if (estadoRemoto.numero < _estadoRonda.value.numero) return@collect

                        val estadoComparacaoRemoto = estadoRemoto.copy(tempoRestanteSegundos = 0)
                        val estadoComparacaoLocal = _estadoRonda.value.copy(tempoRestanteSegundos = 0)

                        if (estadoComparacaoLocal != estadoComparacaoRemoto ||
                            _inventarioJogador1.value != partida.inventarioJogador1 ||
                            _inventarioJogador2.value != partida.inventarioJogador2) {

                            if (estadoComparacaoRemoto.rondaTerminou() && !estadoComparacaoLocal.rondaTerminou()) {
                                _estadoRonda.value = estadoRemoto
                                _inventarioJogador1.value = partida.inventarioJogador1
                                _inventarioJogador2.value = partida.inventarioJogador2
                                verificarFimDaRonda()
                                return@collect
                            }

                            val oponenteId = if (_meuJogadorInterno.value == "jogador1") "jogador2" else "jogador1"

                            val maoLocalOponente = if (oponenteId == "jogador1") estadoComparacaoLocal.maoJogador1 else estadoComparacaoLocal.maoJogador2
                            val maoRemotaOponente = if (oponenteId == "jogador1") estadoRemoto.maoJogador1 else estadoRemoto.maoJogador2

                            val stayLocalOponente = if (oponenteId == "jogador1") estadoComparacaoLocal.stayJogador1 else estadoComparacaoLocal.stayJogador2
                            val stayRemotoOponente = if (oponenteId == "jogador1") estadoRemoto.stayJogador1 else estadoRemoto.stayJogador2

                            val invLocalOponente = if (oponenteId == "jogador1") _inventarioJogador1.value else _inventarioJogador2.value
                            val invRemotoOponente = if (oponenteId == "jogador1") partida.inventarioJogador1 else partida.inventarioJogador2

                            var mensagemAcao: String? = null

                            if (estadoComparacaoLocal.numero == estadoRemoto.numero && estadoComparacaoLocal.turnoAtual == oponenteId) {
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
                "Até 17" -> { objectivoBase = 17; cartaAteXActiva = nomeCarta }
                "Até 24" -> { objectivoBase = 24; cartaAteXActiva = nomeCarta }
                "Até 27" -> { objectivoBase = 27; cartaAteXActiva = nomeCarta }
                "Mais um" -> ajusteObjectivo += 1
                "Menos um" -> ajusteObjectivo -= 1
            }
        }

        val novoEstado = estado.copy(
            aposta = apostaBase.coerceAtLeast(0),
            objectivoBase = objectivoBase,
            ajusteObjectivo = ajusteObjectivo,
            cartaAteXActiva = cartaAteXActiva
        )
        atualizarEstadoSync(novoEstado)
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

    private fun verificarFimDaRonda() {
        if (aProcessarFimDeRonda) return // BLOQUEIO ATIVADO! Impede chamadas duplas do Firebase
        val estado = _estadoRonda.value
        if (!estado.rondaTerminou()) return
        aProcessarFimDeRonda = true // TRANCA A PORTA

        timerJob?.cancel()
        val resultado = MotorJogo.decidirVencedorRonda(estado.pontuacaoJogador1(), estado.pontuacaoJogador2(), estado.objectivoActual)

        val souO1 = _meuJogadorInterno.value == "jogador1"

        val perdedor = when (resultado) {
            is ResultadoRonda.VenceJogador1 -> "jogador2"
            is ResultadoRonda.VenceJogador2 -> "jogador1"
            is ResultadoRonda.Empate -> null
        }

        val mensagem = when (resultado) {
            is ResultadoRonda.VenceJogador1 -> if (souO1) "Ganhaste a ronda!" else "O oponente ganhou a ronda."
            is ResultadoRonda.VenceJogador2 -> if (!souO1) "Ganhaste a ronda!" else "O oponente ganhou a ronda."
            is ResultadoRonda.Empate -> "Empate! Ninguém perde vidas."
        }

        _mensagemFimRonda.value = mensagem

        viewModelScope.launch {
            delay(3000)
            _mensagemFimRonda.value = null

            if (contraIA || _meuJogadorInterno.value == "jogador1") {
                if (perdedor != null) aplicarResultadoRonda(perdedor) else proximaRonda()
            }
        }
    }

    private fun aplicarResultadoRonda(perdedor: String) {
        val estado = _estadoRonda.value
        var aposta = estado.aposta

        val vencedor = oponenteDe(perdedor)
        val maoVencedor = if (vencedor == "jogador1") estado.maoJogador1 else estado.maoJogador2
        val inventarioVencedor = if (vencedor == "jogador1") _inventarioJogador1.value else _inventarioJogador2.value
        if (maoVencedor.any { it.valor == 8 } && inventarioVencedor.contains("Super 8")) aposta += 2

        var vidasPerdedor = if (perdedor == "jogador1") _vidasJogador1.value else _vidasJogador2.value
        vidasPerdedor = MotorJogo.aplicarPerdaVidas(vidasPerdedor, aposta)

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

    private fun proximaRonda() {
        val estadoAnterior = _estadoRonda.value
        val proximoNumero = estadoAnterior.numero + 1
        val novaAposta = MotorJogo.calcularProximaAposta(estadoAnterior.aposta)

        if (contraIA || _meuJogadorInterno.value == "jogador1") {
            if (proximoNumero >= 2) {
                val numCartas = if (proximoNumero % 2 == 0) 1 else 2
                for (i in 1..numCartas) {
                    adicionarCartaAoInventario("jogador1", RegistoCartasEspeciais.todas.random().nome)
                    adicionarCartaAoInventario("jogador2", RegistoCartasEspeciais.todas.random().nome)
                }
            }
            iniciarPrimeiraRonda(numeroRonda = proximoNumero, aposta = novaAposta)
        }
    }

    private fun terminarJogo(vencedor: String) {
        if (contraIA || _meuJogadorInterno.value == "jogador1") {
            val idVencedorReal = idRealDe(vencedor)
            viewModelScope.launch {
                // PRIMEIRO ASSEGURA QUE A DB FOI ATUALIZADA
                partidaRepository.terminarPartida(idPartida, idVencedorReal)
                val idUtilizador = utilizadorRepository.utilizadorActualId()
                if (idUtilizador != null) {
                    utilizadorRepository.actualizarEstatisticas(idUtilizador, venceu = (idVencedorReal == idUtilizador))
                }

                // SÓ MUDA DE ECRÃ APÓS A GRAVAÇÃO SER UM SUCESSO!
                _fimDeJogo.value = vencedor
            }
        }
    }
}