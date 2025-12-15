package com.example.projectfinal.data.model

import com.google.gson.annotations.SerializedName


data class Walker(
    @SerializedName("id")
    val id: Int,

    @SerializedName("name")
    val name: String,

    @SerializedName("email")
    val email: String?,

    @SerializedName("price_hour")
    val priceHour: String?,

    @SerializedName("rating")
    val rating: Double?,

    @SerializedName("photo")
    val photo: String?,

    @SerializedName("latitude")
    val latitude: String?,

    @SerializedName("longitude")
    val longitude: String?,

    @SerializedName("is_available")
    val isAvailable: Boolean?
)


data class WalkerListResponse(
    val data: List<Walker>
)
