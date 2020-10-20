package com.example.appmusic.viewmodels


import android.os.CountDownTimer
import android.util.Log
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.appmusic.models.Episodes
import com.example.appmusic.models.FileJson
import com.example.appmusic.models.Song
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SongViewModel : ViewModel() {

    var songName: MutableLiveData<String> = MutableLiveData()
    var currentLength: MutableLiveData<String> = MutableLiveData()
    var songLength: MutableLiveData<String> = MutableLiveData()
    var currentProcess: MutableLiveData<Int> = MutableLiveData()
    var notification: MutableLiveData<String> = MutableLiveData()
    var listSong: MutableLiveData<List<Song>> = MutableLiveData()
    private var isPlaying = false
    lateinit var countDownTimer: CountDownTimer

    private fun changeEpisodeToSong(e: Episodes): Song {
        val song = Song()
        song.songName = e.name
        song.songLocation = e.audio_preview_url
        return song
    }
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
                            Log.d("muusic", songName+currentLength.value)
                        }
                        override fun onTick(p0: Long) {
                            currentProcess.value = ++count + positionCurrent
                            val curLengthString = changeTimeFromIntToString(count*1000 + positionCurrent*1000)
                            if (curLengthString > changeTimeFromIntToString(songLen)) {
                                currentLength.value = changeTimeFromIntToString(songLen)
                            } else {
                                currentLength.value = curLengthString
                            }
                            Log.d("muusic", songName+currentLength.value+" pos"+positionCurrent)

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

    fun getAllEpisodes(callGet: Call<FileJson>) {
        callGet.enqueue(object: Callback<FileJson> {
            override fun onFailure(call: Call<FileJson>, t: Throwable) {
                if (callGet.isCanceled) {
                    notification.value = "Canceled successful!"
                } else {
                    notification.value = "Can't load data, please try again!"
                }
            }

            override fun onResponse(call: Call<FileJson>, response: Response<FileJson>) {
                if (response.isSuccessful) {
                    val mListEpisodes = response.body()?.episodes
                    var mArrayListSong : ArrayList<Song> = ArrayList()
                    mListEpisodes?.forEachIndexed { _, episodes ->
                        mArrayListSong.add(changeEpisodeToSong(episodes))
                    }
                    listSong.value = mArrayListSong
                    notification.value = "Load successful!"
                } else {
                    notification.value = "Can't load data, please try again!"
                }
            }

        })
    }
}