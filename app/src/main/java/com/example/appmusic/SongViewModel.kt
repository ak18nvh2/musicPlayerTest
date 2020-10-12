package com.example.appmusic


import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SongViewModel : ViewModel() {

    var songName: MutableLiveData<String> = MutableLiveData()
    var currentLength: MutableLiveData<Int> = MutableLiveData()
    var isPlaying = false
    lateinit var countDownTimer: CountDownTimer

    fun runASong(songLen: Int, positionCurrent: Int, isPlay: Boolean,songName: String) {
        this.songName.value = songName
        if (isPlay) {
            if (isPlaying) {
                countDownTimer.cancel()
                isPlaying = false
            }
            var count = 0
            countDownTimer =
                object : CountDownTimer((songLen - positionCurrent) * 1000L, 1000L) {
                    override fun onFinish() {
                        currentLength.value = songLen
                    }
                    override fun onTick(p0: Long) {
                        currentLength.value = count++ + positionCurrent
                    }
                }.start()
            isPlaying = true
        } else {
            currentLength.value = positionCurrent
            if (isPlaying) {
                isPlaying = false
                countDownTimer.cancel()
            }
        }
    }
}