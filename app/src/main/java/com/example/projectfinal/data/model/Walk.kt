package com.example.projectfinal.data.model

import com.google.gson.annotations.SerializedName

data class Walk(
    @SerializedName("id")
    val id: Int,
    @SerializedName("status")
    val status: String,
    @SerializedName("scheduled_at")
    val scheduledAt: String?,
    @SerializedName("duration_minutes")
    val durationMinutes: Int?,
    @SerializedName("notes")
    val notes: String?,
    @SerializedName("walker_id")
    val walkerId: Int?,
    @SerializedName("pet_id")
    val petId: Int?,
    @SerializedName("user_address_id")
    val userAddressId: Int?,

    @SerializedName("pet")
    val pet: PetInfo?,

    @SerializedName("walker")
    val walker: WalkerInfo?,

    @SerializedName("walker_latitude")
    val walkerLatitude: String?,

    @SerializedName("walker_longitude")
    val walkerLongitude: String?
) {
    val petName: String? get() = pet?.name ?: "Mascota #$petId"
    val walkerName: String? get() = walker?.name ?: "Paseador #$walkerId"
    val walkerPhoto: String? get() = walker?.photo
    val petPhoto: String? get() = pet?.photo
    val date: String? get() = scheduledAt
}

data class WalkerInfo(
    @SerializedName("id")
    val id: Int?,

    @SerializedName("name")
    val name: String?,

    @SerializedName("photoUrl")
    val photo: String?,

    @SerializedName("price_hour")
    val priceHour: String?,

    @SerializedName("rating")
    val rating: Double?
)

data class PetInfo(
    @SerializedName("id")
    val id: Int?,

    @SerializedName("name")
    val name: String?,

    @SerializedName("species")
    val type: String?,

    @SerializedName("photoUrl")
    val photo: String?
)

data class CreateWalkRequest(
    @SerializedName("walker_id")
    val walkerId: String,
    @SerializedName("pet_id")
    val petId: String,
    @SerializedName("scheduled_at")
    val scheduledAt: String,
    @SerializedName("duration_minutes")
    val durationMinutes: String,
    @SerializedName("user_address_id")
    val userAddressId: String?,
    @SerializedName("notes")
    val notes: String?,
    @SerializedName("route_start_latitude")
    val routeStartLatitude: String? = null,

    @SerializedName("route_start_longitude")
    val routeStartLongitude: String? = null,

    @SerializedName("route_end_latitude")
    val routeEndLatitude: String? = null,

    @SerializedName("route_end_longitude")
    val routeEndLongitude: String? = null
)

data class LocationRequest(
    @SerializedName("latitude")
    val latitude: String,

    @SerializedName("longitude")
    val longitude: String
)

data class UserAddress(
    @SerializedName("id")
    val id: Int,

    @SerializedName("address")
    val address: String?,

    @SerializedName("latitude")
    val latitude: String?,

    @SerializedName("longitude")
    val longitude: String?
)

data class WalkListResponse(
    val data: List<Walk>
)

data class AddressListResponse(
    val data: List<UserAddress>
)

data class CreateAddressRequest(
    @SerializedName("is_available")
    val isAvailable: Boolean = true
)

