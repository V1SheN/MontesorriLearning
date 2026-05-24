package com.example.montesorrilearning.domain.model

data class Child(
    val id: String = "",
    val name: String = "",
    val dateOfBirth: String? = null,
    val classroomId: String = "",
    val photoPath: String? = null,
    val active: Boolean = true
)
