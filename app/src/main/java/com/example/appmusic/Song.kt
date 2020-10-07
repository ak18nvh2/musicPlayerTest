package com.example.appmusic

import java.io.Serializable

class Song : Serializable {
    var songName: String = ""
    var songLocation : String = ""
    var songLength : Int = 0
    var pausePosition : Int = 0
}