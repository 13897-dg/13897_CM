package com.a13897.a21_duel.ui.resultados

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a13897.a21_duel.data.model.Partida
import com.a13897.a21_duel.data.repository.PartidaRepository
import com.a13897.a21_duel.data.repository.UtilizadorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.launch

class ResultadosViewModel(
    private val partidaRepository: PartidaRepository = PartidaRepository(),
    private val utilizadorRepository: UtilizadorRepository = UtilizadorRepository()
) : ViewModel() {

    private val _partida = MutableStateFlow<Partida?>(null)
    val partida: StateFlow<Partida?> = _partida

    /** true se o utilizador actual foi o vencedor da partida. */
    val venceu: Boolean
        get() {
            val p = _partida.value ?: return false
            val meuId = utilizadorRepository.utilizadorActualId()
            return p.vencedor == meuId
        }

    fun carregarResultado(idPartida: String) {
        viewModelScope.launch {
            partidaRepository.observarPartida(idPartida).collect { partidaActualizada ->
                _partida.value = partidaActualizada
            }
        }
    }
}
