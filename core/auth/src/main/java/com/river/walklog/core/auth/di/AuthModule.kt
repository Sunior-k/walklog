package com.river.walklog.core.auth.di

import com.google.firebase.auth.FirebaseAuth
import com.river.walklog.core.auth.AuthRepository
import com.river.walklog.core.auth.FirebaseAuthRepository
import dagger.Binds
import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
abstract class AuthBindingModule {

    @Binds
    @Singleton
    abstract fun bindAuthRepository(impl: FirebaseAuthRepository): AuthRepository
}

@Module
@InstallIn(SingletonComponent::class)
object AuthProviderModule {

    @Provides
    @Singleton
    fun provideFirebaseAuth(): FirebaseAuth = FirebaseAuth.getInstance()
}
