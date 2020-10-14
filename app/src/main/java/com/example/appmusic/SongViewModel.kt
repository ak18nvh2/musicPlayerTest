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
        if (songLen == positionCurrent) {
            this.songLength.value = changeTimeFromIntToString(songLen)
            currentLength.value = changeTimeFromIntToString(songLen)
            currentProcess.value = songLen

        } else {
            this.songName.value = songName
            this.songLength.value = changeTimeFromIntToString(songLen)
            if (isPlay) {
                if (isPlaying) {
                    countDownTimer.cancel()
                    isPlaying = false
                }
                var count = 0
                isPlaying = true
                countDownTimer =
                    object : CountDownTimer((songLen - positionCurrent*1000).toLong(), 1000L) {
                        override fun onFinish() {
                            currentLength.value = changeTimeFromIntToString(songLen)
                            currentProcess.value = songLen
                        }
                        override fun onTick(p0: Long) {
                            currentProcess.value = ++count + positionCurrent
                            val curLengthString = changeTimeFromIntToString(count*1000 + positionCurrent*1000)
                            if (curLengthString > changeTimeFromIntToString(songLen)) {
                                currentLength.value = changeTimeFromIntToString(songLen)
                            } else {
                                currentLength.value = curLengthString
                            }

                        }
                    }.start()

            } else {
                currentLength.value = changeTimeFromIntToString(positionCurrent*1000)
                currentProcess.value = positionCurrent
                if (isPlaying) {
                    isPlaying = false
                    countDownTimer.cancel()
                }
            }
        }

    }
}