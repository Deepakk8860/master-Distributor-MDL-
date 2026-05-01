package com.android.masterdistributormdl.gskDistributor.view.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.PointF
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.os.Environment
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.ViewFullBannerBinding
import com.bumptech.glide.Glide
import com.bumptech.glide.request.target.CustomTarget
import com.android.masterdistributormdl.gskDistributor.download.DownloadFIle
import com.android.masterdistributormdl.gskDistributor.utils.ZoomImageView.Companion.DRAG
import com.android.masterdistributormdl.gskDistributor.utils.ZoomImageView.Companion.NONE
import com.android.masterdistributormdl.gskDistributor.utils.ZoomImageView.Companion.ZOOM
import com.android.masterdistributormdl.gskDistributor.utils.hideSoftKeyBoard
import com.android.masterdistributormdl.gskDistributor.utils.loadImage
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import java.io.File
import java.io.FileOutputStream

import kotlin.math.atan2
import kotlin.math.sqrt


class ViewFullBannerImages : Fragment() {
    private lateinit var binding: ViewFullBannerBinding
    var reference=""
    var title=""
    var bannerImage=""
    var shareLink=""

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
    ): View {
        binding=DataBindingUtil.inflate(inflater, R.layout.view_full_banner,container,false)

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
    }

    private fun initView() {

        requireArguments().getString("bannerImage")?.let { bannerImage ->
            this.bannerImage = bannerImage
        }

        requireArguments().getString("shareLink")?.let { shareLink ->
            this.shareLink = shareLink
        }

        requireArguments().getString("title")?.let { title ->
            this.title = title
        }


       loadImage(binding.ivBanner,bannerImage)
    }



    private fun onClick() {

        binding.back.setOnClickListener {
            hideSoftKeyBoard(it)
            requireActivity().onBackPressed()
        }

//        binding.ivBanner.setOnTouchListener { v, event ->
//            val view = v as ImageView
//            view.bringToFront()
//            viewTransformation(view, event)
//            true
//        }


        binding.frameShareOther.setOnClickListener {
            shareImageWithText(bannerImage,title,shareLink)
        }

        binding.ivCopy.setOnClickListener {
            val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Your Referral Link", shareLink)
            clipboard.setPrimaryClip(clip)
            showToastShort("Link Copied!!")
        }

        binding.llDownload.setOnClickListener {
            val downloader = DownloadFIle()
            downloader.download(requireContext(), bannerImage)
        }
    }


    private fun shareImageWithText(imageUrl: String, text: String, url: String) {
        // Download image using Glide
        Glide.with(requireContext())
            .asBitmap()  // Request a Bitmap image
            .load(imageUrl)  // Provide the URL to load the image from
            .into(object : CustomTarget<Bitmap>() {  // CustomTarget for Bitmap
                override fun onResourceReady(
                    resource: Bitmap,
                    transition: com.bumptech.glide.request.transition.Transition<in Bitmap>?
                ) {
                    try {
                        // Save the image to a file in the app's external storage
                        val file = File(
                            requireContext().getExternalFilesDir(Environment.DIRECTORY_PICTURES),
                            "shared_image.png"
                        )
                        val fos = FileOutputStream(file)
                        resource.compress(Bitmap.CompressFormat.PNG, 100, fos)
                        fos.close()

                        // Create URI for the file using FileProvider
                        val uri: Uri = FileProvider.getUriForFile(
                            requireContext(),
                            requireActivity().packageName + ".provider",  // Use the correct authority
                            file
                        )


                        // Create an intent to share the image
                        val shareIntent = Intent(Intent.ACTION_SEND).apply {
                            type = "image/png"
                            putExtra(Intent.EXTRA_STREAM, uri)
                            putExtra(Intent.EXTRA_TEXT, "$text $url")
                            addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION)
                        }

                        // Launch the share dialog
                        requireContext().startActivity(Intent.createChooser(shareIntent, "Share Image"))

                    } catch (e: Exception) {
                        e.printStackTrace()  // Handle error if needed
                        Log.d("fjhhbufhg", "onResourceReady: ${e.message}")
                    }
                }

                override fun onLoadCleared(placeholder: Drawable?) {
                    // Handle cleanup if needed
                }
            })
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


