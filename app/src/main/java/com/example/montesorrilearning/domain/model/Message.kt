package com.example.montesorrilearning.domain.model

import com.google.gson.annotations.SerializedName

data class Message(
    val id: String = "",
    @SerializedName("sender_id") val senderId: String = "",
    @SerializedName("classroom_id") val classroomId: String? = null,
    val subject: String? = null,
    val body: String = "",
    @SerializedName("created_at") val createdAt: String = "",
    @SerializedName("read_at") val readAt: String? = null,
    @SerializedName("sender_name") val senderName: String? = null
)
