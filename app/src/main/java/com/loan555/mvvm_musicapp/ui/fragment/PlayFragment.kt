package com.loan555.mvvm_musicapp.ui.fragment

import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import android.widget.Toast
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.ViewModelProviders
import com.loan555.mvvm_musicapp.R
import com.loan555.mvvm_musicapp.databinding.PlayFragmentBinding
import com.loan555.mvvm_musicapp.ui.viewmodel.AppViewModel
import java.lang.Exception

class PlayFragment : Fragment() {

    companion object {
        fun newInstance() = PlayFragment()
    }

    private lateinit var viewModel: AppViewModel

    private lateinit var binding: PlayFragmentBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        viewModel = activity?.let {
            ViewModelProviders.of(it).get(AppViewModel::class.java)
        } ?: throw Exception("Activity is null")

        binding = DataBindingUtil.inflate(inflater, R.layout.play_fragment, container, false)
        binding.lifecycleOwner = this
        binding.viewModel = viewModel
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initController()
        initEvent()
    }

    private fun initController() {
        viewModel.isPlaying.observe(viewLifecycleOwner, {
            if (it) binding.play.setBackgroundResource(R.drawable.ic_pause)
            else binding.play.setBackgroundResource(R.drawable.ic_play)
        })
        viewModel.imgPlaying.observe(viewLifecycleOwner, {
            binding.imgSrc.setImageBitmap(it)
        })
        viewModel.isLocal.observe(viewLifecycleOwner, {
            if (it) binding.btnDownload.setBackgroundResource(R.drawable.ic_download_color)
            else binding.btnDownload.setBackgroundResource(R.drawable.ic_download)
        })
        viewModel.isFavorite.observe(viewLifecycleOwner, {
            if (it) binding.likeBtn.setBackgroundResource(R.drawable.ic_favorite_border_color)
            else binding.likeBtn.setBackgroundResource(R.drawable.ic_favorite)
        })
        viewModel.statePlay.observe(viewLifecycleOwner, {
            when (it) {
                //0 la tuan tu roi ket thuc
                //1 la lap lai list
                //2 la phat ngau nhien
                //3 lap lai 1 bai
                0 -> {
                    binding.loop.setBackgroundResource(R.drawable.ic_repeat)
                }
                1 -> {
                    binding.loop.setBackgroundResource(R.drawable.ic_baseline_repeat_24_color)
                }
                2 -> {
                    binding.loop.setBackgroundResource(R.drawable.ic_baseline_shuffle_24)
                }
                3 -> {
                    binding.loop.setBackgroundResource(R.drawable.ic_baseline_repeat_one_24)
                }
            }
        })
    }

    private fun initEvent() {
        binding.btnDownload.setOnClickListener {
            val songPlaying =
                viewModel.mBinder.value!!.getService().songs[viewModel.mBinder.value!!.getService().songPos]
            if (songPlaying.thumbnail != null)
                songPlaying.downLoad(requireContext())
            else Toast.makeText(this.context, "Bài hát đã có trong thiết bị", Toast.LENGTH_SHORT)
                .show()
        }
        var currentProcess = 0
        binding.seekBar.setOnSeekBarChangeListener(object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
                currentProcess = progress
            }

            override fun onStartTrackingTouch(seekBar: SeekBar?) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar?) {
                viewModel.seekTo(currentProcess)
            }

        })
    }
}