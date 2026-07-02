package com.river.walklog.core.model

data class AuthUser(
    val uid: String,
    val displayName: String?,
    val email: String?,
    val photoUrl: String?,
    val isNewUser: Boolean = false,
)
