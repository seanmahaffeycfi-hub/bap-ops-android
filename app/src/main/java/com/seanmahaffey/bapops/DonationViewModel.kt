package com.seanmahaffey.bapops

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.seanmahaffey.bapops.data.AppDatabase
import com.seanmahaffey.bapops.data.Donation
import kotlinx.coroutines.launch

class DonationViewModel(application: Application) : AndroidViewModel(application) {

    private val dao = AppDatabase.getInstance(application).donationDao()

    fun saveDonation(donation: Donation, onSaved: (Long) -> Unit) {
        viewModelScope.launch {
            val newId = dao.insert(donation)
            onSaved(newId)
        }
    }
}