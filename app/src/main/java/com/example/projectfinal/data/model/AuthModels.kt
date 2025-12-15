package com.example.projectfinal.data.model


import com.google.gson.annotations.SerializedName


data class LoginRequest(
    val email: String,
    val password: String
)


data class RegisterRequest(
    val name: String,
    val email: String,
    val password: String
)


data class LoginResponse(
    val status: Boolean? = null,
    val message: String? = null,
    @SerializedName("access_token") val accessToken: String? = null,
    @SerializedName("token") val token: String? = null
) {

    fun getAuthToken(): String? = accessToken ?: token
}
