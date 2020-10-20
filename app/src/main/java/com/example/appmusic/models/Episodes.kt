package com.example.appmusic.models

import com.google.gson.annotations.SerializedName

data class Episodes (

    @SerializedName("audio_preview_url") val audio_preview_url : String,
    @SerializedName("description") val description : String,
    @SerializedName("duration_ms") val duration_ms : Int,
    @SerializedName("explicit") val explicit : Boolean,
    @SerializedName("external_urls") val external_urls : ExternalUrls,
    @SerializedName("href") val href : String,
    @SerializedName("id") val id : String,
    @SerializedName("images") val images : List<Images>,
    @SerializedName("is_externally_hosted") val is_externally_hosted : Boolean,
    @SerializedName("is_playable") val is_playable : Boolean,
    @SerializedName("language") val language : String,
    @SerializedName("languages") val languages : List<String>,
    @SerializedName("name") val name : String,
    @SerializedName("release_date") val release_date : String,
    @SerializedName("release_date_precision") val release_date_precision : String,
    @SerializedName("resume_point") val resume_point : ResumePoint,
    @SerializedName("show") val show : Show,
    @SerializedName("type") val type : String,
    @SerializedName("uri") val uri : String
)