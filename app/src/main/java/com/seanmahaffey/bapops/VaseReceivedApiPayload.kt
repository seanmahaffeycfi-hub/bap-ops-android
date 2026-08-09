package com.seanmahaffey.bapops

data class VaseReceivedApiPayload(
    val date_received: String,
    val quantity: Int,
    val poc_name: String,
    val poc_facility_name: String,
    val poc_phone: String,
    val poc_email: String,
    val recipient: String
)