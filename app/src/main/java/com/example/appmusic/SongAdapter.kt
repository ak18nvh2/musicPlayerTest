package com.example.appmusic

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ArrayAdapter
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class SongAdapter(var mContext: Context,var iRecyclerViewWithActivity: IRecyclerViewWithActivity): RecyclerView.Adapter<SongAdapter.ViewHolder>() {
    private var listSong = ArrayList<Song>()
    fun setList( list: ArrayList<Song>){
        this.listSong = list
        notifyDataSetChanged()
    }
    class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val songName = itemView.findViewById<TextView>(R.id.tv_SongName)
        val songLocation = itemView.findViewById<TextView>(R.id.tv_SongLocation)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        return ViewHolder(LayoutInflater.from(mContext).inflate(R.layout.item_list_song, parent, false))
    }

    override fun getItemCount(): Int {
        return this.listSong.size
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        holder.songLocation.text = listSong[position].songLocation
        holder.songName.text = listSong[position].songName
        holder.songName.setOnClickListener(){
            iRecyclerViewWithActivity.onSongNameClick(listSong[position])
        }
    }
    interface IRecyclerViewWithActivity{
        fun onSongNameClick(song: Song)
    }
}