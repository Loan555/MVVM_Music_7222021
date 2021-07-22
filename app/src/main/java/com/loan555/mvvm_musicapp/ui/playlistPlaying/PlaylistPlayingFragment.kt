package com.loan555.mvvm_musicapp.ui.playlistPlaying

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.loan555.musicapplication.service.MusicControllerService
import com.loan555.mvvm_musicapp.R
import com.loan555.mvvm_musicapp.databinding.PlaylistPlayingFragmentBinding
import com.loan555.mvvm_musicapp.model.Playlist
import com.loan555.mvvm_musicapp.model.SongCustom
import com.loan555.mvvm_musicapp.ui.activity.PlaySongActivity
import com.loan555.mvvm_musicapp.ui.adapter.SongAdapter
import com.loan555.mvvm_musicapp.ui.fragment.myTag
import com.loan555.mvvm_musicapp.ui.viewmodel.AppViewModel
import java.lang.Exception

class PlaylistPlayingFragment : Fragment() {

    companion object {
        fun newInstance() = PlaylistPlayingFragment()
    }

    private lateinit var viewModel: AppViewModel
    private lateinit var binding: PlaylistPlayingFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = activity?.let {
            ViewModelProviders.of(it).get(AppViewModel::class.java)
        } ?: throw Exception("Activity is null")
        binding =
            DataBindingUtil.inflate(inflater, R.layout.playlist_playing_fragment, container, false)
        binding.viewModel = viewModel
        binding.lifecycleOwner = this
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.getBinder().observe(viewLifecycleOwner, {
            Log.d(myTag, "get binder? ")
            if (it != null) {
                val mService: MusicControllerService = it.getService()
                mService.songPlaying.observe(viewLifecycleOwner, { song ->
                    if (song != null) {
                        viewModel.loadRelate(song.id)
                    }
                })
            }
        })

        initController()
        intiEvent()
        initData()
    }

    private fun initController() {
        binding.playingList.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)
        viewModel.songsPlaying.observe(viewLifecycleOwner, {
            if (it != null) {
                binding.playingList.adapter =
                    SongAdapter(
                        this.requireContext(),
                        this.activity?.application,
                        onItemClick
                    ).apply { setSongs(it) }
            } else {
                binding.playingList.adapter =
                    SongAdapter(
                        this.requireContext(),
                        this.activity?.application,
                        onItemClick
                    )
            }
        })

        binding.relateList.layoutManager =
            LinearLayoutManager(this.context, LinearLayoutManager.VERTICAL, false)
        viewModel.getRelateSong().observe(viewLifecycleOwner, {
            if (it != null) {
                binding.relateList.adapter =
                    SongAdapter(
                        this.requireContext(),
                        this.activity?.application,
                        onItemRelateClick
                    ).apply { setSongs(it) }
            } else {
                binding.relateList.adapter =
                    SongAdapter(
                        this.requireContext(),
                        this.activity?.application,
                        onItemRelateClick
                    )
            }
        })
    }

    private fun intiEvent() {
        binding.swipeRefresh.setOnRefreshListener {
            initData()
        }
    }


    private fun initData() {
        viewModel.handSongsPlaying()
        binding.swipeRefresh.isRefreshing = false
    }

    private val onItemClick: (Int, List<SongCustom>) -> Unit = { pos, songs ->
        val playlist =
            Playlist("Playing", "Danh sach dang phat", null, songs)// cho nay can toi uu nha
        viewModel.playSong(playlist, pos)
        viewModel.setCurrentPager(0)
    }

    private val onItemRelateClick: (Int, List<SongCustom>) -> Unit = { pos, songs ->
        val new: List<SongCustom> = mutableListOf(songs[pos])
        val playlist = Playlist("Relate", "Bai hat lien quan", null, new)// cho nay can toi uu nha
        viewModel.playSong(playlist, 0)
        viewModel.setCurrentPager(0)
    }
}