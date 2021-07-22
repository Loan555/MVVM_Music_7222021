package com.loan555.mvvm_musicapp.repository

import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.loan555.mvvm_musicapp.api.client.ApiRelateSongService
import com.loan555.mvvm_musicapp.api.config.ApiRelateConfig
import com.loan555.mvvm_musicapp.model.RelatedSong
import com.loan555.mvvm_musicapp.model.SongCustom
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

private const val myTag = "myTagRelateSong"

class RelatedRepository {
    private val _relateList = MutableLiveData<List<SongCustom>>().apply { value = listOf() }
    val relateList: LiveData<List<SongCustom>> = _relateList

    private val client: ApiRelateSongService =
        ApiRelateConfig.retrofit.create(ApiRelateSongService::class.java)

    private fun getData(id: String) {// get data with key
        val call = client.getCurrentData(id)
        call.enqueue(object : Callback<RelatedSong> {
            override fun onResponse(
                call: Call<RelatedSong>,
                response: Response<RelatedSong>
            ) {
                if (response.code() == 200) {
                    val newList = mutableListOf<SongCustom>()
                    val list = response.body()?.data?.items
                    list?.forEach {
                        val songCustom = SongCustom(
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
                        newList += songCustom
                    }
                    _relateList.value = newList
                    Log.d(myTag, "load relateSong OK size = ${relateList.value?.size}")
                } else Log.e(myTag, "response.code() = ${response.code()}")
            }

            override fun onFailure(call: Call<RelatedSong>, t: Throwable) {
                Log.e(myTag, "error getCurrentSongData ${t.message}")
            }
        })
    }

    private fun setList(newList: MutableList<SongCustom>) {
        _relateList.value = newList
    }

    fun loadAllSongRelate(id: String): LiveData<List<SongCustom>> {
        getData(id)
        return relateList
    }
}