package com.a13897.a21_duel.data.model

data class SubscricaoPRO(
    val id: String = "",
    val idUtilizador: String = "",
    val dataInicio: Long = System.currentTimeMillis(),
    val dataRenovacao: Long = 0L,
    val activa: Boolean = false
)
