package com.seanmahaffey.bapops

import android.Manifest
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import com.google.android.gms.location.LocationServices
import com.seanmahaffey.bapops.data.MileageEntry
import com.seanmahaffey.bapops.data.RecordType
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class MileageEntryActivity : AppCompatActivity() {

    private val viewModel: MileageViewModel by viewModels()

    private var selectedDateMillis: Long = System.currentTimeMillis()
    private val displayDateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)

    private var lastEndingMileage: Double? = null
    private var startLat: Double? = null
    private var startLng: Double? = null
    private var endLat: Double? = null
    private var endLng: Double? = null

    private var pendingLocationTarget: LocationTarget? = null

    private enum class LocationTarget { START, END }

    private val requestLocationPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                pendingLocationTarget?.let { captureLocation(it) }
            } else {
                Toast.makeText(this, "Location permission is required to capture GPS coordinates", Toast.LENGTH_LONG).show()
            }
            pendingLocationTarget = null
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_mileage_entry)

        val dateButton = findViewById<android.widget.Button>(R.id.dateButton)
        val saveButton = findViewById<android.widget.Button>(R.id.saveButton)
        val captureStartButton = findViewById<android.widget.Button>(R.id.captureStartLocationButton)
        val captureEndButton = findViewById<android.widget.Button>(R.id.captureEndLocationButton)

        dateButton.text = displayDateFormat.format(selectedDateMillis)
        dateButton.setOnClickListener { showDatePicker(dateButton) }

        captureStartButton.setOnClickListener { requestLocationCapture(LocationTarget.START) }
        captureEndButton.setOnClickListener { requestLocationCapture(LocationTarget.END) }

        saveButton.setOnClickListener { saveMileageEntry() }

        viewModel.loadLastEntry { lastEntry ->
            val label = findViewById<android.widget.TextView>(R.id.lastEndingMileageLabel)
            if (lastEntry != null) {
                lastEndingMileage = lastEntry.endMileage
                label.text = "Last recorded ending mileage: ${lastEntry.endMileage}"
            } else {
                label.text = "Last recorded ending mileage: (none yet)"
            }
        }
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

    private fun requestLocationCapture(target: LocationTarget) {
        if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION)
            == PackageManager.PERMISSION_GRANTED
        ) {
            captureLocation(target)
        } else {
            pendingLocationTarget = target
            requestLocationPermission.launch(Manifest.permission.ACCESS_FINE_LOCATION)
        }
    }

    @android.annotation.SuppressLint("MissingPermission")
    private fun captureLocation(target: LocationTarget) {
        val fusedClient = LocationServices.getFusedLocationProviderClient(this)
        fusedClient.lastLocation
            .addOnSuccessListener { location: Location? ->
                if (location == null) {
                    Toast.makeText(this, "No location available, try again in a moment", Toast.LENGTH_SHORT).show()
                    return@addOnSuccessListener
                }
                when (target) {
                    LocationTarget.START -> {
                        startLat = location.latitude
                        startLng = location.longitude
                        findViewById<android.widget.TextView>(R.id.startLocationLabel).text =
                            "Start location: ${location.latitude}, ${location.longitude}"
                    }
                    LocationTarget.END -> {
                        endLat = location.latitude
                        endLng = location.longitude
                        findViewById<android.widget.TextView>(R.id.endLocationLabel).text =
                            "End location: ${location.latitude}, ${location.longitude}"
                    }
                }
            }
            .addOnFailureListener {
                Toast.makeText(this, "Failed to get location: ${it.message}", Toast.LENGTH_LONG).show()
            }
    }

    private fun saveMileageEntry() {
        val startMileageText = findViewById<android.widget.EditText>(R.id.startMileageInput).text.toString().trim()
        val endMileageText = findViewById<android.widget.EditText>(R.id.endMileageInput).text.toString().trim()
        val recordType = if (findViewById<android.widget.RadioButton>(R.id.radio1099).isChecked) {
            RecordType.TAX_1099
        } else {
            RecordType.NONPROFIT_501
        }

        val startMileage = startMileageText.toDoubleOrNull()
        val endMileage = endMileageText.toDoubleOrNull()

        if (startMileage == null) {
            Toast.makeText(this, "Enter a valid starting mileage", Toast.LENGTH_SHORT).show()
            return
        }
        if (endMileage == null) {
            Toast.makeText(this, "Enter a valid ending mileage", Toast.LENGTH_SHORT).show()
            return
        }
        if (endMileage < startMileage) {
            Toast.makeText(this, "Ending mileage cannot be less than starting mileage", Toast.LENGTH_SHORT).show()
            return
        }
        if (lastEndingMileage != null && startMileage == lastEndingMileage) {
            Toast.makeText(
                this,
                "Starting mileage matches the last recorded ending mileage — take a fresh odometer reading instead of reusing it",
                Toast.LENGTH_LONG
            ).show()
            return
        }

        val entry = MileageEntry(
            date = selectedDateMillis,
            startMileage = startMileage,
            endMileage = endMileage,
            recordType = recordType,
            startLat = startLat,
            startLng = startLng,
            endLat = endLat,
            endLng = endLng
        )

        viewModel.saveMileageEntry(entry) {
            Toast.makeText(this, "Mileage entry saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}