package com.loan555.mvvm_musicapp.model

import java.io.Serializable

data class Playlist(val id: String, var name: String,val img: Int?, var songs: List<SongCustom>): Serializable
