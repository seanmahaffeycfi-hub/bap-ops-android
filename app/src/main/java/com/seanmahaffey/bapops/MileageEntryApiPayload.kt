package com.seanmahaffey.bapops

data class MileageEntryApiPayload(
    val date: String,
    val start_mileage: String,
    val end_mileage: String,
    val record_type: String,
    val start_lat: Double?,
    val start_lng: Double?,
    val end_lat: Double?,
    val end_lng: Double?
)