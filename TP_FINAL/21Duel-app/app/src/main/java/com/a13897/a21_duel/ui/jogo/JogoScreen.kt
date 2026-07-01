package com.a13897.a21_duel.ui.jogo

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.combinedClickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
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
    val mensagemAcaoOponente by viewModel.mensagemAcaoOponente.collectAsState()
    val estadoRonda by viewModel.estadoRonda.collectAsState()
    val vidasJogador1 by viewModel.vidasJogador1.collectAsState()
    val vidasJogador2 by viewModel.vidasJogador2.collectAsState()
    val fimDeJogo by viewModel.fimDeJogo.collectAsState()
    val meuJogadorInterno by viewModel.meuJogadorInterno.collectAsState()
    val mensagemFimRonda by viewModel.mensagemFimRonda.collectAsState()

    // Agora recolhemos ambos os inventários para podermos escolher o correto
    val inventarioJogador1 by viewModel.inventarioJogador1.collectAsState()
    val inventarioJogador2 by viewModel.inventarioJogador2.collectAsState()
    val inventarioMeu = if (meuJogadorInterno == "jogador1") inventarioJogador1 else inventarioJogador2

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
        inventario = inventarioMeu,
        mensagemAcaoOponente = mensagemAcaoOponente,
        mensagemFimRonda = mensagemFimRonda,
        meuJogadorInterno = meuJogadorInterno,
        onPedirCarta = { viewModel.pedirCarta(meuJogadorInterno) },
        onFicar = { viewModel.ficar(meuJogadorInterno) },
        onJogarCartaEspecial = { nome -> viewModel.jogarCartaEspecial(meuJogadorInterno, nome) }
    )
}

@Composable
fun JogoScreenContent(
    estadoRonda: EstadoRonda,
    vidasJogador1: Int,
    vidasJogador2: Int,
    inventario: List<String>,
    mensagemFimRonda: String?,
    mensagemAcaoOponente: String?,
    onPedirCarta: () -> Unit,
    onFicar: () -> Unit,
    onJogarCartaEspecial: (String) -> Unit,
    meuJogadorInterno: String
) {
    // 1. Determina as identidades e perspetivas dinamicamente
    val oponenteInterno = if (meuJogadorInterno == "jogador1") "jogador2" else "jogador1"

    val vidasMinhas = if (meuJogadorInterno == "jogador1") vidasJogador1 else vidasJogador2
    val vidasOponente = if (oponenteInterno == "jogador1") vidasJogador1 else vidasJogador2

    val maoMinha = if (meuJogadorInterno == "jogador1") estadoRonda.maoJogador1 else estadoRonda.maoJogador2
    val maoOponente = if (oponenteInterno == "jogador1") estadoRonda.maoJogador1 else estadoRonda.maoJogador2

    val pontuacaoMinha = if (meuJogadorInterno == "jogador1") estadoRonda.pontuacaoJogador1() else estadoRonda.pontuacaoJogador2()
    val pontuacaoOponente = if (oponenteInterno == "jogador1") estadoRonda.pontuacaoJogador1() else estadoRonda.pontuacaoJogador2()

    val meuStay = if (meuJogadorInterno == "jogador1") estadoRonda.stayJogador1 else estadoRonda.stayJogador2

    // 2. Determina se é o teu turno
    val eOmeuTurno = estadoRonda.turnoAtual == meuJogadorInterno && !meuStay && mensagemFimRonda == null

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        // --- Topo: Oponente ---
        Spacer(modifier = Modifier.height(16.dp))
        JogadorInfo(nome = "Oponente", vidas = vidasOponente)
        Spacer(modifier = Modifier.height(8.dp))

        // Mostra a ação do oponente em texto
        if (mensagemAcaoOponente != null) {
            Spacer(modifier = Modifier.height(8.dp))
            Text(
                text = mensagemAcaoOponente,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.tertiary
            )
        }

        // Se a ronda terminou, revela as cartas do oponente
        MaoDoJogador(mao = maoOponente, mostrarOcultas = estadoRonda.rondaTerminou())

        // Se a ronda terminou, mostra os pontos dele em vez de "??"
        Text(
            text = "Pontuação: ${if (estadoRonda.rondaTerminou()) pontuacaoOponente else "??"}",
            style = MaterialTheme.typography.bodyMedium
        )

        Spacer(modifier = Modifier.weight(1f))

        // --- Centro: cartas especiais em jogo + aposta/objectivo + Mensagem Final ---
        Card(
            colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant),
            modifier = Modifier.fillMaxWidth()
        ) {
            Column(
                modifier = Modifier.padding(16.dp).fillMaxWidth(),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                if (mensagemFimRonda != null) {
                    // Mostra o vencedor da ronda em grande destaque
                    Text(
                        text = mensagemFimRonda,
                        style = MaterialTheme.typography.headlineSmall,
                        color = MaterialTheme.colorScheme.primary,
                        textAlign = TextAlign.Center
                    )
                } else {
                    // Informação normal da ronda
                    Text(
                        text = if (eOmeuTurno) "O TEU TURNO" else "TURNO DO OPONENTE",
                        style = MaterialTheme.typography.labelLarge,
                        color = if (eOmeuTurno) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.error
                    )
                    Spacer(modifier = Modifier.height(8.dp))

                    Text("Timer: ${estadoRonda.tempoRestanteSegundos}s", style = MaterialTheme.typography.titleMedium)
                    Text("Ronda: ${estadoRonda.numero}")
                    Text("Aposta: ${estadoRonda.aposta}  |  Objectivo: ${estadoRonda.objectivoActual}")

                    if (estadoRonda.cartasEspeciaisEmCampo.isNotEmpty()) {
                        Spacer(modifier = Modifier.height(8.dp))
                        Text("Em campo: ${estadoRonda.cartasEspeciaisEmCampo.joinToString()}")
                    }
                }
            }
        }

        Spacer(modifier = Modifier.weight(1f))

        // --- Baixo: Tu (Jogador Local) ---
        Text(
            text = "Pontuação: $pontuacaoMinha",
            style = MaterialTheme.typography.headlineSmall
        )
        MaoDoJogador(mao = maoMinha, mostrarOcultas = true)
        Spacer(modifier = Modifier.height(8.dp))
        JogadorInfo(nome = "Tu", vidas = vidasMinhas)

        Spacer(modifier = Modifier.height(16.dp))

        // --- Botões de acção ---
        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            Button(
                onClick = onPedirCarta,
                enabled = eOmeuTurno && estadoRonda.cartasDisponiveis.isNotEmpty()
            ) { Text("Pedir carta") }

            Button(
                onClick = onFicar,
                enabled = eOmeuTurno
            ) { Text("Parar") }
        }

        Spacer(modifier = Modifier.height(16.dp))

        // --- Inventário de cartas especiais ---
        Text("Inventário:", style = MaterialTheme.typography.titleSmall)
        Spacer(modifier = Modifier.height(4.dp))
        InventarioCartas(
            inventario = inventario,
            podeJogar = eOmeuTurno,
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
            val eCartaOcultaParaOponente = mostrarOcultas && !carta.visivel

            Card(
                modifier = Modifier.size(48.dp, 64.dp),
                colors = CardDefaults.cardColors(
                    containerColor = if (eCartaOcultaParaOponente)
                        MaterialTheme.colorScheme.tertiaryContainer // Cor de destaque para cartas ocultas
                    else
                        MaterialTheme.colorScheme.secondaryContainer // Cor normal
                )
            ){
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
fun InventarioCartas(inventario: List<String>, podeJogar: Boolean, onJogar: (String) -> Unit) {
    Row(horizontalArrangement = Arrangement.spacedBy(4.dp)) {
        inventario.forEach { nomeCarta ->
            var mostrarTooltip by remember { mutableStateOf(false) }

            if (mostrarTooltip) {
                AlertDialog(
                    onDismissRequest = { mostrarTooltip = false },
                    title = { Text(nomeCarta) },
                    text = { Text(RegistoCartasEspeciais.descricoes[nomeCarta] ?: "Sem descrição.") },
                    confirmButton = {
                        TextButton(onClick = { mostrarTooltip = false }) { Text("Fechar") }
                    }
                )
            }

            Card(
                modifier = Modifier
                    .size(56.dp, 72.dp)
                    .combinedClickable(
                        onClick = { if (podeJogar) onJogar(nomeCarta) },
                        onLongClick = { mostrarTooltip = true }
                    ),
                colors = CardDefaults.cardColors(
                    containerColor = if (podeJogar) MaterialTheme.colorScheme.surface else MaterialTheme.colorScheme.surfaceVariant
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
                maoJogador1 = listOf(CartaMao(10, true), CartaMao(7, false)),
                maoJogador2 = listOf(CartaMao(5, true), CartaMao(2, false)),
                cartasEspeciaisEmCampo = listOf("Escudo"),
                tempoRestanteSegundos = 25,
                turnoAtual = "jogador1"
            ),
            vidasJogador1 = 3,
            vidasJogador2 = 3,
            inventario = listOf("Mais1", "Menos1", "Escudo"),
            mensagemFimRonda = null,
            mensagemAcaoOponente = null,
            onPedirCarta = {},
            onFicar = {},
            onJogarCartaEspecial = {},
            meuJogadorInterno = "jogador1"
        )
    }
}