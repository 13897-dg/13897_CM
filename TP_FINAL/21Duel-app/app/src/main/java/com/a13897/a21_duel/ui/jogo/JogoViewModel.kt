package com.a13897.a21_duel.ui.jogo

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a13897.a21_duel.data.repository.PartidaRepository
import com.a13897.a21_duel.data.repository.UtilizadorRepository
import com.a13897.a21_duel.game.*
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/** Identificador fixo usado como idJogador2 quando a partida é contra a IA. */
const val ID_JOGADOR_IA = "IA"

class JogoViewModel(
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

    /** Chamado pelo Screen ao entrar — prepara a partida (local para IA, observa Firestore se online). */
    fun iniciar(idPartidaRecebido: String, ehContraIA: Boolean) {
        idPartida = idPartidaRecebido
        contraIA = ehContraIA
        iniciarPrimeiraRonda()

        if (!contraIA) {
            observarPartidaOnline()
        }
    }

    private fun iniciarPrimeiraRonda() {
        val baralhoInicial = (1..11).toList()
        // cada jogador começa com 2 cartas: uma visível, uma oculta (ACD secção 3.1)
        val (mao1, baralhoApos1) = distribuirMaoInicial(baralhoInicial)
        val (mao2, baralhoApos2) = distribuirMaoInicial(baralhoApos1)

        _estadoRonda.value = EstadoRonda(
            numero = 1,
            aposta = 1,
            maoJogador1 = mao1,
            maoJogador2 = mao2,
            cartasDisponiveis = baralhoApos2
        )
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

    /** O jogador pede carta — escolhe uma aleatória do baralho disponível e remove-a. */
    fun pedirCarta(jogador: String) {
        val estado = _estadoRonda.value
        if (!MotorJogo.baralhoTemCartas(estado.cartasDisponiveis)) return // baralho vazio, não pode pedir

        val carta = estado.cartasDisponiveis.random()
        val novoBaralho = MotorJogo.removerCartaDoBaralho(estado.cartasDisponiveis, carta)
        val novaCarta = CartaMao(valor = carta, visivel = true)

        val novoEstado = adicionarCartaAMao(estado, jogador, novaCarta).copy(cartasDisponiveis = novoBaralho)
        _estadoRonda.value = novoEstado

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

        verificarFimDaRonda()
        depoisDaJogada(jogador)
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

        _estadoRonda.value = estado
        removerCartaDoInventario(jogador, nomeCarta)

        // efeitos que afectam o inventário (fora do EstadoRonda) tratados aqui:
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
        depoisDaJogada(jogador)
    }

    /**
     * Recalcula aposta e objectivo a partir de TODAS as cartas passivas actualmente em campo,
     * em vez de aplicar deltas isolados — assim destruir uma carta (Destroi) remove
     * exactamente o efeito dela, sem afectar as restantes.
     */
    private fun recalcularApostaEObjectivoApartirDoCampo() {
        val estado = _estadoRonda.value
        var apostaBase = 1 // valor inicial da ronda antes de cartas especiais
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
        if (contraIA && quemAcabouDeJogar == "jogador1" && !_estadoRonda.value.rondaTerminou()) {
            jogadaDaIA()
        }
    }

    private fun jogadaDaIA() {
        val estado = _estadoRonda.value
        val pontuacaoIA = estado.pontuacaoJogador2()

        // 1 carta especial aleatória do inventário da IA por ronda, se houver (ACD secção 4.1)
        val cartaEspecialIA = IAJogador.escolherCartaEspecial(_inventarioJogador2.value)
        if (cartaEspecialIA != null) {
            jogarCartaEspecial("jogador2", cartaEspecialIA)
        }

        when (IAJogador.decidirAccao(pontuacaoIA, _estadoRonda.value.objectivoActual)) {
            AccaoIA.PEDIR_CARTA -> pedirCarta("jogador2")
            AccaoIA.FICAR -> ficar("jogador2")
        }
    }

    // -----------------------------------------------------------------------
    // Fim de ronda / fim de jogo
    // -----------------------------------------------------------------------

    private fun verificarFimDaRonda() {
        val estado = _estadoRonda.value
        if (!estado.rondaTerminou()) return

        val resultado = MotorJogo.decidirVencedorRonda(
            estado.pontuacaoJogador1(), estado.pontuacaoJogador2(), estado.objectivoActual
        )

        when (resultado) {
            is ResultadoRonda.VenceJogador1 -> aplicarResultadoRonda(perdedor = "jogador2")
            is ResultadoRonda.VenceJogador2 -> aplicarResultadoRonda(perdedor = "jogador1")
            is ResultadoRonda.Empate -> proximaRonda() // ninguém perde vidas, mas aposta sobe na mesma
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
        val novaAposta = MotorJogo.calcularProximaAposta(estadoAnterior.aposta)
        // nova ronda: campo de especiais reset, inventário mantém-se (ACD secção 3.5)
        iniciarPrimeiraRonda()
        _estadoRonda.value = _estadoRonda.value.copy(
            numero = estadoAnterior.numero + 1,
            aposta = novaAposta
        )
    }

    private fun terminarJogo(vencedor: String) {
        _fimDeJogo.value = vencedor
        viewModelScope.launch {
            partidaRepository.terminarPartida(idPartida, vencedor)
            val idUtilizador = utilizadorRepository.utilizadorActualId()
            if (idUtilizador != null) {
                utilizadorRepository.actualizarEstatisticas(idUtilizador, venceu = (vencedor == "jogador1"))
            }
        }
    }
}
