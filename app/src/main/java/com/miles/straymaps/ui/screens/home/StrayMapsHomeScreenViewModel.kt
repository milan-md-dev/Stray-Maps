package com.miles.straymaps.ui.screens.home

import com.miles.straymaps.StrayMapsScreen
import com.miles.straymaps.data.User
import com.miles.straymaps.data.firebase.AccountServiceInterface
import com.miles.straymaps.ui.screens.StrayMapsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import javax.inject.Inject

@HiltViewModel
class StrayMapsHomeScreenViewModel @Inject constructor(
    private val accountService: AccountServiceInterface
) : StrayMapsViewModel() {

    private val _currentUser = MutableStateFlow<User?>(null)
    val currentUser: StateFlow<User?> = _currentUser.asStateFlow()

    private val _isCurrentUserAnonymous = MutableStateFlow<Boolean?>(null)
    val isCurrentUserAnonymous: StateFlow<Boolean?> = _isCurrentUserAnonymous.asStateFlow()

    private val _currentUserProfile = MutableStateFlow<User?>(null)
    val currentUserProfile: StateFlow<User?> = _currentUserProfile.asStateFlow()

    fun initialize(restartApp: (String) -> Unit) {
        launchCatching {
            accountService.currentUser.collect { user ->
                if (user == null) restartApp(StrayMapsScreen.Welcome.route)
                else {
                    _currentUser.value = user
                    _isCurrentUserAnonymous.value = accountService.isUserAnonymous()
                    getCurrentUserProfile()
                }
            }
        }
    }

    fun getCurrentUserProfile() {
        launchCatching {
            _currentUserProfile.value = accountService.getUserProfile()
        }
    }

    fun onSignOutClick() {
        launchCatching {
            accountService.signOut()
        }
    }

    fun onDeleteAccountClick() {
        launchCatching {
            accountService.deleteAccount()
        }
    }

}