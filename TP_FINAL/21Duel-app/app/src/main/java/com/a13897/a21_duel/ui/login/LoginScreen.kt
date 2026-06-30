package com.a13897.a21_duel.ui.login

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.a13897.a21_duel.ui.theme._21DuelTheme

@Composable
fun LoginScreen(
    onLoginSuccess: () -> Unit,
    viewModel: LoginViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()
    var modoRegisto by remember { mutableStateOf(false) }

    // navega automaticamente quando o login/registo é bem sucedido
    LaunchedEffect(estado) {
        if (estado is EstadoLogin.Sucesso) {
            onLoginSuccess()
        }
    }

    LoginScreenContent(
        estado = estado,
        modoRegisto = modoRegisto,
        onModoRegistoChange = { modoRegisto = it },
        onLogin = { email, password -> viewModel.login(email, password) },
        onRegistar = { email, password, username -> viewModel.registar(email, password, username) }
    )
}

@Composable
fun LoginScreenContent(
    estado: EstadoLogin,
    modoRegisto: Boolean,
    onModoRegistoChange: (Boolean) -> Unit,
    onLogin: (String, String) -> Unit,
    onRegistar: (String, String, String) -> Unit
) {
    var email by remember { mutableStateOf("") }
    var password by remember { mutableStateOf("") }
    var username by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "21 Duel", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(32.dp))

        OutlinedTextField(
            value = email,
            onValueChange = { email = it },
            label = { Text("Email") },
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(8.dp))

        if (modoRegisto) {
            OutlinedTextField(
                value = username,
                onValueChange = { username = it },
                label = { Text("Nome de utilizador") },
                modifier = Modifier.fillMaxWidth()
            )
            Spacer(modifier = Modifier.height(8.dp))
        }

        OutlinedTextField(
            value = password,
            onValueChange = { password = it },
            label = { Text("Password") },
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth()
        )
        Spacer(modifier = Modifier.height(16.dp))

        if (estado is EstadoLogin.AAutenticar) {
            CircularProgressIndicator()
        } else {
            Button(
                onClick = {
                    if (modoRegisto) onRegistar(email, password, username)
                    else onLogin(email, password)
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text(if (modoRegisto) "Criar conta" else "Login")
            }
        }

        Spacer(modifier = Modifier.height(8.dp))
        TextButton(onClick = { onModoRegistoChange(!modoRegisto) }) {
            Text(if (modoRegisto) "Já tenho conta" else "Criar conta")
        }

        val estadoActual = estado
        if (estadoActual is EstadoLogin.Erro) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(text = estadoActual.mensagem, color = MaterialTheme.colorScheme.error)
        }
    }
}

// --- PREVIEW ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LoginScreenPreview() {
    _21DuelTheme {
        LoginScreenContent(
            estado = EstadoLogin.Inicial,
            modoRegisto = false,
            onModoRegistoChange = {},
            onLogin = { _, _ -> },
            onRegistar = { _, _, _ -> }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Registo")
@Composable
fun LoginScreenRegistoPreview() {
    _21DuelTheme {
        LoginScreenContent(
            estado = EstadoLogin.Inicial,
            modoRegisto = true,
            onModoRegistoChange = {},
            onLogin = { _, _ -> },
            onRegistar = { _, _, _ -> }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "A Autenticar")
@Composable
fun LoginScreenLoadingPreview() {
    _21DuelTheme {
        LoginScreenContent(
            estado = EstadoLogin.AAutenticar,
            modoRegisto = false,
            onModoRegistoChange = {},
            onLogin = { _, _ -> },
            onRegistar = { _, _, _ -> }
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Erro")
@Composable
fun LoginScreenErrorPreview() {
    _21DuelTheme {
        LoginScreenContent(
            estado = EstadoLogin.Erro("Credenciais inválidas"),
            modoRegisto = false,
            onModoRegistoChange = {},
            onLogin = { _, _ -> },
            onRegistar = { _, _, _ -> }
        )
    }
}