package com.loan555.mvvm_musicapp.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import com.loan555.mvvm_musicapp.database.dao.SongDao
import com.loan555.mvvm_musicapp.model.SongCustom

@Database(entities = [SongCustom::class], version = 1)//danh sach nhung class entity da tao
abstract class SongDatabase : RoomDatabase() {
    abstract fun getSongDao(): SongDao

    companion object {
        @Volatile // chu thich chay tren JVM
        private var instance: SongDatabase? = null

        fun getInstance(context: Context): SongDatabase {
            if (instance == null) {
                instance =
                    Room.databaseBuilder(context, SongDatabase::class.java, "FavoriteDatabase").build()
            }
            return instance!!
        }
    }
}