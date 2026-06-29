package com.a13897.a21_duel.ui.login

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a13897.a21_duel.data.repository.UtilizadorRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

sealed class EstadoLogin {
    object Inicial : EstadoLogin()
    object AAutenticar : EstadoLogin()
    object Sucesso : EstadoLogin()
    data class Erro(val mensagem: String) : EstadoLogin()
}

class LoginViewModel(
    private val utilizadorRepository: UtilizadorRepository = UtilizadorRepository()
) : ViewModel() {

    private val _estado = MutableStateFlow<EstadoLogin>(EstadoLogin.Inicial)
    val estado: StateFlow<EstadoLogin> = _estado

    fun login(email: String, password: String) {
        if (email.isBlank() || password.isBlank()) {
            _estado.value = EstadoLogin.Erro("Preenche email e password.")
            return
        }

        _estado.value = EstadoLogin.AAutenticar
        viewModelScope.launch {
            val resultado = utilizadorRepository.login(email, password)
            _estado.value = if (resultado.isSuccess) {
                EstadoLogin.Sucesso
            } else {
                EstadoLogin.Erro(resultado.exceptionOrNull()?.message ?: "Erro ao fazer login.")
            }
        }
    }

    fun registar(email: String, password: String, username: String) {
        if (email.isBlank() || password.isBlank() || username.isBlank()) {
            _estado.value = EstadoLogin.Erro("Preenche todos os campos.")
            return
        }

        _estado.value = EstadoLogin.AAutenticar
        viewModelScope.launch {
            val resultado = utilizadorRepository.registar(email, password, username)
            _estado.value = if (resultado.isSuccess) {
                EstadoLogin.Sucesso
            } else {
                EstadoLogin.Erro(resultado.exceptionOrNull()?.message ?: "Erro ao criar conta.")
            }
        }
    }
}
