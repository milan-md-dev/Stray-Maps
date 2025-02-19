package com.miles.straymaps.ui.screens.sign_up


import com.miles.straymaps.StrayMapsScreen
import com.miles.straymaps.data.firebase.AccountServiceInterface
import com.miles.straymaps.misc.isValidEmail
import com.miles.straymaps.misc.isValidPassword
import com.miles.straymaps.ui.screens.StrayMapsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import javax.inject.Inject

@HiltViewModel
open class StrayMapsSignUpScreenViewModel @Inject constructor(
    private val accountService: AccountServiceInterface
) : StrayMapsViewModel() {

    val signUpEmail = MutableStateFlow("")
    val signUpPassword = MutableStateFlow("")
    val signUpConfirmPassword = MutableStateFlow("")

    val emailFormatNotSupported = MutableStateFlow<Boolean?>(null)
    val passwordFormatNotSupported = MutableStateFlow<Boolean?>(null)
    val passwordsDoNotMatch = MutableStateFlow<Boolean?>(null)

    fun updateEmail(email: String) {
        signUpEmail.value = email
    }

    fun updatePassword(password: String) {
        signUpPassword.value = password
    }

    fun updateConfirmPassword(newConfirmPassword: String) {
        signUpConfirmPassword.value = newConfirmPassword
    }

    fun onSignUpClick(openAndPopUp: (String, String) -> Unit) {
        launchCatching {
            if (!signUpEmail.value.isValidEmail()) {
                emailFormatNotSupported.value = true
                throw IllegalArgumentException("Invalid email format.")
            }

            if (!signUpPassword.value.isValidPassword()) {
                passwordFormatNotSupported.value = true
                throw IllegalArgumentException("Invalid password format.")
            }

            if (signUpPassword.value != signUpConfirmPassword.value) {
                passwordsDoNotMatch.value = true
                throw Exception("Passwords do not match!")
            }

            accountService.linkAccount(signUpEmail.value, signUpPassword.value)
            openAndPopUp(StrayMapsScreen.Home.route, StrayMapsScreen.SignIn.route)
        }
    }

    fun clearErrorStates() {
        emailFormatNotSupported.value = null
        passwordFormatNotSupported.value = null
        passwordsDoNotMatch.value = null
    }


}