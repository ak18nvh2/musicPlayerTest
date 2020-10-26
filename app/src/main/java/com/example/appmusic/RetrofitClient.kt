package com.example.appmusic

import okhttp3.Interceptor
import okhttp3.OkHttpClient
import okhttp3.logging.HttpLoggingInterceptor
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit

object RetrofitClient {
    private const val BASE_URL = "https://api.spotify.com/v1/"
    private val logger = HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.BODY)
    private const val token =
        "BQBaPk7ieyAooF0k_FH9zDyE8m6UUrOVEE6xmdqvVJFjVZSLsKiTA3-tcAycrkrBC0O0MfIx7czf49u-D3rAlm4EnGRLKfwgoXqVVNV6KqtS9VDygwW_5f31mCN-eA0s1n6uW1jctsZt0ylgUTVB2_cHQyHkTYjbUfxu_yilvxDta2Fj44a0RC7NCRAmNJ8yt_Hi1c9A7OkZe04WSDwroQ"
    private val headerInterceptor = Interceptor { chain ->
        var request = chain.request()
        request = request.newBuilder()
            .addHeader("Accept", "application/json")
            .addHeader("Content-type", "application/json")
            .addHeader("Authorization", "Bearer $token")
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
    val instance: Api by lazy {
        val retrofit = Retrofit.Builder()
            .baseUrl(BASE_URL)
            .client(builder)
            .addConverterFactory(GsonConverterFactory.create())
            .build()
        retrofit.create(Api::class.java)
    }
}