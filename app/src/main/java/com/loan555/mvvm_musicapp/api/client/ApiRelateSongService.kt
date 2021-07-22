package com.loan555.mvvm_musicapp.api.client

import com.loan555.mvvm_musicapp.model.RelatedSong
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiRelateSongService {
    //http://mp3.zing.vn/xhr/recommend?type=audio&id=ZW67OIA0
    @GET("xhr/recommend?type=audio")
    fun getCurrentData(
        @Query("id") id: String
    ): Call<RelatedSong>
}