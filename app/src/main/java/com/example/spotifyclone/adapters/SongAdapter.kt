package com.example.spotifyclone.adapters

import androidx.recyclerview.widget.AsyncListDiffer
import com.bumptech.glide.RequestManager
import com.example.spotifyclone.R
import com.plcoding.spotifycloneyt.adapters.BaseSongAdapter

import javax.inject.Inject

class SongAdapter @Inject constructor(
    private val glide: RequestManager
) : BaseSongAdapter(R.layout.list_item) {

    override val differ = AsyncListDiffer(this, diffCallback)

    override fun onBindViewHolder(holder: SongViewHolder, position: Int) {
        val song = songs[position]
        holder.itemView.apply {
            holder.TVTitlePlaylist.text = song.title
            holder.TVtitle.text = song.subtitle
            glide.load(song.imageUrl).into(holder.IVImage)

            setOnClickListener {
                onItemClickListener?.let { click ->
                    click(song)
                }
            }
        }
    }

}



















