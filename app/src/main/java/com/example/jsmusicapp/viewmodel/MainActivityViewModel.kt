package com.example.jsmusicapp.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.SavedStateHandle
import androidx.lifecycle.ViewModel
import com.example.jsmusicapp.entities.Song

class MainActivityViewModel(state: SavedStateHandle) : ViewModel() {
    private var savedStateHandle = state
    var songListLiveData = MutableLiveData<ArrayList<Song>>()

    companion object {
        const val SONGPOSITION = "CurrentSongPosition"
        const val SONG_LIST = "SongList"
        const val CURRENT_SONG_IS_PLAYING = "CurrentSongIsPlaying"
    }

    fun setCurrentSongPosition(pos: Int?) {
        savedStateHandle.set(SONGPOSITION, pos)
    }

    fun getCurrentSongPosition(): Int? {
        return savedStateHandle.get(SONGPOSITION)
    }

    fun setSongList(songs: ArrayList<Song>?) {
        savedStateHandle.set(SONG_LIST, songs)
    }

    fun getSongList(): ArrayList<Song>? {
        return savedStateHandle.get(SONG_LIST)
    }

    fun setCurrentSongPlaying(currentSongPlaying: Boolean?) {
        savedStateHandle.set(CURRENT_SONG_IS_PLAYING, currentSongPlaying)
    }

    fun getCurrentSongPlaying(): Boolean? {
        return savedStateHandle.get(CURRENT_SONG_IS_PLAYING)
    }
}