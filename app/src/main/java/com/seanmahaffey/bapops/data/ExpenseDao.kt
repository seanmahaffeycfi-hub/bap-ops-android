package com.seanmahaffey.bapops.data

import androidx.room3.Dao
import androidx.room3.Delete
import androidx.room3.Insert
import androidx.room3.Query
import androidx.room3.Update
import kotlinx.coroutines.flow.Flow

@Dao
interface ExpenseDao {
    @Insert
    suspend fun insert(expense: Expense): Long

    @Update
    suspend fun update(expense: Expense)

    @Delete
    suspend fun delete(expense: Expense)

    @Query("SELECT * FROM expenses ORDER BY date DESC")
    fun getAll(): Flow<List<Expense>>

    @Query("SELECT * FROM expenses WHERE isSynced = 0")
    suspend fun getUnsynced(): List<Expense>

    @Query("UPDATE expenses SET isSynced = 1 WHERE id = :id")
    suspend fun markSynced(id: Long)
}