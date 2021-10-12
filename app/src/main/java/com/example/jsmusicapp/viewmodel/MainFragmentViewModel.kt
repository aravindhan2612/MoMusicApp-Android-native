package com.example.jsmusicapp.viewmodel

import android.database.Cursor
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import android.util.Log
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.example.jsmusicapp.R
import com.example.jsmusicapp.activity.MainActivity
import com.example.jsmusicapp.entities.Song
import com.example.jsmusicapp.fragments.MainFragment
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class MainFragmentViewModel(savedStateHandle: SavedStateHandle) : ViewModel() {

    companion object {
        const val OPEN_SETTING = "setting screen"
    }

    val state = savedStateHandle

    fun initSongs(mainActivity: MainActivity, mainFragment: MainFragment) {
        viewModelScope.launch {
            val result: ArrayList<Song> =
                getSongs(mainActivity, mainFragment.mainActivityViewModel?.songListLiveData?.value)
            mainFragment.mainActivityViewModel?.songListLiveData?.value = result
        }
    }

    private suspend fun getSongs(
        mainActivity: MainActivity,
        songList: ArrayList<Song>?,
    ): ArrayList<Song> =
        withContext(Dispatchers.IO) {
            var tempList = songList
            if (tempList == null)
                tempList = ArrayList<Song>()
            val collection =
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
                    MediaStore.Audio.Media.getContentUri(
                        MediaStore.VOLUME_EXTERNAL
                    )
                } else {
                    MediaStore.Video.Media.EXTERNAL_CONTENT_URI
                }
            val mAudioCursor: Cursor? = mainActivity.contentResolver.query(
                collection,
                null,
                null,
                null,
                MediaStore.Audio.AudioColumns.TITLE
            )
            mAudioCursor?.let {

                for (i in 0 until it.count) {
                    val urId = it.getColumnIndexOrThrow(MediaStore.Audio.Media._ID)
                    it.moveToNext()
                    //Song title
                    val indexTitle: Int = it.getColumnIndex(MediaStore.Audio.Media.TITLE)
                    //Singer
                    val indexARTIST: Int = it
                        .getColumnIndex(MediaStore.Audio.Media.ARTIST)
                    //The id of the music picture
                    val indexALBUM: Int? = it
                        .getColumnIndex(MediaStore.Audio.Media.ALBUM_ID)
                    val strTitle: String = it.getString(indexTitle)
                    val strARTIST: String = it.getString(indexARTIST)
//                    val strALBUM: String? = indexALBUM?.let { it1 -> it.getString(it1) }
                    val uri = Uri.withAppendedPath(collection, "" + it.getLong(urId))
//                    val pic = strALBUM?.let { it2-> Integer.valueOf(it2) }
//
//                    //According to the id of the music picture
//                    val bitmap: Bitmap? = getAlbumArt(pic, mainActivity)
                    val song = Song(strTitle, strARTIST, null, uri)
                    if (!tempList.contains(song) && song.songName != "")
                        tempList.add(song)
                }
            }
            mAudioCursor?.close()
            return@withContext tempList
        }

    private fun getAlbumArt(album_id: Int?, mainActivity: MainActivity): Bitmap? {
        //In front we just got the album picture id, here we can get the album picture by id, albums store the album information
        val mUriAlbums = "content://media/external/audio/albums"

        //The album_art field stores the path of the music picture
        val projection = arrayOf("album_art")
        val imageCursor: Cursor? = mainActivity.contentResolver.query(
            Uri.parse("$mUriAlbums/$album_id"),
            projection,
            null,
            null,
            null
        )
        var albumArt: String? = null
        imageCursor?.let {
            if (it.count > 0 && it.columnCount > 0) {
                it.moveToNext()
                albumArt = it.getString(0)
                Log.d("liukun", "getAlbumArt: $albumArt")
            }
        }

        imageCursor?.close()
        var bm: Bitmap? = null
        bm = if (albumArt != null) {
            BitmapFactory.decodeFile(albumArt)
        } else BitmapFactory.decodeResource(mainActivity.resources, R.drawable.ic_music_icon)
        return bm
    }

    fun setSettingScreen(open: String?) {
        state.set(OPEN_SETTING, open)
    }

    fun isSettingScreenOpen(): String? {
        return state.get(OPEN_SETTING)
    }
    
}