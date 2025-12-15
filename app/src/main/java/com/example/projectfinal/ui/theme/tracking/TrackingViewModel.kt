package com.example.projectfinal.ui.theme.tracking

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.projectfinal.data.api.RetrofitClient
import com.example.projectfinal.data.model.PhotoListResponse
import com.example.projectfinal.data.model.ReviewRequest
import com.example.projectfinal.data.model.Walk
import com.example.projectfinal.data.model.WalkPhoto
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch

class TrackingViewModel(application: Application) : AndroidViewModel(application) {

    private val apiService = RetrofitClient.getApiService(application)

    var walk by mutableStateOf<Walk?>(null)
    var photos by mutableStateOf<List<WalkPhoto>>(emptyList())
    var walkerLat by mutableStateOf<Double?>(null)
    var walkerLng by mutableStateOf<Double?>(null)
    var isSendingReview by mutableStateOf(false)
    var reviewSuccess by mutableStateOf(false)

    fun startTracking(walkId: Int) {
        viewModelScope.launch {
            while (isActive) {
                fetchWalkDetail(walkId)
                fetchPhotos(walkId)
                if (walk?.status == "finished") {
                    break
                }

                delay(60000)
            }
        }
    }

    private suspend fun fetchWalkDetail(walkId: Int) {
        try {
            val response = apiService.getWalkDetail(walkId)
            if (response.isSuccessful && response.body() != null) {
                walk = response.body()

                walk?.let { w ->
                    walkerLat = w.walkerLatitude?.toDoubleOrNull()
                    walkerLng = w.walkerLongitude?.toDoubleOrNull()
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    private suspend fun fetchPhotos(walkId: Int) {
        try {
            val response = apiService.getWalkPhotos(walkId)
            if (response.isSuccessful && response.body() != null) {
                photos = response.body()!!.data
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    fun sendReview(walkId: Int, rating: Int, comment: String) {
        viewModelScope.launch {
            isSendingReview = true
            try {
                val request = ReviewRequest(rating, comment)
                val response = apiService.submitReview(walkId, request)
                if (response.isSuccessful) {
                    reviewSuccess = true
                }
            } catch (e: Exception) {
                e.printStackTrace()
            } finally {
                isSendingReview = false
            }
        }
    }
}