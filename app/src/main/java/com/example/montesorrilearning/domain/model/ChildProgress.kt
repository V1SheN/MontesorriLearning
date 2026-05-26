package com.example.montesorrilearning.domain.model

data class ChildProgress(
    val id: String = "",
    val childId: String = "",
    val syllabusId: String? = null,
    val teacherPlanId: String? = null,
    val status: String = "pending",
    val observationNotes: String? = null,
    val completedAt: String? = null,
    val createdAt: String = "",
    val updatedAt: String = "",
    val childName: String? = null,
    val syllabusTitle: String? = null,
    val syllabusArea: String? = null,
    val planTitle: String? = null
) {
    val displayTitle: String
        get() = syllabusTitle ?: planTitle ?: "Unknown"

    val statusLabel: String
        get() = when (status) {
            "pending" -> "Pending"
            "in_progress" -> "In Progress"
            "completed" -> "Completed"
            "mastered" -> "Mastered"
            else -> status
        }
}
