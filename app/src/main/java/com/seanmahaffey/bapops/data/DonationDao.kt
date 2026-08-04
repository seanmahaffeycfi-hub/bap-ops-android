package com.seanmahaffey.bapops.data

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface DonationDao {
    @Insert
    suspend fun insert(donation: Donation): Long

    @Update
    suspend fun update(donation: Donation)

    @Delete
    suspend fun delete(donation: Donation)

    @Query("SELECT * FROM donations ORDER BY date DESC")
    fun getAll(): Flow<List<Donation>>

    @Query("SELECT * FROM donations WHERE isSynced = 0")
    suspend fun getUnsynced(): List<Donation>

    @Query("UPDATE donations SET isSynced = 1 WHERE id = :id")
    suspend fun markSynced(id: Long)
}