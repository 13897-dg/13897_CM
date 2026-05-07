package com.a13897.weathercomposed

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.a13897.weathercomposed.ui.WeatherUI
import com.a13897.weathercomposed.ui.theme.WeatherComposedTheme

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            WeatherComposedTheme {
                WeatherUI()
            }
        }
    }
}