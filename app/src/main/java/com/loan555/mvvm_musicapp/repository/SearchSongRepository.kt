package com.loan555.mvvm_musicapp.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.loan555.mvvm_musicapp.api.client.ApiSearchService
import com.loan555.mvvm_musicapp.api.config.ApiSearchConfig
import com.loan555.mvvm_musicapp.model.DataSearchResult
import com.loan555.mvvm_musicapp.model.DatumSearch
import com.loan555.mvvm_musicapp.model.SongCustom
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val myTag = "myTagServiceDebug"

class SearchSongRepository {
    private val _songList = MutableLiveData<List<SongCustom>>().apply { value = listOf() }
    private val songList: LiveData<List<SongCustom>> = _songList

    private val client: ApiSearchService =
        ApiSearchConfig.retrofit.create(ApiSearchService::class.java)

    private fun getData(name: String) {// get data with key
        val typeSearch = "artist,song,key,code"
        val num = 500.toLong()
        val call = client.getCurrentData(typeSearch, num, name)
        call.enqueue(object : Callback<DataSearchResult> {
            override fun onResponse(
                call: Call<DataSearchResult>,
                response: Response<DataSearchResult>
            ) {
                if (response.code() == 200) {
                    try {
                        var newList = mutableListOf<SongCustom>()
                        if (response.body() != null) {
                            if (response.body()?.data != null) {
                                var dataResponse: DatumSearch? = null
                                try {
                                    dataResponse = response.body()!!.data[0]
                                } catch (e: Exception) {
                                    Log.e(myTag, "error get data search: ${e.message}")
                                }
                                //load du lieu
                                var count = 0
                                dataResponse?.song?.forEach {
                                    count++
                                    if (count > 20) return@forEach
                                    var thumb: String? =
                                        "https://photo-resize-zmp3.zadn.vn/w320_r1x1_png/${it.thumb}"
                                    newList += SongCustom(
                                        it.id,
                                        it.name,
                                        it.artist,
                                        thumb,
                                        it.duration.toLong(),
                                        it.name,
                                        "http://api.mp3.zing.vn/api/streaming/audio/${it.id}/320",
                                        favorite = false,
                                        isLocal = false
                                    )
                                }
                            }
                        }
                        _songList.value = newList
                        Log.d(myTag,"search ok size = ${songList.value?.size}")
                    } catch (e: ExceptionInInitializerError) {
                        Log.e(myTag, "${e.message}")
                    }
                } else Log.e("my", "response.code() = ${response.code()}")
            }

            override fun onFailure(call: Call<DataSearchResult>, t: Throwable) {
                Log.e(myTag, "error getCurrentSongData ${t.message}")
            }
        })
    }

    fun searchList(query: String): LiveData<List<SongCustom>> {
        Log.d(myTag,"searchList")
        getData(query)
        return songList
    }

    fun getList() = songList
}