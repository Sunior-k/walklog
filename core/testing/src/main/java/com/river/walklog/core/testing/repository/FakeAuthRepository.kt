package com.river.walklog.core.testing.repository

import com.river.walklog.core.auth.AuthRepository
import com.river.walklog.core.model.AuthUser
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeAuthRepository : AuthRepository {

    private val _currentUser = MutableStateFlow<AuthUser?>(null)
    override val currentUser: Flow<AuthUser?> = _currentUser
    override val currentUserIdOrNull: String?
        get() = _currentUser.value?.uid

    var signInResult: Result<AuthUser> = Result.success(defaultAuthUser())
    var signedOut: Boolean = false

    override suspend fun signInWithGoogle(idToken: String): Result<AuthUser> = signInResult

    override suspend fun signOut() {
        signedOut = true
        _currentUser.value = null
    }

    fun setCurrentUser(user: AuthUser?) {
        _currentUser.value = user
    }
}

fun defaultAuthUser(
    uid: String = "test-uid",
    displayName: String? = "Test User",
    email: String? = "test@example.com",
    photoUrl: String? = null,
    isNewUser: Boolean = false,
) = AuthUser(
    uid = uid,
    displayName = displayName,
    email = email,
    photoUrl = photoUrl,
    isNewUser = isNewUser,
)
