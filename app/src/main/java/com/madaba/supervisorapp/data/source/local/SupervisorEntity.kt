package com.madaba.supervisorapp.data.source.local

import androidx.room.Entity
import androidx.room.PrimaryKey
import androidx.room.TypeConverters
import com.madaba.supervisorapp.data.models.Supervisor
import com.madaba.supervisorapp.data.models.UserRole

@Entity(tableName = "supervisors")
@TypeConverters(Converters::class)
data class SupervisorEntity(
    @PrimaryKey
    val id: String,
    val name: String,
    val email: String,
    val areaIds: List<String>,
    val role: UserRole
)

fun SupervisorEntity.toModel(): Supervisor {
    return Supervisor(
        id = id,
        name = name,
        email = email,
        areaIds = areaIds,
        role = role
    )
}

fun Supervisor.toEntity(): SupervisorEntity {
    return SupervisorEntity(
        id = id,
        name = name,
        email = email,
        areaIds = areaIds,
        role = role
    )
}

