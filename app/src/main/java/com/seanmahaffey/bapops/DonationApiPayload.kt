package com.seanmahaffey.bapops

data class DonationApiPayload(
    val date: String,
    val description: String,
    val value: String,
    val donor_name: String,
    val receipt_generated: Boolean
)