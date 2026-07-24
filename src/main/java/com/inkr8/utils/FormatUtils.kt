package com.inkr8.utils

import java.text.NumberFormat
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

object FormatUtils {
    private val numberFormatter = NumberFormat.getNumberInstance(Locale.US)
    private val dateFormatter = SimpleDateFormat("MMM dd, yyyy", Locale.US)
    private val timeFormatter = SimpleDateFormat("HH:mm", Locale.US)

    fun formatPlace(place: Int): String {
        if (place % 100 in 11..13) {
            return "${place}th"
        }
        return when (place % 10) {
            1 -> "${place}st"
            2 -> "${place}nd"
            3 -> "${place}rd"
            else -> "${place}th"
        }
    }

    fun formatScore(score: Double): String {
        return String.format(Locale.US, "%.1f", score)
    }

    fun formatPercentage(percentage: Double): String {
        return String.format(Locale.US, "%.2f%%", percentage)
    }

    fun formatMerit(merit: Long): String {
        return numberFormatter.format(merit)
    }

    fun formatDate(timestamp: Long): String {
        return dateFormatter.format(Date(timestamp))
    }

    fun formatTime(timestamp: Long): String {
        return timeFormatter.format(Date(timestamp))
    }
}
