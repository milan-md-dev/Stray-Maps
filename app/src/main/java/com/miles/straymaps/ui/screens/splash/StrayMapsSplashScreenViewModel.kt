package com.miles.straymaps.ui.screens.splash

import com.miles.straymaps.StrayMapsScreen
import com.miles.straymaps.data.firebase.AccountServiceInterface
import com.miles.straymaps.ui.screens.StrayMapsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
class StrayMapsSplashScreenViewModel @Inject constructor(
    private val accountService: AccountServiceInterface
) : StrayMapsViewModel() {

    fun onSplashComplete(openAndPopUp: (String, String) -> Unit) {
        openAndPopUp(
            StrayMapsScreen.Welcome.route, StrayMapsScreen.SplashScreen.route
        )
    }

}