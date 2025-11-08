// FILE: app/src/main/java/com/madaba/supervisorapp/MainActivity.kt
package com.madaba.supervisorapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.madaba.supervisorapp.ui.screens.LoginScreen
import com.madaba.supervisorapp.ui.screens.ManageWorkersScreen
import com.madaba.supervisorapp.ui.screens.OfflineQueueScreen
import com.madaba.supervisorapp.ui.screens.SupervisorMainScreen
import com.madaba.supervisorapp.ui.screens.WorkerAttendanceScreen
import com.madaba.supervisorapp.ui.theme.SupervisorAppTheme
import dagger.hilt.android.AndroidEntryPoint

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            SupervisorAppTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val navController = rememberNavController()
                    NavHost(navController = navController, startDestination = "login") {
                        composable("login") {
                            LoginScreen(onLoginSuccess = {
                                navController.navigate("main") {
                                    popUpTo("login") { inclusive = true }
                                }
                            })
                        }
                        composable("main") {
                            SupervisorMainScreen(
                                onWorkerClicked = { workerId ->
                                    navController.navigate("attendance/$workerId")
                                },
                                onSyncClicked = { /* Sync handled by ViewModel */ },
                                onOfflineQueueClicked = {
                                    navController.navigate("offline_queue")
                                },
                                onManageWorkersClicked = {
                                    navController.navigate("manage_workers")
                                }
                            )
                        }
                        composable("manage_workers") {
                            ManageWorkersScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable(
                            route = "attendance/{workerId}",
                            arguments = listOf(navArgument("workerId") { type = NavType.StringType })
                        ) { backStackEntry ->
                            val workerId = backStackEntry.arguments?.getString("workerId")
                            requireNotNull(workerId) { "workerId parameter not found" }
                            WorkerAttendanceScreen(
                                workerId = workerId,
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                        composable("offline_queue") {
                            OfflineQueueScreen(
                                onNavigateBack = { navController.popBackStack() }
                            )
                        }
                    }
                }
            }
        }
    }
}