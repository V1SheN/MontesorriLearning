package com.example.montesorrilearning.domain.model

data class TeacherPlan(
    val id: String = "",
    val syllabusId: String? = null,
    val teacherId: String = "",
    val classroomId: String = "",
    val termId: String = "",
    val title: String = "",
    val montessoriArea: String = "",
    val description: String = "",
    val plannedDate: String = "",
    val dayOfWeek: Int = 1,
    val weekNumber: Int? = null,
    val isExtracurricular: Boolean = false,
    val activityType: String? = null,
    val durationMinutes: Int? = null,
    val isCompleted: Boolean = false,
    val teacherNotes: String? = null,
    val createdAt: String = "",
    val updatedAt: String = "",
    val classroomName: String? = null,
    val termName: String? = null
) {
    val dayLabel: String
        get() = when (dayOfWeek) {
            1 -> "Mon"; 2 -> "Tue"; 3 -> "Wed"; 4 -> "Thu"; 5 -> "Fri"
            else -> "Day $dayOfWeek"
        }
}
