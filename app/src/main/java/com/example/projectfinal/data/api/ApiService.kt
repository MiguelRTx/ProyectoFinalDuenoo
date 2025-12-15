package com.example.projectfinal.data.api

import com.example.projectfinal.data.model.*
import okhttp3.MultipartBody
import retrofit2.Response
import retrofit2.http.*

interface ApiService {

    @POST("auth/clientlogin")
    suspend fun login(@Body request: LoginRequest): Response<LoginResponse>

    @POST("auth/clientregister")
    suspend fun register(@Body request: RegisterRequest): Response<LoginResponse>

    @Multipart
    @POST("owners/photo")
    suspend fun uploadOwnerPhoto(
        @Part photo: MultipartBody.Part
    ): Response<Any>


    @GET("pets")
    suspend fun getPets(): Response<List<Pet>>

    @POST("pets")
    suspend fun createPet(@Body request: PetRequest): Response<Pet>

    @PUT("pets/{id}")
    suspend fun updatePet(@Path("id") id: Int, @Body request: PetRequest): Response<Any>

    @DELETE("pets/{id}")
    suspend fun deletePet(@Path("id") id: Int): Response<Any>

    @Multipart
    @POST("pets/{id}/photo")
    suspend fun uploadPetPhoto(
        @Path("id") id: Int,
        @Part photo: MultipartBody.Part
    ): Response<Any>


    @GET("walks")
    suspend fun getWalks(): Response<List<Walk>>

    @POST("walks")
    suspend fun createWalk(@Body request: CreateWalkRequest): Response<Any>

    @GET("walks/{id}")
    suspend fun getWalkDetail(@Path("id") id: Int): Response<Walk>


    @POST("walkers/nearby")
    suspend fun getNearbyWalkers(@Body request: LocationRequest): Response<WalkerListResponse>


    @GET("walkers/{id}")
    suspend fun getWalkerDetail(@Path("id") id: Int): Response<Walker>


    @GET("walks/{id}/photos")
    suspend fun getWalkPhotos(@Path("id") id: Int): Response<PhotoListResponse>

    @POST("walks/{id}/review")
    suspend fun submitReview(@Path("id") id: Int, @Body request: ReviewRequest): Response<Any>


    @GET("addresses")
    suspend fun getAddresses(): Response<List<UserAddress>>

    @POST("addresses")
    suspend fun createAddress(@Body request: CreateAddressRequest): Response<Any>

    @GET("me")
    suspend fun getCurrentUser(): Response<User>
}