package com.example.projectfinal.ui.theme.booking

import android.annotation.SuppressLint
import android.app.Application
import android.location.Location
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectfinal.data.api.RetrofitClient
import com.example.projectfinal.data.model.CreateWalkRequest
import com.example.projectfinal.data.model.CreateAddressRequest
import com.example.projectfinal.data.model.LocationRequest
import com.example.projectfinal.data.model.UserAddress
import com.example.projectfinal.data.model.Walker
import com.google.android.gms.location.LocationServices
import kotlinx.coroutines.launch

class BookingViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RetrofitClient.getApiService(application)
    private val fusedLocationClient = LocationServices.getFusedLocationProviderClient(application)

    // Estado del Mapa
    var userLocation by mutableStateOf<Location?>(null)
    var nearbyWalkers by mutableStateOf<List<Walker>>(emptyList())

    // Estado del Detalle/Reserva
    var selectedWalker by mutableStateOf<Walker?>(null)
    var isBookingLoading by mutableStateOf(false)
    var bookingMessage by mutableStateOf<String?>(null)
    var bookingSuccess by mutableStateOf(false)

    // Estado de direcciones
    var userAddresses by mutableStateOf<List<UserAddress>>(emptyList())
    private var defaultAddressId: String? = null

    // Estado de la ruta seleccionada
    var selectedRoutePointA by mutableStateOf<com.google.android.gms.maps.model.LatLng?>(null)
        private set
    var selectedRoutePointB by mutableStateOf<com.google.android.gms.maps.model.LatLng?>(null)
        private set

    // 1. Obtener ubicación actual
    @SuppressLint("MissingPermission") // Se gestiona en la UI
    fun getCurrentLocation() {
        fusedLocationClient.lastLocation.addOnSuccessListener { location ->
            if (location != null) {
                userLocation = location
                // Una vez tenemos ubicación, buscamos paseadores y direcciones
                searchNearbyWalkers(location.latitude, location.longitude)
                ensureUserAddress()
            }
        }
    }

    // Obtener o crear direcciones del usuario
    private fun ensureUserAddress() {
        viewModelScope.launch {
            try {
                val response = apiService.getAddresses()
                if (response.isSuccessful && response.body() != null) {
                    userAddresses = response.body()!!
                    if (userAddresses.isNotEmpty()) {
                        defaultAddressId = userAddresses.first().id.toString()
                    } else {
                        // Crear una dirección por defecto
                        createDefaultAddress()
                    }
                } else {
                    // Si no hay direcciones, crear una por defecto
                    createDefaultAddress()
                }
            } catch (e: Exception) {
                // En caso de error, usar fallback
                defaultAddressId = "1"
            }
        }
    }

    private suspend fun createDefaultAddress() {
        try {
            val response = apiService.createAddress(CreateAddressRequest(true))
            if (response.isSuccessful) {
                // Recargar direcciones después de crear
                val addressesResponse = apiService.getAddresses()
                if (addressesResponse.isSuccessful && addressesResponse.body() != null) {
                    userAddresses = addressesResponse.body()!!
                    defaultAddressId = userAddresses.lastOrNull()?.id?.toString() ?: "1"
                }
            } else {
                defaultAddressId = "1" // Fallback
            }
        } catch (e: Exception) {
            defaultAddressId = "1" // Fallback
        }
    }

    // 2. Buscar paseadores
    private fun searchNearbyWalkers(lat: Double, lng: Double) {
        viewModelScope.launch {
            try {
                val req = LocationRequest(lat.toString(), lng.toString())
                val response = apiService.getNearbyWalkers(req)
                if (response.isSuccessful && response.body() != null) {
                    nearbyWalkers = response.body()!!.data
                }
            } catch (e: Exception) {
                // Manejar error silenciosamente o mostrar en UI
            }
        }
    }

    // 3. Obtener detalle del paseador seleccionado
    fun selectWalker(walkerId: Int) {
        viewModelScope.launch {
            try {
                // Primero limpiamos selección anterior
                selectedWalker = null
                val response = apiService.getWalkerDetail(walkerId)
                if (response.isSuccessful) {
                    selectedWalker = response.body()
                }
            } catch (e: Exception) {
                // Error
            }
        }
    }

    // 4. Crear Reserva
    fun createWalk(petId: Int, date: String, duration: String, notes: String) {
        val walker = selectedWalker ?: return

        // Validación básica
        if (date.isEmpty() || duration.isEmpty()) {
            bookingMessage = "Faltan datos (Fecha o Duración)"
            return
        }

        viewModelScope.launch {
            isBookingLoading = true
            bookingMessage = null
            try {
                // OJO: Aquí estamos "hardcodeando" pet_id=1 y address_id=1
                // para que funcione el tutorial sin hacer todas las pantallas de dirección.
                // En un app real, esto viene de un selector.
                val request = CreateWalkRequest(
                    walkerId = walker.id.toString(),
                    petId = petId.toString(), // Usar la mascota seleccionada
                    scheduledAt = date, // Formato esperado: "2025-11-27 10:59"
                    durationMinutes = duration, // Ya como string
                    userAddressId = defaultAddressId ?: "1", // Usar dirección real o fallback
                    notes = notes,
                    // Incluir ruta si está disponible
                    routeStartLatitude = selectedRoutePointA?.latitude?.toString(),
                    routeStartLongitude = selectedRoutePointA?.longitude?.toString(),
                    routeEndLatitude = selectedRoutePointB?.latitude?.toString(),
                    routeEndLongitude = selectedRoutePointB?.longitude?.toString()
                )

                val response = apiService.createWalk(request)
                if (response.isSuccessful) {
                    bookingSuccess = true
                    bookingMessage = "¡Paseo solicitado con éxito!"
                    clearSelectedRoute()
                } else {
                    bookingMessage = "Error al solicitar: ${response.code()}"
                }
            } catch (e: Exception) {
                bookingMessage = "Error: ${e.message}"
            } finally {
                isBookingLoading = false
            }
        }
    }

    fun resetBookingState() {
        bookingSuccess = false
        bookingMessage = null
    }

    fun setSelectedRoute(pointA: com.google.android.gms.maps.model.LatLng, pointB: com.google.android.gms.maps.model.LatLng) {
        selectedRoutePointA = pointA
        selectedRoutePointB = pointB
    }

    fun clearSelectedRoute() {
        selectedRoutePointA = null
        selectedRoutePointB = null
    }

    fun hasSelectedRoute(): Boolean {
        return selectedRoutePointA != null && selectedRoutePointB != null
    }
}