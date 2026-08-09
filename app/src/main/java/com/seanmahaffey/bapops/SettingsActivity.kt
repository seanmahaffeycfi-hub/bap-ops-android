package com.seanmahaffey.bapops

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity

class SettingsActivity : AppCompatActivity() {

    private lateinit var settings: SettingsManager

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_settings)

        settings = SettingsManager(this)

        val serverUrlInput = findViewById<android.widget.EditText>(R.id.serverUrlInput)
        val wifiSsidInput = findViewById<android.widget.EditText>(R.id.wifiSsidInput)
        val authTokenInput = findViewById<android.widget.EditText>(R.id.authTokenInput)
        val saveButton = findViewById<android.widget.Button>(R.id.saveSettingsButton)

        serverUrlInput.setText(settings.serverBaseUrl)
        wifiSsidInput.setText(settings.targetWifiSsid)
        authTokenInput.setText(settings.authToken)

        saveButton.setOnClickListener {
            var url = serverUrlInput.text.toString().trim()
            if (url.isNotEmpty() && !url.endsWith("/")) {
                url += "/"
            }
            settings.serverBaseUrl = url
            settings.targetWifiSsid = wifiSsidInput.text.toString().trim()
            settings.authToken = authTokenInput.text.toString().trim()
            Toast.makeText(this, "Settings saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}