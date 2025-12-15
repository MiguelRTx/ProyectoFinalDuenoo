package com.example.projectfinal.utils

import android.content.Context
import android.net.Uri
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream

object FileUtils {
    fun getFileFromUri(context: Context, uri: Uri): File? {
        return try {
            println("DEBUG FileUtils - Iniciando conversión de URI a File")
            println("DEBUG FileUtils - URI: $uri")

            val inputStream: InputStream? = context.contentResolver.openInputStream(uri)
            if (inputStream == null) {
                println(" ERROR FileUtils - No se pudo abrir InputStream desde URI")
                return null
            }

            val fileName = "temp_image_${System.currentTimeMillis()}.jpg"
            val file = File(context.cacheDir, fileName)

            println("DEBUG FileUtils - Archivo temporal: ${file.absolutePath}")

            val outputStream = FileOutputStream(file)
            var bytesCopied = 0L

            inputStream.use { input ->
                outputStream.use { output ->
                    bytesCopied = input.copyTo(output)
                }
            }

            println("DEBUG FileUtils - Bytes copiados: $bytesCopied")
            println("DEBUG FileUtils - Archivo existe: ${file.exists()}")
            println("DEBUG FileUtils - Tamaño final: ${file.length()} bytes")

            if (file.exists() && file.length() > 0) {
                println("SUCCESS FileUtils - Archivo creado exitosamente")
                file
            } else {
                println("ERROR FileUtils - Archivo vacío o no existe")
                null
            }
        } catch (e: Exception) {
            println("ERROR FileUtils - Excepción: ${e.message}")
            e.printStackTrace()
            null
        }
    }
}