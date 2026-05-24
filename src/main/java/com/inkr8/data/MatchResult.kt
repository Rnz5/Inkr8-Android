package com.inkr8.data

data class MatchResult(
    val opponentId: String = "",
    val opponentName: String = "",
    val opponentScore: Double = 0.0,
    val outcome: String = "",
    val ratingChange: Long = 0
)
