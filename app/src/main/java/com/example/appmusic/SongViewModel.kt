package com.example.appmusic

import android.content.ContentResolver
import android.os.CountDownTimer
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import java.util.logging.Handler

class SongViewModel: ViewModel() {
    var listSong: MutableLiveData<ArrayList<Song>> = MutableLiveData()
    var songLength: MutableLiveData<Int> = MutableLiveData()
    lateinit var countDownTimer: CountDownTimer

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
    fun runASong(songLen : Int, positionCurrent: Int){
        this.songLength.value = 0
        var count = 0
        countDownTimer = object : CountDownTimer((songLen-positionCurrent)*1000L,1000L){
            override fun onFinish() {
                songLength.value = songLen
            }
            override fun onTick(p0: Long) {
                songLength.value = ++count + positionCurrent
            }

        }.start()
    }
}