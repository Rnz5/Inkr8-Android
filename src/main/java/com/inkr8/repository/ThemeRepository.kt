package com.inkr8.repository

import com.google.firebase.firestore.FirebaseFirestore
import com.inkr8.data.Theme
import com.inkr8.utils.SystemConfig
import kotlinx.coroutines.tasks.await
import kotlin.random.Random

class ThemeRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    private val themesCollection = firestore.collection(SystemConfig.THEMES_COLLECTION)

    suspend fun getRandomTheme(): Theme? {
        val randomOffset = Random.nextDouble()

        var snapshot = themesCollection
            .whereGreaterThanOrEqualTo("randomIndex", randomOffset)
            .limit(1)
            .get()
            .await()

        if (snapshot.isEmpty) {
            snapshot = themesCollection
                .whereLessThan("randomIndex", randomOffset)
                .limit(1)
                .get()
                .await()
        }

        return snapshot.documents.firstOrNull()?.let { doc ->
            doc.toObject(Theme::class.java)?.copy(id = doc.id)
        }
    }
}
