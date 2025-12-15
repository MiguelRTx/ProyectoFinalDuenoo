package com.example.projectfinal.utils

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import androidx.core.content.FileProvider
import com.example.projectfinal.R
import java.io.File
import java.io.FileOutputStream

object SampleImageHelper {

    fun createSampleImageFile(context: Context, fileName: String): File? {
        return try {
            val bitmap = createSampleBitmap()
            val file = File(context.cacheDir, fileName)
            val outputStream = FileOutputStream(file)

            bitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream)
            outputStream.flush()
            outputStream.close()

            file
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    fun createSampleImageUri(context: Context, fileName: String): Uri? {
        return try {
            val file = createSampleImageFile(context, fileName)
            file?.let {
                FileProvider.getUriForFile(
                    context,
                    "${context.packageName}.fileprovider",
                    it
                )
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun createSampleBitmap(): Bitmap {
        val size = 200
        val bitmap = Bitmap.createBitmap(size, size, Bitmap.Config.ARGB_8888)

        for (x in 0 until size) {
            for (y in 0 until size) {
                val color = when {
                    (x + y) % 40 < 20 -> 0xFF4CAF50.toInt()
                    else -> 0xFF2196F3.toInt()
                }
                bitmap.setPixel(x, y, color)
            }
        }

        return bitmap
    }


}
