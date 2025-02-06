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

    private val cloudFirebaseDatabase = Firebase.firestore

    private val TAG = "cloudFirebaseDatabase StrayAnimal upload/download status"

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

    override suspend fun upsertStrayAnimal(strayAnimal: StrayAnimal) {
        withContext(ioDispatcher) {
            strayAnimalDao.upsert(strayAnimal)
            if (!strayAnimal.strayAnimalIsUploaded) {
                val strayAnimalReportFromRoomDB = loadAllNotUploadedReports()
                strayAnimalReportFromRoomDB.forEach {
                    try {
                        val uploadSuccess = uploadReportToCloudFirebaseDatabase(it)
                        if (uploadSuccess) {
                            strayAnimalDao.upsert(strayAnimal = it.copy(strayAnimalIsUploaded = true))
                            Log.d(
                                TAG,
                                "Successfully changed strayAnimal RoomDB->Cloud upload state."
                            )
                        } else {
                            Log.w(TAG, "Error trying to change report upload status in RoomDB.")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error trying to upload reports from RoomDB to the Cloud", e)
                    }
                }
            }
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


    //Function that processes the captured image
    fun processCapturedImage(): Uri {
        return ComposeFileProvider.getImageUri(context)
    }

    //This function saves a drawable as a PNG,
    //I use it to save Drawable "No image available" as a PNG, and then use its path as the
    //default path for Stray Animal reports where there is no photo provided
    fun saveDrawableAsPNG(
    ): String {
        return DefaultImageProvider.getDefaultImagePath(context)
    }

    //Getting the image from Gallery and making modifications to it
    //This function gets the image's meta data
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

    //This function resizes the image
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

    //This function saves the image without resizing
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

    //Function to save the resized Bitmap to a file in internal storage and get the file path
    fun saveBitmapToFileAndReturnPath(bitmap: Bitmap): String {
        val filename = "Stray_maps_report_image_${System.currentTimeMillis()}.png"
        val file = File(context.filesDir, filename)
        FileOutputStream(file).use {
            bitmap.compress(Bitmap.CompressFormat.PNG, 100, it)
        }
        return file.absolutePath
    }
}