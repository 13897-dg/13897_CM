package com.a13897.a21_duel.ui.resultados

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun ResultadosScreen(
    idPartida: String,
    onJogarOutraVez: () -> Unit,
    onMenuPrincipal: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Resultados")
        Text(text = "Partida: $idPartida")
        Spacer(modifier = Modifier.height(24.dp))
        Button(onClick = onJogarOutraVez) { Text("Jogar outra vez") }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onMenuPrincipal) { Text("Menu principal") }
    }
}
