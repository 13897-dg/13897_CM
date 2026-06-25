package com.a13897.a21_duel.ui.menu

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp

@Composable
fun MenuScreen(
    onJogarOnline: () -> Unit,
    onJogarIA: () -> Unit,
    onTutorial: () -> Unit,
    onPerfil: () -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "21 Duel", style = androidx.compose.material3.MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onJogarOnline) { Text("Jogar Online") }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onJogarIA) { Text("vs IA") }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onTutorial) { Text("Tutorial") }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onPerfil) { Text("Perfil / Loja") }
    }
}
