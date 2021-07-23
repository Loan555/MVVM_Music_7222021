package com.loan555.mvvm_musicapp.ui.activity

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentManager
import androidx.fragment.app.FragmentPagerAdapter
import androidx.lifecycle.ViewModelProvider
import androidx.viewpager.widget.ViewPager
import com.loan555.musicapplication.service.MusicControllerService
import com.loan555.mvvm_musicapp.R
import com.loan555.mvvm_musicapp.databinding.ActivityMainBinding
import com.loan555.mvvm_musicapp.model.Playlist
import com.loan555.mvvm_musicapp.model.SongCustom
import com.loan555.mvvm_musicapp.ui.fragment.*
import com.loan555.mvvm_musicapp.ui.viewmodel.AppViewModel

private const val NUM_PAGES = 3
private const val myTag = "myTag"
const val STORAGE_REQUEST_CODE = 1

class MainActivity : AppCompatActivity() {

    private lateinit var viewModel: AppViewModel

    private val binding: ActivityMainBinding by lazy {
        DataBindingUtil.setContentView(this, R.layout.activity_main)
    }

    private var mService: MusicControllerService? = null

    private val resultLauncher =
        registerForActivityResult(ActivityResultContracts.StartActivityForResult()) {
            if (it.resultCode == Activity.RESULT_OK) {
                val data: Intent? = it.data
                doPlay(data)
            }
        }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        viewModel = ViewModelProvider(this).get(AppViewModel::class.java)
        setContentView(binding.root)
        binding.lifecycleOwner = this
        binding.mainViewModel = viewModel

        setSupportActionBar(binding.toolbar)
        intiViewPager()
        initItemPlaying()
        initNavigation()

        viewModel.getBinder().observe(this, {
            if (it != null) {
                Log.d(myTag, "onChange: connected to  service")
                mService = it.getService()
                mService!!.songPlaying.observe(this, { song ->
                    viewModel.initViewSongPlaying(song)
                })
                mService!!.isPlaying.observe(this, { isPl ->
                    viewModel.setIsPlaying(isPl)
                })
            } else {
                Log.d(myTag, "onChange: unbound from service")
                mService = null
            }
        })

    }

    override fun onStart() {
        super.onStart()
        Log.d(myTag, "onStart activity dang ky service va broast cast")
        val intentService = Intent(this, MusicControllerService::class.java)
        bindService(intentService, viewModel.getServiceConnection(), Context.BIND_AUTO_CREATE)
    }

    override fun onDestroy() {
        super.onDestroy()
        unbindService(viewModel.getServiceConnection())
    }

    override fun onBackPressed() {
        if (binding.viewPager.currentItem == 0) {
            super.onBackPressed()
        } else {
            binding.viewPager.currentItem = binding.viewPager.currentItem - 1
        }
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            R.id.btnSearch -> {
                startSearchActivity()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }

    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        menuInflater.inflate(R.menu.menu_toolbar, menu)
        return true
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        Log.d(
            myTag, "onRequestPermissionsResult"
        )
        when (requestCode) {
            STORAGE_REQUEST_CODE -> {
                if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    //permission granted
                    Toast.makeText(this, "Allow...", Toast.LENGTH_SHORT)
                        .show()
                    viewModel.getAllSongOffline()
                } else {
                    //permission denied
                    Toast.makeText(this, "Storage permission required...", Toast.LENGTH_SHORT)
                        .show()
                    finish()
                }
            }
        }
    }

    private fun doPlay(data: Intent?) {
        val bundle = data?.getBundleExtra("bundle")
        if (bundle != null) {
            val result = bundle.getSerializable("songSearch") as SongCustom
            val playList = Playlist("SHEARCH", "Search", null, listOf(result))
            viewModel.playSong(playList, 0)
            val intent = Intent(this, PlaySongActivity::class.java)
            startActivity(intent)
        }
    }

    private fun startSearchActivity() {
        val intent = Intent(this, SearchActivity::class.java)
        resultLauncher.launch(intent)
    }

    private fun intiViewPager() {
        binding.viewPager.adapter = PagerAdapter(supportFragmentManager)
        binding.viewPager.addOnPageChangeListener(object : ViewPager.OnPageChangeListener {
            override fun onPageScrolled(
                position: Int,
                positionOffset: Float,
                positionOffsetPixels: Int
            ) {

            }

            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> {
                        binding.navView.menu.findItem(R.id.navigation_offline).isChecked = true
                        supportActionBar?.setTitle(R.string.title_offline)
                    }
                    1 -> {
                        binding.navView.menu.findItem(R.id.navigation_chart).isChecked = true
                        supportActionBar?.setTitle(R.string.title_chart)
                    }
                    2 -> {
                        binding.navView.menu.findItem(R.id.navigation_favorite).isChecked = true
                        supportActionBar?.setTitle(R.string.title_favorite)
                    }
                }
            }

            override fun onPageScrollStateChanged(state: Int) {

            }

        })
    }

    private fun initNavigation() {
        binding.navView.setOnItemSelectedListener {
            when (it.itemId) {
                R.id.navigation_chart -> {
                    binding.viewPager.currentItem = 1
                    true
                }
                R.id.navigation_favorite -> {
                    binding.viewPager.currentItem = 2
                    true
                }
                else -> {
                    binding.viewPager.currentItem = 0
                    true
                }
            }
        }
    }

    private fun initItemPlaying() {
        viewModel.visibility.observe(this, {
            if (it) binding.itemPlaying.visibility = View.VISIBLE
            else binding.itemPlaying.visibility = View.GONE
        })
        viewModel.imgPlaying.observe(this, {
            binding.imgSong.setImageBitmap(it)
        })
        viewModel.isPlaying.observe(this, {
            if (it) binding.btnPlayPause.setImageResource(R.drawable.ic_pause)
            else binding.btnPlayPause.setImageResource(R.drawable.ic_play)
        })

        binding.itemPlaying.setOnClickListener {
            val intent = Intent(this, PlaySongActivity::class.java)
            val mService = viewModel.mBinder.value?.getService()
            val bundle = Bundle()
            if (mService != null)
                bundle.putSerializable("play", mService?.songs[mService?.songPos])
            intent.putExtra("play_playlist", bundle)
            startActivity(intent)
        }
    }

    private inner class PagerAdapter(fa: FragmentManager) : FragmentPagerAdapter(fa) {
        override fun getCount(): Int = NUM_PAGES

        override fun getItem(position: Int): Fragment {
            return when (position) {
                1 -> ChartMusicFragment()
                2 -> FavoritesFragment()
                else -> HomeFragment()
            }
        }
    }
}