package com.example.montesorrilearning.domain.model

import com.google.gson.annotations.SerializedName

data class Child(
    val id: String = "",
    val name: String = "",
    @SerializedName("date_of_birth") val dateOfBirth: String? = null,
    @SerializedName("classroom_id") val classroomId: String = "",
    @SerializedName("photo_path") val photoPath: String? = null,
    val active: Boolean = true
)
