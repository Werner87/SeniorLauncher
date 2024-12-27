package com.example.seniorapp

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.runtime.Composable
import androidx.navigation.NavHostController
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable

@Composable
fun AppNavHost(navController: NavHostController) {
    NavHost(
        navController = navController,
        startDestination = "home"
    ) {
        composable(
            "home",
            enterTransition = { fadeIn(tween(1000)) },
            exitTransition = { fadeOut(tween(1000)) }
        ) {
            HomePage(onNavigateToSettings = {
                navController.navigate("settings")
            })
        }
        composable(
            "settings",
            enterTransition = { fadeIn(tween(1000)) },
            exitTransition = { fadeOut(tween(1000)) }
        ) {
            SettingsScreen(onBackPressed = {
                navController.popBackStack()
            })
        }
    }
}
