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
                navController.navigate("menu")
            })
        }

        composable("menu") {
            MenuScreen(
                onJogarOnline = { navController.navigate("lobby") },
                onJogarIA = { idPartida, _, _ ->
                    navController.navigate("jogo/$idPartida")
                },
                onTutorial = { navController.navigate("tutorial") },
                onPerfil = { navController.navigate("perfil") }
            )
        }

        composable("lobby") {
            LobbyScreen(onPartidaEncontrada = { idPartida ->
                navController.navigate("jogo/$idPartida")
            })
        }

        composable(
            "jogo/{idPartida}",
            arguments = listOf(navArgument("idPartida") { type = NavType.StringType })
        ) { backStackEntry ->
            val idPartida = backStackEntry.arguments?.getString("idPartida") ?: ""
            JogoScreen(idPartida = idPartida, onFimDeJogo = {
                navController.navigate("resultados/$idPartida") {
                    popUpTo("jogo/{idPartida}") { inclusive = true }
                }
            })
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
