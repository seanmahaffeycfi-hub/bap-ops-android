package com.seanmahaffey.bapops

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.seanmahaffey.bapops.data.AppDatabase
import com.seanmahaffey.bapops.data.VaseReturned
import kotlinx.coroutines.launch

class VaseReturnedViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).vaseReturnedDao()

    fun saveVaseReturned(vaseReturned: VaseReturned, onSaved: () -> Unit) {
        viewModelScope.launch {
            dao.insert(vaseReturned)
            onSaved()
        }
    }
}