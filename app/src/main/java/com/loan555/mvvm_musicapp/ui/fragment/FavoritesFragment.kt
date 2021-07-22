package com.loan555.mvvm_musicapp.ui.fragment

import android.content.Intent
import android.graphics.Bitmap
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Build
import androidx.lifecycle.ViewModelProvider
import android.os.Bundle
import android.util.Log
import android.util.Size
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.loan555.mvvm_musicapp.R
import com.loan555.mvvm_musicapp.databinding.FavoritesFragmentBinding
import com.loan555.mvvm_musicapp.model.Playlist
import com.loan555.mvvm_musicapp.model.SongCustom
import com.loan555.mvvm_musicapp.ui.activity.PlaySongActivity
import com.loan555.mvvm_musicapp.ui.adapter.DemoAdapter
import com.loan555.mvvm_musicapp.ui.adapter.SongAdapter
import com.loan555.mvvm_musicapp.ui.viewmodel.AppViewModel
import java.io.IOException
import java.lang.Exception

class FavoritesFragment : Fragment() {

    companion object {
        fun newInstance() = FavoritesFragment()
    }

    private lateinit var viewModel: AppViewModel
    private lateinit var binding: FavoritesFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.favorites_fragment, container, false)
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
        initData()
        initEvent()
    }


    private fun initControl() {
        val adapter = SongAdapter(requireContext(), this.activity?.application, onItemClick)
        viewModel.loadAllSongFavorite().observe(viewLifecycleOwner, {
            adapter.setSongs(it)
            binding.swipeRefresh.isRefreshing = false
        })
        binding.recycleSong.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recycleSong.adapter = adapter
    }

    private fun initData() {

    }

    private fun initEvent() {
        binding.swipeRefresh.setOnRefreshListener {
            binding.swipeRefresh.isRefreshing = false
        }
    }

    private val onItemClick: (Int, List<SongCustom>) -> Unit = { pos, songs ->
        val playlist = Playlist("FAVORITE", "Nhac yeu thich", null, songs)
        viewModel.playSong(playlist, pos)
        val intent = Intent(this.requireContext(), PlaySongActivity::class.java)
        startActivity(intent)
    }

}