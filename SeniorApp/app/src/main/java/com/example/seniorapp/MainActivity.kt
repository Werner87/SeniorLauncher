package com.example.seniorapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.OnBackPressedCallback
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.DisposableEffect
import androidx.navigation.NavController
import androidx.navigation.compose.rememberNavController
import com.example.seniorapp.ui.theme.SeniorAppTheme

class MainActivity : ComponentActivity() {

    private lateinit var backPressedCallback: OnBackPressedCallback

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setupWindow()

        setContent {
            SeniorAppTheme {
                val navController = rememberNavController()
                AppNavHost(navController = navController)
                HandleBackPress(navController)
            }
        }
    }

    override fun onDestroy() {
        backPressedCallback.remove()
        super.onDestroy()
    }

    @Composable
    fun HandleBackPress(navController: NavController) {
        backPressedCallback = onBackPressedDispatcher.addCallback {
            if (navController.currentBackStackEntry?.destination?.route == "home") {
            } else {
                navController.popBackStack()
            }
        }

        DisposableEffect(Unit) {
            onDispose {
                backPressedCallback.remove()
            }
        }
    }
}
