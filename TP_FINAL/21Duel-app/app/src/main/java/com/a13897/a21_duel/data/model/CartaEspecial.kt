package com.a13897.a21_duel.data.model

data class CartaEspecial(
    val id: String = "",
    val nome: String = "",
    val descricao: String = "",
    val tipo: TipoEfeito = TipoEfeito.IMEDIATO,
    val idJogador: String = "",
    val usada: Boolean = false
)

enum class TipoEfeito { IMEDIATO, PASSIVO }
