package com.a13897.a21_duel.ui.sobre

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.a13897.a21_duel.ui.theme._21DuelTheme

@Composable
fun SobreScreen(onVoltar: () -> Unit = {}) {
    SobreScreenContent(onVoltar = onVoltar)
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SobreScreenContent(onVoltar: () -> Unit = {}) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Sobre") },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(horizontal = 24.dp, vertical = 16.dp)
                .verticalScroll(rememberScrollState()),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            // --- Título e versão ---
            Text(
                text = "21 Duel",
                style = MaterialTheme.typography.displaySmall
            )
            Text(
                text = "Versão 1.0",
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )

            Spacer(modifier = Modifier.height(32.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(24.dp))

            // --- Inspiração ---
            SeccaoSobre(titulo = "Inspiração") {
                Text(
                    text = "21 Duel tem como base o mini-jogo \"21\" presente em Resident Evil 7, " +
                            "desenvolvido originalmente pela Capcom. O conceito, as mecânicas de jogo " +
                            "e as cartas especiais foram adaptados e expandidos para uma experiência " +
                            "casual de telemóvel, independente de qualquer elemento da franchise original.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Justify
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Desenvolvimento ---
            SeccaoSobre(titulo = "Desenvolvimento") {
                Text(
                    text = "Desenvolvido em Vibe Coding com o apoio do modelo de inteligência artificial " +
                            "Claude (Anthropic), utilizando Android Studio, Kotlin e Jetpack Compose.",
                    style = MaterialTheme.typography.bodyMedium,
                    textAlign = TextAlign.Justify
                )
                Spacer(modifier = Modifier.height(8.dp))
                Text(
                    text = "Backend e autenticação via Firebase (Firestore + Authentication).",
                    style = MaterialTheme.typography.bodyMedium
                )
            }

            Spacer(modifier = Modifier.height(24.dp))

            // --- Contexto académico ---
            SeccaoSobre(titulo = "Contexto Académico") {
                InfoRow(label = "Autor", valor = "David Gonçalves")
                InfoRow(label = "Unidade Curricular", valor = "Computação Móvel")
                InfoRow(label = "Curso", valor = "Engenharia Informática e de Computadores")
                InfoRow(label = "Instituição", valor = "Escola Superior Náutica Infante D. Henrique")
                InfoRow(label = "Docente", valor = "Pedro Fazenda")
            }

            Spacer(modifier = Modifier.height(24.dp))
            HorizontalDivider()
            Spacer(modifier = Modifier.height(16.dp))

            Text(
                text = "© 2026 David Gonçalves — Projecto Académico",
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                textAlign = TextAlign.Center
            )
        }
    }
}

@Composable
fun SeccaoSobre(titulo: String, conteudo: @Composable ColumnScope.() -> Unit) {
    Column(modifier = Modifier.fillMaxWidth()) {
        Text(
            text = titulo,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.primary
        )
        Spacer(modifier = Modifier.height(8.dp))
        conteudo()
    }
}

@Composable
fun InfoRow(label: String, valor: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp),
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.weight(0.4f)
        )
        Text(
            text = valor,
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(0.6f)
        )
    }
}

// --- PREVIEWS ---

@Preview(showBackground = true, showSystemUi = true)
@Composable
fun SobreScreenPreview() {
    _21DuelTheme {
        SobreScreenContent()
    }
}
