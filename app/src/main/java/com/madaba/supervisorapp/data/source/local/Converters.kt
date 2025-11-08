package com.madaba.supervisorapp.data.source.local

import androidx.room.TypeConverter
import com.madaba.supervisorapp.data.models.UserRole

class Converters {
    @TypeConverter
    fun fromStringList(value: String): List<String> {
        return if (value.isEmpty()) {
            emptyList()
        } else {
            value.split(",")
        }
    }

    @TypeConverter
    fun toStringList(value: List<String>): String {
        return value.joinToString(",")
    }

    @TypeConverter
    fun fromUserRole(value: UserRole): String {
        return value.name
    }

    @TypeConverter
    fun toUserRole(value: String): UserRole {
        return UserRole.valueOf(value)
    }
}

