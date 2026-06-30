package com.a13897.a21_duel.ui.perfil

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a13897.a21_duel.data.model.Cosmetico
import com.a13897.a21_duel.data.model.Utilizador
import com.a13897.a21_duel.data.repository.CosmeticoRepository
import com.a13897.a21_duel.data.repository.UtilizadorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

class PerfilViewModel(
    private val utilizadorRepository: UtilizadorRepository = UtilizadorRepository(),
    private val cosmeticoRepository: CosmeticoRepository = CosmeticoRepository()
) : ViewModel() {

    private val _utilizador = MutableStateFlow<Utilizador?>(null)
    val utilizador: StateFlow<Utilizador?> = _utilizador

    private val _cosmeticosDisponiveis = MutableStateFlow<List<Cosmetico>>(emptyList())
    val cosmeticosDisponiveis: StateFlow<List<Cosmetico>> = _cosmeticosDisponiveis

    private val _idsDesbloqueados = MutableStateFlow<List<String>>(emptyList())
    val idsDesbloqueados: StateFlow<List<String>> = _idsDesbloqueados

    fun carregarPerfil() {
        val id = utilizadorRepository.utilizadorActualId() ?: return

        viewModelScope.launch {
            val resultadoUtilizador = utilizadorRepository.obterUtilizador(id)
            _utilizador.value = resultadoUtilizador.getOrNull()

            val resultadoCosmeticos = cosmeticoRepository.listarTodos()
            _cosmeticosDisponiveis.value = resultadoCosmeticos.getOrElse { emptyList() }

            val resultadoDesbloqueados = cosmeticoRepository.listarDesbloqueadosPorUtilizador(id)
            _idsDesbloqueados.value = resultadoDesbloqueados.getOrElse { emptyList() }
        }
    }

    /** Verifica se um cosmético específico está acessível ao utilizador (default ou já desbloqueado). */
    fun estaDesbloqueado(cosmetico: Cosmetico): Boolean {
        return cosmetico.desbloqueio == com.a13897.a21_duel.data.model.TipoDesbloqueio.DEFAULT ||
                _idsDesbloqueados.value.contains(cosmetico.id)
    }

    fun equiparAvatar(idCosmetico: String) {
        val id = utilizadorRepository.utilizadorActualId() ?: return
        viewModelScope.launch {
            utilizadorRepository.actualizarAvatar(id, idCosmetico)
            carregarPerfil() // recarrega para reflectir a mudança
        }
    }

    /**
     * Subscrição PRO — esqueleto por agora.
     * A integração real com Google Play Billing fica para mais tarde;
     * esta função só simula a activação para fins de teste/demonstração.
     */
    fun simularSubscricaoPRO() {
        // TODO: substituir por integração real com Google Play Billing
        val id = utilizadorRepository.utilizadorActualId() ?: return
        viewModelScope.launch {
            // por agora desbloqueia todos os cosméticos marcados como PRO, como demonstração
            val cosmeticosPRO = _cosmeticosDisponiveis.value.filter { it.exclusivoPRO }
            cosmeticosPRO.forEach { cosmetico ->
                cosmeticoRepository.desbloquearCosmetico(id, cosmetico.id)
            }
            carregarPerfil()
        }
    }
}
