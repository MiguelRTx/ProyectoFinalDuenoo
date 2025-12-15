package com.example.projectfinal.data.model

data class ReviewRequest(
    val rating: Int,
    val comment: String
)


data class WalkPhoto(
    val id: Int,
    val url: String
)

data class PhotoListResponse(
    val data: List<WalkPhoto>
)