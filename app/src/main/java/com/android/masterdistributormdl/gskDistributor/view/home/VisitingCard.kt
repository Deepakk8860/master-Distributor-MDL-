package com.android.masterdistributormdl.gskDistributor.view.home

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Rect
import android.os.Build
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.LayoutInflater
import android.view.PixelCopy
import android.view.View
import android.view.ViewGroup
import android.view.Window
import androidx.annotation.RequiresApi
import androidx.core.content.FileProvider
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.VisitingCardBinding
import com.android.masterdistributormdl.gskDistributor.model.User
import com.android.masterdistributormdl.gskDistributor.utils.DownloadFileCustom
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.Loading
import com.android.masterdistributormdl.gskDistributor.utils.STATUS_COLOR2
import com.android.masterdistributormdl.gskDistributor.utils.loadImage
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.android.masterdistributormdl.gskDistributor.view.MainActivity

import java.io.File
import java.io.FileOutputStream
import java.io.OutputStream


class VisitingCard : Fragment() {
    private lateinit var binding: VisitingCardBinding
    lateinit var model: HomeModelLead

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.visiting_card, container, false)
        model = ViewModelProvider(this)[HomeModelLead::class.java]
        binding.model = model
        binding.lifecycleOwner = this
        binding.executePendingBindings()
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
            (activity as MainActivity).setHeader("", STATUS_COLOR2)

        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHeader()

        model.retrofitError.observe(viewLifecycleOwner) {
            if (it.status == 0) {
                showToastShort(it.message)
            } else {
                InternetError.show(requireContext())
            }
        }
        model.errorMessage.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) showToastShort(it)
        }
        model.isLoaderVisible.observe(viewLifecycleOwner) {
            Loading.showHide2(requireContext(), it)
        }

        initListener()
        getUserProfile()
    }

    private fun getUserProfile() {
        model.getUserProfile {
            if (it.status == 0) {
                setData(it.data)
                (requireActivity() as MainActivity).setUserData(it.data)
            }
        }
    }


    private fun saveImage(bitmap: Bitmap): File {
        val imagesFolder = File(requireContext().filesDir, "images")
        imagesFolder.mkdirs()

        val imageFile = File(imagesFolder, "my_image.png")

        val outputStream: OutputStream = FileOutputStream(imageFile)
        bitmap.compress(Bitmap.CompressFormat.PNG, 90, outputStream)
        outputStream.flush()
        outputStream.close()

        return imageFile
    }

    private fun shareImage(imageFile: File) {
        val uri = FileProvider.getUriForFile(
            requireContext(),
            "${requireContext().packageName}.provider",
            imageFile
        )
        val intent = Intent(Intent.ACTION_SEND)
        intent.putExtra(Intent.EXTRA_STREAM, uri)
        intent.type = "image/png"
        startActivity(Intent.createChooser(intent, "Share Image"))
    }

    private fun setData(it: User) {
        loadImage(binding.userImage, it.profilephoto)
        binding.name.text = it.fullname
        binding.email.text = it.email
        binding.mobileNo.text = it.mobile
    }

    @RequiresApi(Build.VERSION_CODES.O)
    private fun initListener() {
        binding.frameShareOther.setOnClickListener {
            viewToBitmap(binding.constShare) { bitmap ->
                if (bitmap != null) {
                    val imageBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                    val imageFile = saveImage(imageBitmap) // Save bitmap to storage and get URI
                    shareImage(imageFile)
                } else {
                    showToastShort("Failed to capture image")
                }
            }

        }

        binding.llDownload.setOnClickListener {
            viewToBitmap(binding.constShare) { bitmap ->
                if (bitmap != null) {
                    val imageBitmap = bitmap.copy(Bitmap.Config.ARGB_8888, true)
                    val imageFile = saveImage(imageBitmap) // Save bitmap to storage and get URI

                    val uri = FileProvider.getUriForFile(
                        requireContext(),
                        "${requireContext().packageName}.provider",
                        imageFile
                    )
                    val downloader = DownloadFileCustom()
                    downloader.download(requireContext(), uri.toString())
                } else {
                    showToastShort("Failed to capture image")
                }
            }
        }

        binding.serviceLink.setOnClickListener {
            val clipboard =
                requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
            val clip = ClipData.newPlainText("Service Link", "https://distributor.gstsuvidhakendra.org/promotional-earnings/?request=NjYtNA==")
            clipboard.setPrimaryClip(clip)
            showToastShort("Link Copied!!")
        }
    }




    @RequiresApi(Build.VERSION_CODES.O)
    fun viewToBitmap(view: View, callback: (Bitmap?) -> Unit) {
        val window: Window? = requireActivity().window
        val bitmap = Bitmap.createBitmap(view.width, view.height, Bitmap.Config.ARGB_8888)
        val location = IntArray(2)
        view.getLocationInWindow(location)

        try {
            if (window != null) {
                PixelCopy.request(
                    window,
                    Rect(
                        location[0],
                        location[1],
                        location[0] + view.width,
                        location[1] + view.height
                    ),
                    bitmap,
                    { copyResult ->
                        if (copyResult == PixelCopy.SUCCESS) {
                            callback(bitmap)
                        } else {
                            callback(null)
                        }
                    },
                    Handler(Looper.getMainLooper())
                )
            }
        } catch (e: IllegalArgumentException) {
            e.printStackTrace()
            callback(null)
        }
    }


}




