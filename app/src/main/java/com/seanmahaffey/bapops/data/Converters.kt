package com.seanmahaffey.bapops.data

import androidx.room3.TypeConverter

class Converters {
    @TypeConverter
    fun fromRecordType(value: RecordType): String = value.name

    @TypeConverter
    fun toRecordType(value: String): RecordType = RecordType.valueOf(value)
}