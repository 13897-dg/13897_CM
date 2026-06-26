package com.a13897.a21_duel.data.model

data class Partida(
    val id: String = "",
    val idJogador1: String = "",
    val idJogador2: String = "",
    val estado: EstadoPartida = EstadoPartida.EM_CURSO,
    val vencedor: String? = null,
    val vidasJogador1: Int = 8,
    val vidasJogador2: Int = 8
)

enum class EstadoPartida { EM_CURSO, TERMINADA }
