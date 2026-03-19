package com.vibecode.jokeapp

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.net.URL

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    JokeScreen()
                }
            }
        }
    }
}

@Composable
fun JokeScreen() {
    var setup by remember { mutableStateOf("Ready for a joke?") }
    var punchline by remember { mutableStateOf("") }
    var isLoading by remember { mutableStateOf(false) }

    val coroutineScope = rememberCoroutineScope()

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(24.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Text(
            text = setup,
            style = MaterialTheme.typography.titleLarge,
            textAlign = TextAlign.Center,
            modifier = Modifier.padding(bottom = 16.dp)
        )

        if (punchline.isNotEmpty()) {
            Text(
                text = punchline,
                style = MaterialTheme.typography.bodyLarge,
                textAlign = TextAlign.Center,
                modifier = Modifier.padding(bottom = 32.dp)
            )
        } else {
            Spacer(modifier = Modifier.height(32.dp))
        }

        Button(
            onClick = {
                isLoading = true
                setup = "Loading..."
                punchline = ""
                
                coroutineScope.launch {
                    try {
                        val response = withContext(Dispatchers.IO) {
                            URL("https://official-joke-api.appspot.com/random_joke").readText()
                        }
                        val json = JSONObject(response)
                        setup = json.getString("setup")
                        punchline = json.getString("punchline")
                        isLoading = false
                    } catch (e: Exception) {
                        e.printStackTrace()
                        setup = "Oops! Failed to load a joke."
                        punchline = "Please check your internet connection."
                        isLoading = false
                    }
                }
            },
            enabled = !isLoading
        ) {
            Text(if (isLoading) "Loading..." else "Tell me a joke!")
        }
    }
}
