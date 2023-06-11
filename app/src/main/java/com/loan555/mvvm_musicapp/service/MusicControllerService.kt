package com.loan555.mvvm_musicapp.service

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
import android.os.IBinder
import android.support.v4.media.session.MediaSessionCompat
import android.util.Log
import androidx.core.app.NotificationCompat
import androidx.lifecycle.MutableLiveData
import com.loan555.mvvm_musicapp.R
import com.loan555.mvvm_musicapp.model.SongCustom
import com.loan555.mvvm_musicapp.ui.fragment.myTag
import timber.log.Timber
import java.io.IOException
import kotlin.random.Random

class MusicControllerService : Service(), MediaPlayer.OnCompletionListener,
    MediaPlayer.OnPreparedListener, MediaPlayer.OnErrorListener, MediaPlayer.OnSeekCompleteListener,
    MediaPlayer.OnInfoListener, MediaPlayer.OnBufferingUpdateListener,
    AudioManager.OnAudioFocusChangeListener {
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
            PendingIntent.FLAG_IMMUTABLE
        )
        val nextIntent = Intent(ACTION_MUSIC).apply {
            putExtra(KEY_ACTION_MUSIC, ACTION_NEXT)
        }
        nextPendingIntent = PendingIntent.getBroadcast(
            this,
            ACTION_NEXT,
            nextIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val stopIntent = Intent(ACTION_MUSIC).apply {
            putExtra(KEY_ACTION_MUSIC, ACTION_STOP)
        }
        stopPendingIntent = PendingIntent.getBroadcast(
            this,
            ACTION_STOP,
            stopIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        val pauseIntent = Intent(ACTION_MUSIC).apply {
            putExtra(KEY_ACTION_MUSIC, ACTION_PLAY_PAUSE)
        }
        pausePendingIntent = PendingIntent.getBroadcast(
            this,
            ACTION_PLAY_PAUSE,
            pauseIntent,
            PendingIntent.FLAG_IMMUTABLE
        )
        initMediaPlayer()
    }

    override fun onBind(intent: Intent?): IBinder? {
        Log.d(myTag, "service onBind")
//        val handler = Handler()
//        handler.postDelayed(object : Runnable {
//            override fun run() {
//                currentTime.value = getPos()
//                handler.postDelayed(this, 1000)
//            }
//        }, 0)
        return binder
    }

    override fun onStartCommand(intent: Intent?, flags: Int, startId: Int): Int {
        Log.d(myTag, "service onStartCommand")
        val pendingIntent =
            PendingIntent.getActivity(this, 0, intent, PendingIntent.FLAG_IMMUTABLE)
        val mediaSession = MediaSessionCompat(this, "tag")
        val notification = NotificationCompat.Builder(this, CHANNEL_ID)
            // Show controls on lock screen even when user hides sensitive content.
            .setVisibility(NotificationCompat.VISIBILITY_PUBLIC)
//            .setContentTitle(songs[songPos].title)
//            .setContentText(songs[songPos].artistsNames)
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
        initMediaPlayer()
        return START_NOT_STICKY
    }

    private fun getBtnImg(): Int {
        return if (isPlaying.value == true) {
            R.drawable.ic_pause
        } else R.drawable.ic_play
    }

    override fun onDestroy() {
        super.onDestroy()
        player?.reset()
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
        player?.reset()
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
                    else -> handAction(ACTION_RESUME)
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

    private fun playPrev(): String {
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
        Timber.d("state play = $statePlay")
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

    private fun initMediaPlayer() {
        if (player == null) player = MediaPlayer()
        //Set up MediaPlayer event listeners
        player?.setOnCompletionListener(this)
        player?.setOnErrorListener(this)
        player?.setOnPreparedListener(this)
        player?.setOnBufferingUpdateListener(this)
        player?.setOnSeekCompleteListener(this)
        player?.setOnInfoListener(this)
        //Reset so that the MediaPlayer is not pointing to another data source
        player?.reset()
        player?.setAudioStreamType(AudioManager.STREAM_MUSIC)
        player?.setOnPreparedListener {
            it.start()
        }
    }

    fun stopMedia() {
        if (player == null) return
        if (player!!.isPlaying) {
            player!!.stop()
        }
    }

    override fun onCompletion(mp: MediaPlayer) {
        //Invoked when playback of a media source has completed.
        stopMedia();
        //stop the service
        stopSelf()
    }

    //Handle errors
    override fun onError(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        //Invoked when there has been an error during an asynchronous operation.
        when (what) {
            MediaPlayer.MEDIA_ERROR_NOT_VALID_FOR_PROGRESSIVE_PLAYBACK -> Log.d(
                "MediaPlayer Error",
                "MEDIA ERROR NOT VALID FOR PROGRESSIVE PLAYBACK $extra"
            )

            MediaPlayer.MEDIA_ERROR_SERVER_DIED -> Log.d(
                "MediaPlayer Error",
                "MEDIA ERROR SERVER DIED $extra"
            )

            MediaPlayer.MEDIA_ERROR_UNKNOWN -> Log.d(
                "MediaPlayer Error",
                "MEDIA ERROR UNKNOWN $extra"
            )
        }
        return false
    }

    override fun onInfo(mp: MediaPlayer, what: Int, extra: Int): Boolean {
        //Invoked to communicate some info.
        Log.d(
            "MediaPlayer onInfo",
            "${mp.trackInfo}"
        )
        return false
    }

    fun playMedia() {
        if (!player!!.isPlaying) {
            player!!.start()
        }
    }

    override fun onPrepared(mp: MediaPlayer) {
        //Invoked when the media source is ready for playback.
        playMedia();
    }

    override fun onSeekComplete(mp: MediaPlayer) {
        //Invoked indicating the completion of a seek operation.
    }

    override fun onAudioFocusChange(focusState: Int) {
        //Invoked when the audio focus of the system is updated.
        when (focusState) {
            AudioManager.AUDIOFOCUS_GAIN -> {
                // resume playback
                if (player == null) initMediaPlayer() else if (!player!!.isPlaying) player!!.start()
                player!!.setVolume(1.0f, 1.0f)
            }

            AudioManager.AUDIOFOCUS_LOSS -> {
                // Lost focus for an unbounded amount of time: stop playback and release media player
                if (player!!.isPlaying) player!!.stop()
                player!!.release()
                player = null
            }

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT ->             // Lost focus for a short time, but we have to stop
                // playback. We don't release the media player because playback
                // is likely to resume
                if (player!!.isPlaying) player!!.pause()

            AudioManager.AUDIOFOCUS_LOSS_TRANSIENT_CAN_DUCK ->             // Lost focus for a short time, but it's ok to keep playing
                // at an attenuated level
                if (player!!.isPlaying) player!!.setVolume(0.1f, 0.1f)
        }
    }

    override fun onBufferingUpdate(mp: MediaPlayer, percent: Int) {
        //Invoked indicating buffering status of
        //a media resource being streamed over the network.
    }

    companion object {
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
        const val URI_API = "http://api.mp3.zing.vn/api/streaming/audio/"
    }
}