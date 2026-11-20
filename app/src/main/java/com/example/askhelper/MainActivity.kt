package com.example.askhelper

import android.content.Intent
import android.os.Bundle
import android.provider.Settings
import android.widget.Button
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.SwitchCompat

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val btnSettings = findViewById<Button>(R.id.btnOpenSettings)
        val switchActive = findViewById<SwitchCompat>(R.id.switchActive)

        btnSettings.setOnClickListener {
            startActivity(Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS))
        }

        switchActive.setOnCheckedChangeListener { _, isChecked ->
            AskAccessibilityService.isServiceEnabled = isChecked
            val msg = if (isChecked) "Скрипт активен" else "Скрипт выключен"
            Toast.makeText(this, msg, Toast.LENGTH_SHORT).show()
        }
    }
}
