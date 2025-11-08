package com.madaba.supervisorapp.data.source.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.madaba.supervisorapp.data.models.Area

@Entity(tableName = "areas")
data class AreaEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val description: String? = null
)

fun AreaEntity.toModel(): Area {
    return Area(
        id = id,
        name = name,
        description = description
    )
}

fun Area.toEntity(): AreaEntity {
    return AreaEntity(
        id = id,
        name = name,
        description = description
    )
}

