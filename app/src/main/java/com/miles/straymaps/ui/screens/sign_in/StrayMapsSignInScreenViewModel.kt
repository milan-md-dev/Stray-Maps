package com.miles.straymaps.ui.screens.sign_in

import com.miles.straymaps.StrayMapsScreen
import com.miles.straymaps.data.firebase.AccountServiceInterface
import com.miles.straymaps.misc.isValidEmail
import com.miles.straymaps.ui.screens.StrayMapsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
class StrayMapsSignInScreenViewModel @Inject constructor(
    private val accountService: AccountServiceInterface
) : StrayMapsViewModel() {

    val email = MutableStateFlow("")
    val password = MutableStateFlow("")

    val emailFormatNotSupported = MutableStateFlow<Boolean?>(null)

    fun updateEmail(newEmail: String) {
        email.value = newEmail
    }

    fun updatePassword(newPassword: String) {
        password.value = newPassword
    }

    fun onSignInClick(openAndPopUp: (String, String) -> Unit) {
        launchCatching {
            if (!email.value.isValidEmail()){
                emailFormatNotSupported.value = true
                throw IllegalArgumentException("Invalid email format.")
            }
            accountService.signIn(email.value.trim(), password.value)
            openAndPopUp(StrayMapsScreen.Home.route, StrayMapsScreen.SignIn.route)
        }
    }

    fun onSignUpClick(openAndPopUp: (String, String) -> Unit) {
        openAndPopUp(StrayMapsScreen.SignUp.route, StrayMapsScreen.SignUp.route)
    }

    fun clearErrorState() {
        emailFormatNotSupported.value = null
    }
}