package com.example.rastaai.data.remote

import retrofit2.Response
import retrofit2.http.GET

data class CategoryDto(val id: Long, val name: String)

interface CategoryApi {
    @GET("categories")
    suspend fun getCategories(): Response<List<CategoryDto>>
}
