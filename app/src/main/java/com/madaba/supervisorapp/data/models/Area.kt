package com.madaba.supervisorapp.data.models

/**
 * Represents a work area.
 * 
 * @property id Unique identifier for the area
 * @property name Name of the area
 * @property description Optional description of the area
 */
data class Area(
    val id: String = "",
    val name: String = "",
    val description: String? = null
)

