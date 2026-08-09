package com.seanmahaffey.bapops

data class ExpenseApiPayload(
    val date: String,
    val description: String,
    val amount: String,
    val record_type: String,
    val is_car_expense: Boolean,
    val receipt_image_url: String? = null,
    val ocr_raw_text: String? = null
)