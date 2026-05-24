package com.example.montesorrilearning.domain.model

data class WorkEntry(
    val id: String = "",
    val childId: String = "",
    val teacherId: String = "",
    val classroomId: String = "",
    val montessoriArea: String = "",
    val title: String = "",
    val teacherComment: String = "",
    val createdAt: String = "",
    val media: List<Media> = emptyList(),
    val childName: String? = null
)
