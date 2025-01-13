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
                fadeIn(tween(500)) + slideInHorizontally(
                    initialOffsetX = { 500 }, // Slide from the right
                    animationSpec = tween(500)
                )
            },
            exitTransition = {
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
                fadeIn(tween(500)) + slideInHorizontally(
                    initialOffsetX = { 500 }, // Slide from the right
                    animationSpec = tween(500)
                )
            },
            exitTransition = {
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