package com.application.requiemproject.data.local.db

import androidx.room.TypeConverter

class RoomTypeConverters {

    @TypeConverter
    fun fromListToString(privilege: List<String>?): String? {
        return privilege?.joinToString(",")
    }

    @TypeConverter
    fun fromStringToList(privilege: String?): List<String>? {
        return privilege?.split(",")?.map { it.trim() }
    }

}