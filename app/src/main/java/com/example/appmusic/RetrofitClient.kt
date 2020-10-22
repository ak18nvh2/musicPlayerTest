package com.example.appmusic

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://api.spotify.com/v1/"
    private  val logger = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    private val headerInterceptor = Interceptor { chain ->
        var request = chain.request()
        request = request.newBuilder()
            .addHeader("Accept","application/json")
            .addHeader("Content-type","application/json")
            .addHeader("Authorization","Bearer BQCIuInNGrnTZPZ7yMPR3wHtbmJl3NRTTyGEe4y18JIg-B6v4nNqA9o0dsZhhDNu9EBtMuud5m65Fej14NAuFTuuee9H128bXX5y_IXLOiwiUc128Co2qme2MlU-p2i6EkoIWnRGW5ChHcXrIEezGcs4IBhL10xW9bxGyP6m4768RLB7dNb1PxQjWrdoivzs112DVS3o0qzop5T710GPVQ")
            .build()
        chain.proceed(request)
    }

    private val builder = OkHttpClient.Builder()
        .readTimeout(5000, TimeUnit.MILLISECONDS)
        .writeTimeout(5000, TimeUnit.MILLISECONDS)
        .connectTimeout(5000, TimeUnit.MILLISECONDS)
        .retryOnConnectionFailure(true)
        .addInterceptor(headerInterceptor)
        .addInterceptor(logger)
        .build()
    val instance : Api by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(builder)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(Api::class.java)
    }
}