package com.a13897.a21_duel.game

/**
 * Representa o estado completo de uma ronda em curso.
 * É este objecto que cada carta especial vai ler e modificar quando for jogada.
 * Mantido como classe imutável (val) — cada alteração devolve uma NOVA cópia,
 * o que torna o estado mais previsível e fácil de sincronizar com o Firestore.
 */
data class EstadoRonda(
    val numero: Int,
    val aposta: Int,
    val objectivoBase: Int = MotorJogo.OBJECTIVO_PADRAO,
    val ajusteObjectivo: Int = 0,           // soma de todas as "mais um" / "menos um" activas
    val cartaAteXActiva: String? = null,    // nome da carta "até X" actualmente em campo, se houver
    val maoJogador1: List<CartaMao> = emptyList(),
    val maoJogador2: List<CartaMao> = emptyList(),
    val cartasEspeciaisEmCampo: List<String> = emptyList(), // nomes das cartas passivas activas nesta ronda
    val cartasDisponiveis: List<Int> = (1..11).toList(),
    val stayJogador1: Boolean = false,
    val stayJogador2: Boolean = false,
    val turnoAtual: String = "jogador1",
    val tempoRestanteSegundos: Int = MotorJogo.TIMER_RONDA_SEGUNDOS
) {
    /** Objectivo efectivo desta ronda, já considerando "até X" e os ajustes "mais um"/"menos um". */
    val objectivoActual: Int
        get() = objectivoBase + ajusteObjectivo

    fun pontuacaoJogador1(): Int = MotorJogo.calcularPontuacao(maoJogador1.map { it.valor })
    fun pontuacaoJogador2(): Int = MotorJogo.calcularPontuacao(maoJogador2.map { it.valor })

    /** A ronda termina quando ambos deram stay (ou quando o baralho está vazio e ambos já não podem pedir). */
    fun rondaTerminou(): Boolean = stayJogador1 && stayJogador2
}

/** Uma carta na mão de um jogador — guarda também se está visível ou oculta. */
data class CartaMao(
    val valor: Int,
    val visivel: Boolean
)
