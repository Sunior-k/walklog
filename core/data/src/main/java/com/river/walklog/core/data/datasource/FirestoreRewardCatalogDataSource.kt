package com.river.walklog.core.data.datasource

import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.river.walklog.core.model.RewardCatalogItem
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.callbackFlow
import javax.inject.Inject
import javax.inject.Singleton

/**
 * 호출부(RewardBridgeModule)가 매번 `.first()`로 한 번만 소비하므로 `addSnapshotListener`를 쓰면
 * 로컬 캐시(콘솔 수정 이전 값)가 먼저 도착해 `.first()`가 그걸로 끝내버리고, 서버의 최신 값은
 * 영영 못 받는 레이스가 생긴다. 매 호출마다 `get()`으로 한 번씩 새로 받아 이 문제를 없앤다
 * (기본 Source는 온라인이면 서버, 오프라인이면 캐시로 자동 폴백).
 */
@Singleton
class FirestoreRewardCatalogDataSource @Inject constructor(
    private val firestore: FirebaseFirestore,
) {
    fun observeCatalog(): Flow<List<RewardCatalogItem>> = callbackFlow {
        firestore.collection(COLLECTION)
            .get()
            .addOnSuccessListener { snapshot ->
                val items = snapshot.documents
                    .mapNotNull { it.toRewardCatalogItemOrNull() }
                    .sortedBy { it.cost }
                trySend(items)
                close()
            }
            .addOnFailureListener { e ->
                close(e)
            }
        awaitClose { }
    }

    private fun DocumentSnapshot.toRewardCatalogItemOrNull(): RewardCatalogItem? {
        val title = getString(FIELD_TITLE) ?: return null
        return RewardCatalogItem(
            id = id,
            emoji = getString(FIELD_EMOJI) ?: "",
            title = title,
            description = getString(FIELD_DESCRIPTION) ?: "",
            cost = (getLong(FIELD_COST) ?: 0L).toInt(),
            isActive = getBoolean(FIELD_IS_ACTIVE) ?: true,
        )
    }

    private companion object {
        const val COLLECTION = "rewardCatalog"
        const val FIELD_TITLE = "title"
        const val FIELD_EMOJI = "emoji"
        const val FIELD_DESCRIPTION = "description"
        const val FIELD_COST = "cost"
        const val FIELD_IS_ACTIVE = "isActive"
    }
}
