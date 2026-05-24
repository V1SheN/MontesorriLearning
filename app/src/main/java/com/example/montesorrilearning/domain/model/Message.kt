package com.example.montesorrilearning.domain.model

data class Message(
    val id: String = "",
    val senderId: String = "",
    val classroomId: String? = null,
    val subject: String? = null,
    val body: String = "",
    val createdAt: String = "",
    val readAt: String? = null,
    val senderName: String? = null
)
