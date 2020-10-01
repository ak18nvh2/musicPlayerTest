package com.example.appmusic

import android.content.ContentResolver
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SongViewModel: ViewModel() {
    var listSong: MutableLiveData<ArrayList<Song>> = MutableLiveData()
    fun getAllSong(contentResolver: ContentResolver){
        var arrayList = ArrayList<Song>()
        val contentResolver = contentResolver
        val songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val songCursor = contentResolver.query(songUri,null,null,null,null)
        if (songCursor != null && songCursor.moveToFirst()) {
            var songName = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            var songLocation = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            do {
                var currentName = songCursor.getString(songName)
                var currentLocation = songCursor.getString(songLocation)
                var song = Song()
                song.songLocation = currentLocation
                song.songName = currentName
                arrayList.add(song)
            } while (songCursor.moveToNext())
        }
        listSong.value = arrayList
    }
}