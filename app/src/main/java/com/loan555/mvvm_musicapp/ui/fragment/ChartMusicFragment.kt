package com.loan555.mvvm_musicapp.ui.fragment

import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.loan555.mvvm_musicapp.R
import com.loan555.mvvm_musicapp.databinding.ChartMusicFragmentBinding
import com.loan555.mvvm_musicapp.model.Playlist
import com.loan555.mvvm_musicapp.model.SongCustom
import com.loan555.mvvm_musicapp.ui.activity.PlaySongActivity
import com.loan555.mvvm_musicapp.ui.adapter.SongAdapter
import com.loan555.mvvm_musicapp.ui.viewmodel.AppViewModel
import java.lang.Exception

class ChartMusicFragment : Fragment() {

    companion object {
        fun newInstance() = ChartMusicFragment()
    }

    private lateinit var binding: ChartMusicFragmentBinding

    private lateinit var viewModel: AppViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.chart_music_fragment, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel = activity?.let {
            ViewModelProviders.of(it).get(AppViewModel::class.java)
        } ?: throw Exception("Activity is null")

        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        initControl()
        initEvent()
        initData()
    }

    private fun initControl() {
        val adapter = SongAdapter(
            requireContext(),
            this.activity?.application, onItemClick
        )
        viewModel.getAllSongChart().observe(viewLifecycleOwner, {
            adapter.setSongs(it)
            binding.swipeRefresh.isRefreshing = false
        })
        binding.recycleSong.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recycleSong.adapter = adapter
    }

    private fun initEvent() {
        binding.swipeRefresh.setOnRefreshListener {
            initData()
        }
    }

    private fun initData() {
        binding.swipeRefresh.isRefreshing = true
        viewModel.loadAllSongChart()
    }

    private val onItemClick: (Int, List<SongCustom>) -> Unit = { pos, songs ->
        val playlist = Playlist("Chart", "Bang xep hang", null, songs)// cho nay can toi uu nha
        viewModel.playSong(playlist, pos)
        val intent = Intent(this.requireContext(), PlaySongActivity::class.java)
        startActivity(intent)
    }
}