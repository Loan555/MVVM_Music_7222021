package com.loan555.mvvm_musicapp.repository

import android.app.Application
import android.content.ContentUris
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import com.loan555.mvvm_musicapp.model.SongCustom
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.lang.Exception

private const val myTag = "myTagLoadDataOffline"

class OfflineRepository(private val application: Application) {
    private val _offlineList = MutableLiveData<List<SongCustom>>().apply { value = listOf() }
    private val offlineList: LiveData<List<SongCustom>> = _offlineList

    private fun getData() {
        val collection: Uri =
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                MediaStore.Audio.Media.getContentUri(
                    MediaStore.VOLUME_EXTERNAL
                )
            } else {
                MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
            }
        val protection = arrayOf(
            MediaStore.Audio.Media._ID,
            MediaStore.Audio.Media.DISPLAY_NAME,
            MediaStore.Audio.Media.ARTIST,
            MediaStore.Audio.Media.DURATION,
            MediaStore.Audio.Media.SIZE,
            MediaStore.Audio.Media.TITLE,
            MediaStore.Audio.Media.ALBUM
        )
        val selection = null
        val selectionArgs = null
        val sortOder = "${MediaStore.Audio.Media.DISPLAY_NAME} ASC"
        val newSongs = mutableListOf<SongCustom>()
        GlobalScope.launch(Dispatchers.Main) {
            val result = async(Dispatchers.IO) {
                val query = application.contentResolver.query(
                    collection,
                    protection,
                    selection,
                    selectionArgs,
                    sortOder
                )
                try {
                    query?.use { cursor ->
                        // Cache column indices.
                        val idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                        val nameColumn =
                            cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DISPLAY_NAME)
                        val artistsColumn =
                            cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST)
                        val durationColumn =
                            cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION)
                        val sizeColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.SIZE)
                        val titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE)
                        val albumsColumn =
                            cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM)
                        while (cursor.moveToNext()) {
                            while (cursor.moveToNext()) {
                                // Get values of columns for a given audio.
                                val id = cursor.getLong(idColumn)
                                val name = cursor.getString(nameColumn)
                                val artists = cursor.getString(artistsColumn)
                                val duration = cursor.getLong(durationColumn)
                                val size = cursor.getInt(sizeColumn)
                                val title = cursor.getString(titleColumn)
                                val albums = cursor.getString(albumsColumn)
                                //load content Uri
                                val contentUri: Uri =
                                    ContentUris.withAppendedId(
                                        MediaStore.Audio.Media.EXTERNAL_CONTENT_URI,
                                        id
                                    )
                                newSongs += SongCustom(
                                    id.toString(),
                                    name,
                                    artists,
                                    thumbnail = null,
                                    duration / 1000,
                                    title,
                                    contentUri.toString(),
                                    favorite = false,
                                    isLocal = true
                                )
                            }
                        }
                    }
                    Log.d(myTag, "load data storage done! size = ${newSongs.size}")
                } catch (e: Exception) {
                    Log.e(myTag, "loadOfflineSong error: ${e.message}")
                }
                return@async newSongs
            }
            setList(result.await())
        }
    }

    private fun setList(newList: MutableList<SongCustom>) {
        _offlineList.value = newList
    }

    fun getAllSongOffline(): LiveData<List<SongCustom>> {
        getData()
        return offlineList
    }

    fun getList(): LiveData<List<SongCustom>> = offlineList
}