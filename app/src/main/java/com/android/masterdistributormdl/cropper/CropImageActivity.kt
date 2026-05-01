package com.android.masterdistributormdl.cropper


import android.Manifest
import android.content.Intent
import android.graphics.drawable.Drawable
import android.net.Uri
import android.os.Bundle
import android.util.Log
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.widget.ImageView
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.graphics.BlendModeColorFilterCompat
import androidx.core.graphics.BlendModeCompat
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.CropImageActivityBinding
import com.android.masterdistributormdl.utils.getDialog
import com.android.masterdistributormdl.utils.showToast
import com.android.masterdistributormdl.cropper.CropImageView.CropResult
import com.android.masterdistributormdl.cropper.CropImageView.OnCropImageCompleteListener
import com.android.masterdistributormdl.cropper.CropImageView.OnSetImageUriCompleteListener

import java.io.File

open class CropImageActivity : AppCompatActivity(),
    OnSetImageUriCompleteListener,
    OnCropImageCompleteListener {
    private var cropImageUri: Uri? = null
    private lateinit var  binding: CropImageActivityBinding

    private lateinit var cropImageOptions: CropImageOptions

    private var latestTmpUri: Uri? = null
    private val pickImageGallery =
        registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
            onPickImageResult(uri)
        }
    private val takePicture = registerForActivityResult(ActivityResultContracts.TakePicture()) {

        onPickImageResult(latestTmpUri)
    }

    override fun onResume() {
        super.onResume()
//        Log.d("$TAG-LifeCycle", "onResume  CropImageActivity")
    }

    override fun onDestroy() {
        super.onDestroy()
//        Log.d("$TAG-LifeCycle", "onDestroy  CropImageActivity")
    }

    public override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding= CropImageActivityBinding.inflate(layoutInflater)
        setContentView(binding.root)

        val bundle = intent.getBundleExtra(CropImage.CROP_IMAGE_EXTRA_BUNDLE)
        cropImageUri = bundle?.getParcelable(CropImage.CROP_IMAGE_EXTRA_SOURCE)
        cropImageOptions =
            bundle?.getParcelable(CropImage.CROP_IMAGE_EXTRA_OPTIONS) ?: CropImageOptions()

        if (cropImageUri == null || cropImageUri == Uri.EMPTY) {
            when {
                cropImageOptions.imageSourceIncludeGallery && cropImageOptions.imageSourceIncludeCamera ->
                    showDialog()

                cropImageOptions.imageSourceIncludeCamera ->
                    checkPermission(Manifest.permission.CAMERA, 1001)

                cropImageOptions.imageSourceIncludeGallery ->
                    checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, 1002)

                else ->
                    finish()
            }
        } else
            binding.cropImageView.setImageUriAsync(cropImageUri)


        supportActionBar?.let {
            titleColor = resources.getColor(R.color.colorPrimary)
            title =
                if (cropImageOptions.activityTitle.isNotEmpty())
                    cropImageOptions.activityTitle
                else {
                    "Crop"
                }
            it.setDisplayHomeAsUpEnabled(true)
        }
    }

    private fun checkPermission(permission: String, requestCode: Int) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), requestCode)
    }

    private fun openGallery() {
        pickImageGallery.launch("image/*")
    }

    private fun openCamera() {
        getTmpFileUri().let { uri ->
            latestTmpUri = uri
            takePicture.launch(uri)
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<out String>,
        grantResults: IntArray
    ) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults)
        try {
            if (requestCode == 1001 && grantResults.isNotEmpty()) {
                openCamera()
            } else if (requestCode == 1002 && grantResults.isNotEmpty()) {
                openGallery()
            } else {
                showToast("Please Allow Camera OR Storage Permissions")
                onBackPressed()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }


    private fun getTmpFileUri(): Uri {
        val time = System.currentTimeMillis()
        val tmpFile = File.createTempFile("tmp_image_$time", ".png", cacheDir).apply {
            createNewFile()
            deleteOnExit()
        }
        return getUriForFile(this, tmpFile)
    }

    open fun showDialog() {
        val dialog = getDialog(this, R.layout.image_picker_dialog)
        dialog.show()
        dialog.setCancelable(false)
        val camera = dialog.findViewById<View>(R.id.camera)
        val gallery = dialog.findViewById<View>(R.id.gallery)
        val cancel = dialog.findViewById<ImageView>(R.id.cancel)
        camera.setOnClickListener {
            dialog.dismiss()
            checkPermission(Manifest.permission.CAMERA, 1001)
        }
        gallery.setOnClickListener {
            dialog.dismiss()
            checkPermission(Manifest.permission.READ_EXTERNAL_STORAGE, 1002)
        }
        cancel.setOnClickListener {
            dialog.dismiss()
            onBackPressed()
        }

    }

    public override fun onStart() {
        super.onStart()
        binding.cropImageView.setOnSetImageUriCompleteListener(this)
        binding.cropImageView.setOnCropImageCompleteListener(this)
    }

    public override fun onStop() {
        super.onStop()
        binding.cropImageView.setOnSetImageUriCompleteListener(null)
        binding.cropImageView.setOnCropImageCompleteListener(null)
    }

    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        menuInflater.inflate(R.menu.crop_image_menu, menu)

        if (!cropImageOptions.allowRotation) {
            menu.removeItem(R.id.ic_rotate_left_24)
            menu.removeItem(R.id.ic_rotate_right_24)
        } else if (cropImageOptions.allowCounterRotation) {
            menu.findItem(R.id.ic_rotate_left_24).isVisible = true
        }

        if (!cropImageOptions.allowFlipping)
            menu.removeItem(R.id.ic_flip_24)

        if (cropImageOptions.cropMenuCropButtonTitle != null) {
            menu.findItem(R.id.crop_image_menu_crop).title =
                cropImageOptions.cropMenuCropButtonTitle
        }
        var cropIcon: Drawable? = null
        try {
            if (cropImageOptions.cropMenuCropButtonIcon != 0) {
                cropIcon = ContextCompat.getDrawable(this, cropImageOptions.cropMenuCropButtonIcon)
                menu.findItem(R.id.crop_image_menu_crop).icon = cropIcon
            }
        } catch (e: Exception) {
            Log.w("AIC", "Failed to read menu crop drawable", e)
        }
        if (cropImageOptions.activityMenuIconColor != 0) {
            updateMenuItemIconColor(
                menu,
                R.id.ic_rotate_left_24,
                cropImageOptions.activityMenuIconColor
            )
            updateMenuItemIconColor(
                menu,
                R.id.ic_rotate_right_24,
                cropImageOptions.activityMenuIconColor
            )
            updateMenuItemIconColor(menu, R.id.ic_flip_24, cropImageOptions.activityMenuIconColor)

            if (cropIcon != null) {
                updateMenuItemIconColor(
                    menu,
                    R.id.crop_image_menu_crop,
                    cropImageOptions.activityMenuIconColor
                )
            }
        }
        return true
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        when (item.itemId) {
            R.id.crop_image_menu_crop ->
                cropImage()

            R.id.ic_rotate_left_24 ->
                rotateImage(-cropImageOptions.rotationDegrees)

            R.id.ic_rotate_right_24 ->
                rotateImage(cropImageOptions.rotationDegrees)

            R.id.ic_flip_24_horizontally ->
                binding.cropImageView.flipImageHorizontally()

            R.id.ic_flip_24_vertically ->
                binding.cropImageView.flipImageVertically()

            android.R.id.home ->
                setResultCancel()

            else ->
                return super.onOptionsItemSelected(item)
        }
        return true
    }

    override fun onBackPressed() {
        super.onBackPressed()
        setResultCancel()
    }

    protected open fun onPickImageResult(resultUri: Uri?) {
        when (resultUri) {
            null ->
                setResultCancel()

            else -> {
                cropImageUri = resultUri
                binding.cropImageView.setImageUriAsync(cropImageUri)
            }
        }
    }

    override fun onSetImageUriComplete(view: CropImageView, uri: Uri, error: Exception?) {
        if (error == null) {
            if (cropImageOptions.initialCropWindowRectangle != null)
                binding.cropImageView.cropRect = cropImageOptions.initialCropWindowRectangle

            if (cropImageOptions.initialRotation > 0)
                binding.cropImageView.rotatedDegrees = cropImageOptions.initialRotation
        } else
            setResult(null, error, 1)
    }

    override fun onCropImageComplete(view: CropImageView, result: CropResult) {
        setResult(result.uriContent, result.error, result.sampleSize)
    }

    /**
     * Execute crop image and save the result tou output uri.
     */
    open fun cropImage() {
        if (cropImageOptions.noOutputImage)
            setResult(null, null, 1)
        else
            binding.cropImageView.croppedImageAsync(
                saveCompressFormat = cropImageOptions.outputCompressFormat,
                saveCompressQuality = cropImageOptions.outputCompressQuality,
                reqWidth = cropImageOptions.outputRequestWidth,
                reqHeight = cropImageOptions.outputRequestHeight,
                options = cropImageOptions.outputRequestSizeOptions,
                customOutputUri = cropImageOptions.customOutputUri,
            )
    }


    open fun rotateImage(degrees: Int) {
        binding.cropImageView.rotateImage(degrees)
    }


    open fun setResult(uri: Uri?, error: Exception?, sampleSize: Int) {
        setResult(
            error?.let { CropImage.CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE } ?: RESULT_OK,
            getResultIntent(uri, error, sampleSize)
        )
        finish()
    }


    open fun setResultCancel() {
        setResult(RESULT_CANCELED)
        finish()
    }


    open fun getResultIntent(uri: Uri?, error: Exception?, sampleSize: Int): Intent {
        val result = CropImage.ActivityResult(
            binding.cropImageView.imageUri,
            uri,
            error,
            binding.cropImageView.cropPoints,
            binding.cropImageView.cropRect,
            binding.cropImageView.rotatedDegrees ?: 0,
            binding.cropImageView.wholeImageRect,
            sampleSize
        )
        val intent = Intent()
        intent.putExtras(getIntent())
        intent.putExtra(CropImage.CROP_IMAGE_EXTRA_RESULT, result)
        return intent
    }

    /**
     * Update the color of a specific menu item to the given color.
     */
    open fun updateMenuItemIconColor(menu: Menu, itemId: Int, color: Int) {
        val menuItem = menu.findItem(itemId)
        if (menuItem != null) {
            val menuItemIcon = menuItem.icon
            if (menuItemIcon != null) {
                try {
                    menuItemIcon.apply {
                        mutate()
                        colorFilter = BlendModeColorFilterCompat.createBlendModeColorFilterCompat(
                            color,
                            BlendModeCompat.SRC_ATOP
                        )
                    }
                    menuItem.icon = menuItemIcon
                } catch (e: Exception) {
                    Log.w("AIC", "Failed to update menu item color", e)
                }
            }
        }
    }

}
