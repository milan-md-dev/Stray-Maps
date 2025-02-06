package com.miles.straymaps.misc


import android.content.Context
import android.graphics.Bitmap
import android.graphics.drawable.BitmapDrawable
import android.net.Uri
import android.util.Log
import androidx.core.content.ContextCompat
import androidx.core.content.FileProvider
import com.miles.straymaps.R
import java.io.File
import java.io.FileOutputStream

class ComposeFileProvider : FileProvider(
    R.xml.path_provider
) {

    companion object {
        fun getImageUri(context: Context): Uri {

            val directory = File(context.cacheDir, "images")
            directory.mkdirs()

            val file = File.createTempFile(
                "selected_image_",
                ".jpeg",
                directory
            )

            val authority = context.packageName + ".fileprovider"

            return getUriForFile(
                context,
                authority,
                file
            )
        }
    }
}

object DefaultImageProvider {
    private var defaultImagePath: String? = null

    fun getDefaultImagePath(context: Context): String {
        if (defaultImagePath.isNullOrEmpty()) {
            defaultImagePath = createDefaultImage(context)
        }
        return defaultImagePath!!
    }

    private fun createDefaultImage(context: Context): String {
        val filename = "no_image_available.png"
        val file = File(context.filesDir, filename)
        return if (file.exists()) {
            file.absolutePath
        } else {
            try {
                val drawable = ContextCompat.getDrawable(context, R.drawable.noimageavailable)
                val bitmap = (drawable as BitmapDrawable).bitmap
                FileOutputStream(file).use {
                    bitmap.compress(Bitmap.CompressFormat.PNG, 50, it)
                }
                file.absolutePath
            } catch (e: Exception) {
                Log.e("saveDrawableAaPNG", e.toString())
                "Unknown error has occurred."
            }
        }
    }
}