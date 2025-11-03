// FILE: app/src/main/java/com/madaba/supervisorapp/data/source/local/WorkerEntity.kt
package com.madaba.supervisorapp.data.source.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import com.madaba.supervisorapp.data.models.Worker

@Entity(tableName = "workers")
data class WorkerEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val role: String,
    val supervisorId: String
)

fun WorkerEntity.toModel(): Worker {
    return Worker(
        id = id,
        name = name,
        role = role,
        supervisorId = supervisorId
    )
}

fun Worker.toEntity(): WorkerEntity {
    return WorkerEntity(
        id = id,
        name = name,
        role = role,
        supervisorId = supervisorId
    )
}