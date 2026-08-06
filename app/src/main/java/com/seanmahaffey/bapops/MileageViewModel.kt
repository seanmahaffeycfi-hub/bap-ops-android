package com.seanmahaffey.bapops

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.seanmahaffey.bapops.data.AppDatabase
import com.seanmahaffey.bapops.data.MileageEntry
import kotlinx.coroutines.launch

class MileageViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).mileageDao()

    fun loadLastEntry(onLoaded: (MileageEntry?) -> Unit) {
        viewModelScope.launch {
            onLoaded(dao.getLastEntry())
        }
    }

    fun saveMileageEntry(entry: MileageEntry, onSaved: () -> Unit) {
        viewModelScope.launch {
            dao.insert(entry)
            onSaved()
        }
    }
}