package com.example.projectfinal.ui.theme.home

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectfinal.data.api.RetrofitClient
import com.example.projectfinal.data.model.User
import kotlinx.coroutines.launch

class HomeViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RetrofitClient.getApiService(application)

    var currentUser by mutableStateOf<User?>(null)
    var isLoading by mutableStateOf(false)
    var errorMessage by mutableStateOf<String?>(null)

    init {
        fetchCurrentUser()
    }

    fun refreshUser() {
        fetchCurrentUser()
    }

    fun fetchCurrentUser() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                println("DEBUG HomeViewModel - Obteniendo usuario actual...")
                val response = apiService.getCurrentUser()

                println("DEBUG HomeViewModel - Respuesta recibida: ${response.code()}")

                if (response.isSuccessful && response.body() != null) {
                    val user = response.body()!!
                    println("DEBUG HomeViewModel - Usuario recibido:")
                    println("DEBUG HomeViewModel - ID: ${user.id}")
                    println("DEBUG HomeViewModel - Nombre: ${user.name}")
                    println("DEBUG HomeViewModel - Email: ${user.email}")
                    println("DEBUG HomeViewModel - Foto: ${user.photo}")

                    currentUser = user
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("ERROR HomeViewModel - Error ${response.code()}: $errorBody")
                    errorMessage = "Error al cargar datos del usuario: ${response.code()}"
                }
            } catch (e: Exception) {
                println("ERROR HomeViewModel - Exception: ${e.message}")
                e.printStackTrace()
                errorMessage = "Error de conexión: ${e.message}"
            } finally {
                isLoading = false
            }
        }
    }
}
