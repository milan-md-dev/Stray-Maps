package com.miles.straymaps.ui.screens.new_reports.lost_pets


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
import com.miles.straymaps.data.lost_pet.LostPet
import com.miles.straymaps.data.repositories.lost_pet.LostPetRepositoryImplementation
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
open class LostPetReportScreenViewModel @Inject constructor(
    private val lostPetRepository: LostPetRepositoryImplementation,
    private val accountService: AccountServiceInterface
) : StrayMapsViewModel() {

    private val TAG = "LostPetReportScreenViewModel"

    private val _defaultNoImageAvailablePath = MutableStateFlow<String?>(null)
    private val defaultNoImageAvailablePath: StateFlow<String?> = _defaultNoImageAvailablePath

    val reportUploadSnackbarState = MutableStateFlow<Boolean?>(null)

    init {
        viewModelScope.launch {
            val defaultPath = lostPetRepository.saveDrawableAsPNG()
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

    // This object represents a "default state" of a Lost Pet Report,
    // Before the user makes modifications
    // Below are functions that the Composable calls to modify each field individually
    var lostPetReport by mutableStateOf(
        LostPet(
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
            "",
            null,
            false,
            "",
            ""
        )
    )
        private set

    private fun updateLostPetReportPhotoPath(photoPath: String) {
        lostPetReport = lostPetReport.copy(lostPetPhoto = photoPath)
    }

    fun updateLostPetReportName(name: String) {
        lostPetReport = lostPetReport.copy(lostPetName = name)
    }

    fun updateLostPetReportType(type: String) {
        lostPetReport = lostPetReport.copy(lostPetType = type)
    }

    fun updateLostPetReportColour(colour: String) {
        lostPetReport = lostPetReport.copy(lostPetColour = colour)
    }

    fun updateLostPetReportAppearanceDescription(appearance: String) {
        lostPetReport = lostPetReport.copy(lostPetAppearanceDescription = appearance)
    }

    fun updateLostPetReportSex(sex: String) {
        lostPetReport = lostPetReport.copy(lostPetSex = sex)
    }

    fun updateLostPetReportLocation(location: String) {
        lostPetReport = lostPetReport.copy(lostPetLastKnownLocation = location)
    }

    fun updateLostPetReportMicrochipId(microchipId: String) {
        lostPetReport = lostPetReport.copy(lostPetMicrochipId = microchipId)
    }

    fun updateLostPetReportContactInformation(info: String) {
        lostPetReport = lostPetReport.copy(lostPetContactInformation = info)
    }

    fun updateLostPetReportAdditionalInformation(info: String) {
        lostPetReport = lostPetReport.copy(lostPetAdditionalInformation = info)
    }

    fun clearErrorState() {
        reportUploadSnackbarState.value = null
    }

    fun resetLostPetReportFields(boolean: Boolean) {
        if (boolean) {
            lostPetReport = lostPetReport.copy(
                lostPetId = null,
                lostPetPhoto = defaultNoImageAvailablePath.value,
                lostPetType = "",
                lostPetName = "",
                lostPetColour = "",
                lostPetSex = "",
                lostPetAppearanceDescription = "",
                lostPetLastKnownLocation = "",
                lostPetMicrochipId = "",
                lostPetContactInformation = "",
                lostPetAdditionalInformation = "",
                lostPetReportDateAndTime = null,
                lostPetIsUploaded = false,
                lostPetReportMadeByUserId = "",
                lostPetReportUniqueId = ""
            )
        }
    }

    private var completeLostPetReport by mutableStateOf(
        LostPet(
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
            "",
            null,
            false,
            "",
            ""
        )
    )


    @RequiresApi(Build.VERSION_CODES.O)
    fun saveLostPetReport() {
        viewModelScope.launch {
            try {
                completeLostPetReport = lostPetReport.copy(
                    // Adding the local date and time when the repost is being created
                    lostPetReportDateAndTime = LocalDateTime.now().toIsoString(),
                    // Modifying microchip ID to be uppercase
                    lostPetMicrochipId = lostPetReport.lostPetMicrochipId.uppercase(Locale.getDefault()),
                    // Adding UniqueID so that the report can be matched to its photo
                    lostPetReportUniqueId = UUID.randomUUID().toString(),
                    // Adding the User ID to the report so that users can modify their reports
                    lostPetReportMadeByUserId = accountService.currentUserId
                )

                // Inserting into RoomDB and CloudFirebase
                lostPetRepository.insertLostPetReportIntoRoomDBAndUploadItToCloudFirestore(
                    completeLostPetReport
                )
                reportUploadSnackbarState.value = true
            } catch (e: Exception) {
                Log.e(TAG, "Error saving lost pet report", e)
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
                lostPetRepository.getMetaDataOfTheImage(uri)
            }

            if (width > reqWidth || height > reqHeight) {
                val properSizedBitmap = withContext(Dispatchers.IO) {
                    lostPetRepository.resizeImageFromUriReturnBitmap(uri, reqWidth, reqHeight)
                }
                _resizedBitmap.value = properSizedBitmap
            } else {
                val properSizedBitmap = withContext(Dispatchers.IO) {
                    lostPetRepository.savingImageFromUriAsBitmap(uri)
                }
                _resizedBitmap.value = properSizedBitmap
            }

            resizedBitmap.value?.let { bitmap ->
                val filePath = withContext(Dispatchers.IO) {
                    lostPetRepository.saveBitmapToFileAndReturnPath(bitmap)
                }
                Log.d("Debug", "File path: $filePath")
                _imagePath.value = loadImageBitmapFromPath(filePath)
                updateLostPetReportPhotoPath(filePath)
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
        val uri = lostPetRepository.processCapturedImage()
        _capturedImagePath.value = uri
    }


}