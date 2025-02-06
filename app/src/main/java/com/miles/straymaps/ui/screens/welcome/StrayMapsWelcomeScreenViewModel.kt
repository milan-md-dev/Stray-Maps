package com.miles.straymaps.ui.screens.welcome

import com.miles.straymaps.StrayMapsScreen
import com.miles.straymaps.data.firebase.AccountServiceInterface
import com.miles.straymaps.ui.screens.StrayMapsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StrayMapsWelcomeScreenViewModel @Inject constructor(
    private val accountService: AccountServiceInterface
) : StrayMapsViewModel() {

    fun onAppStart(openAndPopUp: (String, String) -> Unit) {
        if (accountService.hasUser()) openAndPopUp(
            StrayMapsScreen.Home.route, StrayMapsScreen.Welcome.route
        )
    }

    fun createAnonymousAccount(openAndPopUp: (String, String) -> Unit) {
        launchCatching {
            if (!accountService.hasUser()) {
                accountService.createAnonymousAccount()
                openAndPopUp(StrayMapsScreen.Home.route, StrayMapsScreen.Welcome.route)
            }
        }
    }

}