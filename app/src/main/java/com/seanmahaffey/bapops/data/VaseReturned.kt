package com.seanmahaffey.bapops.data

import androidx.room3.Entity
import androidx.room3.PrimaryKey

@Entity(tableName = "vases_returned")
data class VaseReturned(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,
    val dateReturned: Long,
    val quantity: Int,
    val returnedFrom: String,
    val createdAt: Long = System.currentTimeMillis(),
    val isSynced: Boolean = false
)