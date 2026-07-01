package com.a13897.a21_duel.ui.navigation

import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.navArgument
import com.a13897.a21_duel.ui.jogo.JogoScreen
import com.a13897.a21_duel.ui.lobby.LobbyScreen
import com.a13897.a21_duel.ui.login.LoginScreen
import com.a13897.a21_duel.ui.menu.MenuScreen
import com.a13897.a21_duel.ui.perfil.PerfilScreen
import com.a13897.a21_duel.ui.resultados.ResultadosScreen
import com.a13897.a21_duel.ui.tutorial.TutorialScreen

@Composable
fun NavGraph(navController: NavHostController) {
    NavHost(navController = navController, startDestination = "login") {

        composable("login") {
            LoginScreen(onLoginSuccess = {
                navController.navigate("menu") {
                    popUpTo("login") { inclusive = true }
                }
            })
        }

        composable("menu") {
            MenuScreen(
                onJogarOnline = { navController.navigate("lobby") },
                onJogarIA = { idPartida, idJogador1, idJogador2 ->
                    navController.navigate("jogo/$idPartida/$idJogador1/$idJogador2/true")
                },
                onTutorial = { navController.navigate("tutorial") },
                onPerfil = { navController.navigate("perfil") }
            )
        }

        composable("lobby") {
            LobbyScreen(onPartidaEncontrada = { idPartida ->
                // nota: no lobby os jogadores reais vêm do Firebase (Partida.idJogador1/2)
                // por agora passa placeholders — o JogoViewModel lê os UIDs reais do Firestore
                navController.navigate("jogo/$idPartida/online/online/false")
            })
        }

        composable(
            "jogo/{idPartida}/{idJogador1}/{idJogador2}/{contraIA}",
            arguments = listOf(
                navArgument("idPartida") { type = NavType.StringType },
                navArgument("idJogador1") { type = NavType.StringType },
                navArgument("idJogador2") { type = NavType.StringType },
                navArgument("contraIA") { type = NavType.StringType }
            )
        ) { backStackEntry ->
            val idPartida = backStackEntry.arguments?.getString("idPartida") ?: ""
            val idJogador1 = backStackEntry.arguments?.getString("idJogador1") ?: ""
            val idJogador2 = backStackEntry.arguments?.getString("idJogador2") ?: ""
            val contraIA = backStackEntry.arguments?.getString("contraIA") == "true"

            JogoScreen(
                idPartida = idPartida,
                idJogador1 = idJogador1,
                idJogador2 = idJogador2,
                ehContraIA = contraIA,
                onFimDeJogo = {
                    navController.navigate("resultados/$idPartida") {
                        popUpTo("jogo/{idPartida}/{idJogador1}/{idJogador2}/{contraIA}") { inclusive = true }
                    }
                }
            )
        }

        composable(
            "resultados/{idPartida}",
            arguments = listOf(navArgument("idPartida") { type = NavType.StringType })
        ) { backStackEntry ->
            val idPartida = backStackEntry.arguments?.getString("idPartida") ?: ""
            ResultadosScreen(
                idPartida = idPartida,
                onJogarOutraVez = { navController.navigate("lobby") },
                onMenuPrincipal = {
                    navController.navigate("menu") {
                        popUpTo("menu") { inclusive = true }
                    }
                }
            )
        }

        composable("perfil") {
            PerfilScreen()
        }

        composable("tutorial") {
            TutorialScreen()
        }
    }
}