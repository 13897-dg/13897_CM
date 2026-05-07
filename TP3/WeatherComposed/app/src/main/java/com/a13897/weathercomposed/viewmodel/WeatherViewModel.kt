package com.a13897.weathercomposed.viewmodel

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.a13897.weathercomposed.data.WeatherApiClient
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class WeatherUIState(
    val latitude: Float = 40.6405f,
    val longitude: Float = -8.6538f,
    val temperature: Float = 0f,
    val windspeed: Float = 0f,
    val winddirection: Int = 0,
    val weathercode: Int = 0,
    val seaLevelPressure: Float = 0f,
    val time: String = ""
)

class WeatherViewModel : ViewModel() {
    private val _uiState = MutableStateFlow(WeatherUIState())
    val uiState: StateFlow<WeatherUIState> = _uiState.asStateFlow()

    init {
        fetchWeather()
    }

    fun updateLatitude(lat: Float) {
        _uiState.update { it.copy(latitude = lat) }
    }

    fun updateLongitude(lon: Float) {
        _uiState.update { it.copy(longitude = lon) }
    }

    fun fetchWeather() {
        viewModelScope.launch {
            val data = WeatherApiClient.getWeather(_uiState.value.latitude, _uiState.value.longitude)
            if (data != null) {
                _uiState.update {
                    it.copy(
                        temperature = data.current_weather.temperature,
                        windspeed = data.current_weather.windspeed,
                        winddirection = data.current_weather.winddirection,
                        weathercode = data.current_weather.weathercode,
                        time = data.current_weather.time,
                        seaLevelPressure = data.hourly.pressure_msl.firstOrNull()?.toFloat() ?: 0f
                    )
                }
            }
        }
    }
}
