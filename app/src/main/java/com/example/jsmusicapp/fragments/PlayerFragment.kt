package com.example.jsmusicapp.fragments

import android.content.Context
import android.media.MediaPlayer
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.SeekBar
import androidx.appcompat.content.res.AppCompatResources
import androidx.fragment.app.Fragment
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import com.example.jsmusicapp.R
import com.example.jsmusicapp.activity.MainActivity
import com.example.jsmusicapp.databinding.PlayerFragmentBinding
import com.example.jsmusicapp.viewmodel.MainActivityViewModel
import com.example.jsmusicapp.viewmodel.PlayerViewModel
import com.google.android.material.bottomsheet.BottomSheetDialogFragment


class PlayerFragment : BottomSheetDialogFragment(), SeekBar.OnSeekBarChangeListener,
    MediaPlayer.OnCompletionListener {

    private lateinit var playerFragmentBinding: PlayerFragmentBinding
    private lateinit var mainActivity: MainActivity
    private var mainActivityViewModel: MainActivityViewModel? = null
    private var audioIndex = 0
    private var currentPos: Double? = null
    private var totalDuration: Double? = null

    private lateinit var viewModel: PlayerViewModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        playerFragmentBinding = PlayerFragmentBinding.inflate(inflater, container, false)
        return playerFragmentBinding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initViewModel()
        initAndBindingValues()
        mainActivity.supportActionBar?.hide()
    }

    private fun initAndBindingValues() {
        mainActivity.mediaPlayer?.setOnCompletionListener(this)
        playerFragmentBinding.seekBar.setOnSeekBarChangeListener(this)
        playerFragmentBinding.minimizeButton.setOnClickListener {
            Navigation.findNavController(playerFragmentBinding.root).popBackStack()
        }

        if (mainActivityViewModel?.songListLiveData?.value?.isNotEmpty() == true) {
            playAudio(getCurrentPosition())
            prevAudio()
            nextAudio()
            setPause()
        }
    }

    private fun initViewModel() {
        mainActivityViewModel = ViewModelProvider(
            mainActivity,
            SavedStateViewModelFactory(mainActivity.application, mainActivity)
        ).get(
            MainActivityViewModel::class.java
        )
        viewModel = ViewModelProvider(this).get(PlayerViewModel::class.java)
    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onProgressChanged(seekBar: SeekBar?, progress: Int, fromUser: Boolean) {
    }

    override fun onStartTrackingTouch(seekBar: SeekBar?) {
    }

    override fun onStopTrackingTouch(seekBar: SeekBar?) {
        currentPos = seekBar?.progress?.toDouble()
        mainActivity.mediaPlayer?.seekTo(currentPos?.toInt()!!)
    }

    override fun onCompletion(mp: MediaPlayer?) {
        mainActivityViewModel?.songListLiveData?.value?.get(audioIndex)?.isPlaying = false
        audioIndex++;
        if (audioIndex < (getSongListSize())) {
            updateSongPostionAndPlayAudio(audioIndex)
        } else {
            audioIndex = 0;
            updateSongPostionAndPlayAudio(audioIndex)
        }
    }

    private fun updateSongPostionAndPlayAudio(pos: Int) {
        mainActivityViewModel?.setCurrentSongPosition(pos)
        playAudio(pos);
    }

    private fun playAudio(pos: Int) {
        try {
            playerFragmentBinding.songName.text =
                mainActivityViewModel?.songListLiveData?.value?.get(pos)?.songName
            playerFragmentBinding.singerName.text =
                mainActivityViewModel?.songListLiveData?.value?.get(pos)?.singer
            playerFragmentBinding.songName.isSelected = true
            playerFragmentBinding.singerName.isSelected = true
            if (mainActivityViewModel?.songListLiveData?.value?.get(pos)?.isPlaying == false) {
                mainActivity.mediaPlayer?.reset()
                //set file path
                mainActivityViewModel?.songListLiveData?.value
                    ?.get(pos)?.uri?.let {
                        mainActivity.mediaPlayer?.setDataSource(
                            mainActivity,
                            it
                        )
                    }
                mainActivity.mediaPlayer?.prepare()
                mainActivity.mediaPlayer?.start()
                playerFragmentBinding.playPauseImg.setImageDrawable(
                    AppCompatResources.getDrawable(
                        mainActivity, R.drawable.ic_baseline_pause_circle_outline_24
                    )
                )
                mainActivityViewModel?.songListLiveData?.value?.get(pos)?.isPlaying = true
            } else{
                playerFragmentBinding.playPauseImg.setImageDrawable(
                    AppCompatResources.getDrawable(
                        mainActivity,if (mainActivity.mediaPlayer?.isPlaying ==true) R.drawable.ic_baseline_pause_circle_outline_24 else R.drawable.ic_baseline_play_arrow_24
                    )
                )
            }
            audioIndex = pos
        } catch (e: Exception) {
            e.printStackTrace()
        }
        setAudioProgress()
    }

    //set audio progress
    private fun setAudioProgress() {
        //get the audio duration
        currentPos = mainActivity.mediaPlayer?.currentPosition?.toDouble()
        totalDuration = mainActivity.mediaPlayer?.duration?.toDouble()

        //display the audio duration
        playerFragmentBinding.total.text = timerConversion(totalDuration)
        playerFragmentBinding.current.text = timerConversion(currentPos)
        playerFragmentBinding.seekBar.max = totalDuration?.toInt()!!
        val handler = Handler(Looper.getMainLooper())
        val runnable: Runnable = object : Runnable {
            override fun run() {
                try {
                    currentPos = mainActivity.mediaPlayer?.currentPosition?.toDouble()
                    playerFragmentBinding.current.text = timerConversion(currentPos)
                    playerFragmentBinding.seekBar.progress = currentPos?.toInt()!!
                    handler.postDelayed(this, 1000)
                } catch (ed: IllegalStateException) {
                    ed.printStackTrace()
                }
            }
        }
        handler.postDelayed(runnable, 1000)
    }

    //time conversion
    fun timerConversion(value: Double?): String? {
        value?.let {
            val audioTime: String
            val dur = value.toInt()
            val hrs = dur / 3600000
            val mns = dur / 60000 % 60000
            val scs = dur % 60000 / 1000
            audioTime = if (hrs > 0) {
                String.format("%02d:%02d:%02d", hrs, mns, scs)
            } else {
                String.format("%02d:%02d", mns, scs)
            }
            return audioTime
        }
        return null
    }

    //play previous audio
    private fun prevAudio() {
        playerFragmentBinding.previousImg.setOnClickListener {
            mainActivityViewModel?.songListLiveData?.value?.get(audioIndex)?.isPlaying = false
            if (audioIndex > 0) {
                audioIndex--
                updateSongPostionAndPlayAudio(audioIndex)
            } else {
                audioIndex = getSongListSize() - 1
                updateSongPostionAndPlayAudio(audioIndex)
            }
        }
    }

    //play next audio
    private fun nextAudio() {
        playerFragmentBinding.nextImg.setOnClickListener {
            mainActivityViewModel?.songListLiveData?.value?.get(audioIndex)?.isPlaying = false
            if (audioIndex < getSongListSize() - 1) {
                audioIndex++
                updateSongPostionAndPlayAudio(audioIndex)
            } else {
                audioIndex = 0
                updateSongPostionAndPlayAudio(audioIndex)
            }
        }
    }

    //pause audio
    private fun setPause() {
        playerFragmentBinding.playPauseImg.setOnClickListener {
            if (mainActivity.mediaPlayer?.isPlaying == true) {
                mainActivity.mediaPlayer?.pause()
                mainActivityViewModel?.songListLiveData?.value?.get(audioIndex)?.isPlaying = false
                playerFragmentBinding.playPauseImg.setImageDrawable(
                    AppCompatResources.getDrawable(
                        mainActivity, R.drawable.ic_baseline_play_arrow_24,
                    ),
                )
            } else {
                mainActivity.mediaPlayer?.start()
                mainActivityViewModel?.songListLiveData?.value?.get(audioIndex)?.isPlaying = true
                playerFragmentBinding.playPauseImg.setImageDrawable(
                    AppCompatResources.getDrawable(
                        mainActivity, R.drawable.ic_baseline_pause_circle_outline_24,
                    ),
                )
            }
        }
    }

    private fun getSongListSize(): Int {
        return mainActivityViewModel?.songListLiveData?.value?.size ?: 0
    }

    private fun getCurrentPosition(): Int {
        return mainActivityViewModel?.getCurrentSongPosition() ?: 0
    }
}