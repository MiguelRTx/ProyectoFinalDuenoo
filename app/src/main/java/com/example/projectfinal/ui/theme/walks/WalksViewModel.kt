package com.example.projectfinal.ui.theme.walks

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectfinal.data.api.RetrofitClient
import com.example.projectfinal.data.model.Walk
import kotlinx.coroutines.launch

class WalksViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RetrofitClient.getApiService(application)

    var walks by mutableStateOf<List<Walk>>(emptyList())
        private set
    var isLoading by mutableStateOf(false)
        private set
    var errorMessage by mutableStateOf<String?>(null)
        private set

    fun fetchWalks() {
        viewModelScope.launch {
            isLoading = true
            errorMessage = null
            try {
                println("DEBUG fetchWalks - Obteniendo paseos del usuario logueado...")
                println("DEBUG fetchWalks - Enviando GET /walks con token de autorización")
                val response = apiService.getWalks()

                println("DEBUG fetchWalks - Respuesta recibida: ${response.code()}")

                if (response.isSuccessful && response.body() != null) {
                    val receivedWalks = response.body()!!
                    walks = receivedWalks
                    println("DEBUG fetchWalks - Paseos específicos del usuario: ${walks.size}")

                    if (walks.isEmpty()) {
                        println("DEBUG fetchWalks - Esta cuenta no tiene paseos creados")
                    } else {
                        walks.forEachIndexed { index, walk ->
                            println("DEBUG Walk $index: id=${walk.id}, status=${walk.status}, petId=${walk.petId}, petName=${walk.petName}")
                        }
                    }
                } else {
                    val errorBody = response.errorBody()?.string()
                    println("ERROR fetchWalks - Error ${response.code()}: $errorBody")
                    errorMessage = "Error al cargar paseos: ${response.code()}"
                    walks = emptyList()
                }
            } catch (e: Exception) {
                println("ERROR fetchWalks - Exception: ${e.message}")
                e.printStackTrace()
                errorMessage = "Error de conexión: ${e.message}"
                walks = emptyList()
            } finally {
                isLoading = false
            }
        }
    }

    fun refreshWalks() {
        println("DEBUG refreshWalks - Refrescando paseos del usuario actual...")
        fetchWalks()
    }
}