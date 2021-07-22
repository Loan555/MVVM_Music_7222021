package com.loan555.mvvm_musicapp.model

import com.google.gson.annotations.SerializedName

data class RelatedSong(
    val err: Long,
    val msg: String,
    val data: Data,
    val timestamp: Long
)

data class Data(
    val items: List<Song>,
    val total: Long,

    @SerializedName("image_url")
    val imageURL: String
)
