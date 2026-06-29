package com.a13897.a21_duel.ui.menu

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a13897.a21_duel.data.model.Utilizador
import com.a13897.a21_duel.data.repository.UtilizadorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch
import java.util.UUID

class MenuViewModel(
    private val utilizadorRepository: UtilizadorRepository = UtilizadorRepository()
) : ViewModel() {

    private val _utilizador = MutableStateFlow<Utilizador?>(null)
    val utilizador: StateFlow<Utilizador?> = _utilizador

    fun carregarUtilizador() {
        val id = utilizadorRepository.utilizadorActualId() ?: return
        viewModelScope.launch {
            val resultado = utilizadorRepository.obterUtilizador(id)
            _utilizador.value = resultado.getOrNull()
        }
    }

    /** Gera um id de partida local para o modo vs IA (não precisa de matchmaking). */
    fun criarIdPartidaLocalContraIA(): String {
        return UUID.randomUUID().toString()
    }
}
