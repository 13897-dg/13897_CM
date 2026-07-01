package com.a13897.a21_duel.ui.definicoes

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a13897.a21_duel.data.model.Utilizador
import com.a13897.a21_duel.data.repository.UtilizadorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class DefinicoesViewModel(
    private val utilizadorRepository: UtilizadorRepository = UtilizadorRepository()
) : ViewModel() {

    // Estado da UI: null enquanto carrega, com dados quando terminar
    private val _utilizador = MutableStateFlow<Utilizador?>(null)
    val utilizador: StateFlow<Utilizador?> = _utilizador

    // Estado para gerir erros (ex: falha a ler do Firestore)
    private val _erro = MutableStateFlow<String?>(null)
    val erro: StateFlow<String?> = _erro

    init {
        carregarDadosConta()
    }

    private fun carregarDadosConta() {
        viewModelScope.launch {
            val uid = utilizadorRepository.utilizadorActualId()
            if (uid != null) {
                val resultado = utilizadorRepository.obterUtilizador(uid)
                resultado.onSuccess {
                    _utilizador.value = it
                }.onFailure {
                    _erro.value = it.message ?: "Erro ao carregar dados da conta."
                }
            } else {
                _erro.value = "Nenhum utilizador com sessão iniciada."
            }
        }
    }

    fun terminarSessao() {
        utilizadorRepository.logout()
    }
}