package com.example.appmusic

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SongAdapter(var mContext: Context,var iRecyclerViewWithActivity: IRecyclerViewWithActivity): RecyclerView.Adapter<SongAdapter.ViewHolder>() {
    private var mListSong = ArrayList<Song>()
    private var mCurrentPositionChangeColor: Int = -1
    private var mPrePositionChangeColor: Int = -1

    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val songName: TextView = itemView.findViewById(R.id.tv_SongName)
    }

    fun setPositionChangeColor(position: Int,nameSongInPosition: String){
        this.mPrePositionChangeColor = this.mCurrentPositionChangeColor
        this.mCurrentPositionChangeColor = position
        notifyDataSetChanged()

    }
    fun setList( list: ArrayList<Song>){
        this.mListSong = list
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_list_song, parent, false))
    }

    override fun getItemCount(): Int {
        return this.mListSong.size
    }


    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.songName.text = mListSong[position].songName
        holder.itemView.setOnClickListener(){
            iRecyclerViewWithActivity.onSongNameClick(mListSong[position], position)
        }
        if (position == mCurrentPositionChangeColor) {
            holder.itemView.setBackgroundColor(Color.rgb(102,102,255))
            holder.songName.setTextColor(Color.WHITE)
        } else{
            holder.itemView.setBackgroundColor(Color.WHITE)
            holder.songName.setTextColor(Color.BLACK)
        }


    }
    interface IRecyclerViewWithActivity{
        fun onSongNameClick(song: Song, pos: Int)
    }
}