package com.seanmahaffey.bapops.data

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "expenses")
data class Expense(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long,
    val description: String,
    val amount: Double,
    val recordType: RecordType,
    val isCarExpense: Boolean = false,
    val receiptImagePath: String? = null,
    val ocrRawText: String? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)