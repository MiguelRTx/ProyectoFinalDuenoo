package com.example.projectfinal.data.model


import com.google.gson.annotations.SerializedName

data class Pet(
    @SerializedName("id")
    val id: Int,
    @SerializedName("name")
    val name: String?,
    @SerializedName("species")
    val type: String?,
    @SerializedName("notes")
    val notes: String?,
    @SerializedName("photoUrl")
    val photo: String?
)


data class PetRequest(
    val name: String,
    val type: String,
    val notes: String?
)

data class PetListResponse(
    val data: List<Pet>
)