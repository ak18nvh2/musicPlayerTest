package com.example.appmusic.models

import com.example.appmusic.models.Episodes
import com.google.gson.annotations.SerializedName

data class FileJson (

    @SerializedName("episodes") val episodes : List<Episodes>
)