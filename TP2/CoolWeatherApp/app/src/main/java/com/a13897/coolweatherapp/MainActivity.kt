package com.a13897.coolweatherapp

import android.os.Bundle
import android.widget.ImageView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    var day = true
    override fun onCreate(savedInstanceState: Bundle?) {

        when (resources.configuration.orientation) {
            android.content.res.Configuration.ORIENTATION_PORTRAIT -> {
                if (day) {
                    setTheme(R.style.Theme_Day)
                } else {
                    setTheme(R.style.Theme_Night)
                }
            }
            android.content.res.Configuration.ORIENTATION_LANDSCAPE -> {
                if (day) {
                    setTheme(R.style.Theme_Day_Land)
                } else {
                    setTheme(R.style.Theme_Night_Land)
                }
            }
        }

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val etLatitude = findViewById<android.widget.EditText>(R.id.nmb_lat)
        val etLongitude = findViewById<android.widget.EditText>(R.id.nmb_long)
        val btnUpdate = findViewById<android.widget.Button>(R.id.button)

        val lat = etLatitude.text.toString().toFloatOrNull() ?: 38.76f
        val lon = etLongitude.text.toString().toFloatOrNull() ?: -9.12f

        //
        fetchWeatherData(lat, lon)

        btnUpdate.setOnClickListener {
            val userLat = etLatitude.text.toString().toFloatOrNull() ?: 0.0f
            val userLon = etLongitude.text.toString().toFloatOrNull() ?: 0.0f
            fetchWeatherData(userLat, userLon)
        }
    }
    private fun WeatherAPI_Call(lat: Float, long: Float): WeatherData {
        val reqString = buildString {
            append("https://api.open-meteo.com/v1/forecast?")
            append("latitude=${lat}&longitude=${long}&")
            append("current_weather=true&")
            append("hourly=temperature_2m,weathercode,pressure_msl,windspeed_10m&")
            append("timezone=auto")
        }

        val url = java.net.URL(reqString)
        url.openStream().use {
            val request = com.google.gson.Gson().fromJson(java.io.InputStreamReader(it, "UTF-8"), WeatherData::class.java)
            return request
        }
    }
    private fun fetchWeatherData(lat: Float, long: Float) {
        Thread {
            try {
                val weather = WeatherAPI_Call(lat, long)
                updateUI(weather)
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }.start()
    }

    private fun updateUI(request: WeatherData) {
        runOnUiThread {
            val tvPressure = findViewById<TextView>(R.id.tvPressure)
            val tvWindDir = findViewById<TextView>(R.id.tvWindDir)
            val tvWindSpeed = findViewById<TextView>(R.id.tvWindSpeed)
            val tvTemperature = findViewById<TextView>(R.id.tvTemperature)
            val tvTime = findViewById<TextView>(R.id.tvTime)

            tvPressure.text = "${request.hourly.pressure_msl[12]} hPa"
            tvWindDir.text = "${request.current_weather.winddirection}"
            tvWindSpeed.text = "${request.current_weather.windspeed} km/h"
            tvTemperature.text = "${request.current_weather.temperature} ºC"
            tvTime.text = request.current_weather.time

            val mapt = getWeatherCodeMap()
            val wCode = mapt[request.current_weather.weathercode]

            val wImage = when (wCode) {
                WMO_WeatherCode.CLEAR_SKY,
                WMO_WeatherCode.MAINLY_CLEAR,
                WMO_WeatherCode.PARTLY_CLOUDY -> {
                    if (day) wCode.image + "day" else wCode.image + "night"
                }
                else -> wCode?.image ?: "clear_day"
            }
        }
    }
}