package com.loan555.mvvm_musicapp.ui.adapter

import android.app.Application
import android.content.Context
import android.graphics.Bitmap
import android.net.Uri
import android.os.Build
import android.util.Log
import android.util.Size
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.recyclerview.widget.RecyclerView
import com.bumptech.glide.Glide
import com.loan555.mvvm_musicapp.R
import com.loan555.mvvm_musicapp.model.SongCustom
import com.loan555.mvvm_musicapp.ui.fragment.myTag
import java.io.IOException

class SongCustomAdapter_2(
    private val context: Context,
    private val application: Application?,
    private val onClick: (Int, List<SongCustom>) -> Unit,
    private val onDetailClick: (Int, List<SongCustom>,view: View) -> Unit
) :
    RecyclerView.Adapter<SongCustomAdapter_2.NoteViewHolder>() {
    private var list: List<SongCustom> = listOf()

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtName: TextView = itemView.findViewById(R.id.song_name)
        private val imgNote: ImageView = itemView.findViewById(R.id.img_song)
        private val artist: TextView = itemView.findViewById(R.id.artists_names)
        private val time: TextView = itemView.findViewById(R.id.duration)
        private val btnMoreClick: ImageButton = itemView.findViewById(R.id.btnMore)
        private val layoutItem: ConstraintLayout = itemView.findViewById(R.id.layoutItemSong)
        fun onBind(song: SongCustom) {
            txtName.text = song.title
            artist.text = song.artistsNames
            time.text = song.timeToString()
            loadImg(song, imgNote)
            layoutItem.setOnClickListener { onClick(layoutPosition, list) }
            btnMoreClick.setOnClickListener { onDetailClick(layoutPosition, list, btnMoreClick) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val itemView =
            LayoutInflater.from(context).inflate(R.layout.song_custom_adapter_2, parent, false)
        return NoteViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.onBind(list[position])
    }

    override fun getItemCount(): Int = list.size

    fun setSongs(song: List<SongCustom>) {
        this.list = song
        notifyDataSetChanged()
    }

    fun loadImg(song: SongCustom, imgNote: ImageView) {
        if (song.thumbnail != null) {
            Glide.with(context).load(song.thumbnail).into(imgNote)
        } else {
            if (application != null) {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    try {
                        val thumb =
                            context.applicationContext.contentResolver.loadThumbnail(
                                Uri.parse(song.uri), Size(640, 480), null
                            )
                        val bitmap: Bitmap = thumb
                        Glide.with(context).load(bitmap).into(imgNote)
                    } catch (e: IOException) {
                        Log.e(myTag, "can't find bitmap: ${e.message}")
                        Glide.with(context).load(R.drawable.musical_note_icon).into(imgNote)
                    }
                } else
                    Glide.with(context).load(R.drawable.musical_note_icon).into(imgNote)

            }
        }
    }

}