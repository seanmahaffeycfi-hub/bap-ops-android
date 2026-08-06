package com.seanmahaffey.bapops

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.seanmahaffey.bapops.data.Donation
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class DonationEntryActivity : AppCompatActivity() {

    private val viewModel: DonationViewModel by viewModels()

    private var selectedDateMillis: Long = System.currentTimeMillis()
    private val displayDateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_donation_entry)

        val dateButton = findViewById<android.widget.Button>(R.id.dateButton)
        val saveButton = findViewById<android.widget.Button>(R.id.saveButton)

        dateButton.text = displayDateFormat.format(selectedDateMillis)
        dateButton.setOnClickListener { showDatePicker(dateButton) }

        saveButton.setOnClickListener { saveDonation() }
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

    private fun saveDonation() {
        val donorName = findViewById<android.widget.EditText>(R.id.donorNameInput).text.toString().trim()
        val description = findViewById<android.widget.EditText>(R.id.descriptionInput).text.toString().trim()
        val valueText = findViewById<android.widget.EditText>(R.id.valueInput).text.toString().trim()

        if (donorName.isEmpty()) {
            Toast.makeText(this, "Enter the donor's name", Toast.LENGTH_SHORT).show()
            return
        }
        if (description.isEmpty()) {
            Toast.makeText(this, "Enter a description", Toast.LENGTH_SHORT).show()
            return
        }
        val value = valueText.toDoubleOrNull()
        if (value == null) {
            Toast.makeText(this, "Enter a valid value", Toast.LENGTH_SHORT).show()
            return
        }

        val donation = Donation(
            date = selectedDateMillis,
            description = description,
            value = value,
            donorName = donorName
        )

        viewModel.saveDonation(donation) { newId ->
            Toast.makeText(this, "Donation saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}