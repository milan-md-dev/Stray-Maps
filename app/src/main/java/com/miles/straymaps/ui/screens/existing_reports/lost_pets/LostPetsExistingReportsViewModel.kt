package com.miles.straymaps.ui.screens.existing_reports.lost_pets

import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.miles.straymaps.StrayMapsScreen
import com.miles.straymaps.data.firebase.AccountServiceInterface
import com.miles.straymaps.data.lost_pet.LostPet
import com.miles.straymaps.data.repositories.lost_pet.LostPetRepositoryImplementation
import com.miles.straymaps.ui.screens.StrayMapsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject


@HiltViewModel
class LostPetFiledReportsScreenViewModel @Inject constructor(
    private val accountService: AccountServiceInterface,
    private val lostPetRepository: LostPetRepositoryImplementation
) : StrayMapsViewModel() {

    private val _allLostPetFiledReportsState: MutableStateFlow<List<LostPet>> =
        MutableStateFlow(emptyList())
    val allLostPetFiledReportState: StateFlow<List<LostPet>> =
        _allLostPetFiledReportsState.asStateFlow()

    fun initialize(restartApp: (String) -> Unit) {
        getAllLostPetFiledReportState()
        observeAuthenticationState(restartApp)
    }

    private fun observeAuthenticationState(restartApp: (String) -> Unit) {
        launchCatching {
            accountService.currentUser.collect { user ->
                if (user == null) restartApp(StrayMapsScreen.SplashScreen.route)
            }
        }
    }

    private fun getAllLostPetFiledReportState() {
        viewModelScope.launch {
            lostPetRepository.loadAllLostPetReports().collect() { reports ->
                _allLostPetFiledReportsState.value = reports
            }
        }
    }

    fun getAllLostPetReportsByType() {
        viewModelScope.launch {
            lostPetRepository.loadAllByType().collect() { reports ->
                _allLostPetFiledReportsState.value = reports
            }
        }
    }

    fun getAllLostPetReportsByColour() {
        viewModelScope.launch {
            lostPetRepository.loadAllByColour().collect() { reports ->
                _allLostPetFiledReportsState.value = reports
            }
        }
    }

    fun getAllLostPetReportsBySex() {
        viewModelScope.launch {
            lostPetRepository.loadAllBySex().collect() { reports ->
                _allLostPetFiledReportsState.value = reports
            }
        }
    }

    fun getAllLostPetReportsByDate() {
        viewModelScope.launch {
            lostPetRepository.loadAllByDateAndTime().collect() { reports ->
                _allLostPetFiledReportsState.value = reports
            }
        }
    }

    var microchipIdReportFound = MutableStateFlow<Boolean?>(null)

    fun findLostPetReportByMicrochipId(input: String) {
        viewModelScope.launch {
            val report =
                lostPetRepository.getLostPetsByMicrochipId(input.uppercase(Locale.getDefault()))
            if (report != null) {
                microchipIdReportFound.value = true
                _allLostPetFiledReportsState.value = listOf(report)
            } else {
                microchipIdReportFound.value = false
            }
        }
    }

    //Need to implement functionality for specific type of animal search
    val foundAllLostPetOfSpecificType: Flow<List<LostPet>> =
        lostPetRepository.getAllLostPetsOfSpecificType(type = "dog")

    suspend fun upsertLostPet(lostPet: LostPet) {
        viewModelScope.launch {
            lostPetRepository.upsertLostPet(lostPet)
        }
    }

    suspend fun deleteLostPet(lostPet: LostPet) {
        viewModelScope.launch {
            lostPetRepository.deleteLostPet(lostPet)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatLocalDateTime(localDateTime: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy || HH:mm")
        return localDateTime.format(formatter)
    }

    var userInputMicrochipId by mutableStateOf("")

    fun updateUserInputMicrochipId(input: String) {
        userInputMicrochipId = input
    }
}