package com.miles.straymaps.data.repositories.lost_pet


import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.graphics.ImageDecoder
import android.net.Uri
import android.os.Build
import android.util.Log
import com.miles.straymaps.data.lost_pet.LostPet
import com.miles.straymaps.data.lost_pet.LostPetDao
import com.miles.straymaps.misc.ComposeFileProvider
import com.miles.straymaps.misc.DefaultImageProvider
import com.google.firebase.Firebase
import com.google.firebase.firestore.firestore
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.tasks.await
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import javax.inject.Inject
import javax.inject.Singleton


@Singleton
class LostPetRepositoryImplementation @Inject constructor(
    private val lostPetDao: LostPetDao,
    private val ioDispatcher: CoroutineDispatcher,
    private val context: Context
) : LostPetRepositoryInterface {

    private val cloudFirebaseDatabase = Firebase.firestore

    private val TAG = "cloudFirebaseDatabase LostPet upload/download status"

    override fun loadAllLostPetReports(): Flow<List<LostPet>> = lostPetDao.getAll()

    override fun getLostPetsByMicrochipId(id: String): LostPet =
        lostPetDao.getLostPetByMicrochipId(id)

    override fun getLostPetsByName(name: String): Flow<List<LostPet>> =
        lostPetDao.getLostPetsByName(name)

    override fun getAllLostPetsOfSpecificType(type: String): Flow<List<LostPet>> =
        lostPetDao.getLostPetByType(type)

    override fun loadAllByType(): Flow<List<LostPet>> = lostPetDao.loadAllLostPetsByType()

    override fun loadAllByColour(): Flow<List<LostPet>> = lostPetDao.loadAllLostPetsByColours()

    override fun loadAllBySex(): Flow<List<LostPet>> = lostPetDao.loadAllLostPetsBySex()

    override fun loadAllByDateAndTime(): Flow<List<LostPet>> = lostPetDao.loadAllByDateAndTime()

    override fun loadAllNotUploadedReports(): List<LostPet> = lostPetDao.loadAllNotUploadedReports()

    override suspend fun upsertLostPet(lostPet: LostPet) {
        withContext(ioDispatcher) {
            lostPetDao.upsertLostPet(lostPet)
            if (!lostPet.lostPetIsUploaded) {
                val lostPetReportFromRoomDB = loadAllNotUploadedReports()
                lostPetReportFromRoomDB.forEach {
                    try {
                        val uploadSuccess = uploadReportToCloudFirebaseDatabase(it)
                        if (uploadSuccess) {
                            lostPetDao.upsertLostPet(lostPet = it.copy(lostPetIsUploaded = true))
                            Log.d(TAG, "Successfully changed lostPet RoomDB->Cloud upload state.")
                        } else {
                            Log.w(TAG, "Error trying to change report upload status in RoomDB.")
                        }
                    } catch (e: Exception) {
                        Log.e(TAG, "Error trying to upload reports from RoomDB to the Cloud.", e)
                    }
                }
            }
        }
    }

    override suspend fun deleteLostPet(lostPet: LostPet) {
        withContext(ioDispatcher) {
            lostPetDao.deleteLostPet(lostPet)
        }
    }

    private suspend fun uploadReportToCloudFirebaseDatabase(lostPet: LostPet): Boolean {
        return withContext(ioDispatcher) {
            try {
                cloudFirebaseDatabase.collection("lost_pet_reports")
                    .add(lostPet).await()
                Log.d(TAG, "LostPet report added to Cloud Firebase")
                true
            } catch (e: Exception) {
                Log.e(TAG, "Error adding the LostPet report.", e)
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
    fun saveDrawableAsPNG(): String {
        return DefaultImageProvider.getDefaultImagePath(context)
    }

    //Getting the image from Gallery and making modifications to it
    //This function takes the image's meta data
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