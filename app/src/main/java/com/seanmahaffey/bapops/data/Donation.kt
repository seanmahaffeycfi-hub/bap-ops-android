package com.seanmahaffey.bapops.data

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "donations")
data class Donation(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long,
    val description: String,
    val value: Double,
    val donorName: String,
    val receiptGenerated: Boolean = false,
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)