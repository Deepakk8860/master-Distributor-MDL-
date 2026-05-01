package com.android.masterdistributormdl.gskDistributor.view.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.PointF
import android.net.Uri
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.MediaController
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.lifecycleScope
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.PlayVideoBinding
import com.android.masterdistributormdl.gskDistributor.download.DownloadFIle
import com.android.masterdistributormdl.gskDistributor.utils.ZoomImageView.Companion.DRAG
import com.android.masterdistributormdl.gskDistributor.utils.ZoomImageView.Companion.NONE
import com.android.masterdistributormdl.gskDistributor.utils.ZoomImageView.Companion.ZOOM
import com.android.masterdistributormdl.gskDistributor.utils.hideSoftKeyBoard
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import okhttp3.OkHttpClient
import okhttp3.Request
import java.io.File
import java.io.FileOutputStream

import kotlin.math.atan2
import kotlin.math.sqrt


class PlayVideo : Fragment() {
    private lateinit var binding: PlayVideoBinding
    var reference=""
    var title=""
    var videoUrl=""
    var shareLink=""
    lateinit var videoFile:File
    private var videoPath: String? = ""
    private var xCoOrdinate = 0f
    private var yCoOrdinate = 0f
    private var mode = NONE
    private var lastEvent: FloatArray? = null
    private var oldDist = 1f
    private var d = 0f
    private var newRot = 0f
    private val start = PointF()
    private val mid = PointF()
    private var isOutSide = false
    private var isZoomAndRotate = false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        binding=DataBindingUtil.inflate(inflater, R.layout.play_video,container,false)

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
        downloadAndShareVideo(videoUrl)
    }

    private fun initView() {

        requireArguments().getString("videoUrl")?.let { videoUrl ->
            this.videoUrl = videoUrl
        }

        requireArguments().getString("shareLink")?.let { shareLink ->
            this.shareLink = shareLink
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



        binding.frameShareOther.setOnClickListener {
//            shareTextDetails(shareLink,videoUrl)
//            downloadAndShareVideo(videoUrl)
            shareVideoFile(videoFile)
        }

        binding.ivCopy.setOnClickListener {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Your Referral Link", shareLink)
            clipboard.setPrimaryClip(clip)
            showToastShort("Link Copied!!")
        }

        binding.llDownload.setOnClickListener {
            val downloader = DownloadFIle()
            downloader.download(requireContext(), videoUrl)
        }
    }


    private fun downloadAndShareVideo(videoUrl: String) {
        if (videoUrl.isNotEmpty()){
            CoroutineScope(Dispatchers.IO).launch {
                try {
                    val client = OkHttpClient()

                    val request = Request.Builder()
                        .url(videoUrl)
                        .build()

                    val response = client.newCall(request).execute()
                    if (response.isSuccessful) {
                        response.body?.let { body ->
                            videoFile = File(requireContext().cacheDir, "shared_video.mp4")

                            // Save the file locally
                            FileOutputStream(videoFile).use { outputStream ->
                                outputStream.write(body.bytes())
                            }
                        }
                    } else {
                        // Handle download failure
                        withContext(Dispatchers.Main) {
                            // Show error message (e.g., Toast)
                        }
                    }
                } catch (e: Exception) {
                    e.printStackTrace()
                    withContext(Dispatchers.Main) {
                        // Handle exceptions (e.g., show error message)
                    }
                }
            }
        }

    }


    private fun shareVideoFile(file: File) {
        if (file.exists()){
            val fileUri: Uri = FileProvider.getUriForFile(
                requireContext(),
                requireActivity().packageName + ".provider",  // Use the correct authority
                file
            )

            val shareIntent = Intent(Intent.ACTION_SEND).apply {
                type = "video/mp4"
                putExtra(Intent.EXTRA_STREAM, fileUri)
                putExtra(Intent.EXTRA_TEXT, shareLink)

                addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
            }

            startActivity(Intent.createChooser(shareIntent, "Share Video via"))
        }

    }




    private fun viewTransformation(view: View, event: MotionEvent) {
        when (event.action and MotionEvent.ACTION_MASK) {
            MotionEvent.ACTION_DOWN -> {
                xCoOrdinate = view.x - event.rawX
                yCoOrdinate = view.y - event.rawY

                start.set(event.x, event.y)
                isOutSide = false
                mode = DRAG
                lastEvent = null
            }
            MotionEvent.ACTION_POINTER_DOWN -> {
                oldDist = spacing(event)
                if (oldDist > 10f) {
                    midPoint(mid, event)
                    mode = ZOOM
                }
                lastEvent = FloatArray(4)
                lastEvent!![0] = event.getX(0)
                lastEvent!![1] = event.getX(1)
                lastEvent!![2] = event.getY(0)
                lastEvent!![3] = event.getY(1)
                d = rotation(event)
            }
            MotionEvent.ACTION_UP -> {
                isZoomAndRotate = false
                if (mode == DRAG) {
                    // Handle drag action
                    val x = event.x
                    val y = event.y
                }
            }
            MotionEvent.ACTION_OUTSIDE -> {
                isOutSide = true
                mode = NONE
                lastEvent = null
            }
            MotionEvent.ACTION_POINTER_UP -> {
                mode = NONE
                lastEvent = null
            }
            MotionEvent.ACTION_MOVE -> {
                if (!isOutSide) {
                    if (mode == DRAG) {
                        isZoomAndRotate = false
                        view.animate().x(event.rawX + xCoOrdinate).y(event.rawY + yCoOrdinate).setDuration(0).start()
                    }
                    if (mode == ZOOM && event.pointerCount == 2) {
                        val newDist1 = spacing(event)
                        if (newDist1 > 10f) {
                            val scale = newDist1 / oldDist * view.scaleX
                            view.scaleX = scale
                            view.scaleY = scale
                        }
                        if (lastEvent != null) {
                            newRot = rotation(event)
                            view.rotation = view.rotation + (newRot - d)
                        }
                    }
                }
            }
        }
    }

    private fun rotation(event: MotionEvent): Float {
        val delta_x = (event.getX(0) - event.getX(1)).toDouble()
        val delta_y = (event.getY(0) - event.getY(1)).toDouble()
        val radians = atan2(delta_y, delta_x)
        return Math.toDegrees(radians).toFloat()
    }

    private fun spacing(event: MotionEvent): Float {
        val x = event.getX(0) - event.getX(1)
        val y = event.getY(0) - event.getY(1)
        return sqrt((x * x + y * y).toDouble()).toFloat()
    }

    private fun midPoint(point: PointF, event: MotionEvent) {
        val x = event.getX(0) + event.getX(1)
        val y = event.getY(0) + event.getY(1)
        point.set(x / 2, y / 2)
    }






}


