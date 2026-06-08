package com.inkr8.mappers

import com.inkr8.data.Evaluation
import com.inkr8.data.SubmissionStatus
import com.inkr8.repository.FirestoreEvaluation

fun FirestoreEvaluation.toDomain(): Evaluation {
    return Evaluation(
        submissionId = submissionId ?: "",
        finalScore = finalScore,
        feedback = feedback,
        resultStatus = SubmissionStatus.valueOf(
            resultStatus.ifBlank { "PENDING" }
        ),
        meritEarned = meritEarned,
        ratingChange = ratingChange,
        rankLeaderboard = rankLeaderboard.toInt().takeIf { it > 0 }
    )
}

fun Evaluation.toFirestore(): FirestoreEvaluation {
    return FirestoreEvaluation(
        submissionId = submissionId,
        finalScore = finalScore,
        feedback = feedback,
        resultStatus = resultStatus.name,
        meritEarned = meritEarned,
        ratingChange = ratingChange,
        rankLeaderboard = rankLeaderboard?.toLong() ?: 0L
    )
}