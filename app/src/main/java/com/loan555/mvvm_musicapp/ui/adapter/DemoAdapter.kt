package com.loan555.mvvm_musicapp.ui.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.LinearLayout
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.loan555.mvvm_musicapp.R
import com.loan555.mvvm_musicapp.model.SongCustom

class DemoAdapter(
    private val context: Context,
    private val onClick: (SongCustom) -> Unit,
    private val onDelete: (SongCustom) -> Unit
) :
    RecyclerView.Adapter<DemoAdapter.NoteViewHolder>() {
    private var notes: List<SongCustom> = listOf()

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val txtTitle: TextView = itemView.findViewById(R.id.textView)
        private val layoutItem: LinearLayout = itemView.findViewById(R.id.layoutItem)
        fun onBind(song: SongCustom) {
            txtTitle.text = song.toString()
            layoutItem.setOnClickListener { onClick(song) }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val itemView =
            LayoutInflater.from(context).inflate(R.layout.item_view_note_adapter, parent, false)
        return NoteViewHolder(itemView)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.onBind(notes[position])
    }

    override fun getItemCount(): Int = notes.size

    fun setNotes(song: List<SongCustom>) {
        this.notes = song
        notifyDataSetChanged()
    }
}