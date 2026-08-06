package com.seanmahaffey.bapops

import android.content.Intent
import android.os.Bundle
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
    }
}