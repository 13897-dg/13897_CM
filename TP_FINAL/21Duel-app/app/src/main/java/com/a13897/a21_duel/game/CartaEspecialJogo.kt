package com.a13897.a21_duel.game

/**
 * Representa cada uma das 24 cartas especiais do jogo.
 * Cada carta sabe aplicar o seu próprio efeito sobre o EstadoRonda.
 *
 * "quemJogou" identifica o jogador que jogou a carta ("jogador1" ou "jogador2"),
 * necessário porque vários efeitos são direccionais (afectam só quem jogou, ou só o oponente).
 */
sealed class CartaEspecialJogo(val nome: String, val tipo: TipoEfeitoCarta) {

    /** Aplica o efeito desta carta sobre o estado da ronda, devolvendo um NOVO estado. */
    abstract fun aplicar(estado: EstadoRonda, quemJogou: String): EstadoRonda
}

enum class TipoEfeitoCarta { IMEDIATO, PASSIVO }

/** Devolve o jogador oposto a quem jogou a carta. */
internal fun oponenteDe(jogador: String): String =
    if (jogador == "jogador1") "jogador2" else "jogador1"