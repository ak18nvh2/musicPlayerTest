package com.example.appmusic.views


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
import android.view.View
import android.widget.SeekBar
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.afollestad.materialdialogs.MaterialDialog
import com.afollestad.materialdialogs.customview.customView
import com.example.appmusic.*
import com.example.appmusic.models.Episodes
import com.example.appmusic.models.Song
import com.example.appmusic.viewmodels.SongViewModel
import kotlinx.android.synthetic.main.activity_main.*
import kotlinx.android.synthetic.main.dialog_processbar.*
import kotlinx.android.synthetic.main.input_dialog.*
import java.lang.NumberFormatException


class MainActivity : AppCompatActivity(),
    SongAdapter.IRecyclerViewWithActivity,
    View.OnClickListener {

    private var mTypeMusic: String? = ""
    private lateinit var mSongAdapter: SongAdapter
    private val PERMISSION_REQUEST = 1
    private var mCountHandleClick = 0
    private var mCountRandomSong = 0
    private lateinit var mIntentFilter: IntentFilter
    private var mSongViewModel = SongViewModel()
    private var mIsFirstStart = false
    private var mCountHandleRepeat = 0
    private var mBound = false
    private lateinit var mBinder: MusicService.LocalBinder
    private var mArrayListSong = ArrayList<Song>()
    private var mListEpisodes: ArrayList<Episodes>? = null
    private var checkClear = 0
    private lateinit var dialogProcessLoad: MaterialDialog
    private lateinit var dialogInputTime: MaterialDialog
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
        const val mBroadcastAction = "Music_Broadcast"
        lateinit var mMusicService: MusicService
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        init()
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun init() {
        dialogProcessLoad = MaterialDialog(this).noAutoDismiss()
            .customView(R.layout.dialog_processbar)
        dialogInputTime = MaterialDialog(this).noAutoDismiss()
            .customView(R.layout.input_dialog)
        layout_Child.setBackgroundColor(Color.WHITE)
        mListEpisodes = ArrayList()
        mSongAdapter = SongAdapter(this, this)
        mIntentFilter = IntentFilter()
        mIntentFilter.addAction(mBroadcastAction)
        btn_Handle.setOnClickListener(this)
        btn_NextSong.setOnClickListener(this)
        btn_PreSong.setOnClickListener(this)
        btn_handleRepeat.setOnClickListener(this)
        btn_randomMusic.setOnClickListener(this)
        btn_SelectLocalMusic.setOnClickListener(this)
        btn_SelectOnlineMusic.setOnClickListener(this)
        btn_countDown.setOnClickListener(this)
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

    }


    @RequiresApi(Build.VERSION_CODES.O)
    private fun setFocus(boolean: Boolean) {
        if (!boolean) {
            tv_SongTitle.setTextColor(Color.BLACK)
            btn_randomMusic.visibility = View.GONE
            btn_Handle.visibility = View.GONE
            sb_SongHandler.visibility = View.GONE
            btn_handleRepeat.visibility = View.GONE
            btn_NextSong.visibility = View.GONE
            btn_PreSong.visibility = View.GONE
            tv_CurrentPosition.visibility = View.GONE
            tv_SongLength.visibility = View.GONE
            btn_countDown.visibility = View.GONE
        } else {
            tv_SongTitle.setTextColor(Color.WHITE)
            btn_countDown.visibility = View.VISIBLE
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
            0,
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
                        btn_Handle.setImageResource(R.drawable.play)
                        if (checkClear == 1) {
                            tv_SongTitle.text = "Chưa phát bài hát nào"
                            checkClear = 0
                        } else {
                            mSongViewModel.runASong(
                                mMusicService.music.duration,
                                mMusicService.songCurrentPosition,
                                false,
                                mMusicService.song.songName
                            )
                        }

                    }
                    MusicService.FLAG_PLAY_CONTINUE -> {
                        btn_Handle.setImageResource(R.drawable.pause)
                        if (mMusicService.stateMusic == MusicService.FLAG_STATE_DIE) {
                            Toast.makeText(
                                applicationContext,
                                "Can't play, please try again!",
                                Toast.LENGTH_SHORT
                            ).show()
                        } else {
                            mSongViewModel.runASong(
                                mMusicService.music.duration,
                                mMusicService.songCurrentPosition,
                                true,
                                mMusicService.song.songName
                            )
                        }

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
                        btn_Handle.setImageResource(R.drawable.play)
                        mSongViewModel.runASong(
                            mMusicService.music.duration,
                            mMusicService.music.duration,
                            false,
                            mMusicService.song.songName
                        )
                    }
                    MusicService.FLAG_PLAY_WHEN_START_APP_AGAIN -> {
                        mSongAdapter.setPositionChangeColor(
                            mMusicService.positionOfList
                        )
                        mSongViewModel.runASong(
                            mMusicService.music.duration,
                            mMusicService.music.currentPosition / 1000,
                            mMusicService.isPlaying,
                            mMusicService.song.songName
                        )
                        setFocus(true)
                        mIsFirstStart = true
                        layout_Child.setBackgroundColor(Color.rgb(255, 102, 153))
                        if (mMusicService.isPlaying) {
                            mCountHandleClick = 0
                            btn_Handle.setImageResource(R.drawable.pause)
                        } else {
                            mCountHandleClick = 1
                            btn_Handle.setImageResource(R.drawable.play)
                        }
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

                        if (mMusicService.typeMusic == MusicService.FLAG_ONLINE_MUSIC) {
                            getMusicOnline()
                            btn_SelectOnlineMusic.setTextColor(Color.RED)
                            btn_SelectLocalMusic.setTextColor(Color.WHITE)

                        } else {
                            getMusic()
                            btn_SelectOnlineMusic.setTextColor(Color.WHITE)
                            btn_SelectLocalMusic.setTextColor(Color.RED)
                        }
                    }
                    MusicService.FLAG_CHANGE_SONG -> {
                        mSongAdapter.setPositionChangeColor(
                            mMusicService.positionOfList
                        )
                    }
                    MusicService.FLAG_CAN_NOT_PLAY_MUSIC -> {
                        Toast.makeText(
                            applicationContext,
                            "Can't play, please try again!",
                            Toast.LENGTH_SHORT
                        ).show()
                        btn_Handle.setImageResource(R.drawable.play)
                        mCountHandleClick++
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
                        if (mBound) {
                            mMusicService.pauseMusic()
                            mMusicService.typePause = MusicService.FLAG_PAUSE_IN_APP
                        }
                    } else {
                        if (mBound) {
                            mMusicService.playMusicContinue()
                        }
                    }
                }

            }
            btn_NextSong -> {
                mMusicService.nextSong()
            }
            btn_PreSong -> {
                mMusicService.preSong()
            }
            btn_handleRepeat -> {
                mCountHandleRepeat++
                when {
                    mCountHandleRepeat % 3 == 1 -> {
                        // LAP LAI TAT CA
                        btn_handleRepeat.setImageResource(R.drawable.ic_baseline_repeat_24)
                        mMusicService.repeatState =
                            MusicService.FLAG_REPEAT_ALL
                    }
                    mCountHandleRepeat % 3 == 2 -> {
                        // lap lai bai dang play
                        btn_handleRepeat.setImageResource(R.drawable.ic_baseline_repeat_one_24)
                        mMusicService.repeatState =
                            MusicService.FLAG_REPEAT_ONE
                    }
                    else -> {
                        // het thi k lap
                        btn_handleRepeat.setImageResource(R.drawable.ic_baseline_no_repeat_24)
                        mMusicService.repeatState =
                            MusicService.FLAG_NO_REPEAT
                    }
                }
            }
            btn_randomMusic -> {
                mCountRandomSong++
                if (mCountRandomSong % 2 == 0) {
                    mMusicService.randomState =
                        MusicService.FLAG_NO_RANDOM_MUSIC
                    btn_randomMusic.setImageResource(R.drawable.shuffle)
                } else {
                    mMusicService.randomState =
                        MusicService.FLAG_RANDOM_MUSIC
                    btn_randomMusic.setImageResource(R.drawable.shuffle2)
                }
            }
            btn_SelectLocalMusic -> {
                this.mTypeMusic =
                    MusicService.FLAG_LOCAL_MUSIC
                rv_ListSong.visibility = View.GONE
                getMusic()
                btn_SelectLocalMusic.setTextColor(Color.RED)
                btn_SelectOnlineMusic.setTextColor(Color.WHITE)

            }
            btn_SelectOnlineMusic -> {
                this.mTypeMusic =
                    MusicService.FLAG_ONLINE_MUSIC
                rv_ListSong.visibility = View.GONE
                btn_SelectOnlineMusic.setTextColor(Color.RED)
                btn_SelectLocalMusic.setTextColor(Color.WHITE)
                getMusicOnline()

            }
            btn_countDown -> {
                dialogInputTime.setCancelable(false)
                dialogInputTime.btn_cancel.setOnClickListener {
                    dialogInputTime.dismiss()
                }
                dialogInputTime.btn_Accept.setOnClickListener {

                    try {
                        mSongViewModel.countDownTime(
                            dialogInputTime.edt_inputTime.text.toString().toInt()
                        )
                        Toast.makeText(
                            applicationContext,
                            "Dừng phát nhạc sau " + dialogInputTime.edt_inputTime.text + " phút!",
                            Toast.LENGTH_SHORT
                        ).show()
                        dialogInputTime.dismiss()
                        btn_countDown.setImageResource(R.drawable.ic_baseline_access_alarms_24)
                    } catch (e: NumberFormatException) {
                        Toast.makeText(
                            applicationContext,
                            "Nhập lỗi, mời nhập lại!",
                            Toast.LENGTH_SHORT
                        ).show()
                        dialogInputTime.edt_inputTime.text.clear()
                    }
                }
                dialogInputTime.show()
            }
        }
    }

    private fun getMusicOnline() {
        val callGet = RetrofitClient.instance.getContacts()
        dialogProcessLoad.setCancelable(false)
        dialogProcessLoad.btn_CancelUpdate.setOnClickListener() {
            callGet.cancel()
            dialogProcessLoad.dismiss()
        }
        dialogProcessLoad.show()
        mSongViewModel.getAllEpisodes(callGet)
    }

    private fun registerLiveDataListener() {
        val countDownObserver = Observer<Int> {
            mMusicService.pauseMusic()
            mCountHandleClick++
            btn_countDown.setImageResource(R.drawable.ic_baseline_access_alarms_turn_off)
        }
        mSongViewModel.isTurnOff.observe(this, countDownObserver)

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

        val fJsonObserver = Observer<List<Song>> { newList ->
            mArrayListSong = newList as ArrayList<Song>
            rv_ListSong.visibility = View.VISIBLE
            mSongAdapter.setTypeListSongDisplay(MusicService.FLAG_ONLINE_MUSIC)
            mTypeMusic = MusicService.FLAG_ONLINE_MUSIC
            mSongAdapter.setList(mArrayListSong)
        }
        mSongViewModel.listSong.observe(this, fJsonObserver)

        val notificationObserver = Observer<String> {
            dialogProcessLoad.dismiss()
            Toast.makeText(this, it, Toast.LENGTH_SHORT).show()
        }
        mSongViewModel.notification.observe(this, notificationObserver)
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
        val arr = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)
        val arrayListSong = ArrayList<Song>()
        if (ContextCompat.checkSelfPermission(
                this,
                Manifest.permission.READ_EXTERNAL_STORAGE
            ) != PackageManager.PERMISSION_GRANTED
        ) {
            ActivityCompat.requestPermissions(this, arr, PERMISSION_REQUEST)
        } else {
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
                    arrayListSong.add(song)
                } while (songCursor.moveToNext())
            }
        }
        rv_ListSong.visibility = View.VISIBLE
        this.mTypeMusic = MusicService.FLAG_LOCAL_MUSIC
        this.mSongAdapter.setTypeListSongDisplay(MusicService.FLAG_LOCAL_MUSIC)
        mArrayListSong = arrayListSong
        mSongAdapter.setList(mArrayListSong)
    }

    @SuppressLint("ResourceAsColor")
    @RequiresApi(Build.VERSION_CODES.O)
    override fun onSongNameClick(song: Song, position: Int) {
        if (mBound) {
            mSongAdapter.setTypeSongPlaying(this.mTypeMusic!!)
            mMusicService.typeMusic = this.mTypeMusic!!
            mMusicService.arrayListSong = mArrayListSong
            mMusicService.positionOfList = position
            mMusicService.song = song
            mMusicService.startMusicFirstTime(song, position)
        }
    }

    override fun onDestroy() {
        if (!mMusicService.isPlaying) {
            if (mBound) {
                mMusicService.stopService()
                unbindService(connection)
                mBound = false
            }

            stopService(
                Intent(
                    applicationContext,
                    MusicService::class.java
                )
            )
        }
        super.onDestroy()
    }
}