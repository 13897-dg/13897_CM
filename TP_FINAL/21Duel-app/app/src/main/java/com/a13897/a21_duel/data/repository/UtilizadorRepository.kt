package com.a13897.a21_duel.data.repository

import com.a13897.a21_duel.data.model.Utilizador
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class UtilizadorRepository(
    private val auth: FirebaseAuth = FirebaseAuth.getInstance(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val colecao = db.collection("utilizadores")

    /** UID do utilizador autenticado, ou null se não houver sessão activa. */
    fun utilizadorActualId(): String? = auth.currentUser?.uid

    /** Cria conta nova com email/password e o respectivo documento Utilizador no Firestore. */
    suspend fun registar(email: String, password: String, username: String): Result<Utilizador> {
        return try {
            val resultado = auth.createUserWithEmailAndPassword(email, password).await()
            val uid = resultado.user?.uid ?: return Result.failure(Exception("UID não disponível após registo"))

            val novoUtilizador = Utilizador(
                id = uid,
                email = email,
                username = username
            )

            colecao.document(uid).set(novoUtilizador).await()
            Result.success(novoUtilizador)
        } catch (erro: Exception) {
            Result.failure(erro)
        }
    }

    /** Faz login com email/password. */
    suspend fun login(email: String, password: String): Result<String> {
        return try {
            val resultado = auth.signInWithEmailAndPassword(email, password).await()
            val uid = resultado.user?.uid ?: return Result.failure(Exception("UID não disponível após login"))
            Result.success(uid)
        } catch (erro: Exception) {
            Result.failure(erro)
        }
    }

    fun logout() {
        auth.signOut()
    }

    /** Obtém os dados do documento Utilizador a partir do id. */
    suspend fun obterUtilizador(id: String): Result<Utilizador> {
        return try {
            val documento = colecao.document(id).get().await()
            val utilizador = documento.toObject(Utilizador::class.java)
                ?: return Result.failure(Exception("Utilizador não encontrado"))
            Result.success(utilizador)
        } catch (erro: Exception) {
            Result.failure(erro)
        }
    }

    /** Actualiza vitórias/derrotas após uma partida terminar. */
    suspend fun actualizarEstatisticas(id: String, venceu: Boolean): Result<Unit> {
        return try {
            val campo = if (venceu) "vitorias" else "derrotas"
            colecao.document(id)
                .update(campo, com.google.firebase.firestore.FieldValue.increment(1))
                .await()
            Result.success(Unit)
        } catch (erro: Exception) {
            Result.failure(erro)
        }
    }

    /** Actualiza o avatar escolhido pelo utilizador. */
    suspend fun actualizarAvatar(id: String, idAvatar: String): Result<Unit> {
        return try {
            colecao.document(id).update("idAvatar", idAvatar).await()
            Result.success(Unit)
        } catch (erro: Exception) {
            Result.failure(erro)
        }
    }
}
