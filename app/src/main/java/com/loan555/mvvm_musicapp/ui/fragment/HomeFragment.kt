package com.loan555.mvvm_musicapp.ui.fragment

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
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
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions
import com.bumptech.glide.request.target.CustomTarget
import com.bumptech.glide.request.transition.Transition
import com.loan555.mvvm_musicapp.R
import com.loan555.mvvm_musicapp.databinding.HomeFragmentBinding
import com.loan555.mvvm_musicapp.model.Playlist
import com.loan555.mvvm_musicapp.model.SongCustom
import com.loan555.mvvm_musicapp.ui.activity.PlaySongActivity
import com.loan555.mvvm_musicapp.ui.adapter.SongAdapter
import com.loan555.mvvm_musicapp.ui.viewmodel.AppViewModel
import java.io.IOException
import java.lang.Exception

const val STORAGE_REQUEST_CODE = 1
const val myTag = "myTag"

class HomeFragment : Fragment() {

    companion object {
        fun newInstance() = HomeFragment()
    }

    private lateinit var viewModel: AppViewModel

    private lateinit var binding: HomeFragmentBinding
    private val storagePermission = arrayOf(Manifest.permission.WRITE_EXTERNAL_STORAGE)

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding = DataBindingUtil.inflate(inflater, R.layout.home_fragment, container, false)
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

    private fun initEvent() {

    }

    private fun initControl() {
        val adapter = SongAdapter(requireContext(), this.activity?.application, onItemClick)
        viewModel.getAllSongOffline().observe(viewLifecycleOwner, {
            binding.swipeRefresh.isRefreshing = false
            adapter.setSongs(it)
        })

        binding.recycleSong.layoutManager =
            LinearLayoutManager(requireContext(), LinearLayoutManager.VERTICAL, false)
        binding.recycleSong.adapter = adapter

        binding.swipeRefresh.setOnRefreshListener {
            viewModel.loadAllSongOffline()
        }
    }

    private fun initData() {
        if (checkStoragePermission()) {
            viewModel.loadAllSongOffline()
        } else requestStoragePermission()
    }

    private fun checkStoragePermission(): Boolean {
        Log.e(myTag, "check permission")
        return ContextCompat.checkSelfPermission(
            requireContext(), Manifest.permission.WRITE_EXTERNAL_STORAGE
        ) == (PackageManager.PERMISSION_GRANTED)
    }

    private fun requestStoragePermission() {
        ActivityCompat.requestPermissions(
            requireActivity(),
            storagePermission,
            STORAGE_REQUEST_CODE
        )
    }

    private val onItemClick: (Int, List<SongCustom>) -> Unit = { pos, songs ->
        val playlist = Playlist("OFFLINE", "Nhac offline", null, songs)
        viewModel.playSong(playlist, pos)
        val intent = Intent(this.requireContext(), PlaySongActivity::class.java)
        startActivity(intent)
    }
}