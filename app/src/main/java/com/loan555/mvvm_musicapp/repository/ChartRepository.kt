package com.loan555.mvvm_musicapp.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.loan555.mvvm_musicapp.api.client.ApiChartService
import com.loan555.mvvm_musicapp.api.config.ApiChartConfig
import com.loan555.mvvm_musicapp.model.DataChartResult
import com.loan555.mvvm_musicapp.model.SongCustom
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val myTag = "myTag"

class ChartRepository {
    private val _chartList = MutableLiveData<List<SongCustom>>().apply { value = listOf() }
    private val chartList: LiveData<List<SongCustom>> = _chartList

    private val client: ApiChartService =
        ApiChartConfig.retrofit.create(ApiChartService::class.java)

    private fun getData() {
        val songId = 0
        val videoId = 0
        val albumId = 0
        val chart = "song"
        val time = -1
        val call = client.getCurrentData(songId, videoId, albumId, chart, time)
        call.enqueue(object : Callback<DataChartResult> {
            override fun onResponse(
                call: Call<DataChartResult>?,
                response: Response<DataChartResult>?
            ) {
                Log.d(myTag, "response.code() == ${response?.code()}")
                if (response?.code() == 200) {
                    val dataResponse = response?.body()!!
                    val listChart = dataResponse.data
                    val newList = mutableListOf<SongCustom>()
                    listChart.song.forEach {
                        newList += SongCustom(
                            it.id,
                            it.name,
                            it.artistsNames,
                            it.thumbnail,
                            it.duration,
                            it.title,
                            "http://api.mp3.zing.vn/api/streaming/audio/${it.id}/320",
                            favorite = false,
                            isLocal = false
                        )
                    }
                    setList(newList)
                    Log.d(myTag, "load data chart done! ")
                }
            }

            override fun onFailure(call: Call<DataChartResult>?, t: Throwable?) {
                Log.e(myTag, "load data chart error: ${t?.message}")
            }
        })
    }

    private fun setList(newList: MutableList<SongCustom>) {
        _chartList.value = newList
    }

    fun loadAllSongChart(): LiveData<List<SongCustom>> {
        getData()
        return chartList
    }

    fun getList() = chartList
}