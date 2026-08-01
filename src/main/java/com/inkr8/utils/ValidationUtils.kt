package com.inkr8.utils

object ValidationUtils {

    /**
     * Checks if the written content meets basic quality standards.
     * Returns a Pair: first is true if low quality is detected, second is the error message.
     */
    fun isContentLowQuality(content: String): Pair<Boolean, String?> {
        val trimmed = content.trim()
        if (trimmed.length < 50) return true to "Transmission too short (min 50 chars)"

        val words = trimmed.split("\\s+".toRegex()).filter { it.isNotBlank() }

        if (words.any { it.length > 35 }) {
            return true to "Nonsense detected (excessive word length)"
        }

        if (words.size >= 10) {
            val uniqueWords = words.map { it.lowercase() }.toSet()
            if (uniqueWords.size.toDouble() / words.size.toDouble() < 0.35) {
                return true to "Repetitive content detected"
            }
        }

        val letters = trimmed.replace("[^a-zA-Z]".toRegex(), "")
        if (letters.length > 30) {
            val vowels = letters.count { it.lowercaseChar() in "aeiou" }
            val vowelRatio = vowels.toDouble() / letters.length.toDouble()
            if (vowelRatio < 0.15 || vowelRatio > 0.8) {
                return true to "Unnatural character distribution (nonsense)"
            }

            val uniqueLetters = letters.lowercase().toSet()
            if (uniqueLetters.size < 8 && letters.length > 60) {
                return true to "Low character diversity (nonsense)"
            }
        }

        return false to null
    }
}
