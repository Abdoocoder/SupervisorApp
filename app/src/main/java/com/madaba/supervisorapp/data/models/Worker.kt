// FILE: app/src/main/java/com/madaba/supervisorapp/data/models/Worker.kt
package com.madaba.supervisorapp.data.models

/**
 * Represents a worker in the system.
 * 
 * @property id Unique identifier for the worker
 * @property name Full name of the worker
 * @property role Job role/position of the worker
 * @property supervisorId ID of the supervisor responsible for this worker
 * @property areaId ID of the area where the worker is assigned
 */
data class Worker(
    val id: String = "",
    val name: String = "",
    val role: String = "",
    val supervisorId: String = "",
    val areaId: String = ""
)