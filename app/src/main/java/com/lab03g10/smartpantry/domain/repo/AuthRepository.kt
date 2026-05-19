package com.lab03g10.smartpantry.domain.repo

import com.google.firebase.auth.FirebaseUser
import kotlinx.coroutines.flow.Flow

interface AuthRepository {
    /**
     * Observes the current user state via Firebase authStateChanges.
     */
    fun observeCurrentUser(): Flow<FirebaseUser?>

    /**
     * Sign up with email, name, and password.
     */
    suspend fun signUp(email: String, name: String, password: String): Result<FirebaseUser?>

    /**
     * Sign in with email and password.
     */
    suspend fun signIn(email: String, password: String): Result<FirebaseUser?>

    /**
     * Send a password reset email to the specified address.
     */
    suspend fun sendPasswordReset(email: String): Result<Unit>

    /**
     * Sign in with a Google ID token obtained from Credential Manager.
     */
    suspend fun signInWithGoogle(idToken: String): Result<FirebaseUser?>

    /**
     * Signs the current user out.
     */
    fun signOut()

    /**
     * Helper to check if a user is currently sessioned.
     */
    fun isUserLoggedIn(): Boolean
}
