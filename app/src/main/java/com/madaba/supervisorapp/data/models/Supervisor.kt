package com.madaba.supervisorapp.data.models

/**
 * Represents a supervisor in the system.
 * 
 * @property id Unique identifier for the supervisor (Firebase UID)
 * @property name Full name of the supervisor
 * @property email Email address of the supervisor
 * @property areaIds List of area IDs that this supervisor is responsible for
 * @property role User role (Admin or Supervisor)
 */
data class Supervisor(
    val id: String = "",
    val name: String = "",
    val email: String = "",
    val areaIds: List<String> = emptyList(),
    val role: UserRole = UserRole.Supervisor
)

enum class UserRole {
    Admin,
    Supervisor
}

