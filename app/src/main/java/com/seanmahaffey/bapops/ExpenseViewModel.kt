package com.seanmahaffey.bapops

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.seanmahaffey.bapops.data.AppDatabase
import com.seanmahaffey.bapops.data.Expense
import kotlinx.coroutines.launch

class ExpenseViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).expenseDao()

    fun saveExpense(expense: Expense, onSaved: () -> Unit) {
        viewModelScope.launch {
            dao.insert(expense)
            onSaved()
        }
    }
}