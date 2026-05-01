package com.android.masterdistributormdl.gskDistributor.view.home

import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.PlayTrainingVideoBinding

import com.android.masterdistributormdl.gskDistributor.utils.hideSoftKeyBoard
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import kotlinx.coroutines.launch


class PlayTrainingVideo : Fragment() {
    private lateinit var binding: PlayTrainingVideoBinding
    var title=""
    private var videoUrl=""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding=DataBindingUtil.inflate(inflater, R.layout.play_training_video,container,false)

        return binding.root
    }
    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            setHeader()
        }
    }


    private fun setHeader() {
        try {
            shooterFragment = this
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHeader()
        initView()
        onClick()
        playVideo()
    }

    private fun initView() {

        requireArguments().getString("videoUrl")?.let { videoUrl ->
            this.videoUrl = videoUrl
        }

        requireArguments().getString("title")?.let { title ->
            this.title = title
        }

    }

    private fun playVideo() {
        // Launching a coroutine in the main scope (for UI-related tasks)
        lifecycleScope.launch {
            // Get the video URL from fragment arguments (you can replace this with actual argument fetching)
            val uri = Uri.parse(videoUrl)

            // Set up the media controller to control playback (on main thread)
            val mediaController = MediaController(requireContext()).apply {
                setAnchorView(binding.videoView)
            }

            binding.videoView.apply {
                setMediaController(mediaController)
                setVideoURI(uri)  // Set the URI of the video
                setOnPreparedListener {
                    start() // Auto-play when ready
                }
                setOnCompletionListener {
                    // Handle video completion (optional, e.g., go to another screen or reset UI)
                }
            }
        }
    }



    private fun onClick() {

        binding.back.setOnClickListener {
            hideSoftKeyBoard(it)
            requireActivity().onBackPressed()
        }
    }







}


