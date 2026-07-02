package com.river.walklog.core.data.datasource

import com.google.firebase.firestore.FirebaseFirestore
import com.river.walklog.core.common.dispatcher.WalkLogDispatchers
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class FirestoreUserSettingsDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
    private val dispatchers: WalkLogDispatchers,
) {
    private fun settingsRef(userId: String) =
        firestore.collection("users").document(userId)
            .collection("data").document("settings")

    suspend fun getSettings(userId: String): FirestoreUserSettings? =
        withContext(dispatchers.io) {
            runCatching {
                settingsRef(userId).get().await()
                    .toObject(FirestoreUserSettings::class.java)
            }.getOrNull()
        }

    suspend fun updateSettings(userId: String, settings: FirestoreUserSettings): Boolean =
        withContext(dispatchers.io) {
            runCatching {
                settingsRef(userId).set(settings).await()
            }.isSuccess
        }
}
