package com.a13897.a21_duel.data.model

data class Cosmetico(
    val id: String = "",
    val nome: String = "",
    val tipo: TipoCosmetico = TipoCosmetico.AVATAR,
    val exclusivoPRO: Boolean = false,
    val desbloqueio: TipoDesbloqueio = TipoDesbloqueio.DEFAULT
)

enum class TipoCosmetico { AVATAR, BARALHO, TEMA }
enum class TipoDesbloqueio { DEFAULT, NIVEL, PRO }
