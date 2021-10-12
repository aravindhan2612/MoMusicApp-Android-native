package com.example.jsmusicapp.fragments

import android.Manifest
import android.content.Context
import android.content.Intent
import android.content.pm.PackageManager
import android.net.Uri
import android.os.Bundle
import android.provider.Settings
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.activity.result.contract.ActivityResultContracts.RequestPermission
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.Fragment
import androidx.lifecycle.Observer
import androidx.lifecycle.SavedStateViewModelFactory
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.Navigation
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.jsmusicapp.R
import com.example.jsmusicapp.activity.MainActivity
import com.example.jsmusicapp.adapter.SongAdapter
import com.example.jsmusicapp.databinding.MainFragmentBinding
import com.example.jsmusicapp.entities.Song
import com.example.jsmusicapp.viewmodel.MainActivityViewModel
import com.example.jsmusicapp.viewmodel.MainFragmentViewModel
import com.google.android.material.snackbar.Snackbar


class MainFragment : Fragment() {
    private lateinit var mainFragmentBinding: MainFragmentBinding
    private var viewModel: MainFragmentViewModel? = null
    private lateinit var mainActivity: MainActivity
    private var songAdapter: SongAdapter? = null
    var mainActivityViewModel: MainActivityViewModel? = null

    companion object {
        fun newInstance() = MainFragment()
    }

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        mainFragmentBinding = MainFragmentBinding.inflate(inflater, container, false)
        return mainFragmentBinding.root

    }

    override fun onAttach(context: Context) {
        super.onAttach(context)
        mainActivity = context as MainActivity
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        mainActivity.supportActionBar?.show()
        initViewModel()
        mainActivityViewModel?.songListLiveData?.observe(viewLifecycleOwner, songlistObesever)
        if (viewModel?.isSettingScreenOpen() == null || viewModel?.isSettingScreenOpen() == "openedSettingScreen") {
            checkForMedialPermission()
        }
        initView()
    }

    private fun initViewModel() {
        mainActivityViewModel = ViewModelProvider(
            mainActivity,
            SavedStateViewModelFactory(mainActivity.application, mainActivity)
        ).get(
            MainActivityViewModel::class.java
        )
        viewModel =
            ViewModelProvider(this, SavedStateViewModelFactory(mainActivity.application, this)).get(
                MainFragmentViewModel::class.java
            )
    }

    private fun checkForMedialPermission() {
        if (checkPermission()) {
            mainFragmentBinding.grantAccessBtn.visibility = View.GONE
            mainFragmentBinding.grantAccessDesc.visibility = View.GONE
            mainFragmentBinding.songRecyclerView.visibility = View.VISIBLE
            //showOrHideProgress(View.VISIBLE)
            showShimmerEffect()
            viewModel?.initSongs(mainActivity, this)
        } else {
            requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
        }
    }

    private fun showShimmerEffect() {
        mainFragmentBinding.songRecyclerView.setLayoutManager(
            LinearLayoutManager(mainActivity),
            R.layout.song_view_item
        )
        mainFragmentBinding.songRecyclerView.showShimmer()
    }

    private fun hideShimmerEffect() {
        mainFragmentBinding.songRecyclerView.hideShimmer()
    }

    private fun setAdapter(songList: ArrayList<Song>) {
        songAdapter = SongAdapter(mainActivity, songList, this)
        mainFragmentBinding.songRecyclerView.adapter = songAdapter
    }

    private fun initView() {
        viewModel?.setSettingScreen(null)
        mainFragmentBinding.grantAccessBtn.setOnClickListener {
            if (ActivityCompat.shouldShowRequestPermissionRationale(
                    mainActivity,
                    Manifest.permission.READ_EXTERNAL_STORAGE
                )
            ) {
                requestPermissionLauncher.launch(Manifest.permission.READ_EXTERNAL_STORAGE)
            } else {
                val intent = Intent(
                    Settings.ACTION_APPLICATION_DETAILS_SETTINGS,
                    Uri.parse("package:" + mainActivity.packageName)
                )
                intent.addCategory(Intent.CATEGORY_DEFAULT)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK
                viewModel?.setSettingScreen("openedSettingScreen")
                mainActivity.startActivity(intent)
            }
        }
    }

    private fun checkPermission(): Boolean {
        return ContextCompat.checkSelfPermission(
            mainActivity,
            Manifest.permission.READ_EXTERNAL_STORAGE
        ) == PackageManager.PERMISSION_GRANTED
    }

    private val requestPermissionLauncher = registerForActivityResult(
        RequestPermission()
    )
    { isGranted: Boolean ->
        if (!isGranted && ActivityCompat.shouldShowRequestPermissionRationale(
                mainActivity,
                Manifest.permission.READ_EXTERNAL_STORAGE
            )
        ) {
            mainFragmentBinding.songRecyclerView.visibility = View.GONE
            mainFragmentBinding.grantAccessBtn.visibility = View.VISIBLE
            mainFragmentBinding.grantAccessDesc.visibility = View.VISIBLE
        } else if (isGranted) {
            mainFragmentBinding.grantAccessBtn.visibility = View.GONE
            mainFragmentBinding.grantAccessDesc.visibility = View.GONE
            view?.let { Snackbar.make(it, "Permission granted", Snackbar.LENGTH_SHORT).show() }
            mainFragmentBinding.songRecyclerView.visibility = View.VISIBLE
            //showOrHideProgress(View.VISIBLE)
            showShimmerEffect()
            viewModel?.initSongs(mainActivity, this)
        } else {
            mainFragmentBinding.songRecyclerView.visibility = View.GONE
            mainFragmentBinding.grantAccessBtn.visibility = View.VISIBLE
            mainFragmentBinding.grantAccessDesc.visibility = View.VISIBLE
            mainFragmentBinding.grantAccessDesc.text =
                "Permission denied please enable the permission for media access"
        }
    }

    fun showOrHideProgress(visibleOrGone: Int) {
        mainFragmentBinding.progressBar.visibility = visibleOrGone
    }

    private var songlistObesever = Observer<ArrayList<Song>> { songList ->
        //showOrHideProgress(View.GONE)
        hideShimmerEffect()
        songList?.let {
            setAdapter(it)
        }
    }

    override fun onResume() {
        super.onResume()
        viewModel?.isSettingScreenOpen()?.let {
            if (it == "openedSettingScreen") {
                checkForMedialPermission()
                viewModel?.setSettingScreen("checkedPermission")
            }
        }
    }

    fun navigate(currentSongPosition: Int) {
        if (mainActivity.mediaPlayer != null) {
            if (mainActivityViewModel?.songListLiveData?.value?.get(currentSongPosition)?.isPlaying == false)
                mainActivity.mediaPlayer?.stop()
        }
        mainActivityViewModel?.setCurrentSongPosition(currentSongPosition)
        Navigation.findNavController(mainFragmentBinding.root).navigate(
            R.id.action_mainFragment_to_playerFragment2
        )
    }
}