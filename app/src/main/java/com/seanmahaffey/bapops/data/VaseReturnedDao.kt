package com.seanmahaffey.bapops.data

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface VaseReturnedDao {
    @Insert
    suspend fun insert(vaseReturned: VaseReturned): Long

    @Update
    suspend fun update(vaseReturned: VaseReturned)

    @Delete
    suspend fun delete(vaseReturned: VaseReturned)

    @Query("SELECT * FROM vases_returned ORDER BY dateReturned DESC")
    fun getAll(): Flow<List<VaseReturned>>

    @Query("SELECT * FROM vases_returned WHERE isSynced = 0")
    suspend fun getUnsynced(): List<VaseReturned>

    @Query("UPDATE vases_returned SET isSynced = 1 WHERE id = :id")
    suspend fun markSynced(id: Long)
}