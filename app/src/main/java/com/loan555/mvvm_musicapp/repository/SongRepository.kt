package com.loan555.mvvm_musicapp.repository

import android.app.Application
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.loan555.mvvm_musicapp.database.SongDatabase
import com.loan555.mvvm_musicapp.database.dao.SongDao
import com.loan555.mvvm_musicapp.model.SongCustom

class SongRepository(application: Application) {
    private val songDao: SongDao

    init {
        val songDatabase: SongDatabase = SongDatabase.getInstance(application)
        songDao = songDatabase.getSongDao()
    }

//    private val _chartList = MutableLiveData<List<SongCustom>>().apply { value = listOf() }
//    private var chartList: LiveData<List<SongCustom>> = _chartList

    suspend fun insertSong(song: SongCustom) = songDao.insertSong(song)

    suspend fun updateSong(song: SongCustom) = songDao.updateSong(song)

    suspend fun deleteSong(song: SongCustom) = songDao.deleteSong(song)

    fun loadAllSong(): LiveData<List<SongCustom>> = songDao.getAllSong()
}