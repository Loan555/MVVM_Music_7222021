package com.loan555.musicapplication.service

import android.app.PendingIntent
import android.app.Service
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.media.AudioManager
import android.media.MediaPlayer
import android.net.Uri
import android.os.Binder
import android.os.Handler
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.loan555.mvvm_musicapp.R
import com.loan555.mvvm_musicapp.model.SongCustom
import com.loan555.mvvm_musicapp.ui.fragment.myTag
import kotlinx.coroutines.*
import java.io.IOException
import java.lang.Runnable
import kotlin.random.Random

const val ACTION_PAUSE = -1
const val ACTION_RESUME = 1
const val ACTION_NEXT = 2
const val ACTION_BACK = -2
const val ACTION_PLAY = 3
const val ACTION_STOP = -3
const val ACTION_PLAY_PAUSE = 4

const val CHANNEL_ID = "channel_music_app"
const val ONGOING_NOTIFICATION_ID = 1
const val ACTION_MUSIC = "android.intent.action.MY_MUSIC_ACTION"
const val KEY_ACTION_MUSIC = "action_music"

class MusicControllerService : Service() {
    var isPlaying: MutableLiveData<Boolean> = MutableLiveData(true)

    /**
    0: not loop
    1: loop
    2: shupf
    3: loop one
     */
    var statePlay: Int = 0
    private lateinit var br: BroadcastReceiver
    private val binder = MusicControllerBinder()
    var player: MediaPlayer? = null
    lateinit var songIDPlaying: String
    var songPos: Int = -1
    var songs = mutableListOf<SongCustom>()
    var looping = false
    private lateinit var prevPendingIntent: PendingIntent
    private lateinit var pausePendingIntent: PendingIntent
    private lateinit var nextPendingIntent: PendingIntent
    private lateinit var stopPendingIntent: PendingIntent
    var listPlaying = ""

    val songPlaying: MutableLiveData<SongCustom?> = MutableLiveData<SongCustom?>()
    val currentTime: MutableLiveData<Int?> = MutableLiveData()

    /**
     * Class used for the client Binder.  Because we know this service always
     * runs in the same process as its clients, we don't need to deal with IPC.
     */
    inner class MusicControllerBinder : Binder() {
        // Return this instance of LocalService so clients can call public methods
        fun getService(): MusicControllerService = this@MusicControllerService
    }

    override fun onCreate() {
        Log.d("aaa", "service onCreate")
        super.onCreate()
        val filter = IntentFilter(ACTION_MUSIC)
        br = object : BroadcastReceiver() {
            override fun onReceive(context: Context?, intent: Intent?) {
                val action = intent?.getIntExtra(KEY_ACTION_MUSIC, 0)!!
                Log.d(myTag, "onReceive service ${intent?.action} ------------- $action")
                handAction(action)
                if (action != ACTION_STOP)// gap may su kien nay thi moi update notification
                    Intent(this@MusicControllerService, MusicControllerService::class.java).also {
                        startService(it)
                    }
            }
        }
        registerReceiver(br, filter)
        val prevIntent = Intent(ACTION_MUSIC).apply {
            putExtra(KEY_ACTION_MUSIC, ACTION_BACK)
        }
        prevPendingIntent = PendingIntent.getBroadcast(
            this,
            ACTION_BACK,
            prevIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val nextIntent = Intent(ACTION_MUSIC).apply {
            putExtra(KEY_ACTION_MUSIC, ACTION_NEXT)
        }
        nextPendingIntent = PendingIntent.getBroadcast(
            this,
            ACTION_NEXT,
            nextIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
        val stopIntent = Intent(ACTION_MUSIC).apply {
            putExtra(KEY_ACTION_MUSIC, ACTION_STOP)
        }
        stopPendingIntent = PendingIntent.getBroadcast(
            this,
            ACTION_STOP,
            stopIntent,
            PendingIntent.FLAG_CANCEL_CURRENT
        )
        val pauseIntent = Intent(ACTION_MUSIC).apply {
            putExtra(KEY_ACTION_MUSIC, ACTION_PLAY_PAUSE)
        }
        pausePendingIntent = PendingIntent.getBroadcast(
            this,
            ACTION_PLAY_PAUSE,
            pauseIntent,
            PendingIntent.FLAG_UPDATE_CURRENT
        )
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(myTag, "service onBind")
        val handler = Handler()
        handler.postDelayed(object : Runnable {
            override fun run() {
                currentTime.value = getPos()
                handler.postDelayed(this, 1000)
                Log.d("timeHandler", "postDelayed timeHandler=  ${currentTime.value}")
            }
        }, 0)
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(myTag, "service onStartCommand")
        val pendingIntent =
            PendingIntent.getActivity(this, 0, intent, flags)
        val mediaSession = MediaSessionCompat(this, "tag")
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            // Show controls on lock screen even when user hides sensitive content.
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
            .setContentTitle(songs[songPos].title)
            .setContentText(songs[songPos].artistsNames)
            .setSmallIcon(R.drawable.ic_music_note)
            // Add media control buttons that invoke intents in your media service
            .addAction(
                R.drawable.ic_skip_previous,
                "Previous",
                prevPendingIntent
            ) // #0
            .addAction(getBtnImg(), "Pause", pausePendingIntent) // #1
            .addAction(
                R.drawable.ic_skip_next,
                "Next",
                nextPendingIntent
            ) // #2
            .addAction(R.drawable.ic_close, "Stop", stopPendingIntent) // #3
            .setStyle(
                androidx.media.app.NotificationCompat.MediaStyle()
                    .setShowActionsInCompactView(0, 1, 2 /* #1: pause button \*/)
                    .setMediaSession(mediaSession.sessionToken)
            )
            .setSound(null)
            .setContentIntent(pendingIntent)
            .build()
        startForeground(ONGOING_NOTIFICATION_ID, notification)
        return START_NOT_STICKY
    }

    private fun getBtnImg(): Int {
        return if (isPlaying.value == true) {
            R.drawable.ic_pause
        } else R.drawable.ic_play
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.release()
        player = null
        listPlaying = ""
        unregisterReceiver(br)
        Log.d(myTag, "service onDestroy")
    }

    /**     music controller */
    fun playSong(position: Int) {
        Log.d(myTag, "service playSong")
        isPlaying.value = true
        songPos = position
        val uri: Uri = Uri.parse(songs[position].uri)
        player?.release()
        player = MediaPlayer().apply {
            setAudioStreamType(AudioManager.STREAM_MUSIC)
            try {
                setDataSource(applicationContext, uri)
            } catch (e: IOException) {
                Log.e(myTag, "error play media: ${e.message}")
            }
            isLooping = looping
            try {
                prepareAsync()
            } catch (e: IllegalStateException) {
                Log.e("bbb", "error prepareAsync ${e.message}")
            }
            setOnPreparedListener {
                Log.d("bbb", "music on setOnPreparedListener")
                setOnCompletionListener {
                    Log.d("bbb", "music on completion")
                    playNextAuto()
                }
                start()
            }
            setOnErrorListener { mp, _, _ ->
//                mp.reset()
                Log.e("bbb", "error get setOnErrorListener")
                false
            }
        }
        Intent(this, MusicControllerService::class.java).also {
            it.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startService(it)
        }
        sentMyBroadcast(ACTION_PLAY)
        songPlaying.value = songs[songPos]
    }

    private fun handAction(action: Int) {
        when (action) {
            ACTION_PAUSE -> {
                pausePlayer()
            }
            ACTION_RESUME -> {
                resumePlayer()
            }
            ACTION_BACK -> {
                playPrev()
            }
            ACTION_NEXT -> {
                playNext()
            }
            ACTION_STOP -> {
                stopSelf()
            }
            ACTION_PLAY_PAUSE -> {
                when (isPng()) {
                    true -> handAction(ACTION_PAUSE)
                    false -> handAction(ACTION_RESUME)
                }
            }
        }
    }

    private fun sentMyBroadcast(action: Int) {
        val intent = Intent().also {
            it.action = ACTION_MUSIC
            it.putExtra(KEY_ACTION_MUSIC, action)
        }
        sendBroadcast(intent)
    }

    private fun playPrev(): String? {
        songPos--
        if (songPos == -1) songPos = songs.size - 1;
        this.playSong(songPos)
        songIDPlaying = songs[songPos].uri
        return songIDPlaying
    }

    fun getPos(): Int? {
        return player?.currentPosition?.div(1000)
    }

    fun getDur(): Int? {
        return player?.duration?.div(1000)
    }

    fun isPng(): Boolean? {
        return player?.isPlaying
    }

    private fun pausePlayer() {
        player?.pause()
        isPlaying.value = false
    }

    private fun resumePlayer() {
        player?.start()
        isPlaying.value = true
    }

    fun seek(position: Int) {
        player?.seekTo(position)
    }

    fun setStartPlay(state: Int) {
        this.statePlay = state
        if (statePlay != 3) {
            looping = false
            player?.isLooping = false
        }
        Log.d(myTag, "state play = $statePlay")
    }

    private fun playNext() {
        when (statePlay) {
            2 -> {
                var newPos = songPos
                while (songs.size > 1 && newPos == songPos) {
                    newPos = Random.nextInt(songs.size)
                }
                if (newPos == songPos) {
                    sentMyBroadcast(ACTION_PAUSE)
                } else {
                    songPos = newPos
                    playSong(songPos)
                }
            }
            else -> {
                songPos++;
                if (songPos == songs.size) songPos = 0
                this.playSong(songPos)
            }
        }
    }

    private fun playNextAuto() {
        when (statePlay) {
            0 -> {
                if (songPos < songs.size - 1) {
                    songPos++
                    playSong(songPos)
                } else sentMyBroadcast(ACTION_PAUSE)
            }
            2 -> {
                var newPos = songPos
                while (songs.size > 1 && newPos == songPos) {
                    newPos = Random.nextInt(songs.size)
                }
                if (newPos == songPos) {
                    sentMyBroadcast(ACTION_PAUSE)
                } else {
                    songPos = newPos
                    playSong(songPos)
                }
            }
            3 -> {
                player?.isLooping = true
                looping = true
                player?.start()
            }
            else -> {
                songPos++;
                if (songPos == songs.size) songPos = 0
                this.playSong(songPos)
            }
        }
    }
}