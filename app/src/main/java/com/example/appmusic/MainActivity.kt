package com.example.appmusic


import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.media.MediaPlayer
import android.net.Uri
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.provider.MediaStore
import android.widget.Toast
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView


class MainActivity : AppCompatActivity(), SongAdapter.IRecyclerViewWithActivity {
    private var listSong = ArrayList<Song>()
    private lateinit var songAdapter : SongAdapter
    private val PERMISSION_REQUEST = 1
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        songAdapter = SongAdapter(this,this)

        var rvListSong = findViewById<RecyclerView>(R.id.rv_ListSong)
        rvListSong.layoutManager = LinearLayoutManager(this)
        rvListSong.adapter = songAdapter

        val arr = arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE)

        if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)!= PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
                ActivityCompat.requestPermissions(this, arr , PERMISSION_REQUEST)
            } else {
                ActivityCompat.requestPermissions(this, arr , PERMISSION_REQUEST)
            }
        } else {
            getMusic()
        }

    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        if (requestCode == PERMISSION_REQUEST) {
            if (grantResults.isNotEmpty() && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                    getMusic()
                }
            } else {
                Toast.makeText(this, "no permisson", Toast.LENGTH_SHORT).show()
            }
        }
    }
    private fun getMusic(){
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
                listSong.add(song)
            } while (songCursor.moveToNext())
        }
        songAdapter.setList(this.listSong)
    }

    override fun onSongNameClick(song: Song) {

        var intent = Intent(this, PlayMusic::class.java)
        var bundle = Bundle()
        bundle.putSerializable("SONG", song)
        intent.putExtras(bundle)
        startActivity(intent)
    }

}