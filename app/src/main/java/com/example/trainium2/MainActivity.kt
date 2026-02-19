package com.example.trainium2

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.material3.MaterialTheme
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                val navController = rememberNavController()
                NavHost(navController = navController, startDestination = "main") {

                    composable("main") { MainScreen { navController.navigate("login") } }

                    composable("login") {
                        LoginScreen(
                            onBack = { navController.popBackStack() },
                            onNavigateToRegister = { navController.navigate("register") },
                            onNavigateToForgot = { navController.navigate("forgot") },
                            onLoginSuccess = { nombre, isAdmin, idUsuario, isPremium ->
                                navController.navigate("profile/$nombre/$isAdmin/$idUsuario/$isPremium")
                            }
                        )
                    }

                    composable(
                        "profile/{nombre}/{isAdmin}/{idUsuario}/{isPremium}",
                        arguments = listOf(
                            navArgument("idUsuario") { type = NavType.IntType },
                            navArgument("isPremium") { type = NavType.IntType }
                        )
                    ) { backStackEntry ->
                        val nombre = backStackEntry.arguments?.getString("nombre") ?: ""
                        val isAdmin = backStackEntry.arguments?.getString("isAdmin") == "1"
                        val idUsuario = backStackEntry.arguments?.getInt("idUsuario") ?: 0
                        val isPremium = backStackEntry.arguments?.getInt("isPremium") == 1

                        ProfileScreen(
                            nombre = nombre,
                            isAdmin = isAdmin,
                            idUsuario = idUsuario,
                            isPremium = isPremium,
                            onLogout = { navController.navigate("main") { popUpTo(0) } },
                            onNavigateToMaquinas = { admin, id ->
                                val aStr = if (admin) "1" else "0"
                                navController.navigate("maquinas/$aStr/$id")
                            },
                            onNavigateToPlatos = { navController.navigate("platos") },
                            onNavigateToRegistro = { id -> navController.navigate("registro/$id") },
                            onNavigateToReservas = { id -> navController.navigate("reservas/$id") },
                            onNavigateToEditProfile = { id -> navController.navigate("edit_profile/$id") }
                        )
                    }

                    // Pantalla de Edición (Ajustes) que ahora lleva al historial
                    composable(
                        "edit_profile/{idUsuario}",
                        arguments = listOf(navArgument("idUsuario") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val idUsuario = backStackEntry.arguments?.getInt("idUsuario") ?: 0
                        EditProfileScreen(
                            idUsuario = idUsuario,
                            onBack = { navController.popBackStack() },
                            onNavigateToHistorial = { id -> navController.navigate("historial/$id") }
                        )
                    }

                    // Pantalla de Historial de Pagos
                    composable(
                        "historial/{idUsuario}",
                        arguments = listOf(navArgument("idUsuario") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val idUsuario = backStackEntry.arguments?.getInt("idUsuario") ?: 0
                        HistorialScreen(idUsuario = idUsuario, onBack = { navController.popBackStack() })
                    }

                    composable(
                        "maquinas/{isAdmin}/{idUsuario}",
                        arguments = listOf(navArgument("idUsuario") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val isAdmin = backStackEntry.arguments?.getString("isAdmin") == "1"
                        val idUsuario = backStackEntry.arguments?.getInt("idUsuario") ?: 0
                        MaquinasScreen(isAdmin = isAdmin, idUsuario = idUsuario, onBack = { navController.popBackStack() })
                    }

                    composable("platos") { PlatosScreen(onBack = { navController.popBackStack() }) }

                    composable(
                        "registro/{idUsuario}",
                        arguments = listOf(navArgument("idUsuario") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val idUsuario = backStackEntry.arguments?.getInt("idUsuario") ?: 0
                        RegistroScreen(idUsuario = idUsuario, onBack = { navController.popBackStack() })
                    }

                    composable(
                        "reservas/{idUsuario}",
                        arguments = listOf(navArgument("idUsuario") { type = NavType.IntType })
                    ) { backStackEntry ->
                        val idUsuario = backStackEntry.arguments?.getInt("idUsuario") ?: 0
                        ReservasScreen(idUsuario = idUsuario, onBack = { navController.popBackStack() })
                    }

                    composable("register") { RegisterScreen { navController.popBackStack() } }
                    composable("forgot") { ForgotPasswordScreen { navController.popBackStack() } }
                }
            }
        }
    }
}