package com.a13897.a21_duel.ui.jogo

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.a13897.a21_duel.game.CartaMao
import com.a13897.a21_duel.game.EstadoRonda
import com.a13897.a21_duel.game.RegistoCartasEspeciais
import com.a13897.a21_duel.ui.theme._21DuelTheme

@Composable
fun JogoScreen(
    idPartida: String,
    idJogador1: String,
    idJogador2: String,
    ehContraIA: Boolean,
    onFimDeJogo: () -> Unit,
    viewModel: JogoViewModel = viewModel()
) {
    val estadoRonda by viewModel.estadoRonda.collectAsState()
    val vidasJogador1 by viewModel.vidasJogador1.collectAsState()
    val vidasJogador2 by viewModel.vidasJogador2.collectAsState()
    val inventario by viewModel.inventarioJogador1.collectAsState()
    val fimDeJogo by viewModel.fimDeJogo.collectAsState()

    LaunchedEffect(idPartida) {
        viewModel.iniciar(idPartida, idJogador1, idJogador2, ehContraIA)
    }

    LaunchedEffect(fimDeJogo) {
        if (fimDeJogo != null) onFimDeJogo()
    }

    JogoScreenContent(
        estadoRonda = estadoRonda,
        vidasJogador1 = vidasJogador1,
        vidasJogador2 = vidasJogador2,
        inventario = inventario,
        onPedirCarta = { viewModel.pedirCarta("jogador1") },
        onFicar = { viewModel.ficar("jogador1") },
        onJogarCartaEspecial = { nome -> viewModel.jogarCartaEspecial("jogador1", nome) }
    )
}

@Composable
fun JogoScreenContent(
    estadoRonda: EstadoRonda,
    vidasJogador1: Int,
    vidasJogador2: Int,
    inventario: List<String>,
    onPedirCarta: () -> Unit,
    onFicar: () -> Unit,
    onJogarCartaEspecial: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Topo: oponente ---
        JogadorInfo(nome = "Oponente", vidas = vidasJogador2)
        Spacer(modifier = Modifier.height(8.dp))
        MaoDoJogador(mao = estadoRonda.maoJogador2, mostrarOcultas = false)
        Text(text = "[ ?? ]", style = MaterialTheme.typography.headlineSmall)

        Spacer(modifier = Modifier.height(8.dp))
        HorizontalDivider()

        // --- Centro: cartas especiais em jogo + aposta/objectivo ---
        Text("Cartas em campo: ${estadoRonda.cartasEspeciaisEmCampo.joinToString()}")
        Text("Aposta: ${estadoRonda.aposta}  |  Objectivo: ${estadoRonda.objectivoActual}")
        Text("Timer: ${estadoRonda.tempoRestanteSegundos}s  |  Ronda: ${estadoRonda.numero}")

        HorizontalDivider()
        Spacer(modifier = Modifier.height(8.dp))

        // --- Baixo: jogador local ---
        MaoDoJogador(mao = estadoRonda.maoJogador1, mostrarOcultas = true)
        Text(
            text = "Pontuação: ${estadoRonda.pontuacaoJogador1()}",
            style = MaterialTheme.typography.headlineSmall
        )
        Spacer(modifier = Modifier.height(8.dp))
        JogadorInfo(nome = "Tu", vidas = vidasJogador1)

        Spacer(modifier = Modifier.height(16.dp))

        // --- Botões de acção ---
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onPedirCarta,
                enabled = !estadoRonda.stayJogador1
            ) { Text("Pedir carta") }

            Button(
                onClick = onFicar,
                enabled = !estadoRonda.stayJogador1
            ) { Text("Parar") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Inventário de cartas especiais ---
        Text("Inventário:", style = MaterialTheme.typography.titleSmall)
        InventarioCartas(
            inventario = inventario,
            onJogar = onJogarCartaEspecial
        )
    }
}

@Composable
fun JogadorInfo(nome: String, vidas: Int) {
    Row(verticalAlignment = Alignment.CenterVertically, horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        Text(nome, style = MaterialTheme.typography.labelLarge)
        Text("❤".repeat(vidas.coerceIn(0, 8)))
    }
}

@Composable
fun MaoDoJogador(mao: List<CartaMao>, mostrarOcultas: Boolean) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        mao.forEach { carta ->
            Card(modifier = Modifier.size(48.dp, 64.dp)) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = if (mostrarOcultas || carta.visivel) carta.valor.toString() else "?",
                        style = MaterialTheme.typography.titleMedium
                    )
                }
            }
        }
    }
}

@OptIn(ExperimentalFoundationApi::class)
@Composable
fun InventarioCartas(inventario: List<String>, onJogar: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        inventario.forEach { nomeCarta ->
            var mostrarTooltip by remember { mutableStateOf(false) }

            if (mostrarTooltip) {
                AlertDialog(
                    onDismissRequest = { mostrarTooltip = false },
                    title = { Text(nomeCarta) },
                    text = { Text(RegistoCartasEspeciais.descricoes[nomeCarta] ?: "") },
                    confirmButton = {
                        TextButton(onClick = { mostrarTooltip = false }) { Text("Fechar") }
                    }
                )
            }

            Card(
                modifier = Modifier
                    .size(56.dp, 72.dp)
                    .combinedClickable(
                        onClick = { onJogar(nomeCarta) },
                        onLongClick = { mostrarTooltip = true }
                    )
            ) {
                Box(contentAlignment = Alignment.Center, modifier = Modifier.fillMaxSize()) {
                    Text(
                        text = nomeCarta.take(4),
                        style = MaterialTheme.typography.bodySmall
                    )
                }
            }
        }
    }
}

// --- PREVIEW ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun JogoScreenPreview() {
    _21DuelTheme {
        JogoScreenContent(
            estadoRonda = EstadoRonda(
                numero = 1,
                aposta = 2,
                objectivoBase = 21,
                maoJogador1 = listOf(CartaMao(10, true), CartaMao(7, true)),
                maoJogador2 = listOf(CartaMao(5, true), CartaMao(2, false)),
                cartasEspeciaisEmCampo = listOf("Escudo"),
                tempoRestanteSegundos = 25
            ),
            vidasJogador1 = 3,
            vidasJogador2 = 3,
            inventario = listOf("Mais1", "Menos1", "Escudo"),
            onPedirCarta = {},
            onFicar = {},
            onJogarCartaEspecial = {}
        )
    }
}
