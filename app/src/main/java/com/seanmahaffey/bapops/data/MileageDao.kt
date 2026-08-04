package com.seanmahaffey.bapops.data

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface MileageDao {
    @Insert
    suspend fun insert(entry: MileageEntry): Long

    @Update
    suspend fun update(entry: MileageEntry)

    @Delete
    suspend fun delete(entry: MileageEntry)

    @Query("SELECT * FROM mileage_entries ORDER BY date DESC")
    fun getAll(): Flow<List<MileageEntry>>

    @Query("SELECT * FROM mileage_entries ORDER BY date DESC, id DESC LIMIT 1")
    suspend fun getLastEntry(): MileageEntry?

    @Query("SELECT * FROM mileage_entries WHERE isSynced = 0")
    suspend fun getUnsynced(): List<MileageEntry>

    @Query("UPDATE mileage_entries SET isSynced = 1 WHERE id = :id")
    suspend fun markSynced(id: Long)
}