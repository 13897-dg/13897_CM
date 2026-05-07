package com.a13897.weathercomposed.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import com.a13897.weathercomposed.R

@Composable
fun WeatherCard(windSpeed: Float, windDirection: Int, seaLevelPressure: Float, temperature: Float, time: String) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = Color(0xFFE8E2E9)
        ),
        shape = RoundedCornerShape(16.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            WeatherRow(label = stringResource(R.string.sea_level_pressure), value = "$seaLevelPressure hPa")
            WeatherRow(label = stringResource(R.string.wind_direction), value = "$windDirection°")
            WeatherRow(label = stringResource(R.string.wind_speed), value = "$windSpeed km/h")
            WeatherRow(label = stringResource(R.string.temperature), value = "$temperature°C")
            WeatherRow(label = stringResource(R.string.time), value = time)
        }
    }
}

@Preview(showBackground = true)
@Composable
fun WeatherCardPreview() {
    Box(modifier = Modifier.background(Color.White).padding(16.dp)) {
        WeatherCard(windSpeed = 20.8f, windDirection = 296, seaLevelPressure = 0.0f, temperature = 14.4f, time = "2025-03-26T14:45")
    }
}
