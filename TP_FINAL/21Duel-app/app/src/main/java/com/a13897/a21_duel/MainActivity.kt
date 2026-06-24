package com.a13897.a21_duel

import android.os.Bundle
import android.util.Log
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.tooling.preview.Preview
import com.a13897.a21_duel.ui.theme._21DuelTheme
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MainActivity : ComponentActivity() {

    private val auth = FirebaseAuth.getInstance()
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            _21DuelTheme {
                Scaffold(modifier = Modifier.fillMaxSize()) { innerPadding ->
                    Greeting(
                        name = "Android",
                        modifier = Modifier.padding(innerPadding)
                    )
                }
            }
        }

        testarFirebase()
    }

    private fun testarFirebase() {
        val emailTeste = "teste@21duel.com"
        val passwordTeste = "teste123456"

        // 1. Tenta criar conta de teste (só funciona a 1ª vez, depois dá erro "já existe" — é normal)
        auth.createUserWithEmailAndPassword(emailTeste, passwordTeste)
            .addOnCompleteListener { criarTask ->
                if (criarTask.isSuccessful) {
                    Log.d("FirebaseTeste", "Conta criada com sucesso")
                } else {
                    Log.d("FirebaseTeste", "Conta já existe ou erro: ${criarTask.exception?.message}")
                }

                // 2. Faz login de seguida (cria ou não, tentamos sempre fazer login)
                auth.signInWithEmailAndPassword(emailTeste, passwordTeste)
                    .addOnCompleteListener { loginTask ->
                        if (loginTask.isSuccessful) {
                            Log.d("FirebaseTeste", "Login feito com sucesso. UID: ${auth.currentUser?.uid}")
                            guardarTextoTeste()
                        } else {
                            Log.e("FirebaseTeste", "Erro no login: ${loginTask.exception?.message}")
                        }
                    }
            }
    }

    private fun guardarTextoTeste() {
        val dadosTeste = hashMapOf(
            "mensagem" to "Hello World do 21 Duel",
            "timestamp" to System.currentTimeMillis()
        )

        db.collection("testes")
            .add(dadosTeste)
            .addOnSuccessListener { documentoRef ->
                Log.d("FirebaseTeste", "Documento guardado com ID: ${documentoRef.id}")
                lerTextoTeste()
            }
            .addOnFailureListener { erro ->
                Log.e("FirebaseTeste", "Erro ao guardar: ${erro.message}")
            }
    }

    private fun lerTextoTeste() {
        db.collection("testes")
            .get()
            .addOnSuccessListener { resultado ->
                for (documento in resultado) {
                    Log.d("FirebaseTeste", "Lido: ${documento.data}")
                }
            }
            .addOnFailureListener { erro ->
                Log.e("FirebaseTeste", "Erro ao ler: ${erro.message}")
            }
    }
}

@Composable
fun Greeting(name: String, modifier: Modifier = Modifier) {
    Text(
        text = "Hello $name!",
        modifier = modifier
    )
}

@Preview(showBackground = true)
@Composable
fun GreetingPreview() {
    _21DuelTheme {
        Greeting("Android")
    }
}