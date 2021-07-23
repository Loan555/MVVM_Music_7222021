package com.loan555.mvvm_musicapp.ui.fragment

import android.app.AlertDialog
import android.app.Dialog
import android.app.Notification
import android.content.DialogInterface
import android.content.Intent
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.PopupMenu
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import androidx.recyclerview.widget.LinearLayoutManager
import com.loan555.mvvm_musicapp.R
import com.loan555.mvvm_musicapp.databinding.FavoritesFragmentBinding
import com.loan555.mvvm_musicapp.model.Playlist
import com.loan555.mvvm_musicapp.model.SongCustom
import com.loan555.mvvm_musicapp.ui.activity.PlaySongActivity
import com.loan555.mvvm_musicapp.ui.adapter.SongAdapter
import com.loan555.mvvm_musicapp.ui.adapter.SongCustomAdapter_2
import com.loan555.mvvm_musicapp.ui.viewmodel.AppViewModel
import kotlinx.android.synthetic.main.item_view_note_adapter.*
import java.lang.Exception

class FavoritesFragment : Fragment() {

    companion object {
        fun newInstance() = FavoritesFragment()
    }

    private lateinit var viewModel: AppViewModel
    private lateinit var binding: FavoritesFragmentBinding

    private val adapter: SongCustomAdapter_2 by lazy {
        SongCustomAdapter_2(
            requireContext(),
            this.activity?.application,
            onItemClick,
            onBtnMoreClick
        )
    }

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

    private val onBtnMoreClick: (Int, List<SongCustom>, view: View) -> Unit = { pos, songs, v ->
        val popupMenu = PopupMenu(this.context, v)
        popupMenu.inflate(R.menu.popu_menu)
        popupMenu.setOnMenuItemClickListener {
            when (it.itemId) {
                R.id.popup_detail -> {
                    val builder = AlertDialog.Builder(this.requireContext())
                    builder.setTitle(songs[pos].title).setMessage("${songs[pos]}")
                    builder.setCancelable(true)
                    builder.setIcon(R.drawable.musical_note_icon)
                    builder.setNegativeButton(
                        "OK"
                    ) { dialog, _ ->
                        dialog.cancel()
                    }
                    builder.create().show()
                }
                R.id.popup_delete -> {
                    viewModel.deleteFavoriteSong(songs[pos])
                    viewModel.loadAllSongFavorite()
                }
            }
            true
        }
        popupMenu.show()
    }

}