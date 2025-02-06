package com.miles.straymaps.data.firebase

import com.miles.straymaps.data.User
import kotlinx.coroutines.flow.Flow

interface AccountServiceInterface {
    val currentUser: Flow<User?>
    val currentUserId: String
    fun hasUser(): Boolean
    suspend fun signIn(email: String, password: String)
    suspend fun signUp(email: String, password: String)
    suspend fun signOut()
    suspend fun deleteAccount()
    suspend fun createAnonymousAccount()
    suspend fun linkAccount(email: String, password: String)
    suspend fun getUserProfile() :User
    fun isUserAnonymous(): Boolean
}