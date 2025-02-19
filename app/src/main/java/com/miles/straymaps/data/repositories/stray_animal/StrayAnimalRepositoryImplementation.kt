package com.miles.straymaps.data.repositories.stray_animal


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Log
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import com.google.firebase.storage.storage
import com.miles.straymaps.data.stray_animal.StrayAnimal
import com.miles.straymaps.data.stray_animal.StrayAnimalDao
import com.miles.straymaps.misc.ComposeFileProvider
import com.miles.straymaps.misc.DefaultImageProvider
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
open class StrayAnimalRepositoryImplementation @Inject constructor(
    private val strayAnimalDao: StrayAnimalDao,
    private val ioDispatcher: CoroutineDispatcher,
    private val context: Context
) : StrayAnimalRepositoryInterface {

    private val storage = Firebase.storage

    private val storageReference = storage.reference

    private val cloudFirebaseDatabase = Firebase.firestore

    private val TAG = "Stray Animal Repository Implementation."

    override fun loadAllStrayAnimalReports(): Flow<List<StrayAnimal>> = strayAnimalDao.getAll()

    override fun getStrayAnimalByMicrochipId(id: String): StrayAnimal? =
        strayAnimalDao.getStrayAnimalByMicrochipId(id)

    override fun getAllStrayAnimalOfSpecificType(type: String): Flow<List<StrayAnimal>> =
        strayAnimalDao.getStrayAnimalByType(type)

    override fun loadAllByType(): Flow<List<StrayAnimal>> = strayAnimalDao.loadAllByType()

    override fun loadAllByColour(): Flow<List<StrayAnimal>> = strayAnimalDao.loadAllByColour()

    override fun loadAllBySex(): Flow<List<StrayAnimal>> = strayAnimalDao.loadAllBySex()

    override fun loadAllByDateAndTime(): Flow<List<StrayAnimal>> =
        strayAnimalDao.loadAllByDateAndTime()

    override fun loadAllNotUploadedReports(): List<StrayAnimal> =
        strayAnimalDao.loadAllNotUploadedReports()

    override suspend fun updateLoadState(uniqueId: String, isUploaded: Boolean) {
        strayAnimalDao.updateUploadState(uniqueId, isUploaded)
    }

    override suspend fun insertStrayAnimalReport(strayAnimal: StrayAnimal) {
        withContext(ioDispatcher) {
            strayAnimalDao.insertStrayAnimalReport(strayAnimal)
        }
    }

    suspend fun insertStrayAnimalReportIntoRoomDBAndUploadItToCloudFirestore(strayAnimal: StrayAnimal) {
        withContext(ioDispatcher) {
            try {
                // Insert the stray animal report into RoomDB
                strayAnimalDao.insertStrayAnimalReport(strayAnimal)

                // Check for reports that have not been uploaded to Firestore
                val strayAnimalReportsFromDB = loadAllNotUploadedReports()

                strayAnimalReportsFromDB.forEach { report ->
                    try {
                        val photoUploaded = uploadReportPhotoToFirebaseStorage(report)

                        if (photoUploaded) {
                            val photoUrl =
                                storageReference.child("stray_animal_images/${report.strayAnimalReportUniqueId}").downloadUrl.await()
                                    .toString()

                            val updatedStrayAnimalReport =
                                report.copy(strayAnimalPhotoPath = photoUrl)

                            val uploadSuccess =
                                uploadReportToCloudFirebaseDatabase(updatedStrayAnimalReport)
                            if (uploadSuccess) {
                                // Update the report in RoomDB to mark it as uploaded to Firestore
                                strayAnimalDao.updateUploadState(
                                    report.strayAnimalReportUniqueId,
                                    true
                                )
                                Log.d(
                                    TAG,
                                    "Successfully changed strayAnimal RoomDB->Cloud upload state."
                                )
                            } else {
                                Log.w(TAG, "Error trying to change report upload state in RoomDB.")
                            }
                        } else {
                            Log.w(TAG, "Photo upload failed. Report not uploaded to Firestore.")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error trying to upload reports from RoomDb to the Cloud.", e)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "Error inserting stray animal reports.", e)
            }
        }
    }

    override suspend fun updateStrayAnimal(strayAnimal: StrayAnimal) {
        withContext(ioDispatcher) {
            strayAnimalDao.updateStrayAnimalReport(strayAnimal)
        }
    }

    override suspend fun deleteStrayAnimal(strayAnimal: StrayAnimal) {
        withContext(ioDispatcher) {
            strayAnimalDao.delete(strayAnimal)
        }
    }


    private suspend fun uploadReportToCloudFirebaseDatabase(strayAnimal: StrayAnimal): Boolean {
        return withContext(ioDispatcher) {
            try {
                cloudFirebaseDatabase.collection("stray_animal_reports")
                    .add(strayAnimal).await()
                Log.d(TAG, "StrayAnimal report added to Cloud Firebase.")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error adding the StrayAnimal report", e)
                false
            }
        }
    }

    private suspend fun uploadReportPhotoToFirebaseStorage(strayAnimal: StrayAnimal): Boolean {
        return withContext(ioDispatcher) {
            try {
                val photoPath = strayAnimal.strayAnimalPhotoPath

                if (photoPath.isNullOrEmpty()) {
                    Log.e(TAG, "Stray Animal photo path is null or empty.")
                    return@withContext false
                }

                Log.d(TAG, "Attempting to upload file from path: $photoPath")

                val file = File(photoPath)
                if (!file.exists()) {
                    Log.e(TAG, "Stray Animal photo file does not exist: $photoPath")
                    return@withContext false
                }

                val fileUri = Uri.fromFile(file)
                val photoReference =
                    storageReference.child("stray_animal_images/${strayAnimal.strayAnimalReportUniqueId}")

                // Uploading the file to Storage and waiting for completion
                photoReference.putFile(fileUri).await()

                Log.d(TAG, "StrayAnimal photo added to Storage.")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error adding the Stray Animal photo to Storage.", e)
                false
            }
        }
    }


    // Function that processes the captured image
    fun processCapturedImage(): Uri {
        return ComposeFileProvider.getImageUri(context)
    }

    // This function saves a drawable as a PNG,
    // I use it to save Drawable "No image available" as a PNG, and then use its path as the
    // default path for Stray Animal reports where there is no photo provided
    fun saveDrawableAsPNG(): String {
        return DefaultImageProvider.getDefaultImagePath(context)
    }

    // Getting the image from Gallery and making modifications to it
    // This function gets the image's meta data
    fun getMetaDataOfTheImage(uri: Uri): Pair<Int, Int> {
        Log.d("getMetaDataOfTheGalleryImage", "URI: $uri")
        return BitmapFactory.Options().run {
            inJustDecodeBounds = true
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, this)
            }
            Pair(outWidth, outHeight)
        }
    }

    // This function resizes the image
    fun resizeImageFromUriReturnBitmap(uri: Uri, reqWidth: Int, reqHeight: Int): Bitmap? {
        val options = BitmapFactory.Options().apply {
            inJustDecodeBounds = true
            context.contentResolver.openInputStream(uri)?.use {
                BitmapFactory.decodeStream(it, null, this)
            }
            inSampleSize = calculateInSampleSize(this, reqWidth, reqHeight)
            inJustDecodeBounds = false
        }
        return context.contentResolver.openInputStream(uri)?.use {
            BitmapFactory.decodeStream(it, null, options)
        }
    }

    private fun calculateInSampleSize(
        options: BitmapFactory.Options,
        reqWidth: Int,
        reqHeight: Int
    ): Int {
        val (height: Int, width: Int) = options.run { outHeight to outWidth }
        var inSampleSize = 1

        if (height > reqHeight || width > reqWidth) {

            val halfHeight: Int = height / 2
            val halfWidth: Int = width / 2

            while (halfHeight / inSampleSize >= reqHeight && halfWidth / inSampleSize >= reqWidth) {
                inSampleSize *= 2
            }
        }
        return inSampleSize
    }

    // This function saves the image without resizing
    fun savingImageFromUriAsBitmap(uri: Uri): Bitmap? {
        val contentResolver = context.contentResolver
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val source = ImageDecoder.createSource(contentResolver, uri)
                ImageDecoder.decodeBitmap(source)
            } catch (e: Exception) {
                Log.e("SavingImageFromGallery", "Could not save image.", e)
                null
            }
        } else {
            try {
                contentResolver.openInputStream(uri)?.use { inputSteam ->
                    BitmapFactory.decodeStream(inputSteam)
                }
            } catch (e: Exception) {
                Log.e("SavingImageFromGallery", "Could not save image", e)
                null
            }
        }
    }

    // Function to save the resized Bitmap to a file in internal storage and get the file path
    fun saveBitmapToFileAndReturnPath(bitmap: Bitmap): String {
        val filename = "Stray_maps_report_image_${System.currentTimeMillis()}.png"
        val file = File(context.filesDir, filename)
        FileOutputStream(file).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        return file.absolutePath
    }
}