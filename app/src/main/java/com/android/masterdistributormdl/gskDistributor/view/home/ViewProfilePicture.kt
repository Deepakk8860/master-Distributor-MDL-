package com.android.masterdistributormdl.gskDistributor.view.home

import android.graphics.PointF
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.ViewProfilePictureBinding
import com.android.masterdistributormdl.gskDistributor.utils.ZoomImageView.Companion.DRAG
import com.android.masterdistributormdl.gskDistributor.utils.ZoomImageView.Companion.NONE
import com.android.masterdistributormdl.gskDistributor.utils.ZoomImageView.Companion.ZOOM
import com.android.masterdistributormdl.gskDistributor.utils.hideSoftKeyBoard
import com.android.masterdistributormdl.gskDistributor.utils.loadImage
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment

import kotlin.math.atan2
import kotlin.math.sqrt


class ViewProfilePicture : Fragment() {
    private lateinit var binding: ViewProfilePictureBinding
    var reference=""
    var title=""
    var profilePicture=""

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
        binding=DataBindingUtil.inflate(inflater, R.layout.view_profile_picture,container,false)

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

        requireArguments().getString("profilePicture")?.let { profilePicture ->
            this.profilePicture = profilePicture
        }

       loadImage(binding.ivProfile,profilePicture)
    }



    private fun onClick() {

        binding.back.setOnClickListener {
            hideSoftKeyBoard(it)
            requireActivity().onBackPressed()
        }

        binding.ivProfile.setOnTouchListener { v, event ->
            val view = v as ImageView
            view.bringToFront()
            viewTransformation(view, event)
            true
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


