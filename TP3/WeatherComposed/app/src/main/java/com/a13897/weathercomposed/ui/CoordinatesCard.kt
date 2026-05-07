package com.a13897.weathercomposed.ui

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.LocationOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.a13897.weathercomposed.R
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.foundation.background

@Composable
fun CoordinatesCard(
    latitude: Float,
    longitude: Float,
    onLatitudeChange: (String) -> Unit,
    onLongitudeChange: (String) -> Unit,
    onOpenLocationPicker: () -> Unit
) {
    var latText by remember(latitude) { mutableStateOf(latitude.toString()) }
    var lonText by remember(longitude) { mutableStateOf(longitude.toString()) }

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
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = stringResource(R.string.coordinates),
                    fontWeight = FontWeight.Bold,
                    fontSize = 18.sp,
                    color = Color.Black
                )
                IconButton(onClick = onOpenLocationPicker) {
                    Icon(Icons.Default.LocationOn, contentDescription = null, tint = Color.Black)
                }
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            OutlinedTextField(
                value = latText,
                onValueChange = { 
                    latText = it
                    onLatitudeChange(it) 
                },
                label = { Text(stringResource(R.string.latitude)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.DarkGray
                )
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            OutlinedTextField(
                value = lonText,
                onValueChange = { 
                    lonText = it
                    onLongitudeChange(it) 
                },
                label = { Text(stringResource(R.string.longitude)) },
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number),
                modifier = Modifier.fillMaxWidth(),
                shape = RoundedCornerShape(8.dp),
                colors = OutlinedTextFieldDefaults.colors(
                    focusedTextColor = Color.Black,
                    unfocusedTextColor = Color.Black,
                    focusedBorderColor = Color.Black,
                    unfocusedBorderColor = Color.Gray,
                    focusedLabelColor = Color.Black,
                    unfocusedLabelColor = Color.DarkGray
                )
            )
        }
    }
}

@Preview(showBackground = true)
@Composable
fun CoordinatesCardPreview() {
    Box(modifier = Modifier.background(Color.White).padding(16.dp)) {
        CoordinatesCard(
            latitude = 40.0f,
            longitude = -8.0f,
            onLatitudeChange = {},
            onLongitudeChange = {},
            onOpenLocationPicker = {}
        )
    }
}
