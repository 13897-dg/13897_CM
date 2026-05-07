package com.a13897.weathercomposed.ui

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.google.android.gms.maps.model.CameraPosition
import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.compose.*

class LocationPickerActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        val initialLat = intent.getFloatExtra("lat", 38.7223f).toDouble()
        val initialLon = intent.getFloatExtra("lon", -9.1393f).toDouble()
        
        setContent {
            var selectedLocation by remember { mutableStateOf(LatLng(initialLat, initialLon)) }
            val cameraPositionState = rememberCameraPositionState {
                position = CameraPosition.fromLatLngZoom(selectedLocation, 6f)
            }

            Box(modifier = Modifier.fillMaxSize()) {
                GoogleMap(
                    modifier = Modifier.fillMaxSize(),
                    cameraPositionState = cameraPositionState,
                    onMapClick = { latLng ->
                        selectedLocation = latLng
                    }
                ) {
                    Marker(
                        state = MarkerState(position = selectedLocation),
                        title = "Selected Location"
                    )
                }

                Button(
                    onClick = {
                        val resultIntent = Intent().apply {
                            putExtra("selected_lat", selectedLocation.latitude.toFloat())
                            putExtra("selected_lon", selectedLocation.longitude.toFloat())
                        }
                        setResult(Activity.RESULT_OK, resultIntent)
                        finish()
                    },
                    modifier = Modifier
                        .align(Alignment.BottomCenter)
                        .padding(bottom = 32.dp, start = 32.dp, end = 32.dp)
                        .fillMaxWidth()
                        .height(56.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = Color(0xFF6750A4)),
                    shape = RoundedCornerShape(28.dp)
                ) {
                    Text("Confirm Location", fontSize = 16.sp, fontWeight = FontWeight.Bold, color = Color.White)
                }
            }
        }
    }
}
