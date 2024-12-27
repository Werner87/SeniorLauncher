package com.example.seniorapp

import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
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
            enterTransition = {
                // Fade in and slide in from bottom
                fadeIn(tween(800)) + slideInHorizontally(
                    initialOffsetX = { 1000 }, // Start from the right
                    animationSpec = tween(800)
                )
            },
            exitTransition = {
                // Fade out and slide out to top
                fadeOut(tween(800)) + slideOutHorizontally(
                    targetOffsetX = { -1000 }, // Slide out to the left
                    animationSpec = tween(800)
                )
            }
        ) {
            HomePage(onNavigateToSettings = {
                navController.navigate("settings")
            })
        }
        composable(
            "settings",
            enterTransition = {
                // Fade in and slide in from bottom
                fadeIn(tween(800)) + slideInHorizontally(
                    initialOffsetX = { 1000 }, // Start from the right
                    animationSpec = tween(800)
                )
            },
            exitTransition = {
                // Fade out and slide out to top
                fadeOut(tween(800)) + slideOutHorizontally(
                    targetOffsetX = { -1000 }, // Slide out to the left
                    animationSpec = tween(800)
                )
            }
        ) {
            SettingsScreen(onBackPressed = {
                navController.popBackStack()
            })
        }
    }
}
