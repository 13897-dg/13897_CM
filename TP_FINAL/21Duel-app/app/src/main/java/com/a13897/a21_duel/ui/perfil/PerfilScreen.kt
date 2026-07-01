package com.a13897.a21_duel.ui.perfil

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyRow
import androidx.compose.foundation.lazy.items
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.a13897.a21_duel.data.model.Cosmetico
import com.a13897.a21_duel.data.model.Utilizador
import com.a13897.a21_duel.ui.theme._21DuelTheme

@Composable
fun PerfilScreen(
    viewModel: PerfilViewModel = viewModel()
) {
    val utilizador by viewModel.utilizador.collectAsState()
    val cosmeticos by viewModel.cosmeticosDisponiveis.collectAsState()

    LaunchedEffect(Unit) {
        viewModel.carregarPerfil()
    }

    PerfilScreenContent(
        utilizador = utilizador,
        cosmeticos = cosmeticos,
        estaDesbloqueado = { viewModel.estaDesbloqueado(it) },
        onSimularPRO = { viewModel.simularSubscricaoPRO() },
        onEquiparAvatar = { viewModel.equiparAvatar(it) }
    )
}

@Composable
fun PerfilScreenContent(
    utilizador: Utilizador?,
    cosmeticos: List<Cosmetico>,
    estaDesbloqueado: (Cosmetico) -> Boolean,
    onSimularPRO: () -> Unit,
    onEquiparAvatar: (String) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxSize().padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(text = "Perfil / Loja", style = MaterialTheme.typography.headlineMedium)
        Spacer(modifier = Modifier.height(16.dp))

        utilizador?.let {
            Text("${it.username}")
            Text("Vitórias: ${it.vitorias}  |  Derrotas: ${it.derrotas}")
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Text("⭐ PRO — 1€/mês", style = MaterialTheme.typography.titleMedium)
        Text("Baralhos exclusivos, temas de mesa, avatares animados e sem anúncios.")
        Spacer(modifier = Modifier.height(8.dp))
        Button(onClick = onSimularPRO) {
            Text("Subscrever PRO (simulado)")
        }

        Spacer(modifier = Modifier.height(24.dp))
        HorizontalDivider()
        Spacer(modifier = Modifier.height(16.dp))

        Text("Cosméticos", style = MaterialTheme.typography.titleMedium)
        Spacer(modifier = Modifier.height(8.dp))

        LazyRow(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
            items(cosmeticos) { cosmetico ->
                CosmeticoCard(
                    cosmetico = cosmetico,
                    desbloqueado = estaDesbloqueado(cosmetico),
                    onEquipar = { onEquiparAvatar(cosmetico.id) }
                )
            }
        }
    }
}

@Composable
fun CosmeticoCard(
    cosmetico: Cosmetico,
    desbloqueado: Boolean,
    onEquipar: () -> Unit
) {
    Card(modifier = Modifier.width(100.dp)) {
        Column(
            modifier = Modifier.padding(8.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(text = cosmetico.nome, style = MaterialTheme.typography.bodySmall)
            Spacer(modifier = Modifier.height(4.dp))
            if (desbloqueado) {
                Button(onClick = onEquipar) { Text("Equipar", style = MaterialTheme.typography.bodySmall) }
            } else {
                Text("🔒 PRO", style = MaterialTheme.typography.bodySmall)
            }
        }
    }
}

// --- PREVIEW ---
@Preview(showBackground = true, showSystemUi = true)
@Composable
fun PerfilScreenPreview() {
    _21DuelTheme {
        PerfilScreenContent(
            utilizador = Utilizador(
                username = "Jogador de Exemplo",
                vitorias = 12,
                derrotas = 4
            ),
            cosmeticos = listOf(
                Cosmetico(id = "1", nome = "Avatar Padrão"),
                Cosmetico(id = "2", nome = "Baralho Neon", exclusivoPRO = true),
                Cosmetico(id = "3", nome = "Mesa Clássica")
            ),
            estaDesbloqueado = { it.id != "2" },
            onSimularPRO = {},
            onEquiparAvatar = {}
        )
    }
}
