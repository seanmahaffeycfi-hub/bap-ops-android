package com.seanmahaffey.bapops

import retrofit2.Response
import retrofit2.http.Body
import retrofit2.http.POST

interface ApiService {
    @POST("api/expenses/")
    suspend fun postExpense(@Body expense: ExpenseApiPayload): Response<ExpenseApiPayload>

    @POST("api/donations/")
    suspend fun postDonation(@Body donation: DonationApiPayload): Response<DonationApiPayload>

    @POST("api/vases-received/")
    suspend fun postVaseReceived(@Body vaseReceived: VaseReceivedApiPayload): Response<VaseReceivedApiPayload>

    @POST("api/vases-returned/")
    suspend fun postVaseReturned(@Body vaseReturned: VaseReturnedApiPayload): Response<VaseReturnedApiPayload>

    @POST("api/mileage/")
    suspend fun postMileageEntry(@Body entry: MileageEntryApiPayload): Response<MileageEntryApiPayload>
}