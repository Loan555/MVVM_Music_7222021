package com.loan555.mvvm_musicapp.model

import android.app.Application
import android.app.DownloadManager
import android.content.Context
import android.content.Context.DOWNLOAD_SERVICE
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Environment
import android.util.Log
import android.util.Size
import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.PrimaryKey
import com.google.gson.annotations.SerializedName
import java.io.File
import java.io.IOException
import java.io.Serializable
import java.net.HttpURLConnection
import java.net.URL

@Entity(tableName = "songs_favorite_table")
class SongCustom(
    @ColumnInfo(name = "id_col") var id: String,
    @ColumnInfo(name = "name_col") var name: String,
    @ColumnInfo(name = "artistsNames_col") var artistsNames: String,
    @ColumnInfo(name = "thumbnail_col") var thumbnail: String?,
    @ColumnInfo(name = "duration_col") var duration: Long,
    @ColumnInfo(name = "title_col") var title: String,
    @ColumnInfo(name = "uri_col") var uri: String,
    @ColumnInfo(name= "favorite_col") var favorite: Boolean,
    @ColumnInfo(name= "local_col") var isLocal: Boolean
) : Serializable {
    @PrimaryKey(autoGenerate = true)//tu dong khoi tao
    @ColumnInfo(name = "auto_id_col")
    var idRoom: Int = 0

    fun timeToString(): String {
        val sumSeconds = duration
        val hours = sumSeconds / 3600
        val minute = sumSeconds % 3600 / 60
        val seconds = sumSeconds % 60
        val hString = if (hours < 10) "0$hours" else "$hours"
        val mString = if (minute < 10) "0$minute" else "$minute"
        val sString = if (seconds < 10) "0$seconds" else "$seconds"
        return if (hours == 0L) {
            "$mString:$sString"
        } else "$hString:$mString:$sString"
    }

    fun getBitmapFromURL(): Bitmap? {
        return try {
            val url = URL(this.thumbnail)
            val connection = url.openConnection() as HttpURLConnection
            connection.doInput = true
            connection.connect()
            val input = connection.inputStream
            BitmapFactory.decodeStream(input)
        } catch (e: IOException) {
            // Log exception
            null
        }
    }

    fun getBitmapFromURI(application: Application): Bitmap? {
        return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            try {
                val thumb =
                    application.contentResolver?.loadThumbnail(
                        Uri.parse(this.uri), Size(640, 480), null
                    )
                thumb
            } catch (e: IOException) {
                Log.e("myTag", "can't find bitmap: ${e.message}")
                null
            }
        } else null
    }

    fun downLoad(context: Context) {
        var dir = Environment.DIRECTORY_MUSIC
        dir += "/klp"
        val fileDir = File(dir)
        if (!fileDir.isDirectory) {
            fileDir.mkdir()
        }
        // Download File
        // Download File
        val request = DownloadManager.Request(
            Uri.parse(this.uri)
        )
        request.setDescription(this.name)
        request.setTitle(this.title)
        // in order for this if to run, you must use the android 3.2 to
        // compile your app
        // in order for this if to run, you must use the android 3.2 to
        // compile your app
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.HONEYCOMB) {
            request.allowScanningByMediaScanner()
            request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED)
        }
        request.setDestinationInExternalPublicDir(dir, "nameFile.mp3")

        // get download service and enqueue file

        // get download service and enqueue file
        val manager = context.getSystemService(DOWNLOAD_SERVICE) as DownloadManager
        manager.enqueue(request)
    }
}


data class Song(
    val id: String,
    val name: String,
    val title: String,
    val code: String,

    @SerializedName("content_owner")
    val contentOwner: Long,
    @SerializedName("isoffical")
    val isOfficial: Boolean,
    val isWorldWide: Boolean,

    @SerializedName("playlist_id")
    val playlistID: String,

    val artists: List<ArtistElement>,

    @SerializedName("artists_names")
    val artistsNames: String,
    @SerializedName("total")
    val total: Long,

    val performer: String,
    val type: String,
    val link: String,
    val lyric: String,
    val thumbnail: String,

    @SerializedName("mv_link")
    val mvLink: String,

    val duration: Long,
    val source: Map<String, String>,
    val album: Album,
    val artist: PurpleArtist,
    val ads: Boolean,

    @SerializedName("is_vip")
    val isVip: Boolean,

    val ip: String
)

data class Album(
    val id: String,
    val link: String,
    val title: String,
    val name: String,
    @SerializedName("isoffical")
    val isOfficial: Boolean,

    @SerializedName("artists_names")
    val artistsNames: String,

    val artists: List<ArtistElement>,
    val thumbnail: String,

    @SerializedName("thumbnail_medium")
    val thumbnailMedium: String
)

data class ArtistElement(
    val name: String,
    val link: String
)

data class PurpleArtist(
    val id: String,
    val name: String,
    val link: String,
    val cover: String,
    val thumbnail: String
)
