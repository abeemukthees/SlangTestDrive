package com.msa.slanglabs.entities.response


import com.google.gson.annotations.SerializedName

data class CityItem(
    @SerializedName("ID")
    val id: Int,
    @SerializedName("Latitude")
    val latitude: String,
    @SerializedName("Longitude")
    val longitude: String,
    @SerializedName("Name")
    val name: String
)