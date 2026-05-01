package com.android.masterdistributormdl.gskDistributor.view.onboarding

import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.MediaController
import android.widget.TextView
import android.widget.VideoView
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.lifecycleScope
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.ActivityVideoPlaybackBinding
import com.google.gson.JsonObject
import com.android.masterdistributormdl.gskDistributor.utils.AlertError
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.LoadingVideo
import com.android.masterdistributormdl.gskDistributor.utils.compressAndConvertToBase64
import com.android.masterdistributormdl.gskDistributor.utils.replaceFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.android.masterdistributormdl.gskDistributor.view.VideoKyc
import com.android.masterdistributormdl.gskDistributor.view.home.Home
import kotlinx.coroutines.launch
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import java.io.File

class VideoPlayFragment : Fragment() {
    private lateinit var binding: ActivityVideoPlaybackBinding
    lateinit var model: OnboardModel
    private lateinit var videoView: VideoView
    private lateinit var back: ImageView
    private lateinit var txtUpload: TextView
    private var videoPath: String? = ""
    private var kycVideo: String? = ""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        // Inflate the layout for this fragment
        binding =
            DataBindingUtil.inflate(inflater, R.layout.activity_video_playback, container, false)
        model = ViewModelProvider(this)[OnboardModel::class.java]
        binding.model = model
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        initView()
        loaderVisible()
        initListener()
    }

    private fun loaderVisible() {
        model.retrofitError.observe(viewLifecycleOwner) {
            if (it.status == 0) {
                showToastShort(it.message)
            } else {
                InternetError.show(requireContext())
            }
        }
        model.errorMessage.observe(viewLifecycleOwner) {
            AlertError.show(requireContext(), it) {}
        }
        model.isLoaderVisible.observe(viewLifecycleOwner) {
            LoadingVideo.showHide(requireContext(), it)
        }
    }

    private fun initListener() {
//        back.setOnClickListener {
//            requireActivity().onBackPressedDispatcher.onBackPressed()
//        }

        binding.back2.setOnClickListener {
            replaceFragment(requireActivity(), VideoKyc())
//            requireActivity().onBackPressed()
        }

        binding.next.setOnClickListener {
//                uploadVideo()
            val targetBitrate = 1000000 // Set the desired target bitrate
          // Call this function inside a coroutine scope
            lifecycleScope.launch {
                kycVideo = compressAndConvertToBase64(
                    requireContext(),
                    videoPath!!, targetBitrate
                )
                if (kycVideo.isNullOrBlank()) {
//                    Loading.showHide(requireContext(), false)
                    showToastShort("Your video size is too large.")
                } else {
//                    Loading.showHide(requireContext(), false)
                    uploadVideoFile()
                }
            }

        }

    }

    private fun uploadVideo() {
//        val param=UploadVideo(videoPath.toString(),"66")
//        param.addProperty("kyc_video", kycVideo)
        val videoFile = File(videoPath)

        if (videoFile.exists()) {
            val videoRequestBody = RequestBody.create(
                "video/mp4".toMediaTypeOrNull(), videoFile
            )
            val videoPart =
                MultipartBody.Part.createFormData("video", videoFile.name, videoRequestBody)

            // Prepare UID as a RequestBody
            val uidRequestBody = RequestBody.create(
                "text/plain".toMediaTypeOrNull(), "66"
            )
//            uploadVideoFile(videoPart,uidRequestBody)
        }


    }

    private fun uploadVideoFile() {
        val param = JsonObject()
        param.addProperty("kyc_video", kycVideo)
        model.uploadVideo(param) {
            if (it.status == 0) {
                showToastShort(it.message)
                getUserProfile()
            } else {
                AlertError.show(requireContext(), it.message) {}
            }
        }
    }

    fun getUserProfile() {
        model.getUserProfile {
            if (it.status == 0) {
                model.save(it.data)
                replaceFragment(requireActivity(), Home())
            }
            else if (it.status==100){
                //handle maintenance
            }
            else {
                AlertError.show(requireActivity(), it.message) {}
            }
        }
    }

    private fun initView() {
        videoView = requireView().findViewById(R.id.videoView)
        back = requireView().findViewById(R.id.back)

        // Get the video path from fragment arguments
        videoPath = arguments?.getString("video_path")
        if (videoPath != null) {
            val uri = Uri.parse(videoPath)
            videoView.setVideoURI(uri)
//            kycVideo= videoToBase64(videoPath!!).toString()


            // Set up media controls
            val mediaController = MediaController(requireContext())
            mediaController.setAnchorView(videoView)
            videoView.setMediaController(mediaController)

            videoView.setOnPreparedListener {
                videoView.start() // Auto-play when ready
            }

            videoView.setOnCompletionListener {
                // Handle video completion (optional)
            }
        } else {
            Log.e("VideoPlayFragment", "Video path is null")
        }
    }
}
