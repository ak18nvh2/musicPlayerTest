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
            .addHeader("Authorization","Bearer BQAM4hhP4d-drwHX7bog8ahVrjm0BWO-tYbq_PbJt0MIoQ5PQzypOb4eP_KENAuuJqHlxKHFT_jkHPHc4waCbQCAtosfYLj6GEfGpGcWE1HQ0SXUJserHC4yuyPeu2w-kXnERQKGjDA8RIwABjDkz9eUIVCxmdqFogyITZnfsdTZGFJbCkjvnhKJf9eGAHFZOD0y4UfC0zHC4zYv7hj6Tg")
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