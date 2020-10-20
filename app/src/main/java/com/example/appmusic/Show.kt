package com.example.appmusic

import com.google.gson.annotations.SerializedName

data class Show (

    @SerializedName("available_markets") val available_markets : List<String>,
    @SerializedName("copyrights") val copyrights : List<String>,
    @SerializedName("description") val description : String,
    @SerializedName("explicit") val explicit : Boolean,
    @SerializedName("external_urls") val external_urls : ExternalUrls,
    @SerializedName("href") val href : String,
    @SerializedName("id") val id : String,
    @SerializedName("images") val images : List<Images>,
    @SerializedName("is_externally_hosted") val is_externally_hosted : Boolean,
    @SerializedName("languages") val languages : List<String>,
    @SerializedName("media_type") val media_type : String,
    @SerializedName("name") val name : String,
    @SerializedName("publisher") val publisher : String,
    @SerializedName("total_episodes") val total_episodes : Int,
    @SerializedName("type") val type : String,
    @SerializedName("uri") val uri : String
)