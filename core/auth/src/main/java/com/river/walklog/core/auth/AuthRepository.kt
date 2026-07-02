package com.river.walklog.core.auth

import com.river.walklog.core.model.AuthUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    val currentUser: Flow<AuthUser?>
    suspend fun signInWithGoogle(idToken: String): Result<AuthUser>
    suspend fun signOut()
}
