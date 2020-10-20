package com.example.appmusic.models

import com.google.gson.annotations.SerializedName

data class ResumePoint (

    @SerializedName("fully_played") val fully_played : Boolean,
    @SerializedName("resume_position_ms") val resume_position_ms : Int
)