package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import androidx.compose.animation.Crossfade
import androidx.compose.animation.core.tween
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import com.example.ui.screens.HomeScreen
import com.example.ui.screens.SplashScreen
import com.example.ui.theme.GeoChronicleTheme
import com.example.ui.viewmodel.GeoChronicleViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: GeoChronicleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GeoChronicleTheme {
                var showSplash by rememberSaveable { mutableStateOf(true) }

                Crossfade(
                    targetState = showSplash,
                    animationSpec = tween(durationMillis = 500),
                    label = "SplashCrossfade"
                ) { isSplash ->
                    if (isSplash) {
                        SplashScreen(
                            onSplashComplete = { showSplash = false }
                        )
                    } else {
                        HomeScreen(viewModel = viewModel)
                    }
                }
            }
        }
    }
}
