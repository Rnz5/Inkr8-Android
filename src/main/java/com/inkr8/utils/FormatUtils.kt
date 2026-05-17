package com.inkr8.utils

object FormatUtils {
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
}
