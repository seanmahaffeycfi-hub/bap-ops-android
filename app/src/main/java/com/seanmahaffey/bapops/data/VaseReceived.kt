package com.seanmahaffey.bapops.data

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "vases_received")
data class VaseReceived(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateReceived: Long,
    val quantity: Int,
    val pocName: String,
    val pocFacilityName: String,
    val pocPhone: String,
    val pocEmail: String,
    val recipient: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)