package com.example.montesorrilearning.util

import java.time.LocalDate
import java.time.ZonedDateTime
import java.time.format.DateTimeFormatter
import java.time.format.FormatStyle

object DateUtils {

    private val isoFormatter = DateTimeFormatter.ISO_DATE_TIME
    private val dateFormatter = DateTimeFormatter.ISO_LOCAL_DATE
    private val displayFormatter = DateTimeFormatter.ofLocalizedDateTime(FormatStyle.MEDIUM, FormatStyle.SHORT)
    private val timeFormatter = DateTimeFormatter.ofPattern("h:mm a")

    fun formatForDisplay(isoString: String): String {
        return try {
            val zdt = ZonedDateTime.parse(isoString, isoFormatter)
            zdt.format(displayFormatter)
        } catch (e: Exception) {
            isoString
        }
    }

    fun formatTime(isoString: String): String {
        return try {
            val zdt = ZonedDateTime.parse(isoString, isoFormatter)
            zdt.format(timeFormatter)
        } catch (e: Exception) {
            isoString
        }
    }

    fun formatDate(isoString: String): String {
        return try {
            val zdt = ZonedDateTime.parse(isoString, isoFormatter)
            zdt.format(DateTimeFormatter.ofLocalizedDate(FormatStyle.MEDIUM))
        } catch (e: Exception) {
            isoString
        }
    }

    fun todayIso(): String = LocalDate.now().format(dateFormatter)

    fun isoDate(date: LocalDate): String = date.format(dateFormatter)
}
