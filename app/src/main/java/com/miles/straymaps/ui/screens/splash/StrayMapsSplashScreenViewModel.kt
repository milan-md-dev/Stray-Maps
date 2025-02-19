package com.miles.straymaps.ui.screens.splash

import com.miles.straymaps.StrayMapsScreen
import com.miles.straymaps.ui.screens.StrayMapsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject

@HiltViewModel
open class StrayMapsSplashScreenViewModel @Inject constructor(
) : StrayMapsViewModel() {

    fun onSplashComplete(openAndPopUp: (String, String) -> Unit) {
        openAndPopUp(
            StrayMapsScreen.Welcome.route, StrayMapsScreen.SplashScreen.route
        )
    }

}