package com.a13897.a21_duel.ui.lobby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a13897.a21_duel.data.repository.PartidaRepository
import com.a13897.a21_duel.data.repository.UtilizadorRepository
import com.google.firebase.firestore.FirebaseFirestore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await

sealed class EstadoLobby {
    object Inicial : EstadoLobby()
    object AProcurar : EstadoLobby()
    data class SalaCriada(val codigo: String) : EstadoLobby()
    data class Erro(val mensagem: String) : EstadoLobby()
    data class PartidaEncontrada(val idPartida: String) : EstadoLobby()
}

class LobbyViewModel(
    private val partidaRepository: PartidaRepository = PartidaRepository(),
    private val utilizadorRepository: UtilizadorRepository = UtilizadorRepository(),
    private val db: FirebaseFirestore = FirebaseFirestore.getInstance()
) : ViewModel() {

    private val _estado = MutableStateFlow<EstadoLobby>(EstadoLobby.Inicial)
    val estado: StateFlow<EstadoLobby> = _estado

    /**
     * Matchmaking simplificado: procura uma sala em espera na colecção "matchmaking".
     * Se encontrar, junta-se a ela e cria a partida. Se não encontrar, cria uma entrada
     * de espera para outro jogador encontrar.
     * Nota: implementação simples adequada a um protótipo académico — não lida com
     * concorrência avançada (dois jogadores a entrar exactamente ao mesmo tempo).
     */
    fun procurarPartida() {
        val meuId = utilizadorRepository.utilizadorActualId() ?: return
        _estado.value = EstadoLobby.AProcurar

        viewModelScope.launch {
            try {
                val espera = db.collection("matchmaking").limit(1).get().await()

                if (espera.isEmpty) {
                    // ninguém à espera — cria entrada e fica à espera
                    db.collection("matchmaking").document(meuId).set(mapOf("idJogador" to meuId)).await()
                } else {
                    // encontrou alguém à espera — cria a partida e remove a entrada de matchmaking
                    val documentoEspera = espera.documents.first()
                    val idOponente = documentoEspera.getString("idJogador") ?: return@launch
                    documentoEspera.reference.delete().await()

                    val resultado = partidaRepository.criarPartida(idOponente, meuId)
                    resultado.onSuccess { idPartida ->
                        _estado.value = EstadoLobby.PartidaEncontrada(idPartida)
                    }.onFailure { erro ->
                        _estado.value = EstadoLobby.Erro(erro.message ?: "Erro ao criar partida.")
                    }
                }
            } catch (erro: Exception) {
                _estado.value = EstadoLobby.Erro(erro.message ?: "Erro no matchmaking.")
            }
        }
    }

    fun cancelarProcura() {
        val meuId = utilizadorRepository.utilizadorActualId() ?: return
        viewModelScope.launch {
            db.collection("matchmaking").document(meuId).delete().await()
            _estado.value = EstadoLobby.Inicial
        }
    }

    /** Cria uma sala privada com um código simples de 6 caracteres. */
    fun criarSalaPrivada() {
        val meuId = utilizadorRepository.utilizadorActualId() ?: return
        val codigo = (100000..999999).random().toString()

        viewModelScope.launch {
            db.collection("salasPrivadas").document(codigo)
                .set(mapOf("idJogador1" to meuId)).await()
            _estado.value = EstadoLobby.SalaCriada(codigo)
        }
    }

    /** Entra numa sala privada existente através do código. */
    fun entrarComCodigo(codigo: String) {
        val meuId = utilizadorRepository.utilizadorActualId() ?: return

        viewModelScope.launch {
            try {
                val sala = db.collection("salasPrivadas").document(codigo).get().await()
                if (!sala.exists()) {
                    _estado.value = EstadoLobby.Erro("Código inválido.")
                    return@launch
                }

                val idJogador1 = sala.getString("idJogador1") ?: return@launch
                sala.reference.delete().await()

                val resultado = partidaRepository.criarPartida(idJogador1, meuId)
                resultado.onSuccess { idPartida ->
                    _estado.value = EstadoLobby.PartidaEncontrada(idPartida)
                }.onFailure { erro ->
                    _estado.value = EstadoLobby.Erro(erro.message ?: "Erro ao criar partida.")
                }
            } catch (erro: Exception) {
                _estado.value = EstadoLobby.Erro(erro.message ?: "Erro ao entrar na sala.")
            }
        }
    }
}
