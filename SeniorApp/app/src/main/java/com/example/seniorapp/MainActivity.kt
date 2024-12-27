package com.example.seniorapp

import android.os.Build
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.compose.runtime.DisposableEffect
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.toArgb
import androidx.navigation.compose.rememberNavController
import com.example.seniorapp.ui.theme.SeniorAppTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Ustawienia transparentnego paska nawigacyjnego
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            window.setNavigationBarColor(Color.White.toArgb())
            window.setNavigationBarDividerColor(Color.Black.toArgb())
        }

        setContent {
            SeniorAppTheme {
                // Pamiętamy kontroler nawigacji
                val navController = rememberNavController()

                // Handle back press navigation
                val backPressedCallback = onBackPressedDispatcher.addCallback(this) {
                    // Check if we're on the home screen
                    if (navController.currentBackStackEntry?.destination?.route == "home") {
                        // Prevent back press if we're on the home screen
//                        Toast.makeText(this@MainActivity, "Jesteś już na stronie głównej!", Toast.LENGTH_SHORT).show()
                    } else {
                        // Otherwise allow back press to navigate
                        navController.popBackStack()
                    }
                }

                AppNavHost(navController = navController)

                // Dispose of callback when the Composable is disposed
                DisposableEffect(Unit) {
                    onDispose {
                        backPressedCallback.remove()
                    }
                }
            }
        }
    }
}
