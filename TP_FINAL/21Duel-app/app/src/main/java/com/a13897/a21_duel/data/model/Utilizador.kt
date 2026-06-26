package com.a13897.a21_duel.data.model

data class Utilizador(
    val id: String = "",
    val email: String = "",
    val username: String = "",
    val idAvatar: String = "default_avatar",
    val vitorias: Int = 0,
    val derrotas: Int = 0
)
// nota: "password" não entra aqui — é gerido directamente pelo Firebase Authentication,
// nunca se guarda password em texto no Firestore
