package com.a13897.a21_duel.ui.jogo

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun JogoScreen(idPartida: String, onFimDeJogo: () -> Unit) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "Jogo")
        Text(text = "idPartida: $idPartida") // confirma que o dado chegou pela navegação
        Spacer(modifier = Modifier.height(24.dp))
        // Protótipo: sem lógica de jogo ainda, botão simula fim de partida
        Button(onClick = onFimDeJogo) {
            Text("Simular fim de partida")
        }
    }
}
