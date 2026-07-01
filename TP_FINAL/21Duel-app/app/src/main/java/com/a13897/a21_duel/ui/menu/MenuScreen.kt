package com.a13897.a21_duel.ui.menu

import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.a13897.a21_duel.R
import com.a13897.a21_duel.data.model.Utilizador
import com.a13897.a21_duel.ui.jogo.ID_JOGADOR_IA
import com.a13897.a21_duel.ui.theme._21DuelTheme

@Composable
fun MenuScreen(
    onJogarOnline: () -> Unit,
    onJogarIA: (idPartida: String, idJogador1: String, idJogador2: String) -> Unit,
    onTutorial: () -> Unit,
    onPerfil: () -> Unit,
    onDefinicoes: () -> Unit, // Callback adicionado aqui
    viewModel: MenuViewModel = viewModel()
) {
    val utilizador by viewModel.utilizador.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.carregarUtilizador()
    }

    MenuScreenContent(
        utilizador = utilizador,
        onJogarOnline = onJogarOnline,
        onJogarIA = {
            val idPartida = viewModel.criarIdPartidaLocalContraIA()
            val meuId = utilizador?.id ?: ""
            onJogarIA(idPartida, meuId, ID_JOGADOR_IA)
        },
        onTutorial = onTutorial,
        onPerfil = onPerfil,
        onDefinicoes = onDefinicoes // Passado para o Content
    )
}

@Composable
fun MenuScreenContent(
    utilizador: Utilizador?,
    onJogarOnline: () -> Unit,
    onJogarIA: () -> Unit,
    onTutorial: () -> Unit,
    onPerfil: () -> Unit,
    onDefinicoes: () -> Unit // Callback adicionado aqui
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(text = "21 Duel")
        utilizador?.let {
            Text(text = "Olá, ${it.username} — Vitórias: ${it.vitorias} / Derrotas: ${it.derrotas}")
        }
        Spacer(modifier = Modifier.height(24.dp))

        Button(onClick = onJogarOnline) { Text("Jogar Online") }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onJogarIA) { Text("vs IA") }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onTutorial) { Text("Tutorial") }
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onPerfil) { Text("Perfil / Loja") }
        Spacer(modifier = Modifier.height(8.dp))

        // Novo botão para as Definições
        Button(onClick = onDefinicoes) { Text(stringResource(id = R.string.definicoes_titulo)) }
    }
}

// --- PREVIEWS ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun MenuScreenPreview() {
    _21DuelTheme {
        MenuScreenContent(
            utilizador = Utilizador(
                id = "1",
                username = "Jogador de Exemplo",
                vitorias = 12,
                derrotas = 4
            ),
            onJogarOnline = {},
            onJogarIA = {},
            onTutorial = {},
            onPerfil = {},
            onDefinicoes = {} // Adicionado à Preview
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Sem Utilizador")
@Composable
fun MenuScreenNoUserPreview() {
    _21DuelTheme {
        MenuScreenContent(
            utilizador = null,
            onJogarOnline = {},
            onJogarIA = {},
            onTutorial = {},
            onPerfil = {},
            onDefinicoes = {} // Adicionado à Preview
        )
    }
}