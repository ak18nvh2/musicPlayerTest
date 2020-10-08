package com.example.appmusic


import android.Manifest
import android.annotation.SuppressLint
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.content.IntentFilter
import android.content.pm.PackageManager
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import kotlinx.android.synthetic.main.activity_main.*


class MainActivity : AppCompatActivity(), SongAdapter.IRecyclerViewWithActivity,
    View.OnClickListener {

    private lateinit var mSongAdapter: SongAdapter
    private val PERMISSION_REQUEST = 1
    private var mCountHandleClick = 0
    private lateinit var song: Song
    private lateinit var mIntentFilter: IntentFilter
    private var mSongViewModel = SongViewModel()
    private var mCountHandleSeekBar = 0
    private var mIsFirstStart = false
    private var mSongCurrent = 0
    private var mCountHandleRepeat = 0
    var arrayList: ArrayList<Song>? = null
    companion object {
        const val mBroadcastAction = "SEND_SONG_SIZE"
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mSongAdapter = SongAdapter(this, this)
        mIntentFilter = IntentFilter()
        mIntentFilter.addAction(mBroadcastAction)
        btn_Handle.setOnClickListener(this)
        btn_NextSong.setOnClickListener(this)
        btn_PreSong.setOnClickListener(this)
        btn_handleRepeat.setOnClickListener(this)
        setFocus(false)
        song = Song()
        arrayList = ArrayList()
        registerLiveDataListener()
        sb_SongHandler.setOnSeekBarChangeListener(@SuppressLint("AppCompatCustomView")
        object : SeekBar.OnSeekBarChangeListener {

            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {

                if (mCountHandleSeekBar == 0) {
                    mCountHandleSeekBar++
                } else {
                    song.pausePosition = i
                    runWithSeekBar(i)
                }
            }

            override fun onStartTrackingTouch(seekBar: SeekBar) {
            }

            override fun onStopTrackingTouch(seekBar: SeekBar) {
            }
        })

        val rvListSong = findViewById<RecyclerView>(R.id.rv_ListSong)
        rvListSong.layoutManager = LinearLayoutManager(this)
        rvListSong.adapter = mSongAdapter
        val arr = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arr, PERMISSION_REQUEST)
        } else {
            getMusic()
        }
    }
    @RequiresApi(Build.VERSION_CODES.O)
    private fun setFocus(boolean: Boolean){
        if (!boolean) {
            btn_Handle.visibility = View.GONE
            sb_SongHandler.visibility = View.GONE
            btn_handleRepeat.visibility = View.GONE
            btn_NextSong.visibility = View.GONE
            btn_PreSong.visibility = View.GONE
            tv_CurrentPosition.visibility = View.GONE
            tv_SongLength.visibility = View.GONE
        } else {
            btn_Handle.visibility = View.VISIBLE
            sb_SongHandler.visibility = View.VISIBLE
            btn_handleRepeat.visibility = View.VISIBLE
            btn_NextSong.visibility = View.VISIBLE
            btn_PreSong.visibility = View.VISIBLE
            tv_CurrentPosition.visibility = View.VISIBLE
            tv_SongLength.visibility = View.VISIBLE
        }
    }
    override fun onResume() {
        super.onResume()
        registerReceiver(mReceiver, mIntentFilter)
    }

    override fun onPause() {
        unregisterReceiver(mReceiver)
        super.onPause()
    }

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.action == mBroadcastAction) {
                when (intent.getIntExtra("STATUS", 0)) {
                    0 -> {
                        val sec = intent.getIntExtra("SECONDS", 0)
                        val min = intent.getIntExtra("MINUTES", 0)
                        val secString = if (sec < 10) {
                            "0$sec"
                        } else {
                            "$sec"
                        }
                        val minString = if (min < 10) {
                            "0$min:"
                        } else {
                            "$min:"
                        }
                        song.songLength = min * 60 + sec
                        tv_SongLength.text = minString + secString
                        sb_SongHandler.max = song.songLength
                        sb_SongHandler.min = 0
                        sb_SongHandler.progress = 0
                        mSongViewModel.runASong(song.songLength, 0, true, song.songName)
                        btn_Handle.setImageResource(R.drawable.pause)
                    }
                    1 -> {
                        val sec = intent.getIntExtra("SECONDS", 0)
                        val min = intent.getIntExtra("MINUTES", 0)
                        song.pausePosition = min * 60 + sec
                        mSongViewModel.runASong(song.songLength, song.pausePosition, false,song.songName)
                    }
                    2 -> {
                        mSongViewModel.runASong(song.songLength, song.pausePosition, true, song.songName)
                    }
                    3 -> {
                        mSongViewModel.runASong(
                            song.songLength,
                            song.pausePosition,
                            intent.getBooleanExtra("IS_PLAY", true),
                            song.songName
                        )
                    }
                    4 -> {
                        when {
                            mCountHandleRepeat % 3 == 1 -> {
                                // LAP LAI TAT CA
                                if (mSongCurrent == arrayList?.size!! - 1) {
                                    song = arrayList!![0]
                                    mSongCurrent = 0
                                } else {
                                    song = arrayList!![mSongCurrent + 1]
                                    mSongCurrent++
                                }
                                playNewMusic()
                                btn_handleRepeat.setImageResource(R.drawable.ic_baseline_repeat_24)
                            }
                            mCountHandleRepeat % 3 == 2 -> {
                                // lap lai bai dang play
                                btn_handleRepeat.setImageResource(R.drawable.ic_baseline_repeat_one_24)
                                playNewMusic()
                            }
                            else -> {
                                // het thi k lap
                                btn_handleRepeat.setImageResource(R.drawable.ic_baseline_no_repeat_24)
                                pauseMusic()
                            }
                        }

                    }
                }
            }
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun playNewMusic() {
        val serviceIntent = Intent(this, MusicService::class.java)
        val bundleService = Bundle()
        bundleService.putSerializable("SONG", song)
        serviceIntent.putExtra("STT", 0)
        serviceIntent.putExtras(bundleService)
        ContextCompat.startForegroundService(this, serviceIntent)
        setFocus(true)
    }

    private fun pauseMusic() {
        val serviceIntent = Intent(this, MusicService::class.java)
        serviceIntent.putExtra("STT", 1)// 1 la pause
        ContextCompat.startForegroundService(this, serviceIntent)
        btn_Handle.setImageResource(R.drawable.play)
    }

    private fun runAgain() {
        val serviceIntent = Intent(this, MusicService::class.java)
        serviceIntent.putExtra("STT", 2)// 2 la play again
        ContextCompat.startForegroundService(this, serviceIntent)
        btn_Handle.setImageResource(R.drawable.pause)
    }

    private fun runWithSeekBar(position: Int) {
        val serviceIntent = Intent(this, MusicService::class.java)
        serviceIntent.putExtra("STT", 3)// 3 la play with seek bar
        serviceIntent.putExtra("POS", position)
        serviceIntent.putExtra("IS_PLAY", mSongViewModel.isPlaying)
        ContextCompat.startForegroundService(this, serviceIntent)
    }

    override fun onClick(p0: View?) {
        when (p0) {
            btn_Handle -> {
                if(mIsFirstStart) {
                    mCountHandleClick++
                    if (mCountHandleClick % 2 == 1) {
                        pauseMusic()
                    } else {
                        runAgain()
                    }
                }

            }
            btn_NextSong -> {
                if (mSongCurrent == arrayList?.size!! - 1) {
                    song = arrayList!![0]
                    mSongCurrent = 0
                } else {
                    song = arrayList!![mSongCurrent + 1]
                    mSongCurrent++
                }
                playNewMusic()
            }
            btn_PreSong -> {
                if (mSongCurrent == 0) {
                    song = arrayList!![arrayList!!.size - 1]
                    mSongCurrent = arrayList!!.size - 1
                } else {
                    song = arrayList!![mSongCurrent - 1]
                    mSongCurrent--
                }
                playNewMusic()
            }
            btn_handleRepeat -> {
                mCountHandleRepeat++
                when {
                    mCountHandleRepeat % 3 == 1 -> {
                        // LAP LAI TAT CA
                        btn_handleRepeat.setImageResource(R.drawable.ic_baseline_repeat_24)
                    }
                    mCountHandleRepeat % 3 == 2 -> {
                        // lap lai bai dang play
                        btn_handleRepeat.setImageResource(R.drawable.ic_baseline_repeat_one_24)

                    }
                    else -> {
                        // het thi k lap
                        btn_handleRepeat.setImageResource(R.drawable.ic_baseline_no_repeat_24)
                    }
                }
            }
        }
    }

    private fun registerLiveDataListener() {
        val songObserver = Observer<Int> { currentLength ->
            var sec = currentLength % 60
            var min = currentLength / 60
            var secStringCur = if (sec < 10) {
                "0$sec"
            } else {
                "$sec"
            }
            var minStringCur = if (min < 10) {
                "0$min:"
            } else {
                "$min:"
            }
            tv_CurrentPosition.text = minStringCur + secStringCur
        }
        val songNameObserver = Observer<String> { songName ->
            tv_SongTitle.text = songName
        }
        mSongViewModel.songName.observe(this,songNameObserver)
        mSongViewModel.currentLength.observe(this, songObserver)

    }

    override fun onRequestPermissionsResult(requestCode: Int, permissions: Array<out String>, grantResults: IntArray) {
        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(
                        this,
                        Manifest.permission.READ_EXTERNAL_STORAGE
                    ) == PackageManager.PERMISSION_GRANTED
                ) {
                    getMusic()
                }
            } else {
                Toast.makeText(this, "no permission", Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun getMusic() {

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
                arrayList?.add(song)
            } while (songCursor.moveToNext())
        }
        mSongAdapter.setList(arrayList!!)
    }

    @SuppressLint("ResourceAsColor")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onSongNameClick(song: Song, position: Int) {
        this.song = song
        mIsFirstStart = true
        playNewMusic()
        sb_SongHandler.visibility = View.VISIBLE
        mSongCurrent = position
        setFocus(true)
        layout_Child.setBackgroundColor(R.color.backgroundChildColor)
    }


}