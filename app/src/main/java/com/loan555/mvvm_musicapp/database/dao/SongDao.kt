package com.loan555.mvvm_musicapp.database.dao

import androidx.lifecycle.LiveData
import androidx.room.*
import com.loan555.mvvm_musicapp.model.SongCustom

@Dao
interface SongDao {
    @Insert
    suspend fun insertSong(song: SongCustom)

    @Update
    suspend fun updateSong(song: SongCustom)

    @Delete
    suspend fun deleteSong(song: SongCustom)

    @Query("select * from songs_favorite_table")
    fun getAllSong(): LiveData<List<SongCustom>>
}