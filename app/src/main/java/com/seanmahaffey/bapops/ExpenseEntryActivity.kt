package com.seanmahaffey.bapops

import android.Manifest
import android.content.pm.PackageManager
import android.graphics.BitmapFactory
import android.os.Bundle
import android.os.Environment
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import androidx.camera.core.CameraSelector
import androidx.camera.core.ImageCapture
import androidx.camera.core.ImageCaptureException
import androidx.camera.core.Preview
import androidx.camera.lifecycle.ProcessCameraProvider
import androidx.core.content.ContextCompat
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.TextRecognizerOptions
import com.seanmahaffey.bapops.data.Expense
import com.seanmahaffey.bapops.data.RecordType
import java.io.File
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

class ExpenseEntryActivity : AppCompatActivity() {

    private val viewModel: ExpenseViewModel by viewModels()

    private lateinit var imageCapture: ImageCapture
    private var selectedDateMillis: Long = System.currentTimeMillis()
    private val displayDateFormat = SimpleDateFormat("MM/dd/yyyy", Locale.US)

    private val requestCameraPermission =
        registerForActivityResult(ActivityResultContracts.RequestPermission()) { granted ->
            if (granted) {
                startCamera()
            } else {
                Toast.makeText(this, "Camera permission is required to capture receipts", Toast.LENGTH_LONG).show()
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_expense_entry)

        val dateButton = findViewById<android.widget.Button>(R.id.dateButton)
        val captureButton = findViewById<android.widget.Button>(R.id.captureButton)
        val saveButton = findViewById<android.widget.Button>(R.id.saveButton)

        dateButton.text = displayDateFormat.format(selectedDateMillis)
        dateButton.setOnClickListener { showDatePicker(dateButton) }

        captureButton.setOnClickListener { takePhoto() }
        saveButton.setOnClickListener { saveExpense() }

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
            == PackageManager.PERMISSION_GRANTED
        ) {
            startCamera()
        } else {
            requestCameraPermission.launch(Manifest.permission.CAMERA)
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

    private fun startCamera() {
        val cameraProviderFuture = ProcessCameraProvider.getInstance(this)
        cameraProviderFuture.addListener({
            val cameraProvider = cameraProviderFuture.get()

            val previewView = findViewById<androidx.camera.view.PreviewView>(R.id.previewView)
            val preview = Preview.Builder().build().also {
                it.setSurfaceProvider(previewView.surfaceProvider)
            }

            imageCapture = ImageCapture.Builder().build()

            try {
                cameraProvider.unbindAll()
                cameraProvider.bindToLifecycle(
                    this,
                    CameraSelector.DEFAULT_BACK_CAMERA,
                    preview,
                    imageCapture
                )
            } catch (exc: Exception) {
                Toast.makeText(this, "Failed to start camera: ${exc.message}", Toast.LENGTH_LONG).show()
            }
        }, ContextCompat.getMainExecutor(this))
    }

    private fun takePhoto() {
        val photoDir = getExternalFilesDir(Environment.DIRECTORY_PICTURES) ?: filesDir
        val photoFile = File(photoDir, "receipt_${System.currentTimeMillis()}.jpg")
        val outputOptions = ImageCapture.OutputFileOptions.Builder(photoFile).build()

        imageCapture.takePicture(
            outputOptions,
            ContextCompat.getMainExecutor(this),
            object : ImageCapture.OnImageSavedCallback {
                override fun onError(exc: ImageCaptureException) {
                    Toast.makeText(this@ExpenseEntryActivity, "Capture failed: ${exc.message}", Toast.LENGTH_LONG).show()
                }

                override fun onImageSaved(output: ImageCapture.OutputFileResults) {
                    lastPhotoPath = photoFile.absolutePath
                    showThumbnail(photoFile.absolutePath)
                    runOcr(photoFile.absolutePath)
                }
            }
        )
    }

    private var lastPhotoPath: String? = null

    private fun showThumbnail(path: String) {
        val thumbnail = findViewById<android.widget.ImageView>(R.id.receiptThumbnail)
        val bitmap = BitmapFactory.decodeFile(path)
        thumbnail.setImageBitmap(bitmap)
        thumbnail.visibility = android.view.View.VISIBLE
    }

    private fun runOcr(path: String) {
        val bitmap = BitmapFactory.decodeFile(path) ?: return
        val image = InputImage.fromBitmap(bitmap, 0)
        val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)

        recognizer.process(image)
            .addOnSuccessListener { visionText ->
                autofillFromOcr(visionText.text)
            }
            .addOnFailureListener {
                Toast.makeText(this, "Could not read text from photo, fill in manually", Toast.LENGTH_SHORT).show()
            }
    }

    private fun autofillFromOcr(fullText: String) {
        val lines = fullText.lines().filter { it.isNotBlank() }

        // Best-effort guess at merchant/description: first non-empty line
        if (lines.isNotEmpty()) {
            findViewById<android.widget.EditText>(R.id.descriptionInput).setText(lines.first().trim())
        }

        // Best-effort guess at amount: prefer a line containing "total",
        // otherwise fall back to the largest dollar amount found anywhere.
        val amountPattern = Pattern.compile("\\$?\\s?(\\d{1,5}[.,]\\d{2})")
        var bestAmount: Double? = null

        val totalLine = lines.firstOrNull { it.contains("total", ignoreCase = true) }
        if (totalLine != null) {
            val matcher = amountPattern.matcher(totalLine)
            if (matcher.find()) {
                bestAmount = matcher.group(1)?.replace(",", ".")?.toDoubleOrNull()
            }
        }
        if (bestAmount == null) {
            val matcher = amountPattern.matcher(fullText)
            while (matcher.find()) {
                val candidate = matcher.group(1)?.replace(",", ".")?.toDoubleOrNull()
                if (candidate != null && (bestAmount == null || candidate > bestAmount!!)) {
                    bestAmount = candidate
                }
            }
        }
        if (bestAmount != null) {
            findViewById<android.widget.EditText>(R.id.amountInput).setText(bestAmount.toString())
        }

        // Best-effort guess at date: first MM/DD/YYYY-style pattern found
        val datePattern = Pattern.compile("\\b(\\d{1,2})[/-](\\d{1,2})[/-](\\d{2,4})\\b")
        val dateMatcher = datePattern.matcher(fullText)
        if (dateMatcher.find()) {
            try {
                val month = dateMatcher.group(1)!!.toInt()
                val day = dateMatcher.group(2)!!.toInt()
                var year = dateMatcher.group(3)!!.toInt()
                if (year < 100) year += 2000
                val calendar = Calendar.getInstance()
                calendar.set(year, month - 1, day)
                selectedDateMillis = calendar.timeInMillis
                findViewById<android.widget.Button>(R.id.dateButton).text =
                    displayDateFormat.format(selectedDateMillis)
            } catch (_: Exception) {
                // If parsing fails, leave the manually-selected date as-is
            }
        }

        Toast.makeText(this, "Receipt scanned — review before saving", Toast.LENGTH_SHORT).show()
    }

    private fun saveExpense() {
        val description = findViewById<android.widget.EditText>(R.id.descriptionInput).text.toString().trim()
        val amountText = findViewById<android.widget.EditText>(R.id.amountInput).text.toString().trim()
        val isCarExpense = findViewById<android.widget.CheckBox>(R.id.carExpenseCheckbox).isChecked
        val recordType = if (findViewById<android.widget.RadioButton>(R.id.radio1099).isChecked) {
            RecordType.TAX_1099
        } else {
            RecordType.NONPROFIT_501
        }

        if (description.isEmpty()) {
            Toast.makeText(this, "Enter a description", Toast.LENGTH_SHORT).show()
            return
        }
        val amount = amountText.toDoubleOrNull()
        if (amount == null) {
            Toast.makeText(this, "Enter a valid amount", Toast.LENGTH_SHORT).show()
            return
        }

        val expense = Expense(
            date = selectedDateMillis,
            description = description,
            amount = amount,
            recordType = recordType,
            isCarExpense = isCarExpense,
            receiptImagePath = lastPhotoPath
        )

        viewModel.saveExpense(expense) {
            Toast.makeText(this, "Expense saved", Toast.LENGTH_SHORT).show()
            finish()
        }
    }
}