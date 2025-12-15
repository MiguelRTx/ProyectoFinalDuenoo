package com.example.projectfinal.data.api

import android.content.Context
import com.example.projectfinal.data.network.SessionManager
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {


    private const val BASE_URL = "https://apimascotas.jmacboy.com/api/"

    private var apiService: ApiService? = null

    fun getApiService(context: Context): ApiService {
        if (apiService == null) {
            val sessionManager = SessionManager(context)


            val logging = HttpLoggingInterceptor()
            logging.setLevel(HttpLoggingInterceptor.Level.BODY)


            val authInterceptor = okhttp3.Interceptor { chain ->
                val originalRequest = chain.request()
                val token = sessionManager.getToken()

                val builder = originalRequest.newBuilder()
                    .addHeader("Accept", "application/json")


                if (!token.isNullOrEmpty()) {
                    builder.addHeader("Authorization", "Bearer $token")
                    println("DEBUG AuthInterceptor - Token agregado: Bearer ${token.take(20)}...")
                } else {
                    println("DEBUG AuthInterceptor - No hay token disponible")
                }

                chain.proceed(builder.build())
            }

            val client = OkHttpClient.Builder()
                .addInterceptor(logging)
                .addInterceptor(authInterceptor)
                .connectTimeout(30, TimeUnit.SECONDS)
                .readTimeout(30, TimeUnit.SECONDS)
                .build()

            val retrofit = Retrofit.Builder()
                .baseUrl(BASE_URL)
                .addConverterFactory(GsonConverterFactory.create())
                .client(client)
                .build()

            apiService = retrofit.create(ApiService::class.java)
        }
        return apiService!!
    }
}