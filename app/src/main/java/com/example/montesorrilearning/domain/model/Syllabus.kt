package com.example.montesorrilearning.domain.model

data class Syllabus(
    val id: String = "",
    val classroomId: String = "",
    val montessoriArea: String = "",
    val title: String = "",
    val description: String = "",
    val weekNumber: Int? = null,
    val year: Int = 2026,
    val sortOrder: Int = 0,
    val createdAt: String = "",
    val updatedAt: String = "",
    val classroomName: String? = null
) {
    val areaEnum: MontessoriArea?
        get() = MontessoriArea.entries.find { it.name == montessoriArea.uppercase() }

    val areaDisplayName: String
        get() = areaEnum?.displayName ?: montessoriArea.replace("_", " ").replaceFirstChar { it.uppercase() }
}
