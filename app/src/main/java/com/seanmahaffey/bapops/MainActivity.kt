package com.seanmahaffey.bapops

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import kotlinx.coroutines.launch

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

        findViewById<android.widget.Button>(R.id.addExpenseButton).setOnClickListener {
            startActivity(Intent(this, ExpenseEntryActivity::class.java))
        }
        findViewById<android.widget.Button>(R.id.addDonationButton).setOnClickListener {
            startActivity(Intent(this, DonationEntryActivity::class.java))
        }
        findViewById<android.widget.Button>(R.id.addVaseReceivedButton).setOnClickListener {
            startActivity(Intent(this, VaseReceivedEntryActivity::class.java))
        }
        findViewById<android.widget.Button>(R.id.addVaseReturnedButton).setOnClickListener {
            startActivity(Intent(this, VaseReturnedEntryActivity::class.java))
        }
        findViewById<android.widget.Button>(R.id.addMileageButton).setOnClickListener {
            startActivity(Intent(this, MileageEntryActivity::class.java))
        }
        findViewById<android.widget.Button>(R.id.settingsButton).setOnClickListener {
            startActivity(Intent(this, SettingsActivity::class.java))
        }
        findViewById<android.widget.Button>(R.id.syncNowButton).setOnClickListener {
            syncNow()
        }
    }

    private fun syncNow() {
        lifecycleScope.launch {
            val result = SyncManager(this@MainActivity).syncAll()
            val message = when (result) {
                is SyncResult.Success -> "Synced ${result.count} expense(s)"
                is SyncResult.NotOnTargetNetwork -> "Not on the target network. Detected: \"${result.detectedSsid}\""
                is SyncResult.MissingSettings -> "Set the server URL and auth token in Settings first"
                is SyncResult.NetworkError -> "Sync failed: ${result.message}"
            }
            Toast.makeText(this@MainActivity, message, Toast.LENGTH_LONG).show()
        }
    }
}