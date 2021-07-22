package com.loan555.mvvm_musicapp.api.client

import com.loan555.mvvm_musicapp.model.DataChartResult
import retrofit2.Call
import retrofit2.http.GET
import retrofit2.http.Query

interface ApiChartService {
    //http://mp3.zing.vn/xhr/chart-realtime?songId=0&videoId=0&albumId=0&chart=song&time=-1
    @GET("xhr/chart-realtime")
    fun getCurrentData(
        @Query("songId") songId: Int,
        @Query("videoId") videoId: Int,
        @Query("albumId") albumId: Int,
        @Query("chart") chart: String,
        @Query("time") time: Int
    ): Call<DataChartResult>

}
