package com.evest.antivirus.ui.navigation

import androidx.compose.runtime.Composable
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import com.evest.antivirus.ui.screens.HomeScreen
import com.evest.antivirus.ui.screens.PermissionsScreen
import com.evest.antivirus.ui.screens.ProtectionLogScreen
import com.evest.antivirus.ui.screens.ScanScreen
import com.evest.antivirus.ui.screens.SettingsScreen
import com.evest.antivirus.ui.screens.SplashVideoScreen
import com.evest.antivirus.ui.screens.ThreatLogScreen
import com.evest.antivirus.ui.viewmodel.EvestViewModelFactory
import com.evest.antivirus.ui.viewmodel.HomeViewModel
import com.evest.antivirus.ui.viewmodel.ProtectionLogViewModel
import com.evest.antivirus.ui.viewmodel.ScanViewModel
import com.evest.antivirus.ui.viewmodel.SettingsViewModel
import com.evest.antivirus.ui.viewmodel.ThreatLogViewModel

@Composable
fun EvestNavGraph(
    navController: NavHostController,
    startDestination: String,
    factory: EvestViewModelFactory
) {
    NavHost(navController = navController, startDestination = startDestination) {

        composable(Routes.SPLASH) {
            SplashVideoScreen(onFinished = {
                navController.navigate(Routes.PERMISSIONS) {
                    popUpTo(Routes.SPLASH) { inclusive = true }
                }
            })
        }

        composable(Routes.PERMISSIONS) {
            PermissionsScreen(onDone = {
                navController.navigate(Routes.HOME) {
                    popUpTo(Routes.PERMISSIONS) { inclusive = true }
                }
            })
        }

        composable(Routes.HOME) {
            val vm: HomeViewModel = viewModel(factory = factory)
            HomeScreen(
                viewModel = vm,
                onOpenScan = { navController.navigate(Routes.SCAN) },
                onOpenThreats = { navController.navigate(Routes.THREATS) },
                onOpenProtectionLog = { navController.navigate(Routes.PROTECTION_LOG) },
                onOpenSettings = { navController.navigate(Routes.SETTINGS) }
            )
        }

        composable(Routes.SCAN) {
            val vm: ScanViewModel = viewModel(factory = factory)
            ScanScreen(
                viewModel = vm,
                onBack = { navController.popBackStack() },
                onOpenThreats = { navController.navigate(Routes.THREATS) }
            )
        }

        composable(Routes.THREATS) {
            val vm: ThreatLogViewModel = viewModel(factory = factory)
            ThreatLogScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        composable(Routes.PROTECTION_LOG) {
            val vm: ProtectionLogViewModel = viewModel(factory = factory)
            ProtectionLogScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }

        composable(Routes.SETTINGS) {
            val vm: SettingsViewModel = viewModel(factory = factory)
            SettingsScreen(viewModel = vm, onBack = { navController.popBackStack() })
        }
    }
}
