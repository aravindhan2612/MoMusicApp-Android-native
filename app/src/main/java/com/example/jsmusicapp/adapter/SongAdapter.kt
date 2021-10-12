package com.example.jsmusicapp.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.appcompat.content.res.AppCompatResources
import androidx.recyclerview.widget.RecyclerView
import com.example.jsmusicapp.R
import com.example.jsmusicapp.databinding.SongViewItemBinding
import com.example.jsmusicapp.entities.Song
import com.example.jsmusicapp.fragments.MainFragment
import java.util.*


class SongAdapter(
    private val context: Context,
    private var songList: ArrayList<Song>,
    private val mainFragment: MainFragment
) :
    RecyclerView.Adapter<SongAdapter.SongViewHolder>() {
    private lateinit var songViewItemBinding: SongViewItemBinding

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): SongViewHolder {
        songViewItemBinding =
            SongViewItemBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return SongViewHolder(songViewItemBinding)
    }

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song: Song? = songList?.get(position)
        song?.let {
            if (it.image != null) {
                holder.songViewItemBinding.itemImag.setImageBitmap(it.image)
            } else {
                holder.songViewItemBinding.itemImag.setImageDrawable(
                    AppCompatResources.getDrawable(
                        context,
                        R.drawable.ic_music_icon
                    )
                )
            }
            holder.songViewItemBinding.singer.text = it.singer
            holder.songViewItemBinding.songName.text = it.songName
            holder.songViewItemBinding.songName.isSelected = true
            holder.songViewItemBinding.singer.isSelected = true
            holder.songViewItemBinding.playing.visibility = if (it.isPlaying) View.VISIBLE else View.GONE
            //holder.songViewItemBinding.cardView.setBackgroundColor(randomColorGenerator())
        }
    }

    override fun getItemCount(): Int {
        return songList.size
    }

    inner class SongViewHolder(private val binding: SongViewItemBinding) :
        RecyclerView.ViewHolder(binding.root) {
        val songViewItemBinding = binding

        init {
            songViewItemBinding.cardView.setOnClickListener {
                mainFragment.navigate(bindingAdapterPosition)
            }
        }
    }

    fun filterList(filterllist: ArrayList<Song>) {
        // below line is to add our filtered
        // list in our course array list.
        songList = filterllist
        // below line is to notify our adapter
        // as change in recycler view data.
        notifyDataSetChanged()
    }
}