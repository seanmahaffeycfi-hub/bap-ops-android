package com.seanmahaffey.bapops.data

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "mileage_entries")
data class MileageEntry(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val date: Long,
    val startMileage: Double,
    val endMileage: Double,
    val recordType: RecordType,
    val startLat: Double?,
    val startLng: Double?,
    val endLat: Double?,
    val endLng: Double?,
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)