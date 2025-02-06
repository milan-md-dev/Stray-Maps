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
import com.miles.straymaps.misc.Resource
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
import javax.inject.Inject


@HiltViewModel
class LostPetReportScreenViewModel @Inject constructor(
    private val lostPetRepository: LostPetRepositoryImplementation,
    private val accountService: AccountServiceInterface
) : StrayMapsViewModel() {

    private val TAG = "LostPetReportScreenViewModel"

    private val _defaultNoImageAvailablePath = MutableStateFlow<String?>(null)
    private val defaultNoImageAvailablePath: StateFlow<String?> = _defaultNoImageAvailablePath

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
            false
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

    private fun updateLostPetReportDateAndTime(value: LocalDateTime) {
        lostPetReport = lostPetReport.copy(lostPetReportDateAndTime = value)
    }

    private val _lostPetReportUpsertEventSnackbarMessage = MutableStateFlow<Boolean?>(null)
    val lostPetReportUpsertEventSnackbarMessage: StateFlow<Boolean?> =
        _lostPetReportUpsertEventSnackbarMessage.asStateFlow()

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
            false
        )
    )


    @RequiresApi(Build.VERSION_CODES.O)
    fun saveLostPetReport() {
        viewModelScope.launch {
            val result = try {
                //Adding the local date and time when the repost is being created
                val now = LocalDateTime.now()
                updateLostPetReportDateAndTime(now)

                completeLostPetReport = lostPetReport.copy(
                    lostPetMicrochipId = lostPetReport.lostPetMicrochipId.uppercase(Locale.getDefault())
                )

                lostPetRepository.upsertLostPet(completeLostPetReport)
                Resource.Success("Report filed successfully.")
            } catch (e: Exception) {
                Log.e(TAG, "Error saving lost pet report", e)
                Resource.Error("Error saving Lost Pet report.")
            }
            when (result) {
                is Resource.Success -> {
                    _lostPetReportUpsertEventSnackbarMessage.value = true
                }

                is Resource.Error -> {
                    _lostPetReportUpsertEventSnackbarMessage.value = false
                }

                else -> {}
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

    //This part provides the initial URI for camera image capture
    private val _capturedImagePath = MutableStateFlow<Uri?>(null)
    val capturedImagePath: StateFlow<Uri?> = _capturedImagePath.asStateFlow()

    fun imageProcessing() {
        val uri = lostPetRepository.processCapturedImage()
        _capturedImagePath.value = uri
    }


}