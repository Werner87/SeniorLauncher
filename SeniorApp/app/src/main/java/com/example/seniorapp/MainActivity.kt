package com.example.seniorapp

import android.graphics.Color.TRANSPARENT
import android.os.Build
import android.os.Bundle
import android.view.WindowManager
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.addCallback
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.annotation.RequiresApi
import androidx.compose.runtime.DisposableEffect
import androidx.navigation.compose.rememberNavController
import com.example.seniorapp.ui.theme.SeniorAppTheme

class MainActivity : ComponentActivity() {
    @RequiresApi(Build.VERSION_CODES.P)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        window.setFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS,WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS)

        // Ustawienia transparentnego paska nawigacyjnego
        val lightTransparentStyle = SystemBarStyle.light(
            scrim = TRANSPARENT,
            darkScrim = TRANSPARENT
        )
        enableEdgeToEdge(
            statusBarStyle = lightTransparentStyle,
            navigationBarStyle = lightTransparentStyle
        )

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
