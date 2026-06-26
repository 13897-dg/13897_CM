package com.a13897.a21_duel.data.repository

import com.a13897.a21_duel.data.model.Partida
import com.a13897.a21_duel.data.model.Ronda
import com.a13897.a21_duel.data.model.EstadoPartida
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

class PartidaRepository(
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) {
    private val colecaoPartidas = db.collection("partidas")

    /** Cria uma nova partida (online ou vs IA) e devolve o id gerado. */
    suspend fun criarPartida(idJogador1: String, idJogador2: String): Result<String> {
        return try {
            val novaPartida = Partida(
                idJogador1 = idJogador1,
                idJogador2 = idJogador2,
                estado = EstadoPartida.EM_CURSO
            )
            val documentoRef = colecaoPartidas.add(novaPartida).await()
            Result.success(documentoRef.id)
        } catch (erro: Exception) {
            Result.failure(erro)
        }
    }

    /**
     * Observa a Partida em tempo real — qualquer alteração no Firestore
     * (ex: o oponente jogou, vidas mudaram) é emitida automaticamente aqui.
     */
    fun observarPartida(idPartida: String): Flow<Partida?> = callbackFlow {
        val listener = colecaoPartidas.document(idPartida)
            .addSnapshotListener { snapshot, erro ->
                if (erro != null) {
                    close(erro)
                    return@addSnapshotListener
                }
                val partida = snapshot?.toObject(Partida::class.java)
                trySend(partida)
            }

        // remove o listener automaticamente quando o Flow deixa de ser observado
        // (ex: o utilizador sai do ecrã de Jogo)
        awaitClose { listener.remove() }
    }

    /** Observa a Ronda actual de uma partida em tempo real. */
    fun observarRondaActual(idPartida: String, idRonda: String): Flow<Ronda?> = callbackFlow {
        val listener = colecaoPartidas.document(idPartida)
            .collection("rondas").document(idRonda)
            .addSnapshotListener { snapshot, erro ->
                if (erro != null) {
                    close(erro)
                    return@addSnapshotListener
                }
                trySend(snapshot?.toObject(Ronda::class.java))
            }

        awaitClose { listener.remove() }
    }

    /** Cria uma nova ronda dentro de uma partida. */
    suspend fun criarRonda(idPartida: String, numero: Int, aposta: Int): Result<String> {
        return try {
            val novaRonda = Ronda(
                idPartida = idPartida,
                numero = numero,
                aposta = aposta
            )
            val documentoRef = colecaoPartidas.document(idPartida)
                .collection("rondas").add(novaRonda).await()
            Result.success(documentoRef.id)
        } catch (erro: Exception) {
            Result.failure(erro)
        }
    }

    /** Actualiza as vidas dos dois jogadores após o fim de uma ronda. */
    suspend fun actualizarVidas(idPartida: String, vidasJogador1: Int, vidasJogador2: Int): Result<Unit> {
        return try {
            colecaoPartidas.document(idPartida)
                .update(
                    mapOf(
                        "vidasJogador1" to vidasJogador1,
                        "vidasJogador2" to vidasJogador2
                    )
                ).await()
            Result.success(Unit)
        } catch (erro: Exception) {
            Result.failure(erro)
        }
    }

    /** Marca a partida como terminada e regista o vencedor. */
    suspend fun terminarPartida(idPartida: String, idVencedor: String): Result<Unit> {
        return try {
            colecaoPartidas.document(idPartida)
                .update(
                    mapOf(
                        "estado" to EstadoPartida.TERMINADA,
                        "vencedor" to idVencedor
                    )
                ).await()
            Result.success(Unit)
        } catch (erro: Exception) {
            Result.failure(erro)
        }
    }
}
