package com.example.appmusic

import android.content.ContentResolver
import android.os.CountDownTimer
import android.provider.MediaStore
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SongViewModel : ViewModel() {
    var listSong: MutableLiveData<ArrayList<Song>> = MutableLiveData()
    var currentLength: MutableLiveData<Int> = MutableLiveData()
    var isPlaying = false
    lateinit var countDownTimer: CountDownTimer

    fun getAllSong(contentResolver: ContentResolver) {
        var arrayList = ArrayList<Song>()
        val contentResolver = contentResolver
        val songUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI
        val songCursor = contentResolver.query(songUri, null, null, null, null)
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

    fun runASong(songLen: Int, positionCurrent: Int, isPlay: Boolean) {
        if (isPlay) {
            if (isPlaying) {
                countDownTimer.cancel()
                isPlaying = false
            }
            this.currentLength.value = positionCurrent
            var count = 0
            countDownTimer =
                object : CountDownTimer((songLen - positionCurrent) * 1000L, 1000L) {
                    override fun onFinish() {
                        currentLength.value = songLen
                    }
                    override fun onTick(p0: Long) {
                        currentLength.value = ++count + positionCurrent
                    }
                }.start()
            isPlaying = true
        } else {
            if (isPlaying) {
                countDownTimer.cancel()
            }
        }
    }
}