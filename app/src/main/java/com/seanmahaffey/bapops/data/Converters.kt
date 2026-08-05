package com.seanmahaffey.bapops.data

import androidx.room3.ColumnTypeConverter

class Converters {
    @ColumnTypeConverter
    fun fromRecordType(value: RecordType): String = value.name

    @ColumnTypeConverter
    fun toRecordType(value: String): RecordType = RecordType.valueOf(value)
}