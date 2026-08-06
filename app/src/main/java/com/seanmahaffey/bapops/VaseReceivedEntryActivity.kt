package com.seanmahaffey.bapops

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.seanmahaffey.bapops.data.VaseReceived
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class VaseReceivedEntryActivity : AppCompatActivity() {

    private val viewModel: VaseReceivedViewModel by viewModels()

    private var selectedDateMillis: Long = System.currentTimeMillis()
    private val displayDateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vase_received_entry)

        val dateButton = findViewById<android.widget.Button>(R.id.dateButton)
        val saveButton = findViewById<android.widget.Button>(R.id.saveButton)

        dateButton.text = displayDateFormat.format(selectedDateMillis)
        dateButton.setOnClickListener { showDatePicker(dateButton) }

        saveButton.setOnClickListener { saveVaseReceived() }
    }

    private fun showDatePicker(dateButton: android.widget.Button) {
        val calendar = Calendar.getInstance()
        calendar.timeInMillis = selectedDateMillis
        android.app.DatePickerDialog(
            this,
            { _, year, month, dayOfMonth ->
                calendar.set(year, month, dayOfMonth)
                selectedDateMillis = calendar.timeInMillis
                dateButton.text = displayDateFormat.format(selectedDateMillis)
            },
            calendar.get(Calendar.YEAR),
            calendar.get(Calendar.MONTH),
            calendar.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun saveVaseReceived() {
        val quantityText = findViewById<android.widget.EditText>(R.id.quantityInput).text.toString().trim()
        val pocName = findViewById<android.widget.EditText>(R.id.pocNameInput).text.toString().trim()
        val pocFacilityName = findViewById<android.widget.EditText>(R.id.pocFacilityNameInput).text.toString().trim()
        val pocPhone = findViewById<android.widget.EditText>(R.id.pocPhoneInput).text.toString().trim()
        val pocEmail = findViewById<android.widget.EditText>(R.id.pocEmailInput).text.toString().trim()
        val recipient = findViewById<android.widget.EditText>(R.id.recipientInput).text.toString().trim()

        val quantity = quantityText.toIntOrNull()
        if (quantity == null || quantity <= 0) {
            Toast.makeText(this, "Enter a valid quantity", Toast.LENGTH_SHORT).show()
            return
        }
        if (pocName.isEmpty()) {
            Toast.makeText(this, "Enter the POC name", Toast.LENGTH_SHORT).show()
            return
        }
        if (recipient.isEmpty()) {
            Toast.makeText(this, "Enter who is receiving these vases", Toast.LENGTH_SHORT).show()
            return
        }

        val vaseReceived = VaseReceived(
            dateReceived = selectedDateMillis,
            quantity = quantity,
            pocName = pocName,
            pocFacilityName = pocFacilityName,
            pocPhone = pocPhone,
            pocEmail = pocEmail,
            recipient = recipient
        )

        viewModel.saveVaseReceived(vaseReceived) {
            Toast.makeText(this, "Vases received saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}