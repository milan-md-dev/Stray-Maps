package com.miles.straymaps.ui.screens.existing_reports.stray_animals


import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.viewModelScope
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import com.miles.straymaps.StrayMapsScreen
import com.miles.straymaps.data.firebase.AccountServiceInterface
import com.miles.straymaps.data.repositories.stray_animal.StrayAnimalRepositoryImplementation
import com.miles.straymaps.data.stray_animal.StrayAnimal
import com.miles.straymaps.ui.screens.StrayMapsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.tasks.await
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Locale
import javax.inject.Inject


@HiltViewModel
class StrayAnimalsExistingReportsViewModel @Inject constructor(
    private val accountService: AccountServiceInterface,
    private val strayAnimalRepository: StrayAnimalRepositoryImplementation
) : StrayMapsViewModel() {

    private val db = Firebase.firestore

    private val storage = Firebase.storage

    private val storageReference = storage.reference

    private val TAG = "StrayAnimalExistingReportsViewModel"

    private val _allStrayAnimalFiledReportsState: MutableStateFlow<List<StrayAnimal>> =
        MutableStateFlow(emptyList())
    val allStrayAnimalFiledReportState: StateFlow<List<StrayAnimal>> =
        _allStrayAnimalFiledReportsState.asStateFlow()

    fun initialize(restartApp: (String) -> Unit) {
        viewModelScope.launch {
            try {
                observeAuthenticationState(restartApp)
                getAllDataFromFirestoreDatabase()
                observeStrayAnimalReports()
            } catch (e: Exception) {
                Log.e(TAG, "Initialization error: ", e)
            }
        }
    }

    fun reloadReports() {
        viewModelScope.launch {
            try {
                getAllDataFromFirestoreDatabase()
                observeStrayAnimalReports()
            } catch (e: Exception) {
                Log.e(TAG, "Reloading error: ", e)
            }
        }
    }

    private fun observeAuthenticationState(restartApp: (String) -> Unit) {
        launchCatching {
            accountService.currentUser.collect { user ->
                if (user == null) restartApp(StrayMapsScreen.SplashScreen.route)

            }
        }
    }

    private fun observeStrayAnimalReports() {
        viewModelScope.launch {
            strayAnimalRepository.loadAllStrayAnimalReports().collect { reports ->
                _allStrayAnimalFiledReportsState.value = reports
                Log.d(TAG, "Loaded reports from Room: ${reports.size}")
            }
        }
    }

    private fun getAllDataFromFirestoreDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Get all data
                val data = db.collection("stray_animal_reports")
                    .get()
                    .await()

                // Convert each report to a Stray Animal object
                val reports = data.documents.mapNotNull { document ->
                    document.toObject(StrayAnimal::class.java)?.let { strayAnimal ->
                        // Get the date and time
                        val dateTimeString =
                            document.getString("strayAnimalReportDateAndTime") ?: ""
                        // Get the ID
                        val id = document.getLong("strayAnimalId")?.toInt()
                        // Get the unique ID, used to match the report to its corresponding image in Firebase Storage
                        val uniqueId = document.getString("strayAnimalReportUniqueId") ?: ""

                        // Using the unique ID, download the URL for the image
                        async {
                            val photoUrl = getImageUrlFromFirestoreStorage(uniqueId)
                            Log.d(TAG, "Photo path: ${strayAnimal.strayAnimalPhotoPath}")

                            // Putting it all together
                            strayAnimal.copy(
                                strayAnimalId = id,
                                strayAnimalReportDateAndTime = dateTimeString,
                                strayAnimalReportUniqueId = uniqueId,
                                strayAnimalPhotoPath = photoUrl
                            )
                        }
                    }
                }.map { it.await() }

                if (reports.isNotEmpty()) {
                    reports.forEach { report ->
                        strayAnimalRepository.insertStrayAnimalReport(report)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting reports from Firestore.", e)
            }
        }
    }

    private suspend fun getImageUrlFromFirestoreStorage(uniqueId: String): String {
        return try {
            storageReference.child("stray_animal_images/$uniqueId").downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving the image URL.", e)
            "/data/user/0/com.example.straymaps/files/no_image_available.png"
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

    /** Need to implement functionality for specific type of animal search
    val foundAllStrayAnimalOfSpecificType: Flow<List<StrayAnimal>> =
    strayAnimalRepository.getAllStrayAnimalOfSpecificType(type = "dog")


    suspend fun updateStrayAnimal(strayAnimal: StrayAnimal) {
    viewModelScope.launch {
    strayAnimalRepository.updateStrayAnimal(strayAnimal)
    }
    }

    suspend fun deleteStrayAnimal(strayAnimal: StrayAnimal) {
    viewModelScope.launch {
    strayAnimalRepository.deleteStrayAnimal(strayAnimal)
    }
    }
     */

    @RequiresApi(Build.VERSION_CODES.O)
    fun formatLocalDateTime(localDateTime: LocalDateTime): String {
        val formatter = DateTimeFormatter.ofPattern("dd.MM.yyyy | HH:mm")
        return localDateTime.format(formatter)
    }

    var userInputMicrochipID by mutableStateOf("")

    fun updateUserInputMicrochipId(input: String) {
        userInputMicrochipID = input
    }

}