package com.example.montesorrilearning.domain.model

data class Syllabus(
    val id: String = "",
    val termId: String = "",
    val classroomId: String = "",
    val montessoriArea: String = "",
    val title: String = "",
    val description: String = "",
    val dayOfWeek: Int = 1,
    val weekNumber: Int? = null,
    val sortOrder: Int = 0,
    val isExtracurricular: Boolean = false,
    val activityType: String? = null,
    val durationMinutes: Int? = null,
    val createdAt: String = "",
    val updatedAt: String = "",
    val classroomName: String? = null,
    val termName: String? = null
) {
    val areaEnum: MontessoriArea
        get() = MontessoriArea.fromApiValue(montessoriArea)

    val areaDisplayName: String
        get() = if (isExtracurricular) "Extra-Curricular: ${activityType ?: ""}"
                else areaEnum.displayName

    val dayLabel: String
        get() = when (dayOfWeek) {
            1 -> "Mon"; 2 -> "Tue"; 3 -> "Wed"; 4 -> "Thu"; 5 -> "Fri"
            else -> "Day $dayOfWeek"
        }
}
