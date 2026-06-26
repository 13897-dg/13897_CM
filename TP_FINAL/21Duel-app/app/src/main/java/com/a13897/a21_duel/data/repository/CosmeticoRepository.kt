package com.a13897.a21_duel.data.repository

import com.a13897.a21_duel.data.model.Cosmetico
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.tasks.await

class CosmeticoRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val colecao = db.collection("cosmeticos")

    /** Lê todos os cosméticos disponíveis na app (catálogo completo). */
    suspend fun listarTodos(): Result<List<Cosmetico>> {
        return try {
            val resultado = colecao.get().await()
            val cosmeticos = resultado.toObjects(Cosmetico::class.java)
            Result.success(cosmeticos)
        } catch (erro: Exception) {
            Result.failure(erro)
        }
    }

    /** Lê os ids dos cosméticos que um utilizador específico já desbloqueou. */
    suspend fun listarDesbloqueadosPorUtilizador(idUtilizador: String): Result<List<String>> {
        return try {
            val resultado = db.collection("utilizadores")
                .document(idUtilizador)
                .collection("cosmeticosDesbloqueados")
                .get()
                .await()
            val ids = resultado.documents.map { it.id }
            Result.success(ids)
        } catch (erro: Exception) {
            Result.failure(erro)
        }
    }

    /** Regista um novo cosmético como desbloqueado para o utilizador (ex: ao subscrever PRO). */
    suspend fun desbloquearCosmetico(idUtilizador: String, idCosmetico: String): Result<Unit> {
        return try {
            db.collection("utilizadores")
                .document(idUtilizador)
                .collection("cosmeticosDesbloqueados")
                .document(idCosmetico)
                .set(mapOf("desbloqueadoEm" to System.currentTimeMillis()))
                .await()
            Result.success(Unit)
        } catch (erro: Exception) {
            Result.failure(erro)
        }
    }
}
