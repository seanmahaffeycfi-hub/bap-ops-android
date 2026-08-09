package com.seanmahaffey.bapops

data class VaseReturnedApiPayload(
    val date_returned: String,
    val quantity: Int,
    val returned_from: String
)