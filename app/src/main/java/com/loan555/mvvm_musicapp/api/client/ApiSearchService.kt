package com.loan555.mvvm_musicapp.api.client

import com.loan555.mvvm_musicapp.model.DataSearchResult
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiSearchService {
    //http://ac.mp3.zing.vn/complete?type=artist,song,key,code&num=500&query=Anh Thế Giới Và Em
    @GET("complete")
    fun getCurrentData(
        @Query("type") type: String,
        @Query("num") num: Long,
        @Query("query") query: String
    ): Call<DataSearchResult>
}