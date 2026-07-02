package com.river.walklog.core.auth

import android.content.Context
import androidx.credentials.CredentialManager
import androidx.credentials.CustomCredential
import androidx.credentials.GetCredentialRequest
import androidx.credentials.exceptions.GetCredentialCancellationException
import com.google.android.libraries.identity.googleid.GetSignInWithGoogleOption
import com.google.android.libraries.identity.googleid.GoogleIdTokenCredential

sealed interface GoogleSignInResult {
    data class Success(val idToken: String) : GoogleSignInResult
    data object Cancelled : GoogleSignInResult
    data object Failed : GoogleSignInResult
}

suspend fun getGoogleIdToken(context: Context, clientId: String): GoogleSignInResult {
    if (clientId.isEmpty()) return GoogleSignInResult.Failed
    return runCatching {
        val credentialManager = CredentialManager.create(context)
        val request = GetCredentialRequest.Builder()
            .addCredentialOption(GetSignInWithGoogleOption.Builder(clientId).build())
            .build()
        val result = credentialManager.getCredential(context = context, request = request)
        val credential = result.credential
        check(
            credential is CustomCredential &&
                credential.type == GoogleIdTokenCredential.TYPE_GOOGLE_ID_TOKEN_CREDENTIAL,
        ) { "Unexpected credential type" }
        GoogleIdTokenCredential.createFrom(credential.data).idToken
    }.fold(
        onSuccess = { GoogleSignInResult.Success(it) },
        onFailure = { e ->
            if (e is GetCredentialCancellationException) GoogleSignInResult.Cancelled
            else GoogleSignInResult.Failed
        },
    )
}
