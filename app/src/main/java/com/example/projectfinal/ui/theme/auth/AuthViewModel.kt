package com.example.projectfinal.ui.theme.auth

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectfinal.data.api.RetrofitClient
import com.example.projectfinal.data.model.LoginRequest
import com.example.projectfinal.data.model.RegisterRequest
import com.example.projectfinal.data.network.SessionManager
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody.Companion.asRequestBody
import java.io.File

class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val sessionManager = SessionManager(application)
    private val apiService = RetrofitClient.getApiService(application)

    var isLoading by mutableStateOf(false)
        private set

    var errorMessage by mutableStateOf<String?>(null)
        private set

    var loginSuccess by mutableStateOf(false)
        private set

    var registerSuccess by mutableStateOf(false)
        private set

    fun isUserLoggedIn(): Boolean {
        return !sessionManager.getToken().isNullOrEmpty()
    }

    fun login(email: String, pass: String) {
        if (email.isBlank() || pass.isBlank()) {
            errorMessage = "Por favor ingresa todos los datos"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                val response = apiService.login(LoginRequest(email, pass))
                if (response.isSuccessful && response.body() != null) {
                    val token = response.body()!!.getAuthToken()
                    if (token != null) {
                        sessionManager.saveToken(token)
                        loginSuccess = true
                        println("DEBUG login - Token guardado exitosamente")
                    } else {
                        println("ERROR login - Respuesta exitosa pero sin token: ${response.body()}")
                        errorMessage = "Error: El servidor no devolvió el token."
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("ERROR login - Error ${response.code()}: $errorBody")
                    errorMessage = when (response.code()) {
                        401 -> "Correo o contraseña incorrectos"
                        404 -> "Usuario no encontrado"
                        422 -> "Datos de login inválidos"
                        500 -> "Error del servidor, intenta más tarde"
                        else -> "Error de conexión (${response.code()})"
                    }
                }
            } catch (e: Exception) {
                errorMessage = "Error de conexión: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun register(name: String, email: String, pass: String, photoFile: File? = null) {
        if (name.isBlank() || email.isBlank() || pass.isBlank()) {
            errorMessage = "Por favor completa todos los campos requeridos"
            return
        }

        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                println("DEBUG register - Iniciando registro de $email")

                val registerResponse = apiService.register(RegisterRequest(name, email, pass))

                if (registerResponse.isSuccessful) {
                    println("DEBUG register - Registro exitoso para $email")

                    if (photoFile != null) {
                        try {
                            println("DEBUG register - Subiendo foto de perfil...")


                            val loginResponse = apiService.login(LoginRequest(email, pass))
                            if (loginResponse.isSuccessful && loginResponse.body()?.getAuthToken() != null) {
                                val tempToken = loginResponse.body()!!.getAuthToken()!!
                                sessionManager.saveToken(tempToken)

                                val requestFile = photoFile.asRequestBody("image/*".toMediaTypeOrNull())
                                val body = MultipartBody.Part.createFormData("photo", photoFile.name, requestFile)
                                val uploadResponse = apiService.uploadOwnerPhoto(body)

                                if (uploadResponse.isSuccessful) {
                                    println("DEBUG register - Foto subida exitosamente")
                                } else {
                                    println("WARNING register - Error subiendo foto: ${uploadResponse.code()}")
                                }

                                sessionManager.clearSession()
                            } else {
                                println("ERROR register - No se pudo obtener token para subir foto")
                            }
                        } catch (e: Exception) {
                            println("ERROR register - Error subiendo foto: ${e.message}")
                        }
                    }
                    registerSuccess = true


                } else {
                    val errorBody = registerResponse.errorBody()?.string()
                    println("ERROR register - Error ${registerResponse.code()}: $errorBody")
                    errorMessage = when (registerResponse.code()) {
                        409 -> "El correo electrónico ya está registrado"
                        400 -> "Datos de registro inválidos"
                        422 -> "El formato del correo o contraseña no es válido"
                        else -> "Error en el registro. Código: ${registerResponse.code()}"
                    }
                }
            } catch (e: Exception) {
                println("ERROR register - Exception: ${e.message}")
                e.printStackTrace()
                errorMessage = "Error de conexión: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }

    fun clearError() {
        errorMessage = null
    }

    fun resetStates() {
        errorMessage = null
        loginSuccess = false
        registerSuccess = false
    }


    fun logout() {
        println("DEBUG logout - Cerrando sesión y limpiando token")
        sessionManager.clearSession()
        resetStates()
    }
}