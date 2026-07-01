package com.a13897.a21_duel.data.model

data class Partida(
    val id: String = "",
    val idJogador1: String = "",
    val idJogador2: String = "",
    val estado: EstadoPartida = EstadoPartida.EM_CURSO,
    val vencedor: String? = null,
    val vidasJogador1: Int = 8,
    val vidasJogador2: Int = 8,
    val inventarioJogador1: List<String> = emptyList(),
    val inventarioJogador2: List<String> = emptyList(),
    val estadoRondaJson: String = ""
)

enum class EstadoPartida { EM_CURSO, TERMINADA }
