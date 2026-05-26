package com.example.montesorrilearning.domain.model

import com.google.gson.annotations.SerializedName

data class WorkEntry(
    val id: String = "",
    @SerializedName("child_id") val childId: String = "",
    @SerializedName("teacher_id") val teacherId: String = "",
    @SerializedName("classroom_id") val classroomId: String = "",
    @SerializedName("montessori_area") val montessoriArea: String = "",
    val title: String = "",
    @SerializedName("teacher_comment") val teacherComment: String = "",
    @SerializedName("created_at") val createdAt: String = "",
    val media: List<Media> = emptyList(),
    @SerializedName("child_name") val childName: String? = null
)
