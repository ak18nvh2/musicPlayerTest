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
            .addHeader("Authorization","Bearer BQDj1MkTIeSkBoYqTpcOG4A0pq3dz708iWDjgbyQxCNH9FKMZZimkmpYxCdHdUthADYnFnwrktniZYy7HClUwe8zA9LURBIQPlpNXRyfrvGLuqSYRnpI7o3Rt7QTOlLmND63CFFto96kq41WI5hofrz37pNTOQwxBcBpp2bFvFVPpN9C8ML5Ko8rJahyhEebP36fDtD7D1tGJTtXnwsycw")
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