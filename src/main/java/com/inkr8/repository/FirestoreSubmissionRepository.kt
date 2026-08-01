package com.inkr8.repository

import com.google.firebase.Firebase
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import com.google.firebase.functions.functions
import com.inkr8.AuthManager
import com.inkr8.data.Submissions
import com.inkr8.mappers.toDomain
import com.inkr8.mappers.toFirestore
import com.inkr8.utils.SystemConfig

class FirestoreSubmissionRepository() {
    private val db = FirebaseFirestore.getInstance()
    private val submissionsCollection  = db.collection(SystemConfig.SUBMISSIONS_COLLECTION)
    private val usersCollection = db.collection(SystemConfig.USERS_COLLECTION)

    fun addSubmission(
        submission: Submissions,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        if (submission.authorId.isBlank()) {
            onError(IllegalStateException("Submission authorId cannot be empty"))
            return
        }

        val firestoreSubmission = submission.toFirestore()
        val submissionRef = submissionsCollection.document(firestoreSubmission.id)

        submissionRef.set(firestoreSubmission)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e) }
    }

    fun saveSubmission(
        submissionId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        val data = hashMapOf(
            "action" to SystemConfig.ACTION_SAVE_SUBMISSION,
            "submissionId" to submissionId
        )

        Firebase.functions
            .getHttpsCallable(SystemConfig.APPLY_MERIT_ACTION)
            .call(data)
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { onError(it) }
    }

    fun deleteSubmission(
        submissionId: String,
        onSuccess: () -> Unit,
        onError: (Exception) -> Unit
    ) {
        submissionsCollection.document(submissionId).delete()
            .addOnSuccessListener { onSuccess() }
            .addOnFailureListener { e -> onError(e) }
    }

    fun listenToAllSubmissions(
        authorId: String,
        onUpdate: (List<Submissions>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration {
        return submissionsCollection
            .whereEqualTo("authorId", authorId)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                val submissions = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FirestoreSubmission::class.java)?.copy(id = doc.id)?.toDomain()
                } ?: emptyList()
                onUpdate(submissions)
            }
    }

    fun listenToRecentRankedSubmissions(
        onUpdate: (List<Submissions>) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration? {
        val userId = AuthManager.currentUser()?.uid ?: return null
        val since = System.currentTimeMillis() - (48 * 60 * 60 * 1000L)

        return submissionsCollection
            .whereEqualTo("authorId", userId)
            .whereEqualTo("playmode", "RANKED")
            .whereGreaterThanOrEqualTo("timestamp", since)
            .orderBy("timestamp", Query.Direction.DESCENDING)
            .limit(10)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }
                val submissions = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(FirestoreSubmission::class.java)?.copy(id = doc.id)?.toDomain()
                } ?: emptyList()
                onUpdate(submissions)
            }
    }

    fun getSubmissionContent(
        submissionId: String,
        onSuccess: (String) -> Unit,
        onError: (Exception) -> Unit
    ) {
        submissionsCollection.document(submissionId).get()
            .addOnSuccessListener { snapshot ->
                val content = snapshot.getString("content") ?: ""
                onSuccess(content)
            }
            .addOnFailureListener { onError(it) }
    }

    fun getAllSubmissions(
        authorId: String,
        onSuccess: (List<Submissions>) -> Unit,
        onError: (Exception) -> Unit
    ) {
        submissionsCollection.whereEqualTo("authorId", authorId).orderBy("timestamp", Query.Direction.DESCENDING).get()
            .addOnSuccessListener { snapshot ->
                val submissions = snapshot.documents.mapNotNull { doc ->
                    doc.toObject(FirestoreSubmission::class.java)?.copy(id = doc.id)?.toDomain()
                }
                onSuccess(submissions)
            }
            .addOnFailureListener { onError(it) }
    }

    fun getLastSubmission(
        onSuccess: (Submissions?) -> Unit,
        onError: (Exception) -> Unit
    ) {
        val userId = AuthManager.currentUser()?.uid ?: return
        submissionsCollection.whereEqualTo("authorId", userId).orderBy("timestamp", Query.Direction.DESCENDING).limit(1).get()
            .addOnSuccessListener { snapshot ->
                val doc = snapshot.documents.firstOrNull()
                val submission = doc?.toObject(FirestoreSubmission::class.java)?.copy(id = doc.id)?.toDomain()
                onSuccess(submission)
            }
            .addOnFailureListener { e -> onError(e) }
    }

    fun getLastSubmissionRealtime(
        onUpdate: (Submissions) -> Unit,
        onError: (Exception) -> Unit
    ): ListenerRegistration? {
        val userId = AuthManager.currentUser()?.uid ?: return null

        return submissionsCollection.whereEqualTo("authorId", userId).orderBy("timestamp", Query.Direction.DESCENDING).limit(1)
            .addSnapshotListener { snapshot, error ->

                if (error != null) {
                    onError(error)
                    return@addSnapshotListener
                }

                val doc = snapshot?.documents?.firstOrNull()
                if (doc != null) {
                    val submission = doc.toObject(FirestoreSubmission::class.java)?.copy(id = doc.id)?.toDomain()
                    if (submission != null) {
                        onUpdate(submission)
                    }
                }
            }
    }
}
