package com.a13897.a21_duel.data.model

data class Ronda(
    val id: String = "",
    val idPartida: String = "",
    val numero: Int = 1,
    val vencedor: String? = null, // null enquanto a ronda decorre ou em caso de empate
    val aposta: Int = 1,
    val objectivo: Int = 21,
    val cartasDisponiveis: List<Int> = (1..11).toList(),
    val timestampInicio: Long = System.currentTimeMillis()
)
