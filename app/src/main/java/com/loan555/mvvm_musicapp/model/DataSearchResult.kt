package com.loan555.mvvm_musicapp.model

import com.google.gson.annotations.SerializedName

data class DataSearchResult (
    val result: Boolean,
    val data: List<DatumSearch>
)

data class DatumSearch (
    val song: List<SongSearch>
)

data class SongSearch (
    val hasVideo: String,
    val thumb: String,
    val artist: String,
    val streamingStatus: String,
    val thumbVideo: String,

    @SerializedName("genreIds")
    val genreIDS: String,

    @SerializedName("disable_platform_web")
    val disablePlatformWeb: String,

    @SerializedName("artistIds")
    val artistIDS: String,

    val disSPlatform: String,
    val duration: String,

    @SerializedName("radioPid")
    val radioPID: String,

    @SerializedName("zing_choice")
    val zingChoice: String,

    val name: String,
    val block: String,
    val id: String,
    val disDPlatform: String
)


