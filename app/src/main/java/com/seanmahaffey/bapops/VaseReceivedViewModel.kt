package com.seanmahaffey.bapops

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.seanmahaffey.bapops.data.AppDatabase
import com.seanmahaffey.bapops.data.VaseReceived
import kotlinx.coroutines.launch

class VaseReceivedViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).vaseReceivedDao()

    fun saveVaseReceived(vaseReceived: VaseReceived, onSaved: () -> Unit) {
        viewModelScope.launch {
            dao.insert(vaseReceived)
            onSaved()
        }
    }
}