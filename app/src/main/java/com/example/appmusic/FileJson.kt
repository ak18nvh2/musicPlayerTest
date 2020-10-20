package com.example.appmusic

import com.google.gson.annotations.SerializedName

data class FileJson (

    @SerializedName("episodes") val episodes : List<Episodes>
)