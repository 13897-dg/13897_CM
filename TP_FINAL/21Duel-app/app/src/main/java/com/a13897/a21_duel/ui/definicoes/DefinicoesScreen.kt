package com.a13897.a21_duel.ui.definicoes

import android.content.Intent
import android.provider.Settings
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.a13897.a21_duel.R
import com.a13897.a21_duel.data.model.Utilizador

/**
 * Versão Stateful (com estado) — Lida com o ViewModel e eventos do sistema.
 * É esta função que é chamada pelo NavGraph.
 */
@Composable
fun DefinicoesScreen(
    onVoltar: () -> Unit,
    onSobre: () -> Unit,
    onLogout: () -> Unit,
    viewModel: DefinicoesViewModel = viewModel()
) {
    val utilizador by viewModel.utilizador.collectAsState()
    val erro by viewModel.erro.collectAsState()
    val context = LocalContext.current

    DefinicoesContent(
        utilizador = utilizador,
        erro = erro,
        onVoltar = onVoltar,
        onSobre = onSobre,
        onMudarIdioma = {
            // Abre as definições de idioma do sistema/app
            val intent = Intent(Settings.ACTION_LOCALE_SETTINGS)
            context.startActivity(intent)
        },
        onLogout = {
            viewModel.terminarSessao()
            onLogout()
        }
    )
}

/**
 * Versão Stateless (sem estado) — Apenas desenha a UI baseada nos dados recebidos.
 * Ideal para testes e @Preview.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun DefinicoesContent(
    utilizador: Utilizador?,
    erro: String?,
    onVoltar: () -> Unit,
    onSobre: () -> Unit,
    onMudarIdioma: () -> Unit,
    onLogout: () -> Unit
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(text = stringResource(id = R.string.definicoes_titulo)) },
                navigationIcon = {
                    IconButton(onClick = onVoltar) {
                        Icon(imageVector = Icons.Default.ArrowBack, contentDescription = "Voltar")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(24.dp)
        ) {
            // Secção: Conta
            Text(
                text = stringResource(id = R.string.definicoes_conta),
                fontWeight = FontWeight.Bold,
                fontSize = 18.sp,
                color = MaterialTheme.colorScheme.primary
            )
            
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    if (utilizador != null) {
                        Text(text = "Username: ${utilizador.username}", fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.height(4.dp))
                        Text(text = "Email: ${utilizador.email}")
                    } else if (erro != null) {
                        Text(text = erro, color = MaterialTheme.colorScheme.error)
                    } else {
                        CircularProgressIndicator(modifier = Modifier.align(Alignment.CenterHorizontally))
                    }
                }
            }

            HorizontalDivider()

            // Secção: Opções Gerais
            ListItem(
                headlineContent = { Text(stringResource(id = R.string.definicoes_idioma)) },
                leadingContent = { Icon(Icons.Default.Settings, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onMudarIdioma() },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
            )

            ListItem(
                headlineContent = { Text(stringResource(id = R.string.sobre_titulo)) },
                leadingContent = { Icon(Icons.Default.Info, contentDescription = null) },
                modifier = Modifier
                    .fillMaxWidth()
                    .clickable { onSobre() },
                colors = ListItemDefaults.colors(containerColor = MaterialTheme.colorScheme.background)
            )

            Spacer(modifier = Modifier.weight(1f))

            // Botão Logout
            Button(
                onClick = onLogout,
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = MaterialTheme.colorScheme.error)
            ) {
                Text(text = stringResource(id = R.string.definicoes_terminar_sessao))
            }
        }
    }
}

/**
 * Preview do Ecrã de Definições com dados simulados.
 */
@Preview(showBackground = true, name = "Definições - Utilizador Carregado")
@Composable
fun DefinicoesScreenPreview() {
    MaterialTheme {
        DefinicoesContent(
            utilizador = Utilizador(
                id = "123",
                email = "jogador@enautica.pt",
                username = "O_Competitivo"
            ),
            erro = null,
            onVoltar = {},
            onSobre = {},
            onMudarIdioma = {},
            onLogout = {}
        )
    }
}

@Preview(showBackground = true, name = "Definições - A carregar")
@Composable
fun DefinicoesScreenLoadingPreview() {
    MaterialTheme {
        DefinicoesContent(
            utilizador = null, // Força a mostrar o CircularProgressIndicator
            erro = null,
            onVoltar = {},
            onSobre = {},
            onMudarIdioma = {},
            onLogout = {}
        )
    }
}