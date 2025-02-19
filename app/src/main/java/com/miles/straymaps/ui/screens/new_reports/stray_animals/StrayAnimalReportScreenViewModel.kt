package com.miles.straymaps.ui.screens.new_reports.stray_animals


import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.util.Log
import androidx.annotation.RequiresApi
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asImageBitmap
import androidx.lifecycle.viewModelScope
import com.miles.straymaps.StrayMapsScreen
import com.miles.straymaps.data.firebase.AccountServiceInterface
import com.miles.straymaps.data.repositories.stray_animal.StrayAnimalRepositoryImplementation
import com.miles.straymaps.data.stray_animal.StrayAnimal
import com.miles.straymaps.data.toIsoString
import com.miles.straymaps.ui.screens.StrayMapsViewModel
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.time.LocalDateTime
import java.util.Locale
import java.util.UUID
import javax.inject.Inject


@HiltViewModel
class StrayAnimalReportScreenViewModel @Inject constructor(
    private val strayAnimalRepository: StrayAnimalRepositoryImplementation,
    private val accountService: AccountServiceInterface,
) : StrayMapsViewModel() {

    private val TAG = "StrayAnimalReportScreenViewModel"

    private val _defaultNoImageAvailablePath = MutableStateFlow<String?>(null)
    private val defaultNoImageAvailablePath: StateFlow<String?> = _defaultNoImageAvailablePath

    val reportUploadSnackbarState = MutableStateFlow<Boolean?>(null)

    init {
        viewModelScope.launch {
            val defaultPath = strayAnimalRepository.saveDrawableAsPNG()
            _defaultNoImageAvailablePath.value = defaultPath
        }
    }

    fun initialize(restartApp: (String) -> Unit) {
        launchCatching {
            accountService.currentUser.collect { user ->
                if (user == null) restartApp(StrayMapsScreen.SplashScreen.route)
            }
        }
    }

    // This object represents a "default state" of a Stray Animal Report,
    // Before the user makes modifications
    // Below are functions that the Composable calls to modify each field individually
    var strayAnimalReport by mutableStateOf(
        StrayAnimal(
            null,
            defaultNoImageAvailablePath.value,
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            null,
            false,
            "",
            ""
        )
    )
        private set

    private fun updateStrayAnimalReportPhotoPath(photoPath: String) {
        strayAnimalReport = strayAnimalReport.copy(strayAnimalPhotoPath = photoPath)
    }

    fun updateStrayAnimalReportType(type: String) {
        strayAnimalReport = strayAnimalReport.copy(strayAnimalType = type)
    }

    fun updateStrayAnimalReportColour(colour: String) {
        strayAnimalReport = strayAnimalReport.copy(strayAnimalColour = colour)
    }

    fun updateStrayAnimalReportSex(sex: String) {
        strayAnimalReport = strayAnimalReport.copy(strayAnimalSex = sex)
    }

    fun updateStrayAnimalReportAppearanceDescription(appearance: String) {
        strayAnimalReport = strayAnimalReport.copy(strayAnimalAppearanceDescription = appearance)
    }

    fun updateStrayAnimalReportLocation(location: String) {
        strayAnimalReport = strayAnimalReport.copy(strayAnimalLocationDescription = location)
    }

    fun updateStrayAnimalReportMicrochipId(microchipId: String) {
        strayAnimalReport = strayAnimalReport.copy(strayAnimalMicrochipID = microchipId)
    }

    fun updateStrayAnimalReportContactInformation(info: String) {
        strayAnimalReport = strayAnimalReport.copy(strayAnimalContactInformation = info)
    }

    fun updateStrayAnimalReportAdditionalInformation(info: String) {
        strayAnimalReport = strayAnimalReport.copy(strayAnimalAdditionalInformation = info)
    }

    fun clearErrorState() {
        reportUploadSnackbarState.value = null
    }

    fun resetStrayAnimalReportFields(boolean: Boolean) {
        if (boolean) {
            strayAnimalReport = strayAnimalReport.copy(
                strayAnimalId = null,
                strayAnimalPhotoPath = defaultNoImageAvailablePath.value,
                strayAnimalType = "",
                strayAnimalColour = "",
                strayAnimalSex = "",
                strayAnimalAppearanceDescription = "",
                strayAnimalLocationDescription = "",
                strayAnimalMicrochipID = "",
                strayAnimalContactInformation = "",
                strayAnimalAdditionalInformation = "",
                strayAnimalReportDateAndTime = null,
                strayAnimalIsUploaded = false,
                strayAnimalReportMadeByUserId = "",
                strayAnimalReportUniqueId = ""
            )
        }
    }

    private var completeStrayAnimalReport by mutableStateOf(
        StrayAnimal(
            null,
            "none",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            "",
            null,
            false,
            "",
            ""
        )
    )

    @RequiresApi(Build.VERSION_CODES.O)
    fun saveStrayAnimalReport() {
        viewModelScope.launch {
            try {
                completeStrayAnimalReport = completeStrayAnimalReport.copy(
                    // Adding the local date and time when the repost is being created
                    strayAnimalReportDateAndTime = LocalDateTime.now().toIsoString(),
                    // Modifying microchip ID to be uppercase
                    strayAnimalMicrochipID = strayAnimalReport.strayAnimalMicrochipID?.uppercase(
                        Locale.getDefault()
                    ),
                    // Adding UniqueID so that the report can be matched to its photo
                    strayAnimalReportUniqueId = UUID.randomUUID().toString(),
                    // Adding the User ID to the report so that users can modify their reports
                    strayAnimalReportMadeByUserId = accountService.currentUserId
                )

                // Inserting into RoomDB and CloudFirebase
                strayAnimalRepository.insertStrayAnimalReportIntoRoomDBAndUploadItToCloudFirestore(
                    completeStrayAnimalReport
                )
                reportUploadSnackbarState.value = true
            } catch (e: Exception) {
                Log.e(TAG, "Error saving stray animal report", e)
                reportUploadSnackbarState.value = false
            }
        }
    }


    private val defaultNoImageAvailableBitmap =
        loadImageBitmapFromPath(defaultNoImageAvailablePath.value.toString())

    private val _imagePath = MutableStateFlow(defaultNoImageAvailableBitmap)
    val imagePath: StateFlow<ImageBitmap?> = _imagePath.asStateFlow()

    private val _resizedBitmap = MutableStateFlow<Bitmap?>(null)
    private val resizedBitmap: StateFlow<Bitmap?> = _resizedBitmap.asStateFlow()

    fun getMetaDataThenResizeAndSave(uri: Uri, reqWidth: Int, reqHeight: Int) {
        viewModelScope.launch {

            val (width, height) = withContext(Dispatchers.IO) {
                strayAnimalRepository.getMetaDataOfTheImage(uri)
            }

            if (width > reqWidth || height > reqHeight) {
                val properSizedBitmap = withContext(Dispatchers.IO) {
                    strayAnimalRepository.resizeImageFromUriReturnBitmap(uri, reqWidth, reqHeight)
                }
                _resizedBitmap.value = properSizedBitmap
            } else {
                val properSizedBitmap = withContext(Dispatchers.IO) {
                    strayAnimalRepository.savingImageFromUriAsBitmap(uri)
                }
                _resizedBitmap.value = properSizedBitmap
            }

            resizedBitmap.value?.let { bitmap ->
                val filePath = withContext(Dispatchers.IO) {
                    strayAnimalRepository.saveBitmapToFileAndReturnPath(bitmap)
                }
                Log.d("Debug", "File path: $filePath")
                _imagePath.value = loadImageBitmapFromPath(filePath)
                updateStrayAnimalReportPhotoPath(filePath)
            }
        }
    }

    private fun loadImageBitmapFromPath(filePath: String?): ImageBitmap? {
        if (filePath == null) return null

        return try {
            val bitmap = BitmapFactory.decodeFile(filePath)
            bitmap?.asImageBitmap()
        } catch (e: Exception) {
            null
        }
    }

    // This part provides the initial URI for camera image capture
    private val _capturedImagePath = MutableStateFlow<Uri?>(null)
    val capturedImagePath: StateFlow<Uri?> = _capturedImagePath.asStateFlow()

    fun imageProcessing() {
        val uri = strayAnimalRepository.processCapturedImage()
        _capturedImagePath.value = uri
    }


}