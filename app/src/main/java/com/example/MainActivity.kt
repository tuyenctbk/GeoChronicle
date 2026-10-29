package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.activity.viewModels
import com.example.ui.screens.HomeScreen
import com.example.ui.theme.GeoChronicleTheme
import com.example.ui.viewmodel.GeoChronicleViewModel

class MainActivity : ComponentActivity() {
    private val viewModel: GeoChronicleViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            GeoChronicleTheme {
                HomeScreen(viewModel = viewModel)
            }
        }
    }
}
