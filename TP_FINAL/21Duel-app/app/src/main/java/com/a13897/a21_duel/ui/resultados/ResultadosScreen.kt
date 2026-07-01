package com.a13897.a21_duel.ui.resultados

import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.a13897.a21_duel.data.model.Partida
import com.a13897.a21_duel.ui.theme._21DuelTheme

@Composable
fun ResultadosScreen(
    idPartida: String,
    onJogarOutraVez: () -> Unit,
    onMenuPrincipal: () -> Unit,
    viewModel: ResultadosViewModel = viewModel()
) {
    val partida by viewModel.partida.collectAsState()
    val venceu by viewModel.venceu.collectAsState()

    LaunchedEffect(idPartida) {
        viewModel.carregarResultado(idPartida)
    }

    ResultadosScreenContent(
        partida = partida,
        venceu = venceu,
        onJogarOutraVez = onJogarOutraVez,
        onMenuPrincipal = onMenuPrincipal
    )
}

@Composable
fun ResultadosScreenContent(
    partida: Partida?,
    venceu: Boolean,
    onJogarOutraVez: () -> Unit,
    onMenuPrincipal: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        partida?.let {
            Text(
                text = if (venceu) "Vitória!" else "Derrota",
                style = MaterialTheme.typography.headlineMedium
            )

        } ?: CircularProgressIndicator()

        Spacer(modifier = Modifier.height(32.dp))
        Button(
            onClick = onJogarOutraVez,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Jogar outra vez")
        }
        Spacer(modifier = Modifier.height(8.dp))
        OutlinedButton(
            onClick = onMenuPrincipal,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("Menu principal")
        }
    }
}

// --- PREVIEWS ---

@Preview(showBackground = true, showSystemUi = true, name = "Vitória")
@Composable
fun ResultadosScreenVitoriaPreview() {
    _21DuelTheme {
        ResultadosScreenContent(
            partida = Partida(
                vidasJogador1 = 5,
                vidasJogador2 = 0
            ),
            venceu = true,
            onJogarOutraVez = {},
            onMenuPrincipal = {}
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Derrota")
@Composable
fun ResultadosScreenDerrotaPreview() {
    _21DuelTheme {
        ResultadosScreenContent(
            partida = Partida(
                vidasJogador1 = 0,
                vidasJogador2 = 3
            ),
            venceu = false,
            onJogarOutraVez = {},
            onMenuPrincipal = {}
        )
    }
}


