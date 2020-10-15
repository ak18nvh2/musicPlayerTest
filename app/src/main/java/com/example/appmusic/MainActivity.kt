package com.example.appmusic


import android.Manifest
import android.annotation.SuppressLint
import android.content.*
import android.content.pm.PackageManager
import android.graphics.Color
import android.os.Build
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.os.IBinder
import android.provider.MediaStore
import android.util.Log
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
    private var mCountRandomSong = 0
    private lateinit var mIntentFilter: IntentFilter
    private var mSongViewModel = SongViewModel()
    private var mIsFirstStart = false
    private var mCountHandleRepeat = 0
    private lateinit var mMusicService: MusicService
    private var mBound = false
    private lateinit var mBinder: MusicService.LocalBinder
    private var mSong = Song()
    private var mArrayListSong = ArrayList<Song>()
    private var mCurrentPositionOfList = 0
    private var mCurrentPositionOfSong = 0

    private val connection = object : ServiceConnection {
        override fun onServiceDisconnected(p0: ComponentName?) {
            mBound = false
        }

        override fun onServiceConnected(p0: ComponentName?, p1: IBinder?) {
            mBinder = p1 as MusicService.LocalBinder
            mMusicService = mBinder.getService()
            mBound = true
        }
    }

    companion object {
        const val mBroadcastAction = "SEND_SONG_SIZE"
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun init() {
        mSongAdapter = SongAdapter(this, this)
        mIntentFilter = IntentFilter()
        mIntentFilter.addAction(mBroadcastAction)
        btn_Handle.setOnClickListener(this)
        btn_NextSong.setOnClickListener(this)
        btn_PreSong.setOnClickListener(this)
        btn_handleRepeat.setOnClickListener(this)
        btn_randomMusic.setOnClickListener(this)
        sb_SongHandler.min = 0
        setFocus(false)
        registerLiveDataListener()
        sb_SongHandler.setOnSeekBarChangeListener(@SuppressLint("AppCompatCustomView")
        object : SeekBar.OnSeekBarChangeListener {
            override fun onProgressChanged(seekBar: SeekBar, i: Int, b: Boolean) {
                if (b) {
                    mMusicService.playWithSeekBar(i)
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
    private fun setFocus(boolean: Boolean) {
        if (!boolean) {
            btn_randomMusic.visibility = View.GONE
            btn_Handle.visibility = View.GONE
            sb_SongHandler.visibility = View.GONE
            btn_handleRepeat.visibility = View.GONE
            btn_NextSong.visibility = View.GONE
            btn_PreSong.visibility = View.GONE
            tv_CurrentPosition.visibility = View.GONE
            tv_SongLength.visibility = View.GONE
        } else {
            tv_SongTitle.setTextColor(Color.WHITE)
            btn_Handle.visibility = View.VISIBLE
            sb_SongHandler.visibility = View.VISIBLE
            btn_handleRepeat.visibility = View.VISIBLE
            btn_NextSong.visibility = View.VISIBLE
            btn_randomMusic.visibility = View.VISIBLE
            btn_PreSong.visibility = View.VISIBLE
            tv_CurrentPosition.visibility = View.VISIBLE
            tv_SongLength.visibility = View.VISIBLE
        }
    }

    override fun onResume() {
        super.onResume()
        registerReceiver(mReceiver, mIntentFilter)
        statService()

    }

    override fun onStop() {
        unregisterReceiver(mReceiver)
        if (mBound) {
            mBound = false
            unbindService(connection)
        }
        super.onStop()
    }

    private fun bindService() {
        Intent(this, MusicService::class.java).also { intent ->
            bindService(intent, connection, Context.BIND_AUTO_CREATE)
        }
        mBound = true
    }

    private fun statService() {
        bindService()
        ContextCompat.startForegroundService(this, Intent(this, MusicService::class.java))
    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun playFirstTime() {
        mIsFirstStart = true
        layout_Child.setBackgroundColor(Color.rgb(255, 102, 153))
        btn_Handle.setImageResource(R.drawable.pause)
        mCountHandleClick = 0
        sb_SongHandler.max =
            (mMusicService.music.duration / 60000) * 60 + (mMusicService.music.duration / 1000) % 60
        setFocus(true)
        mSongViewModel.runASong(
            mMusicService.music.duration,
            mMusicService.music.currentPosition,
            true,
            mMusicService.arrayListSong[mMusicService.positionOfList].songName
        )
    }

    private val mReceiver: BroadcastReceiver = object : BroadcastReceiver() {
        @SuppressLint("ResourceAsColor")
        @RequiresApi(Build.VERSION_CODES.O)
        override fun onReceive(context: Context?, intent: Intent) {
            if (intent.action == mBroadcastAction) {
                when (intent.getStringExtra("STATUS")) {
                    MusicService.FLAG_FIRST_START -> {
                        playFirstTime()
                    }
                    MusicService.FLAG_PAUSE -> {
                        mSongViewModel.runASong(
                            mMusicService.music.duration,
                            mMusicService.songCurrentPosition,
                            false,
                            mMusicService.song.songName
                        )
                    }
                    MusicService.FLAG_PLAY_CONTINUE -> {
                        mSongViewModel.runASong(
                            mMusicService.music.duration,
                            mMusicService.songCurrentPosition,
                            true,
                            mMusicService.song.songName
                        )
                    }
                    MusicService.FLAG_PLAY_WITH_SEEK_BAR -> {
                        mSongViewModel.runASong(
                            mMusicService.music.duration,
                            mMusicService.songCurrentPosition,
                            mMusicService.isPlaying,
                            mMusicService.song.songName
                        )
                    }
                    MusicService.FLAG_NO_REPEAT -> {
                        mCountHandleClick++
                        Log.d("TAGGF", mCountHandleClick.toString())
                        btn_Handle.setImageResource(R.drawable.play)
                        mSongViewModel.runASong(
                            mMusicService.music.duration,
                            mMusicService.music.duration,
                            false,
                            mMusicService.song.songName
                        )
                    }
                    MusicService.FLAG_PLAY_WHEN_START_APP_AGAIN -> {
                        Log.d(
                            "AGAIN",
                            "${mMusicService.music.currentPosition} ${mMusicService.music.duration}"
                        )
                        mSongAdapter.setPositionChangeColor(mMusicService.positionOfList,mMusicService.song.songName)
                        mSongViewModel.runASong(
                            mMusicService.music.duration,
                            mMusicService.music.currentPosition / 1000,
                            true,
                            mMusicService.song.songName
                        )
                        setFocus(true)
                        mIsFirstStart = true
                        layout_Child.setBackgroundColor(Color.rgb(255, 102, 153))
                        if (mMusicService.isPlaying) {
                            btn_Handle.setImageResource(R.drawable.pause)
                        } else {
                            mCountHandleClick = 1
                            btn_Handle.setImageResource(R.drawable.play)
                        }
                        mCountHandleClick = 0
                        sb_SongHandler.max =
                            (mMusicService.music.duration / 60000) * 60 + (mMusicService.music.duration / 1000) % 60
                        if (mMusicService.randomState == MusicService.FLAG_RANDOM_MUSIC) {
                            btn_randomMusic.setImageResource(R.drawable.shuffle2)
                            mCountRandomSong++
                        }
                        if (mMusicService.repeatState == MusicService.FLAG_REPEAT_ONE) {
                            btn_handleRepeat.setImageResource(R.drawable.ic_baseline_repeat_one_24)
                            mCountHandleRepeat = 2
                        } else if (mMusicService.repeatState == MusicService.FLAG_REPEAT_ALL) {
                            btn_handleRepeat.setImageResource(R.drawable.ic_baseline_repeat_24)
                            mCountHandleRepeat = 1
                        }
                    }
                    MusicService.FLAG_CHANGE_SONG -> {
                        mSongAdapter.setPositionChangeColor(mMusicService.positionOfList,mMusicService.song.songName)
                    }
                }
            }
        }
    }


    @RequiresApi(Build.VERSION_CODES.O)
    override fun onClick(p0: View?) {
        when (p0) {
            btn_Handle -> {
                if (mIsFirstStart) {
                    mCountHandleClick++
                    if (mCountHandleClick % 2 == 1) {
                        btn_Handle.setImageResource(R.drawable.play)
                        if (mBound) {
                            mMusicService.pauseMusic()
                        }
                    } else {
                        btn_Handle.setImageResource(R.drawable.pause)
                        if (mBound) {
                            mMusicService.playMusicContinue()
                        }
                    }
                    Log.d("TAGG", mCountHandleClick.toString())
                }

            }
            btn_NextSong -> {
                if (mMusicService.positionOfList == mMusicService.arrayListSong.size - 1) {
                    mMusicService.song = mMusicService.arrayListSong[0]
                    mMusicService.positionOfList = 0
                } else {
                    mMusicService.song = mMusicService.arrayListSong[++mMusicService.positionOfList]
                }
                mMusicService.startMusicFirstTime(mMusicService.song, mMusicService.positionOfList)
            }
            btn_PreSong -> {
                if (mMusicService.positionOfList == 0) {
                    mMusicService.song =
                        mMusicService.arrayListSong[mMusicService.arrayListSong.size - 1]
                    mMusicService.positionOfList = mMusicService.arrayListSong.size - 1
                } else {
                    mMusicService.song = mMusicService.arrayListSong[--mMusicService.positionOfList]

                }
                mMusicService.startMusicFirstTime(mMusicService.song, mMusicService.positionOfList)
            }
            btn_handleRepeat -> {
                mCountHandleRepeat++
                when {
                    mCountHandleRepeat % 3 == 1 -> {
                        // LAP LAI TAT CA
                        btn_handleRepeat.setImageResource(R.drawable.ic_baseline_repeat_24)
                        mMusicService.repeatState = MusicService.FLAG_REPEAT_ALL
                    }
                    mCountHandleRepeat % 3 == 2 -> {
                        // lap lai bai dang play
                        btn_handleRepeat.setImageResource(R.drawable.ic_baseline_repeat_one_24)
                        mMusicService.repeatState = MusicService.FLAG_REPEAT_ONE
                    }
                    else -> {
                        // het thi k lap
                        btn_handleRepeat.setImageResource(R.drawable.ic_baseline_no_repeat_24)
                        mMusicService.repeatState = MusicService.FLAG_NO_REPEAT
                    }
                }
            }
            btn_randomMusic -> {
                mCountRandomSong++
                if (mCountRandomSong % 2 == 0) {
                    mMusicService.randomState = MusicService.FLAG_NO_RANDOM_MUSIC
                    btn_randomMusic.setImageResource(R.drawable.shuffle)
                } else {
                    mMusicService.randomState = MusicService.FLAG_RANDOM_MUSIC
                    btn_randomMusic.setImageResource(R.drawable.shuffle2)
                }
            }
        }
    }

    private fun registerLiveDataListener() {
        val songObserver = Observer<String> { currentLength ->
            tv_CurrentPosition.text = currentLength
        }
        mSongViewModel.currentLength.observe(this, songObserver)

        val songNameObserver = Observer<String> { songName ->
            tv_SongTitle.text = songName
        }
        mSongViewModel.songName.observe(this, songNameObserver)

        val songLengthObserver = Observer<String> { songLength ->
            tv_SongLength.text = songLength
        }
        mSongViewModel.songLength.observe(this, songLengthObserver)

        val currentProcessObserver = Observer<Int> { currentProcess ->
            sb_SongHandler.progress = currentProcess
        }
        mSongViewModel.currentProcess.observe(this, currentProcessObserver)
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
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
            val songName = songCursor.getColumnIndex(MediaStore.Audio.Media.TITLE)
            val songLocation = songCursor.getColumnIndex(MediaStore.Audio.Media.DATA)
            do {
                val currentName = songCursor.getString(songName)
                val currentLocation = songCursor.getString(songLocation)
                val song = Song()
                song.songLocation = currentLocation
                song.songName = currentName
                mArrayListSong.add(song)
            } while (songCursor.moveToNext())
        }
        mSongAdapter.setList(mArrayListSong)
    }

    @SuppressLint("ResourceAsColor")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onSongNameClick(song: Song, position: Int) {
        if (mBound) {
            mMusicService.arrayListSong = mArrayListSong
            mMusicService.positionOfList = position
            mMusicService.song = song
            mMusicService.startMusicFirstTime(song, position)
        }
    }


    override fun onDestroy() {
        if (!mMusicService.isPlaying) {
            if (mBound) {
                unbindService(connection)
                mBound = false
            }
            stopService(Intent(applicationContext,MusicService::class.java))
        }
        super.onDestroy()
    }
}