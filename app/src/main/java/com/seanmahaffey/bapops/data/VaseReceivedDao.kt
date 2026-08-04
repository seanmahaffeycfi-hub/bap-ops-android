package com.seanmahaffey.bapops.data

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VaseReceivedDao {
    @Insert
    suspend fun insert(vaseReceived: VaseReceived): Long

    @Update
    suspend fun update(vaseReceived: VaseReceived)

    @Delete
    suspend fun delete(vaseReceived: VaseReceived)

    @Query("SELECT * FROM vases_received ORDER BY dateReceived ASC")
    fun getAllOldestFirst(): Flow<List<VaseReceived>>

    @Query("SELECT * FROM vases_received WHERE isSynced = 0")
    suspend fun getUnsynced(): List<VaseReceived>

    @Query("UPDATE vases_received SET isSynced = 1 WHERE id = :id")
    suspend fun markSynced(id: Long)
}