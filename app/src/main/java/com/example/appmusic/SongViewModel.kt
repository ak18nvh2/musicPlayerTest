package com.example.appmusic


import android.os.CountDownTimer
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel

class SongViewModel : ViewModel() {

    var songName: MutableLiveData<String> = MutableLiveData()
    var currentLength: MutableLiveData<String> = MutableLiveData()
    var songLength: MutableLiveData<String> = MutableLiveData()
    var currentProcess: MutableLiveData<Int> = MutableLiveData()
    private var isPlaying = false
    lateinit var countDownTimer: CountDownTimer

    fun changeTimeFromIntToString(time: Int): String {
        val min = time / 60000
        val sec = (time / 1000) % 60
        val secString = if (sec < 10) "0$sec" else "$sec"
        val minString = if (min < 10) "0$min:" else "$min:"
        return minString + secString
    }

    fun runASong(songLen: Int, positionCurrent: Int, isPlay: Boolean, songName: String) {
        this.songName.value = songName
        this.songLength.value = changeTimeFromIntToString(songLen)
        if (isPlay) {
            if (isPlaying) {
                countDownTimer.cancel()
                isPlaying = false
            }
            var count = 0
            countDownTimer =
                object : CountDownTimer((songLen - positionCurrent).toLong(), 1000L) {
                    override fun onFinish() {
                        currentLength.value = changeTimeFromIntToString(songLen)
                        currentProcess.value = songLen
                    }

                    override fun onTick(p0: Long) {
                        currentProcess.value = ++count + positionCurrent
                        currentLength.value = changeTimeFromIntToString(count*1000 + positionCurrent)
                    }
                }.start()
            isPlaying = true
        } else {
            currentLength.value = changeTimeFromIntToString(positionCurrent)
            currentProcess.value = positionCurrent
            if (isPlaying) {
                isPlaying = false
                countDownTimer.cancel()
            }
        }
    }
}