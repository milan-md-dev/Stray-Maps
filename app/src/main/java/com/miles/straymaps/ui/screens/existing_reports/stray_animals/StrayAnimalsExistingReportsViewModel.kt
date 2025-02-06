package com.miles.straymaps.ui.screens.existing_reports.stray_animals


import android.os.Build
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.miles.straymaps.StrayMapsScreen
import com.miles.straymaps.data.firebase.AccountServiceInterface
import com.miles.straymaps.data.repositories.stray_animal.StrayAnimalRepositoryImplementation
import com.miles.straymaps.data.stray_animal.StrayAnimal
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
class StrayAnimalsExistingReportsViewModel @Inject constructor(
    private val accountService: AccountServiceInterface,
    private val strayAnimalRepository: StrayAnimalRepositoryImplementation
) : StrayMapsViewModel() {

    private val _allStrayAnimalFiledReportsState: MutableStateFlow<List<StrayAnimal>> =
        MutableStateFlow(emptyList())
    val allStrayAnimalFiledReportState: StateFlow<List<StrayAnimal>> =
        _allStrayAnimalFiledReportsState.asStateFlow()

    fun initialize(restartApp: (String) -> Unit) {
        getAllStrayAnimalReports()
        observeAuthenticationState(restartApp)
    }

    private fun observeAuthenticationState(restartApp: (String) -> Unit) {
        launchCatching {
            accountService.currentUser.collect { user ->
                if (user == null) restartApp(StrayMapsScreen.SplashScreen.route)

            }
        }
    }

    private fun getAllStrayAnimalReports() {
        viewModelScope.launch {
            strayAnimalRepository.loadAllStrayAnimalReports().collect { reports ->
                _allStrayAnimalFiledReportsState.value = reports
            }
        }
    }

    fun getAllStrayReportsByType() {
        viewModelScope.launch {
            strayAnimalRepository.loadAllByType().collect { reports ->
                _allStrayAnimalFiledReportsState.value = reports
            }
        }
    }

    fun getAllStrayReportsByColour() {
        viewModelScope.launch {
            strayAnimalRepository.loadAllByColour().collect { reports ->
                _allStrayAnimalFiledReportsState.value = reports
            }
        }
    }

    fun getAllStrayReportsBySex() {
        viewModelScope.launch {
            strayAnimalRepository.loadAllBySex().collect { reports ->
                _allStrayAnimalFiledReportsState.value = reports
            }
        }
    }

    fun getAllStrayReportsByDate() {
        viewModelScope.launch {
            strayAnimalRepository.loadAllByDateAndTime().collect { reports ->
                _allStrayAnimalFiledReportsState.value = reports
            }
        }
    }

    var microchipIdReportFound = MutableStateFlow<Boolean?>(null)

    fun findStrayAnimalReportByMicrochipId(input: String) {
        viewModelScope.launch {
            val report =
                strayAnimalRepository.getStrayAnimalByMicrochipId(input.uppercase(Locale.getDefault()))
            if (report != null) {
                microchipIdReportFound.value = true
                _allStrayAnimalFiledReportsState.value = listOf(report)
            } else {
                microchipIdReportFound.value = false
            }
        }
    }

    //Need to implement functionality for specific type of animal search
    val foundAllStrayAnimalOfSpecificType: Flow<List<StrayAnimal>> =
        strayAnimalRepository.getAllStrayAnimalOfSpecificType(type = "dog")


    suspend fun upsertStrayAnimal(strayAnimal: StrayAnimal) {
        viewModelScope.launch {
            strayAnimalRepository.upsertStrayAnimal(strayAnimal)
        }
    }

    suspend fun deleteStrayAnimal(strayAnimal: StrayAnimal) {
        viewModelScope.launch {
            strayAnimalRepository.deleteStrayAnimal(strayAnimal)
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatLocalDateTime(localDateTime: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy | HH:mm")
        return localDateTime.format(formatter)
    }


    companion object {
        private const val TIMEOUT_MILIS = 5_000L
    }

    var userInputMicrochipID by mutableStateOf("")

    fun updateUserInputMicrochipId(input: String) {
        userInputMicrochipID = input
    }

}