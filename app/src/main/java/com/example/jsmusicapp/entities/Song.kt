package com.example.jsmusicapp.entities

import android.graphics.Bitmap
import android.net.Uri
import android.os.Parcelable
import kotlinx.parcelize.Parcelize

@Parcelize
data class Song(
    var songName: String = "",
    var singer: String = "",
    var image: Bitmap? = null,
    var uri: Uri? = null,
    var isPlaying: Boolean = false
) : Parcelable
