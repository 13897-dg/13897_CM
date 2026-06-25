package com.a13897.a21_duel.ui.lobby

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import java.util.UUID

@Composable
fun LobbyScreen(onPartidaEncontrada: (String) -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Lobby")
        Spacer(modifier = Modifier.height(24.dp))
        // Protótipo: id de partida hardcoded/gerado localmente, sem matchmaking real ainda
        Button(onClick = { onPartidaEncontrada(UUID.randomUUID().toString()) }) {
            Text("Simular partida encontrada")
        }
    }
}
