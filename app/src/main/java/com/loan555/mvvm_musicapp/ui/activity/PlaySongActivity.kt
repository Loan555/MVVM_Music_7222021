package com.loan555.mvvm_musicapp.ui.activity

import android.content.Context
import android.content.Intent
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.loan555.musicapplication.service.MusicControllerService
import com.loan555.mvvm_musicapp.R
import com.loan555.mvvm_musicapp.ui.fragment.PlayFragment
import com.loan555.mvvm_musicapp.ui.playlistPlaying.PlaylistPlayingFragment
import com.loan555.mvvm_musicapp.ui.viewmodel.AppViewModel
import kotlinx.android.synthetic.main.activity_play_song.*

class PlaySongActivity : AppCompatActivity() {
    private val NUM_PAGES = 2
    private lateinit var viewModel: AppViewModel
    private var mService: MusicControllerService? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_play_song)
        setSupportActionBar(toolbar)
        viewModel = ViewModelProvider(this).get(AppViewModel::class.java)
        viewPagerPlay.adapter = PagerPlayAdapter(supportFragmentManager)

        viewModel.mBinder.observe(this, {
            if (it != null) {
                mService = it.getService()
                mService!!.songPlaying.observe(this, { song ->
                    viewModel.initViewSongPlaying(song)
                })
                mService!!.currentTime.observe(this, { time ->
                    if (time != null) {
                        viewModel.postTimeToSeekbar(time)
                    } else viewModel.postTimeToSeekbar(0)
                })
                mService!!.isPlaying.observe(this, { isPlaying ->
                    viewModel.setIsPlaying(isPlaying)
                })
                viewModel.handState()
            } else mService = null
        })

        viewModel.currentPager.observe(this, {
            viewPagerPlay.currentItem = it
        })
    }

    override fun onStart() {
        super.onStart()
        val intentService = Intent(this, MusicControllerService::class.java)
        bindService(intentService, viewModel.getServiceConnection(), Context.BIND_AUTO_CREATE)
        viewModel.mBound.observe(this, {
            Log.d("myTag", "mBound = $it")
        })
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_play, menu)
        return true
    }

    private inner class PagerPlayAdapter(fa: FragmentManager) : FragmentPagerAdapter(fa) {
        override fun getCount(): Int = NUM_PAGES

        override fun getItem(position: Int): Fragment {
            return when (position) {
                1 -> PlaylistPlayingFragment()
                else -> PlayFragment()
            }
        }
    }
}