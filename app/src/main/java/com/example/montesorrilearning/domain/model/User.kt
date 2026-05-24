package com.example.montesorrilearning.domain.model

data class User(
    val id: String = "",
    val email: String = "",
    val displayName: String = "",
    val role: String = "",
    val avatarPath: String? = null
)
