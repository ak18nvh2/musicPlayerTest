package com.example.appmusic

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.media.MediaMetadataRetriever
import java.io.ByteArrayInputStream
import java.io.InputStream

object Utils {
    fun songArt(path: String): Bitmap? {
        val retriever = MediaMetadataRetriever()
        val inputStream: InputStream
        var bitmap: Bitmap? = null
        try {
            retriever.setDataSource(path)
            if (retriever.embeddedPicture != null) {
                inputStream = ByteArrayInputStream(retriever.embeddedPicture)
                bitmap = BitmapFactory.decodeStream(inputStream)
                retriever.release()
            }
        }catch (e:Exception){
            e.printStackTrace()
        }
        return bitmap
    }
}