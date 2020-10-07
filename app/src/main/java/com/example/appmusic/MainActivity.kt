package com.example.appmusic


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class MainActivity : AppCompatActivity(), SongAdapter.IRecyclerViewWithActivity {

    private lateinit var mSongAdapter: SongAdapter
    private val PERMISSION_REQUEST = 1
    private var mSongViewModel = SongViewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        mSongAdapter = SongAdapter(this, this)

        var rvListSong = findViewById<RecyclerView>(R.id.rv_ListSong)
        rvListSong.layoutManager = LinearLayoutManager(this)
        rvListSong.adapter = mSongAdapter
        registerLiveDataListener()
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



    private fun registerLiveDataListener() {
        val songObserver = Observer<ArrayList<Song>> { newListContact ->
            mSongAdapter.setList(newListContact)
        }
        mSongViewModel.listSong.observe(this, songObserver)

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
        mSongViewModel.getAllSong(contentResolver)
    }

    override fun onSongNameClick(song: Song) {

        var intent = Intent(this, PlayMusicActivity::class.java)
        var bundle = Bundle()
        stopService(Intent(this, MusicService::class.java))
        bundle.putSerializable("SONG", song)
        intent.putExtras(bundle)
        startActivity(intent)
    }



}