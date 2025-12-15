package com.example.projectfinal.ui.theme.pets

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectfinal.data.api.RetrofitClient
import com.example.projectfinal.data.model.Pet
import com.example.projectfinal.data.model.PetRequest
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class PetsViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RetrofitClient.getApiService(application)

    var pets by mutableStateOf<List<Pet>>(emptyList())
        private set

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    init {
        fetchPets()
    }

    fun fetchPets() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                println("DEBUG fetchPets - Obteniendo lista de mascotas...")
                val response = apiService.getPets()
                if (response.isSuccessful && response.body() != null) {
                    println("DEBUG fetchPets - Respuesta JSON bruta: ${response.body()}")
                    pets = response.body()!!
                    println("DEBUG fetchPets - Mascotas recibidas: ${pets.size}")
                    pets.forEachIndexed { index, pet ->
                        println("DEBUG Pet $index: id=${pet.id}, name='${pet.name}', type='${pet.type}', notes='${pet.notes}', photo='${pet.photo}'")
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("ERROR fetchPets - Error ${response.code()}: $errorBody")
                    errorMessage = "Error al cargar mascotas: ${response.code()}"
                }
            } catch (e: Exception) {
                println("ERROR fetchPets - Exception: ${e.message}")
                e.printStackTrace()
                errorMessage = "Error de conexión: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun addPet(name: String, type: String, notes: String, photoFile: File?, onSuccess: () -> Unit) {
        if (name.isBlank() || type.isBlank()) {
            errorMessage = "El nombre y el tipo son obligatorios"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {

                val notesValue = if (notes.isBlank()) null else notes
                val request = PetRequest(name, type, notesValue)

                println("DEBUG addPet - Enviando: name='$name', type='$type', notes=$notesValue")
                println("DEBUG addPet - Request JSON: ${com.google.gson.Gson().toJson(request)}")

                val response = apiService.createPet(request)

                if (response.isSuccessful && response.body() != null) {
                    val newPet = response.body()!!
                    println("DEBUG addPet - Mascota creada: id=${newPet.id}, type=${newPet.type}")
                    if (photoFile != null && photoFile.exists()) {
                        println("DEBUG addPet - Subiendo foto: ${photoFile.absolutePath}, size=${photoFile.length()}")
                        uploadPhoto(newPet.id, photoFile)
                    } else if (photoFile != null) {
                        println("ERROR addPet - Archivo de foto no existe: ${photoFile.absolutePath}")
                    }

                    fetchPets()
                    onSuccess()
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("ERROR addPet - Respuesta: ${response.code()}, body: $errorBody")
                    errorMessage = "No se pudo crear la mascota: ${response.code()}"
                }
            } catch (e: Exception) {
                println("ERROR addPet - Exception: ${e.message}")
                e.printStackTrace()
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun updatePet(id: Int, name: String, type: String, notes: String, photoFile: File?, onSuccess: () -> Unit) {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val notesValue = if (notes.isBlank()) null else notes
                val request = PetRequest(name, type, notesValue)

                println("DEBUG updatePet - Actualizando mascota id=$id: name='$name', type='$type', notes=$notesValue")
                println("DEBUG updatePet - Request JSON: ${com.google.gson.Gson().toJson(request)}")

                val response = apiService.updatePet(id, request)

                if (response.isSuccessful) {
                    println("DEBUG updatePet - Mascota actualizada exitosamente")
                    if (photoFile != null && photoFile.exists()) {
                        println("DEBUG updatePet - Subiendo nueva foto")
                        uploadPhoto(id, photoFile)
                    } else if (photoFile != null) {
                        println("ERROR updatePet - Archivo de foto no existe: ${photoFile.absolutePath}")
                    }
                    fetchPets()
                    onSuccess()
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("ERROR updatePet - Respuesta: ${response.code()}, body: $errorBody")
                    errorMessage = "No se pudo actualizar: ${response.code()}"
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun deletePet(id: Int) {
        viewModelScope.launch {
            isLoading = true
            try {
                val response = apiService.deletePet(id)
                if (response.isSuccessful) {
                    fetchPets()
                } else {
                    errorMessage = "No se pudo eliminar"
                }
            } catch (e: Exception) {
                errorMessage = "Error: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    private suspend fun uploadPhoto(petId: Int, file: File) {
        try {
            if (!file.exists()) {
                println("ERROR uploadPhoto - El archivo no existe: ${file.absolutePath}")
                errorMessage = "El archivo de foto no existe"
                return
            }

            val fileSize = file.length()
            println("DEBUG uploadPhoto - Iniciando subida para mascota $petId")
            println("DEBUG uploadPhoto - Archivo: ${file.name}")
            println("DEBUG uploadPhoto - Ruta: ${file.absolutePath}")
            println("DEBUG uploadPhoto - Tamaño: $fileSize bytes (${fileSize / 1024}KB)")

            if (!file.canRead()) {
                println("ERROR uploadPhoto - No se puede leer el archivo")
                errorMessage = "No se puede acceder al archivo de foto"
                return
            }
            val mediaType = when {
                file.name.lowercase().endsWith(".jpg") || file.name.lowercase().endsWith(".jpeg") -> "image/jpeg"
                file.name.lowercase().endsWith(".png") -> "image/png"
                else -> "image/jpeg" // Default
            }

            println("DEBUG uploadPhoto - Usando tipo MIME: $mediaType")

            val requestFile = file.asRequestBody(mediaType.toMediaTypeOrNull())
            val body = MultipartBody.Part.createFormData("photo", file.name, requestFile)

            println("DEBUG uploadPhoto - Enviando petición POST...")
            val response = apiService.uploadPetPhoto(petId, body)

            println("DEBUG uploadPhoto - Respuesta recibida: ${response.code()}")

            if (response.isSuccessful) {
                println("SUCCESS uploadPhoto - Foto subida exitosamente para mascota $petId")
                val responseBody = response.body()
                if (responseBody != null) {
                    println("DEBUG uploadPhoto - Respuesta del servidor: $responseBody")
                }
            } else {
                val errorBody = response.errorBody()?.string()
                println("ERROR uploadPhoto - Error HTTP ${response.code()}")
                println("ERROR uploadPhoto - Mensaje de error: $errorBody")
                errorMessage = "Error al subir foto (${response.code()}): ${errorBody ?: "Error desconocido"}"
            }
        } catch (e: Exception) {
            println("ERROR uploadPhoto - Excepción capturada: ${e.javaClass.simpleName}")
            println("ERROR uploadPhoto - Mensaje: ${e.message}")
            e.printStackTrace()
            errorMessage = "Error de conexión al subir foto: ${e.message}"
        }
    }

    fun getPetById(id: Int): Pet? {
        return pets.find { it.id == id }
    }

    fun clearError() {
        errorMessage = null
    }
}