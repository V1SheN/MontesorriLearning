package com.example.montesorrilearning.domain.model

enum class MontessoriArea(val displayName: String) {
    PRACTICAL_LIFE("Practical Life"),
    SENSORIAL("Sensorial"),
    LANGUAGE("Language"),
    MATH("Mathematics"),
    CULTURAL("Cultural Studies"),
    EXTRACURRICULAR("Extra-Curricular");

    companion object {
        fun fromApiValue(value: String): MontessoriArea =
            entries.find { it.name == value.uppercase() } ?: EXTRACURRICULAR
    }
}
