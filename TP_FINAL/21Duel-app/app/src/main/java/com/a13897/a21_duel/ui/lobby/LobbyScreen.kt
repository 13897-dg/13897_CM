package com.a13897.a21_duel.ui.lobby

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.a13897.a21_duel.ui.theme._21DuelTheme

@Composable
fun LobbyScreen(
    onPartidaEncontrada: (idPartida: String) -> Unit,
    viewModel: LobbyViewModel = viewModel()
) {
    val estado by viewModel.estado.collectAsState()

    LaunchedEffect(estado) {
        val estadoActual = estado
        if (estadoActual is EstadoLobby.PartidaEncontrada) {
            onPartidaEncontrada(estadoActual.idPartida)
        }
    }

    LobbyScreenContent(
        estado = estado,
        onProcurarPartida = { viewModel.procurarPartida() },
        onCancelarProcura = { viewModel.cancelarProcura() },
        onCriarSalaPrivada = { viewModel.criarSalaPrivada() },
        onEntrarComCodigo = { viewModel.entrarComCodigo(it) }
    )
}

@Composable
fun LobbyScreenContent(
    estado: EstadoLobby,
    onProcurarPartida: () -> Unit,
    onCancelarProcura: () -> Unit,
    onCriarSalaPrivada: () -> Unit,
    onEntrarComCodigo: (String) -> Unit
) {
    var codigoIntroduzido by remember { mutableStateOf("") }

    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Lobby", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        when (val estadoActual = estado) {
            is EstadoLobby.AProcurar -> {
                CircularProgressIndicator()
                Spacer(modifier = Modifier.height(8.dp))
                Text("A procurar adversário...")
                Spacer(modifier = Modifier.height(8.dp))
                TextButton(onClick = onCancelarProcura) {
                    Text("Cancelar")
                }
            }
            is EstadoLobby.SalaCriada -> {
                Text("Código da sala: ${estadoActual.codigo}", style = MaterialTheme.typography.titleLarge)
                Spacer(modifier = Modifier.height(8.dp))
                Text("Partilha este código com o teu amigo")
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onCancelarProcura, modifier = Modifier.fillMaxWidth()) {
                    Text("Voltar")
                }
            }
            is EstadoLobby.Erro -> {
                Text(text = estadoActual.mensagem, color = MaterialTheme.colorScheme.error)
                Spacer(modifier = Modifier.height(16.dp))
                Button(onClick = onCancelarProcura, modifier = Modifier.fillMaxWidth()) {
                    Text("Tentar novamente")
                }
            }
            else -> {
                Button(
                    onClick = onProcurarPartida,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Procurar jogo online")
                }
                Spacer(modifier = Modifier.height(16.dp))
                HorizontalDivider(modifier = Modifier.padding(vertical = 16.dp))
                Text("Partida Privada", style = MaterialTheme.typography.titleMedium)
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = onCriarSalaPrivada,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Criar sala privada")
                }
                Spacer(modifier = Modifier.height(16.dp))
                OutlinedTextField(
                    value = codigoIntroduzido,
                    onValueChange = { codigoIntroduzido = it },
                    label = { Text("Código da sala") },
                    modifier = Modifier.fillMaxWidth()
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                    onClick = { onEntrarComCodigo(codigoIntroduzido) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("Entrar com código")
                }
            }
        }
    }
}

// --- PREVIEWS ---

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun LobbyScreenPreview() {
    _21DuelTheme {
        LobbyScreenContent(
            estado = EstadoLobby.Inicial,
            onProcurarPartida = {},
            onCancelarProcura = {},
            onCriarSalaPrivada = {},
            onEntrarComCodigo = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "A Procurar")
@Composable
fun LobbyScreenSearchingPreview() {
    _21DuelTheme {
        LobbyScreenContent(
            estado = EstadoLobby.AProcurar,
            onProcurarPartida = {},
            onCancelarProcura = {},
            onCriarSalaPrivada = {},
            onEntrarComCodigo = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Sala Criada")
@Composable
fun LobbyScreenRoomCreatedPreview() {
    _21DuelTheme {
        LobbyScreenContent(
            estado = EstadoLobby.SalaCriada("123456"),
            onProcurarPartida = {},
            onCancelarProcura = {},
            onCriarSalaPrivada = {},
            onEntrarComCodigo = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Erro")
@Composable
fun LobbyScreenErrorPreview() {
    _21DuelTheme {
        LobbyScreenContent(
            estado = EstadoLobby.Erro("Falha na ligação"),
            onProcurarPartida = {},
            onCancelarProcura = {},
            onCriarSalaPrivada = {},
            onEntrarComCodigo = {}
        )
    }
}
