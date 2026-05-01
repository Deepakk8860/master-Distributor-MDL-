package com.android.masterdistributormdl.gskDistributor.view.home

import android.app.Application
import android.graphics.BitmapFactory
import android.graphics.PointF
import android.os.Bundle
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import androidx.databinding.DataBindingUtil
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.cropper.CropImageContract
import com.android.masterdistributormdl.cropper.CropOptions
import com.android.masterdistributormdl.databinding.ViewProfilePhotoBinding
import com.google.gson.JsonObject
import com.gsk.distributor.model.ApiResponse
import com.gsk.distributor.model.ErrorAlert
import com.android.masterdistributormdl.gskDistributor.model.User
import com.gsk.distributor.model.UserResult
import com.android.masterdistributormdl.gskDistributor.network.errorRetrofit
import com.android.masterdistributormdl.gskDistributor.network.getClient
import com.android.masterdistributormdl.gskDistributor.utils.STATUS_COLOR2
import com.android.masterdistributormdl.gskDistributor.utils.ZoomImageView.Companion.DRAG
import com.android.masterdistributormdl.gskDistributor.utils.ZoomImageView.Companion.NONE
import com.android.masterdistributormdl.gskDistributor.utils.ZoomImageView.Companion.ZOOM
import com.android.masterdistributormdl.gskDistributor.utils.gson
import com.android.masterdistributormdl.gskDistributor.utils.hideSoftKeyBoard
import com.android.masterdistributormdl.gskDistributor.utils.imageToBase64
import com.android.masterdistributormdl.gskDistributor.utils.loadImage
import com.android.masterdistributormdl.gskDistributor.utils.setOnBackResult
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.user_data
import com.android.masterdistributormdl.gskDistributor.utils.user_id
import com.android.masterdistributormdl.gskDistributor.view.MainActivity
import com.android.masterdistributormdl.utils.SharedPreference
import com.android.masterdistributormdl.utils.showToastShort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File

import kotlin.math.atan2
import kotlin.math.sqrt


class ViewProfilePhoto : Fragment() {
    private lateinit var binding: ViewProfilePhotoBinding
    lateinit var model: ProfilePhotoModel
    var reference = ""
    var title = ""
    var profilePicture = ""

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
    private var isEdit=false

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.view_profile_photo, container, false)
        model = ViewModelProvider(this)[ProfilePhotoModel::class.java]
        binding.model = model
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
            if (activity is MainActivity) (activity as MainActivity).setHeader(
                "ViewProfilePhoto",
                STATUS_COLOR2
            )
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

        requireArguments().getString("profilePhoto")?.let { profilePicture ->
            this.profilePicture = profilePicture
        }

        loadImage(binding.ivProfile, profilePicture)
    }


    private fun onClick() {
        binding.ivEditProfilePhoto.setOnClickListener {
            startCrop()
        }

        binding.back.setOnClickListener {
            hideSoftKeyBoard(it)
            if (isEdit){
                setOnBackResult(requireActivity(), "viewProfile")
            }else{
               requireActivity().onBackPressed()
            }


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
                        view.animate().x(event.rawX + xCoOrdinate).y(event.rawY + yCoOrdinate)
                            .setDuration(0).start()
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

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val uri = result.uriContent!!
            val filePath = result.getUriFilePath(requireContext())
            val file = File(filePath)
            val bitmap = BitmapFactory.decodeFile(filePath)
            isEdit=true
            binding.ivProfile.setImageBitmap(bitmap)
            val base64 = imageToBase64(file)
            file.delete()
            model.uploadprofileimg(base64) {
                if (it.status==0) {
                    showToastShort(it.message)
                    getUserProfile()
                }

            }
        } else {
            val exception = result.error
            // AlertError.show(requireActivity(), exception!!.localizedMessage)
        }
    }

    private fun getUserProfile() {
        model.getUserProfile {
            if (it.status == 0) {
                model.save(it.data)
                setData()
            }
        }
    }

    fun setData() {
        val user = model.user
        loadImage(binding.ivProfile, user.profilephoto)
    }


    private fun startCrop() {
        cropImage.launch(CropOptions {
            setAspectRatio(500, 500)
            setActivityTitle("Pick Image")
            setRequestedSize(300, 300)
            setAllowFlipping(true)
            setAllowRotation(true)
            setImageSource(includeGallery = true, includeCamera = true)
        })
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

class ProfilePhotoModel(application: Application) : AndroidViewModel(application) {
    val sharedPreference = SharedPreference()
    val isLoaderVisible = MutableLiveData<Boolean>()
    val isDialogVisible = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val retrofitError = MutableLiveData<ErrorAlert>()
    var user = sharedPreference.getUserDist()!!

    fun save(user: User) {
        this.user = user
        sharedPreference.putString(user_data, gson.toJson(user))
    }


    fun uploadprofileimg(base64: String, result: (ApiResponse) -> Unit) {
        isLoaderVisible.value = true
        val param = JsonObject()
        param.addProperty("uid", sharedPreference.getString(user_id))
        param.addProperty("profileimg", base64)
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().uploadprofileimg(param).body()!!
                }
            }.onSuccess {
                isLoaderVisible.value = false
                result.invoke(it)
            }.onFailure {
                isLoaderVisible.value = false
                retrofitError.postValue(errorRetrofit(it))
            }
        }
    }



    fun getUserProfile(result: (UserResult) -> Unit) {
//        isLoaderVisible.value = true
        val param = JsonObject()
        param.addProperty("uid", sharedPreference.getString(user_id))
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().getUserProfile(param).body()!!
                }
            }.onSuccess {
                isLoaderVisible.value = false
                result.invoke(it)
            }.onFailure {
                isLoaderVisible.value = false
                retrofitError.postValue(errorRetrofit(it))
            }
        }
    }



}


