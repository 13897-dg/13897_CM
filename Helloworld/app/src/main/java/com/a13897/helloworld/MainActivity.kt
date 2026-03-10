package com.a13897.helloworld

import android.os.Build
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Logcat messages from previous step
        println(this@MainActivity.localClassName + " onCreate")
        println(getString(R.string.activity_oncreate_msg, this@MainActivity.localClassName))

        // Get the TextView where we will display the info
        val systemInfoTextView = findViewById<TextView>(R.id.systemInfoTextView)

        // Gather system information
        val systemInfo = StringBuilder()
        systemInfo.append("Manufacturer: ${Build.MANUFACTURER}\n")
        systemInfo.append("Model: ${Build.MODEL}\n")
        systemInfo.append("Brand: ${Build.BRAND}\n")
        systemInfo.append("Type: ${Build.TYPE}\n")
        systemInfo.append("User: ${Build.USER}\n")
        systemInfo.append("Base: ${Build.VERSION_CODES.BASE}\n")
        systemInfo.append("Incremental: ${Build.VERSION.INCREMENTAL}\n")
        systemInfo.append("SDK: ${Build.VERSION.SDK_INT}\n")
        systemInfo.append("Version Code: ${Build.VERSION.RELEASE}\n")
        systemInfo.append("Display: ${Build.DISPLAY}\n")

        // Display the information
        systemInfoTextView.text = systemInfo.toString()
    }
}