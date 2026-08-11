package com.seanmahaffey.bapops

import android.content.Context
import com.seanmahaffey.bapops.data.AppDatabase
import kotlinx.coroutines.withTimeoutOrNull
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class SyncManager(private val context: Context) {

    private val settings = SettingsManager(context)
    private val isoDateFormat = SimpleDateFormat("yyyy-MM-dd", Locale.US)

    private suspend fun checkPreconditions(): SyncResult? {
        val detectedSsid = withTimeoutOrNull(5000) { WifiChecker.getCurrentSsid(context) } ?: ""
        if (!detectedSsid.equals(settings.targetWifiSsid, ignoreCase = true)) {
            return SyncResult.NotOnTargetNetwork(detectedSsid)
        }
        if (settings.serverBaseUrl.isBlank() || settings.authToken.isBlank()) {
            return SyncResult.MissingSettings
        }
        return null
    }

    suspend fun syncExpenses(): SyncResult {
        checkPreconditions()?.let { return it }

        val dao = AppDatabase.getInstance(context).expenseDao()
        val unsynced = dao.getUnsynced()
        if (unsynced.isEmpty()) return SyncResult.Success(0)

        val api = RetrofitClient.create(settings.serverBaseUrl, settings.authToken)
        var successCount = 0

        for (expense in unsynced) {
            try {
                val payload = ExpenseApiPayload(
                    date = isoDateFormat.format(Date(expense.date)),
                    description = expense.description,
                    amount = String.format(Locale.US, "%.2f", expense.amount),
                    record_type = expense.recordType.name,
                    is_car_expense = expense.isCarExpense,
                    receipt_image_url = null,
                    ocr_raw_text = expense.ocrRawText
                )
                val response = api.postExpense(payload)
                if (response.isSuccessful) {
                    dao.markSynced(expense.id)
                    successCount++
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    return SyncResult.NetworkError("Expense rejected (${response.code()}): $errorBody")
                }
            } catch (e: Exception) {
                return SyncResult.NetworkError(e.message ?: "Unknown error")
            }
        }
        return SyncResult.Success(successCount)
    }

    suspend fun syncDonations(): SyncResult {
        checkPreconditions()?.let { return it }

        val dao = AppDatabase.getInstance(context).donationDao()
        val unsynced = dao.getUnsynced()
        if (unsynced.isEmpty()) return SyncResult.Success(0)

        val api = RetrofitClient.create(settings.serverBaseUrl, settings.authToken)
        var successCount = 0

        for (donation in unsynced) {
            try {
                val payload = DonationApiPayload(
                    date = isoDateFormat.format(Date(donation.date)),
                    description = donation.description,
                    value = String.format(Locale.US, "%.2f", donation.value),
                    donor_name = donation.donorName,
                    receipt_generated = donation.receiptGenerated
                )
                val response = api.postDonation(payload)
                if (response.isSuccessful) {
                    dao.markSynced(donation.id)
                    successCount++
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    return SyncResult.NetworkError("Donation rejected (${response.code()}): $errorBody")
                }
            } catch (e: Exception) {
                return SyncResult.NetworkError(e.message ?: "Unknown error")
            }
        }
        return SyncResult.Success(successCount)
    }

    suspend fun syncVasesReceived(): SyncResult {
        checkPreconditions()?.let { return it }

        val dao = AppDatabase.getInstance(context).vaseReceivedDao()
        val unsynced = dao.getUnsynced()
        if (unsynced.isEmpty()) return SyncResult.Success(0)

        val api = RetrofitClient.create(settings.serverBaseUrl, settings.authToken)
        var successCount = 0

        for (vase in unsynced) {
            try {
                val payload = VaseReceivedApiPayload(
                    date_received = isoDateFormat.format(Date(vase.dateReceived)),
                    quantity = vase.quantity,
                    poc_name = vase.pocName,
                    poc_facility_name = vase.pocFacilityName,
                    poc_phone = vase.pocPhone,
                    poc_email = vase.pocEmail,
                    recipient = vase.recipient
                )
                val response = api.postVaseReceived(payload)
                if (response.isSuccessful) {
                    dao.markSynced(vase.id)
                    successCount++
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    return SyncResult.NetworkError("Vase received rejected (${response.code()}): $errorBody")
                }
            } catch (e: Exception) {
                return SyncResult.NetworkError(e.message ?: "Unknown error")
            }
        }
        return SyncResult.Success(successCount)
    }

    suspend fun syncVasesReturned(): SyncResult {
        checkPreconditions()?.let { return it }

        val dao = AppDatabase.getInstance(context).vaseReturnedDao()
        val unsynced = dao.getUnsynced()
        if (unsynced.isEmpty()) return SyncResult.Success(0)

        val api = RetrofitClient.create(settings.serverBaseUrl, settings.authToken)
        var successCount = 0

        for (vase in unsynced) {
            try {
                val payload = VaseReturnedApiPayload(
                    date_returned = isoDateFormat.format(Date(vase.dateReturned)),
                    quantity = vase.quantity,
                    returned_from = vase.returnedFrom
                )
                val response = api.postVaseReturned(payload)
                if (response.isSuccessful) {
                    dao.markSynced(vase.id)
                    successCount++
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    return SyncResult.NetworkError("Vase returned rejected (${response.code()}): $errorBody")
                }
            } catch (e: Exception) {
                return SyncResult.NetworkError(e.message ?: "Unknown error")
            }
        }
        return SyncResult.Success(successCount)
    }

    suspend fun syncMileage(): SyncResult {
        checkPreconditions()?.let { return it }

        val dao = AppDatabase.getInstance(context).mileageDao()
        val unsynced = dao.getUnsynced()
        if (unsynced.isEmpty()) return SyncResult.Success(0)

        val api = RetrofitClient.create(settings.serverBaseUrl, settings.authToken)
        var successCount = 0

        for (entry in unsynced) {
            try {
                val payload = MileageEntryApiPayload(
                    date = isoDateFormat.format(Date(entry.date)),
                    start_mileage = String.format(Locale.US, "%.1f", entry.startMileage),
                    end_mileage = String.format(Locale.US, "%.1f", entry.endMileage),
                    record_type = entry.recordType.name,
                    start_lat = entry.startLat,
                    start_lng = entry.startLng,
                    end_lat = entry.endLat,
                    end_lng = entry.endLng
                )
                val response = api.postMileageEntry(payload)
                if (response.isSuccessful) {
                    dao.markSynced(entry.id)
                    successCount++
                } else {
                    val errorBody = response.errorBody()?.string() ?: "Unknown error"
                    return SyncResult.NetworkError("Mileage entry rejected (${response.code()}): $errorBody")
                }
            } catch (e: Exception) {
                return SyncResult.NetworkError(e.message ?: "Unknown error")
            }
        }
        return SyncResult.Success(successCount)
    }

    suspend fun syncAll(): SyncResult {
        checkPreconditions()?.let { return it }

        var totalCount = 0
        for (result in listOf(
            syncExpenses(),
            syncDonations(),
            syncVasesReceived(),
            syncVasesReturned(),
            syncMileage()
        )) {
            when (result) {
                is SyncResult.Success -> totalCount += result.count
                is SyncResult.NetworkError -> return result
                is SyncResult.NotOnTargetNetwork -> return result
                is SyncResult.MissingSettings -> return result
            }
        }
        return SyncResult.Success(totalCount)
    }
}

sealed class SyncResult {
    data class Success(val count: Int) : SyncResult()
    data class NotOnTargetNetwork(val detectedSsid: String) : SyncResult()
    object MissingSettings : SyncResult()
    data class NetworkError(val message: String) : SyncResult()
}