package com.seanmahaffey.bapops

import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.seanmahaffey.bapops.data.VaseReturned
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class VaseReturnedEntryActivity : AppCompatActivity() {

    private val viewModel: VaseReturnedViewModel by viewModels()

    private var selectedDateMillis: Long = System.currentTimeMillis()
    private val displayDateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_vase_returned_entry)

        val dateButton = findViewById<android.widget.Button>(R.id.dateButton)
        val saveButton = findViewById<android.widget.Button>(R.id.saveButton)

        dateButton.text = displayDateFormat.format(selectedDateMillis)
        dateButton.setOnClickListener { showDatePicker(dateButton) }

        saveButton.setOnClickListener { saveVaseReturned() }
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

    private fun saveVaseReturned() {
        val quantityText = findViewById<android.widget.EditText>(R.id.quantityInput).text.toString().trim()
        val returnedFrom = findViewById<android.widget.EditText>(R.id.returnedFromInput).text.toString().trim()

        val quantity = quantityText.toIntOrNull()
        if (quantity == null || quantity <= 0) {
            Toast.makeText(this, "Enter a valid quantity", Toast.LENGTH_SHORT).show()
            return
        }
        if (returnedFrom.isEmpty()) {
            Toast.makeText(this, "Enter who returned these vases", Toast.LENGTH_SHORT).show()
            return
        }

        val vaseReturned = VaseReturned(
            dateReturned = selectedDateMillis,
            quantity = quantity,
            returnedFrom = returnedFrom
        )

        viewModel.saveVaseReturned(vaseReturned) {
            Toast.makeText(this, "Vases returned saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}