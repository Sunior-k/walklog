package com.river.walklog.core.auth

import com.river.walklog.core.model.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<AuthUser?>

    /** Flow 구독 없이 즉시 로그인 상태를 확인해야 할 때 사용. */
    val currentUserIdOrNull: String?
    suspend fun signInWithGoogle(idToken: String): Result<AuthUser>
    suspend fun signOut()
}
