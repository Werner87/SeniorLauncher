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
                // Fade in with a smooth slide-in from the right
                fadeIn(tween(500)) + slideInHorizontally(
                    initialOffsetX = { 500 }, // Slide from the right
                    animationSpec = tween(500)
                )
            },
            exitTransition = {
                // Fade out with a smooth slide-out to the left
                fadeOut(tween(500)) + slideOutHorizontally(
                    targetOffsetX = { -500 }, // Slide to the left
                    animationSpec = tween(500)
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
                // Fade in with a smooth slide-in from the right
                fadeIn(tween(500)) + slideInHorizontally(
                    initialOffsetX = { 500 }, // Slide from the right
                    animationSpec = tween(500)
                )
            },
            exitTransition = {
                // Fade out with a smooth slide-out to the left
                fadeOut(tween(500)) + slideOutHorizontally(
                    targetOffsetX = { -500 }, // Slide to the left
                    animationSpec = tween(500)
                )
            }
        ) {
            SettingsScreen(onBackPressed = {
                navController.popBackStack()
            })
        }
    }
}