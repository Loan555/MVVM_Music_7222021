package com.loan555.mvvm_musicapp.ui.viewmodel

import android.app.Application
import android.content.*
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.ConnectivityManager
import android.net.Uri
import android.os.Build
import android.os.IBinder
import android.util.Log
import android.util.Size
import android.widget.Toast
import androidx.lifecycle.*
import com.loan555.musicapplication.service.*
import com.loan555.mvvm_musicapp.model.Playlist
import com.loan555.mvvm_musicapp.model.SongCustom
import com.loan555.mvvm_musicapp.repository.ChartRepository
import com.loan555.mvvm_musicapp.repository.OfflineRepository
import com.loan555.mvvm_musicapp.repository.RelatedRepository
import com.loan555.mvvm_musicapp.repository.SongRepository
import com.loan555.mvvm_musicapp.ui.fragment.myTag
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.io.IOException
import java.lang.Exception
import java.net.HttpURLConnection
import java.net.URL

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val getApplication = application

    private val songRepository: SongRepository = SongRepository(application)
    private val chartRepository = ChartRepository()
    private val offlineRepository = OfflineRepository(application)
    private val relateRepository = RelatedRepository()

    private fun insertSong(song: SongCustom) = viewModelScope.launch {
        songRepository.insertSong(song)
    }

    fun updateFavoriteSong(song: SongCustom) = viewModelScope.launch {
        songRepository.updateSong(song)
    }

    fun deleteFavoriteSong(song: SongCustom) = viewModelScope.launch {
        songRepository.deleteSong(song)
    }

    fun loadAllSongFavorite(): LiveData<List<SongCustom>> = songRepository.loadAllSong()

    fun loadAllSongChart(): LiveData<List<SongCustom>> = chartRepository.loadAllSongChart()

    fun getAllSongChart(): LiveData<List<SongCustom>> = chartRepository.getList()

    fun loadAllSongOffline(): LiveData<List<SongCustom>> = offlineRepository.getAllSongOffline()

    fun getAllSongOffline(): LiveData<List<SongCustom>> = offlineRepository.getList()

    fun getRelateSong() = relateRepository.relateList

    fun loadRelate(id: String) = relateRepository.loadAllSongRelate(id)

    /**
     * service
     */

    var mBinder: MutableLiveData<MusicControllerService.MusicControllerBinder?> =
        MutableLiveData<MusicControllerService.MusicControllerBinder?>()
    var mBound: MutableLiveData<Boolean> = MutableLiveData<Boolean>()

    private val conn = object : ServiceConnection {
        override fun onServiceConnected(className: ComponentName?, service: IBinder?) {
            // We've bound to LocalService, cast the IBinder and get LocalService instance
            val binder = service as MusicControllerService.MusicControllerBinder
            mBinder.value = binder
            mBound.value = true
            Log.d(myTag, "onServiceConnected ${mBinder.value} , ${mBound.value}")
        }

        override fun onServiceDisconnected(arg0: ComponentName) {
            mBinder.value = null
            mBound.value = false
            Log.d(myTag, "onServiceDisconnected")
        }
    }

    fun getServiceConnection() = conn

    fun getBinder() = mBinder

    fun initViewSongPlaying(song: SongCustom?) {
        if (song != null) {
            _duration.value = (song.duration).toInt()
            _visibility.value = true
            _title.value = song.title
            _artistsNames.value = song.artistsNames
            getBitmap(song)
            _isPlaying.value = mBinder.value!!.getService().isPlaying.value
            _timeMax.value = song.timeToString()
            _isLocal.value = song.isLocal
            _isFavorite.value = song.favorite
        } else Log.d(myTag, "Không có bài hát nào đang chơi")
    }

    private fun getBitmap(song: SongCustom) {
        if (song.thumbnail == null) {
            setImg(song.getBitmapFromURI(getApplication))
        } else {
            GlobalScope.launch(Dispatchers.Main) {
                val result = async(Dispatchers.IO) {
                    return@async song.getBitmapFromURL()
                }
                setImg(result.await())
            }
        }
    }

    fun sentActionMusic(action: Int) {
        if (mBinder.value != null) {
            when (action) {
                0 -> {
                }
                ACTION_PLAY_PAUSE -> {
                    if (mBinder.value!!.getService().player != null)
                        controlMusic(ACTION_PLAY_PAUSE)
                }
                ACTION_BACK, ACTION_NEXT -> {
                    if (mBinder.value!!.getService().player != null) {
                        controlMusic(action)
                    }
                }
            }
        }
    }

    fun playSong(list: Playlist, p: Int) {
        if (mBinder.value != null) {
            when (list.id) {
                mBinder.value?.getService()?.listPlaying, "Playing" -> {

                }
                else -> {
                    mBinder.value!!.getService().songs = list.songs as MutableList<SongCustom>
                    songsPlaying.value = list.songs
                }
            }
            mBinder.value!!.getService().playSong(p)
        } else Log.e(myTag, "service not connect mBinder.value = ${mBinder.value}")
    }

    fun playOrPauseClick() {
        handBtnPlayPause()
    }

    private fun handBtnPlayPause() {
        if (mBinder.value != null) {
            if (mBinder.value!!.getService().isPng() == true) {
                controlMusic(ACTION_PAUSE)
                setIsPlaying(true)
            } else {
                controlMusic(ACTION_RESUME)
                setIsPlaying(false)
            }
        }
    }

    fun btnSkipNextClick() {
        controlMusic(ACTION_NEXT)
    }

    fun btnSkipBackClick() {
        controlMusic(ACTION_BACK)
    }

    fun loopClick() {
        var newState = statePlay.value
        if (newState != null)
            setStatePlay(newState + 1)
    }

    private fun controlMusic(action: Int) {
        if (mBinder.value != null) {
            val intent = Intent().also {
                it.action = ACTION_MUSIC
                it.putExtra(KEY_ACTION_MUSIC, action)
            }
            mBinder.value!!.getService().sendBroadcast(intent)
        }
    }

    /**
     * View
     */
    private val _visibility = MutableLiveData<Boolean>().apply { value = false }
    val visibility: LiveData<Boolean> = _visibility
    private val _imgPlaying = MutableLiveData<Bitmap?>().apply { value = null }
    val imgPlaying: LiveData<Bitmap?> = _imgPlaying
    private val _isPlaying = MutableLiveData<Boolean>().apply { value = false }
    val isPlaying: LiveData<Boolean> = _isPlaying
    private val _isLocal = MutableLiveData<Boolean>().apply { value = false }
    val isLocal: LiveData<Boolean> = _isLocal
    private val _isFavorite = MutableLiveData<Boolean>().apply { value = false }
    val isFavorite: LiveData<Boolean> = _isFavorite


    private val _title = MutableLiveData<String>().apply { value = "" }
    private val _artistsNames = MutableLiveData<String>().apply { value = "" }
    private val _timeMax = MutableLiveData<String>().apply { value = "00:00" }
    private val _timeCurrent = MutableLiveData<String>().apply { value = "00:00" }
    private var _statePlay = MutableLiveData<Int>().apply { value = 0 }
    private val _duration = MutableLiveData<Int>().apply { value = 0 }
    private val _current = MutableLiveData<Int>().apply { value = 0 }

    val title: LiveData<String> = _title
    val artistsNames: LiveData<String> = _artistsNames
    val timeMax: LiveData<String> = _timeMax
    val timeCurrent: LiveData<String> = _timeCurrent
    val statePlay: LiveData<Int> = _statePlay
    val duration: LiveData<Int> = _duration
    val current: LiveData<Int> = _current
    val currentPager = MutableLiveData<Int>().apply { value = 0 }

    val songsPlaying = MutableLiveData<List<SongCustom>>()

    fun setCurrentPager(pager: Int) {
        currentPager.value = pager
    }

    fun postTimeToSeekbar(timeCurrent: Int) {
        _current.value = timeCurrent
        _timeCurrent.value = createTimerLabel(timeCurrent)
        Log.d(
            "timeHandle",
            "post seek bar: ${current.value} , ${duration.value}"
        )
    }

    fun seekTo(currentProcess: Int) {
        if (mBinder.value != null) {
            try {
                mBinder.value!!.getService().seek(currentProcess * 1000)
                Log.d(myTag, "seek to")
            } catch (e: IllegalStateException) {
                Log.e(myTag, "seek to error ${e.message}")
            }
        }
    }

    private fun createTimerLabel(duration: Int): String {
        val sumSeconds = duration
        val hours = sumSeconds / 3600
        val minute = sumSeconds % 3600 / 60
        val seconds = sumSeconds % 60
        val hString = if (hours < 10) "0$hours" else "$hours"
        val mString = if (minute < 10) "0$minute" else "$minute"
        val sString = if (seconds < 10) "0$seconds" else "$seconds"
        return if (hours == 0) {
            "$mString:$sString"
        } else "$hString:$mString:$sString"
    }

    private fun setStatePlay(state: Int) {
        mBinder.value!!.getService().setStartPlay(state % 4)
        _statePlay.value = state % 4
    }

    fun handState() {
        _statePlay.value = mBinder.value!!.getService().statePlay
    }

    fun handSongsPlaying() {
        if (mBinder.value != null) {
            songsPlaying.value = mBinder.value!!.getService().songs
        }
    }

    fun btnFavoriteClick(song: SongCustom) {
        try {
            insertSong(song)
            mBinder.value!!.getService().songs[mBinder.value!!.getService().songPos].favorite =
                true
            _isFavorite.value = true
            Toast.makeText(getApplication, "Thêm vào yêu thích", Toast.LENGTH_SHORT).show()
        } catch (e: Exception) {
            Log.e(myTag, "error btnFavoriteClick : ${e.message}")
        }
    }

    fun setIsPlaying(isPlay: Boolean) {
        _isPlaying.value = isPlay
    }

    fun setImg(bitmap: Bitmap?) {
        _imgPlaying.value = bitmap
    }

    private val _text = MutableLiveData<String>().apply { value = "" }
    private val _size = MutableLiveData<String>().apply { value = "" }
    val playList = MutableLiveData<Playlist>().apply { value = null }

    val text: LiveData<String> = _text
    val size: LiveData<String> = _size

    fun setText(str: String) {
        _text.value = str
    }

    fun setList(list: Playlist) {
        playList.value = list
        _size.value = "${list.songs.size} bài hát"
    }
}