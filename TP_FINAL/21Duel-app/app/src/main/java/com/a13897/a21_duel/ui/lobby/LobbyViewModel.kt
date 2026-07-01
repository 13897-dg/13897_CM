package com.a13897.a21_duel.ui.lobby

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a13897.a21_duel.data.repository.PartidaRepository
import com.a13897.a21_duel.data.repository.UtilizadorRepository
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
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

    // Controladores para podermos parar de ouvir a BD quando cancelamos ou mudamos de ecrã
    private var listenerMatchmaking: ListenerRegistration? = null
    private var listenerSalaPrivada: ListenerRegistration? = null

    /**
     * Matchmaking: Jogador 1 fica a ouvir o próprio documento.
     * Jogador 2 cria a partida e escreve o ID nesse documento.
     */
    fun procurarPartida() {
        val meuId = utilizadorRepository.utilizadorActualId() ?: return
        _estado.value = EstadoLobby.AProcurar

        viewModelScope.launch {
            try {
                // Procura alguém à espera que AINDA NÃO TENHA recebido uma partida
                val espera = db.collection("matchmaking")
                    .whereEqualTo("idPartida", null)
                    .limit(1).get().await()

                if (espera.isEmpty) {
                    // 1. Sou o primeiro. Crio a entrada de espera com idPartida a null
                    val docRef = db.collection("matchmaking").document(meuId)
                    docRef.set(mapOf("idJogador1" to meuId, "idPartida" to null)).await()

                    // 2. Fico a OUVIR o meu próprio documento
                    listenerMatchmaking = docRef.addSnapshotListener { snapshot, erro ->
                        if (erro != null) return@addSnapshotListener

                        val idPartidaGerado = snapshot?.getString("idPartida")
                        if (idPartidaGerado != null) {
                            // Alguém encontrou-me e disse-me para ir para esta partida!
                            _estado.value = EstadoLobby.PartidaEncontrada(idPartidaGerado)
                            listenerMatchmaking?.remove()
                            docRef.delete() // Limpo a fila de espera
                        }
                    }
                } else {
                    // 1. Encontrei alguém à espera
                    val documentoEspera = espera.documents.first()
                    val idOponente = documentoEspera.getString("idJogador1") ?: return@launch

                    // 2. Crio a partida real no Firestore com nós os dois
                    val resultado = partidaRepository.criarPartida(idOponente, meuId)

                    resultado.onSuccess { idPartida ->
                        // 3. Aviso o outro jogador! Escrevo o ID no documento que ele está a ouvir
                        documentoEspera.reference.update("idPartida", idPartida).await()
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

        listenerMatchmaking?.remove() // Paramos de ouvir

        viewModelScope.launch {
            db.collection("matchmaking").document(meuId).delete().await()
            _estado.value = EstadoLobby.Inicial
        }
    }

    /** * Salas Privadas: Jogador 1 fica a ouvir o documento do código criado.
     */
    fun criarSalaPrivada() {
        val meuId = utilizadorRepository.utilizadorActualId() ?: return
        val codigo = (100000..999999).random().toString()

        viewModelScope.launch {
            val docRef = db.collection("salasPrivadas").document(codigo)
            docRef.set(mapOf("idJogador1" to meuId, "idPartida" to null)).await()

            _estado.value = EstadoLobby.SalaCriada(codigo)

            // Fica a ouvir o documento da sala para saber quando alguém introduz o código
            listenerSalaPrivada = docRef.addSnapshotListener { snapshot, erro ->
                if (erro != null) return@addSnapshotListener

                val idPartida = snapshot?.getString("idPartida")
                if (idPartida != null) {
                    _estado.value = EstadoLobby.PartidaEncontrada(idPartida)
                    listenerSalaPrivada?.remove()
                    docRef.delete() // Limpa a sala para mais ninguém entrar
                }
            }
        }
    }

    /** * Jogador 2 entra, cria a partida e partilha o ID com o Jogador 1.
     */
    fun entrarComCodigo(codigo: String) {
        val meuId = utilizadorRepository.utilizadorActualId() ?: return

        viewModelScope.launch {
            try {
                val salaRef = db.collection("salasPrivadas").document(codigo)
                val sala = salaRef.get().await()

                if (!sala.exists()) {
                    _estado.value = EstadoLobby.Erro("Código inválido.")
                    return@launch
                }

                val idJogador1 = sala.getString("idJogador1") ?: return@launch

                val resultado = partidaRepository.criarPartida(idJogador1, meuId)
                resultado.onSuccess { idPartida ->
                    // Avisa o criador da sala que já pode ir jogar
                    salaRef.update("idPartida", idPartida).await()
                    _estado.value = EstadoLobby.PartidaEncontrada(idPartida)
                }.onFailure { erro ->
                    _estado.value = EstadoLobby.Erro(erro.message ?: "Erro ao criar partida.")
                }
            } catch (erro: Exception) {
                _estado.value = EstadoLobby.Erro(erro.message ?: "Erro ao entrar na sala.")
            }
        }
    }

    override fun onCleared() {
        super.onCleared()
        // Garante que não ficam listeners presos na memória se sairmos do ecrã
        listenerMatchmaking?.remove()
        listenerSalaPrivada?.remove()
    }
}