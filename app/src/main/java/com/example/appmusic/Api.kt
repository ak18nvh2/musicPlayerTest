package com.example.appmusic

import retrofit2.Call
import retrofit2.http.*

interface Api {

    @GET("episodes?ids=77o6BIVlYM3msb4MMIL1jH%2C0Q86acNRm6V9GYx55SXKwf&market=ES")
    fun getContacts(): Call<FileJson>

}