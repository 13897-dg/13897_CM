package com.a13897.a21_duel.ui.tutorial

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.ArrowForward
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.a13897.a21_duel.ui.theme._21DuelTheme

data class PassoTutorial(
    val numero: Int,
    val emoji: String,
    val texto: String
)

val passosTutorial = listOf(
    PassoTutorial(1, "🎯", "Chega o mais perto possível de 21 sem ultrapassar. Quem estiver mais perto no fim da ronda ganha!"),
    PassoTutorial(2, "🃏", "O baralho tem 11 cartas numeradas de 1 a 11. Começas com 2 cartas — uma visível e uma escondida. Só tu sabes a tua mão completa."),
    PassoTutorial(3, "🔄", "Em cada turno podes pedir carta, ficar (stay) ou jogar uma carta especial. Quando ambos ficarem, as mãos são reveladas."),
    PassoTutorial(4, "❤️", "Cada jogador começa com 8 vidas. Quem perde a ronda perde vidas igual à aposta dessa ronda. A aposta vai aumentando — cuidado!"),
    PassoTutorial(5, "✨", "A partir da 2ª ronda recebes cartas especiais. Usa-as para virar o jogo a teu favor — mudam apostas, objectivos, cartas em campo e muito mais."),
    PassoTutorial(6, "🏆", "O jogo acaba quando um jogador chegar a 0 vidas. Boa sorte!")
)

@Composable
fun TutorialScreen(
    onVoltar: () -> Unit = {}
) {
    var passoActual by remember { mutableIntStateOf(0) }

    TutorialScreenContent(
        passo = passosTutorial[passoActual],
        totalPassos = passosTutorial.size,
        onAnterior = { if (passoActual > 0) passoActual -= 1 },
        onSeguinte = { if (passoActual < passosTutorial.size - 1) passoActual += 1 },
        onSaltar = onVoltar,
        podeVoltar = passoActual > 0,
        podeSeguir = passoActual < passosTutorial.size - 1,
        eUltimoStep = passoActual == passosTutorial.size - 1,
    )
}

@Composable
fun TutorialScreenContent(
    passo: PassoTutorial,
    totalPassos: Int,
    onAnterior: () -> Unit,
    onSeguinte: () -> Unit,
    onSaltar: () -> Unit,
    podeVoltar: Boolean,
    podeSeguir: Boolean,
    eUltimoStep: Boolean
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        // --- Topo: botão saltar ---
        Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.End) {
            TextButton(onClick = onSaltar) {
                Text("Saltar tutorial")
            }
        }

        // --- Centro: conteúdo do passo ---
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.Center,
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = passo.emoji,
                style = MaterialTheme.typography.displayLarge
            )
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = passo.texto,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center
            )
        }

        // --- Baixo: indicador de progresso e navegação ---
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            Text(
                text = "${passo.numero} / $totalPassos",
                style = MaterialTheme.typography.labelMedium,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
            Spacer(modifier = Modifier.height(16.dp))

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                IconButton(onClick = onAnterior, enabled = podeVoltar) {
                    Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Anterior")
                }

                if (eUltimoStep) {
                    Button(onClick = onSaltar) {
                        Text("Começar a jogar!")
                    }
                } else {
                    IconButton(onClick = onSeguinte, enabled = podeSeguir) {
                        Icon(Icons.AutoMirrored.Filled.ArrowForward, contentDescription = "Seguinte")
                    }
                }
            }
        }
    }
}

// --- PREVIEWS ---

@Preview(showBackground = true, showSystemUi = true, name = "Passo 1")
@Composable
fun TutorialPasso1Preview() {
    _21DuelTheme {
        TutorialScreenContent(
            passo = passosTutorial[0],
            totalPassos = 6,
            onAnterior = {}, onSeguinte = {}, onSaltar = {},
            podeVoltar = false, podeSeguir = true, eUltimoStep = false
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Passo intermedio")
@Composable
fun TutorialPassoIntermedioPreview() {
    _21DuelTheme {
        TutorialScreenContent(
            passo = passosTutorial[2],
            totalPassos = 6,
            onAnterior = {}, onSeguinte = {}, onSaltar = {},
            podeVoltar = true, podeSeguir = true, eUltimoStep = false
        )
    }
}

@Preview(showBackground = true, showSystemUi = true, name = "Ultimo passo")
@Composable
fun TutorialUltimoPassoPreview() {
    _21DuelTheme {
        TutorialScreenContent(
            passo = passosTutorial[5],
            totalPassos = 6,
            onAnterior = {}, onSeguinte = {}, onSaltar = {},
            podeVoltar = true, podeSeguir = false, eUltimoStep = true
        )
    }
}
