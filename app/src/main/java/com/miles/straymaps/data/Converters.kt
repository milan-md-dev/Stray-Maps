package com.miles.straymaps.data

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.room.TypeConverter
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter

class Converters {

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun fromLocalDateTime(value: LocalDateTime): String {
        return value.format(DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }

    @RequiresApi(Build.VERSION_CODES.O)
    @TypeConverter
    fun toLocalDateTime(value: String): LocalDateTime {
        return LocalDateTime.parse(value, DateTimeFormatter.ISO_LOCAL_DATE_TIME)
    }
}

// Converts LocalDateTime to String
fun LocalDateTime.toIsoString(): String = this.toString()

// Converts String to LocalDateTime
@RequiresApi(Build.VERSION_CODES.O)
fun String.toLocalDateTime(): LocalDateTime = LocalDateTime.parse(this)