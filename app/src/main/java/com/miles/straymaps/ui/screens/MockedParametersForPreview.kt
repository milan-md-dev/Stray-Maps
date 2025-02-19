package com.miles.straymaps.ui.screens

import com.miles.straymaps.data.User
import com.miles.straymaps.data.firebase.AccountServiceInterface
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flowOf

class FakeAccountService : AccountServiceInterface {

    override val currentUser: Flow<User?> = flowOf(null)

    override val currentUserId: String = "fake_user_id"

    override fun hasUser(): Boolean = false

    override suspend fun signIn(email: String, password: String) {}

    override suspend fun signUp(email: String, password: String) {}

    override suspend fun signOut() {}

    override suspend fun deleteAccount() {}

    override suspend fun createAnonymousAccount() {}

    override suspend fun linkAccount(email: String, password: String) {}

    override suspend fun getUserProfile(): User = User()

    override fun isUserAnonymous(): Boolean = false
}