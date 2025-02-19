package com.miles.straymaps.ui.screens.existing_reports.lost_pets

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
import com.miles.straymaps.data.lost_pet.LostPet
import com.miles.straymaps.data.repositories.lost_pet.LostPetRepositoryImplementation
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
class LostPetFiledReportsScreenViewModel @Inject constructor(
    private val accountService: AccountServiceInterface,
    private val lostPetRepository: LostPetRepositoryImplementation
) : StrayMapsViewModel() {

    private val db = Firebase.firestore

    private val storage = Firebase.storage

    private val storageReference = storage.reference

    private val TAG = "LostPetsExistingReportsViewModel"

    private val _allLostPetFiledReportsState: MutableStateFlow<List<LostPet>> =
        MutableStateFlow(emptyList())
    val allLostPetFiledReportState: StateFlow<List<LostPet>> =
        _allLostPetFiledReportsState.asStateFlow()

    @RequiresApi(Build.VERSION_CODES.O)
    fun initialize(restartApp: (String) -> Unit) {
        viewModelScope.launch {
            try {
                observeAuthenticationState(restartApp)
                getAllDataFromFirestoreDatabase()
                observeLostPetReports()
            } catch (e: Exception) {
                Log.e(TAG, "Initialization error: ", e)
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    fun reloadReports() {
        viewModelScope.launch {
            try {
                getAllDataFromFirestoreDatabase()
                observeLostPetReports()
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

    private fun observeLostPetReports() {
        viewModelScope.launch {
            lostPetRepository.loadAllLostPetReports().collect { reports ->
                _allLostPetFiledReportsState.value = reports
                Log.d(TAG, "Loaded reports from Room: ${reports.size}")
            }
        }
    }

    // Function for downloading data from Firestore Database for already existing reports
    @RequiresApi(Build.VERSION_CODES.O)
    private fun getAllDataFromFirestoreDatabase() {
        viewModelScope.launch(Dispatchers.IO) {
            try {
                // Get all data
                val data = db.collection("lost_pet_reports")
                    .get()
                    .await()

                // Convert each report to a Lost Pet object
                val reports = data.documents.mapNotNull { document ->
                    document.toObject(LostPet::class.java)?.let { lostPet ->
                        // Get the date and time
                        val dateTimeString = document.getString("lostPetReportDateAndTime") ?: ""
                        // Get the ID
                        val id = document.getLong("lostPetId")?.toInt()
                        // Get the unique ID, used to match the report to its corresponding image in Firebase Storage
                        val uniqueId = document.getString("lostPetReportUniqueId") ?: ""

                        // Using the unique ID, download the URL for the image
                        async {
                            val photoUrl = getImageUrlFromFirestoreStorage(uniqueId)
                            Log.d(TAG, "Photo path: ${lostPet.lostPetPhoto}")

                            // Putting it all together
                            lostPet.copy(
                                lostPetId = id,
                                lostPetReportDateAndTime = dateTimeString,
                                lostPetReportUniqueId = uniqueId,
                                lostPetPhoto = photoUrl
                            )
                        }
                    }
                }.map { it.await() }

                if (reports.isNotEmpty()) {
                    reports.forEach { report ->
                        lostPetRepository.insertLostPetReport(report)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error getting reports from Firestore.", e)
            }
        }
    }

    private suspend fun getImageUrlFromFirestoreStorage(uniqueId: String): String {
        return try {
            storageReference.child("lost_pet_images/$uniqueId").downloadUrl.await().toString()
        } catch (e: Exception) {
            Log.e(TAG, "Error retrieving the image URL", e)
            "/data/user/0/com.example.straymaps/files/no_image_available.png"
        }
    }

    fun getAllLostPetReportsByType() {
        viewModelScope.launch {
            lostPetRepository.loadAllByType().collect { reports ->
                _allLostPetFiledReportsState.value = reports
            }
        }
    }

    fun getAllLostPetReportsByColour() {
        viewModelScope.launch {
            lostPetRepository.loadAllByColour().collect { reports ->
                _allLostPetFiledReportsState.value = reports
            }
        }
    }

    fun getAllLostPetReportsBySex() {
        viewModelScope.launch {
            lostPetRepository.loadAllBySex().collect { reports ->
                _allLostPetFiledReportsState.value = reports
            }
        }
    }

    fun getAllLostPetReportsByDate() {
        viewModelScope.launch {
            lostPetRepository.loadAllByDateAndTime().collect { reports ->
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

    /** Need to implement functionality for specific type of animal search
    val foundAllLostPetOfSpecificType: Flow<List<LostPet>> =
    lostPetRepository.getAllLostPetsOfSpecificType(type = "dog")

    suspend fun updateLostPet(lostPet: LostPet) {
    viewModelScope.launch {
    lostPetRepository.updateLostPetReport(lostPet)
    }
    }

    suspend fun deleteLostPet(lostPet: LostPet) {
    viewModelScope.launch {
    lostPetRepository.deleteLostPetReport(lostPet)
    }
    }
     */

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