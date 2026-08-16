package com.inkr8.repository

import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.functions.FirebaseFunctions
import com.google.firebase.functions.functions
import com.inkr8.data.Tournament
import com.inkr8.data.Submissions
import com.inkr8.data.TournamentStatus
import com.inkr8.utils.SystemConfig

/**
 * Repository for managing Tournament data.
 * Economy-critical actions (Creation, Enrollment) are handled via Cloud Functions 
 * to ensure server-side security and prevent Merit manipulation.
 */
class FirestoreTournamentRepository {
    private val db = FirebaseFirestore.getInstance()
    private val functions = FirebaseFunctions.getInstance()
    private val tournamentsCollection = db.collection(SystemConfig.TOURNAMENTS_COLLECTION)

    /**
     * Creates a new user-hosted tournament via Cloud Function.
     * Merit deduction and escrow are handled server-side.
     */
    fun createTournament(
        title: String,
        gamemode: String,
        prizePool: Long,
        maxPlayers: Int,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val data = hashMapOf(
            "title" to title,
            "gamemode" to gamemode,
            "prizePool" to prizePool,
            "maxPlayers" to maxPlayers
        )

        functions
            .getHttpsCallable(SystemConfig.CREATE_USER_TOURNAMENT)
            .call(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e ->
                onError(Exception(e.message ?: "Error creating tournament"))
            }
    }

    /**
     * Enrolls a user in a tournament via Cloud Function.
     * Merit checks and deductions are handled server-side for security.
     */
    fun enrollInTournament(
        tournamentId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val data = hashMapOf(
            "tournamentId" to tournamentId
        )

        functions
            .getHttpsCallable(SystemConfig.ENROLL_IN_TOURNAMENT)
            .call(data)
            .addOnSuccessListener {
                onSuccess()
            }
            .addOnFailureListener { error: Exception ->
                onError(Exception(error.message ?: "Failed to enroll"))
            }
    }

    /**
     * Submits writing to an active tournament.
     * This uses a transaction to verify enrollment status before writing the document.
     */
    fun submitToTournament(
        tournamentId: String,
        userId: String,
        submission: Submissions,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val tournamentRef = tournamentsCollection.document(tournamentId)
        val tournamentSubmissionRef = tournamentRef.collection(SystemConfig.SUBMISSIONS_COLLECTION).document(userId)
        val enrollmentRef = tournamentRef.collection("enrollments").document(userId)

        db.runTransaction { transaction ->
            val tournamentSnapshot = transaction.get(tournamentRef)
            if (!tournamentSnapshot.exists()) {
                throw Exception("Tournament not found")
            }

            val tournament = tournamentSnapshot.toObject(Tournament::class.java)
                ?: throw Exception("Invalid tournament")

            if (userId == tournament.creatorId) {
                throw Exception("Host cannot submit to their own tournament")
            }

            if (tournament.status != TournamentStatus.ACTIVE) {
                throw Exception("Tournament not active")
            }

            if (System.currentTimeMillis() > tournament.submissionDeadline) {
                throw Exception("Deadline passed")
            }

            if (!transaction.get(enrollmentRef).exists()) {
                throw Exception("User not enrolled")
            }

            if (transaction.get(tournamentSubmissionRef).exists()) {
                throw Exception("Already submitted")
            }

            if (submission.authorId != userId) {
                throw Exception("Invalid submission author")
            }

            transaction.set(tournamentSubmissionRef, submission)

        }.addOnSuccessListener { onSuccess() }.addOnFailureListener { onError(it) }
    }

    fun getLeaderboard(
        tournamentId: String,
        onSuccess: (List<Submissions>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        tournamentsCollection
            .document(tournamentId)
            .collection(SystemConfig.SUBMISSIONS_COLLECTION)
            .orderBy("evaluation.rankLeaderboard")
            .get()
            .addOnSuccessListener { snapshot ->
                val leaderboard = snapshot.toObjects(Submissions::class.java)
                onSuccess(leaderboard)
            }.addOnFailureListener { onError(it) }
    }

    fun sendTournamentTip(
        tournamentId: String,
        tipperId: String,
        recipientId: String,
        amount: Long,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val tipId = "${tipperId}_${recipientId}"

        val tipData = mapOf(
            "tipperId" to tipperId,
            "recipientId" to recipientId,
            "amount" to amount,
            "createdAt" to System.currentTimeMillis(),
            "processed" to false
        )

        tournamentsCollection
            .document(tournamentId)
            .collection("tips")
            .document(tipId)
            .set(tipData)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun hasUserTippedInTournament(
        tournamentId: String,
        tipperId: String,
        recipientId: String,
        onResult: (Boolean) -> Unit
    ) {
        val tipId = "${tipperId}_${recipientId}"
        tournamentsCollection
            .document(tournamentId)
            .collection("tips")
            .document(tipId)
            .get()
            .addOnSuccessListener { snapshot ->
                onResult(snapshot.exists())
            }
            .addOnFailureListener { onResult(false) }
    }

    fun listenToTournamentFeed(
        onUpdate: (List<Tournament>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return tournamentsCollection
            .whereIn("status", listOf("ENROLLING", "ACTIVE"))
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                if (snapshot == null) {
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }

                val tournaments = snapshot.documents.mapNotNull { doc ->
                    try {
                        doc.toObject(Tournament::class.java)?.copy(id = doc.id)
                    } catch (e: Exception) {
                        Log.e("TournamentRepository", "Failed to parse tournament ${doc.id}", e)
                        null
                    }
                }

                val sorted = tournaments.sortedWith(
                    compareBy<Tournament> {
                        when (it.status) {
                            TournamentStatus.ENROLLING -> 0
                            TournamentStatus.ACTIVE -> 1
                            else -> 2
                        }
                    }.thenBy {
                        when (it.status) {
                            TournamentStatus.ENROLLING -> it.enrollmentDeadline
                            TournamentStatus.ACTIVE -> it.submissionDeadline
                            else -> Long.MAX_VALUE
                        }
                    }
                )

                onUpdate(sorted)
            }
    }

    fun listenToEnrollmentStatus(
        tournamentId: String,
        userId: String,
        onUpdate: (Boolean) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return tournamentsCollection
            .document(tournamentId)
            .collection("enrollments")
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                onUpdate(snapshot?.exists() == true)
            }
    }

    fun listenToTournament(
        tournamentId: String,
        onUpdate: (Tournament?) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return tournamentsCollection
            .document(tournamentId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                if (snapshot == null || !snapshot.exists()) {
                    onUpdate(null)
                    return@addSnapshotListener
                }

                try {
                    onUpdate(snapshot.toObject(Tournament::class.java)?.copy(id = snapshot.id))
                } catch (e: Exception) {
                    onError(e)
                }
            }
    }

    fun listenToSubmissionStatus(
        tournamentId: String,
        userId: String,
        onUpdate: (Boolean) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return tournamentsCollection
            .document(tournamentId)
            .collection(SystemConfig.SUBMISSIONS_COLLECTION)
            .document(userId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                onUpdate(snapshot?.exists() == true)
            }
    }
}
