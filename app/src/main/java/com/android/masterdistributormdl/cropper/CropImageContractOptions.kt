package com.android.masterdistributormdl.cropper

import android.graphics.Bitmap
import android.graphics.Rect
import android.net.Uri
import androidx.annotation.DrawableRes
import androidx.annotation.Keep
import com.android.masterdistributormdl.cropper.CropImageOptions.Companion.DEGREES_360
import com.android.masterdistributormdl.cropper.CropImageView.CropShape
import com.android.masterdistributormdl.cropper.CropImageView.Guidelines
import com.android.masterdistributormdl.cropper.CropImageView.RequestSizeOptions

@Keep
data class CropImageContractOptions @JvmOverloads constructor(
    val uri: Uri?,
    val cropImageOptions: CropImageOptions,
) {

    fun setImageSource(includeGallery: Boolean, includeCamera: Boolean): CropImageContractOptions {
        cropImageOptions.imageSourceIncludeGallery = includeGallery
        cropImageOptions.imageSourceIncludeCamera = includeCamera
        return this
    }

    fun setCropShape(cropShape: CropShape): CropImageContractOptions {
        cropImageOptions.cropShape = cropShape
        return this
    }

    fun setSnapRadius(snapRadius: Float): CropImageContractOptions {
        cropImageOptions.snapRadius = snapRadius
        return this
    }

    fun setTouchRadius(touchRadius: Float): CropImageContractOptions {
        cropImageOptions.touchRadius = touchRadius
        return this
    }

    fun setGuidelines(guidelines: Guidelines): CropImageContractOptions {
        cropImageOptions.guidelines = guidelines
        return this
    }


    fun setScaleType(scaleType: CropImageView.ScaleType): CropImageContractOptions {
        cropImageOptions.scaleType = scaleType
        return this
    }


    fun setShowCropOverlay(showCropOverlay: Boolean): CropImageContractOptions {
        cropImageOptions.showCropOverlay = showCropOverlay
        return this
    }


    fun setAutoZoomEnabled(autoZoomEnabled: Boolean): CropImageContractOptions {
        cropImageOptions.autoZoomEnabled = autoZoomEnabled
        return this
    }


    fun setMultiTouchEnabled(multiTouchEnabled: Boolean): CropImageContractOptions {
        cropImageOptions.multiTouchEnabled = multiTouchEnabled
        return this
    }


    fun setCenterMoveEnabled(centerMoveEnabled: Boolean): CropImageContractOptions {
        cropImageOptions.centerMoveEnabled = centerMoveEnabled
        return this
    }


    fun setMaxZoom(maxZoom: Int): CropImageContractOptions {
        cropImageOptions.maxZoom = maxZoom
        return this
    }


    fun setInitialCropWindowPaddingRatio(initialCropWindowPaddingRatio: Float): CropImageContractOptions {
        cropImageOptions.initialCropWindowPaddingRatio = initialCropWindowPaddingRatio
        return this
    }


    fun setFixAspectRatio(fixAspectRatio: Boolean): CropImageContractOptions {
        cropImageOptions.fixAspectRatio = fixAspectRatio
        return this
    }
    fun setAspectRatio(aspectRatioX: Int, aspectRatioY: Int): CropImageContractOptions {
        cropImageOptions.aspectRatioX = aspectRatioX
        cropImageOptions.aspectRatioY = aspectRatioY
        cropImageOptions.fixAspectRatio = true
        return this
    }

    fun setBorderLineThickness(borderLineThickness: Float): CropImageContractOptions {
        cropImageOptions.borderLineThickness = borderLineThickness
        return this
    }


    fun setBorderLineColor(borderLineColor: Int): CropImageContractOptions {
        cropImageOptions.borderLineColor = borderLineColor
        return this
    }


    fun setBorderCornerThickness(borderCornerThickness: Float): CropImageContractOptions {
        cropImageOptions.borderCornerThickness = borderCornerThickness
        return this
    }


    fun setBorderCornerOffset(borderCornerOffset: Float): CropImageContractOptions {
        cropImageOptions.borderCornerOffset = borderCornerOffset
        return this
    }


    fun setBorderCornerLength(borderCornerLength: Float): CropImageContractOptions {
        cropImageOptions.borderCornerLength = borderCornerLength
        return this
    }


    fun setBorderCornerColor(borderCornerColor: Int): CropImageContractOptions {
        cropImageOptions.borderCornerColor = borderCornerColor
        return this
    }


    fun setGuidelinesThickness(guidelinesThickness: Float): CropImageContractOptions {
        cropImageOptions.guidelinesThickness = guidelinesThickness
        return this
    }


    fun setGuidelinesColor(guidelinesColor: Int): CropImageContractOptions {
        cropImageOptions.guidelinesColor = guidelinesColor
        return this
    }


    fun setBackgroundColor(backgroundColor: Int): CropImageContractOptions {
        cropImageOptions.backgroundColor = backgroundColor
        return this
    }


    fun setMinCropWindowSize(
        minCropWindowWidth: Int,
        minCropWindowHeight: Int
    ): CropImageContractOptions {
        cropImageOptions.minCropWindowWidth = minCropWindowWidth
        cropImageOptions.minCropWindowHeight = minCropWindowHeight
        return this
    }


    fun setMinCropResultSize(
        minCropResultWidth: Int,
        minCropResultHeight: Int
    ): CropImageContractOptions {
        cropImageOptions.minCropResultWidth = minCropResultWidth
        cropImageOptions.minCropResultHeight = minCropResultHeight
        return this
    }

    fun setMaxCropResultSize(
        maxCropResultWidth: Int,
        maxCropResultHeight: Int
    ): CropImageContractOptions {
        cropImageOptions.maxCropResultWidth = maxCropResultWidth
        cropImageOptions.maxCropResultHeight = maxCropResultHeight
        return this
    }

    fun setActivityTitle(activityTitle: CharSequence): CropImageContractOptions {
        cropImageOptions.activityTitle = activityTitle
        return this
    }


    fun setActivityMenuIconColor(activityMenuIconColor: Int): CropImageContractOptions {
        cropImageOptions.activityMenuIconColor = activityMenuIconColor
        return this
    }


    fun setOutputUri(outputUri: Uri?): CropImageContractOptions {
        cropImageOptions.customOutputUri = outputUri
        return this
    }


    fun setOutputCompressFormat(outputCompressFormat: Bitmap.CompressFormat): CropImageContractOptions {
        cropImageOptions.outputCompressFormat = outputCompressFormat
        return this
    }


    fun setOutputCompressQuality(outputCompressQuality: Int): CropImageContractOptions {
        cropImageOptions.outputCompressQuality = outputCompressQuality
        return this
    }


    fun setRequestedSize(reqWidth: Int, reqHeight: Int): CropImageContractOptions {
        return setRequestedSize(reqWidth, reqHeight, RequestSizeOptions.RESIZE_INSIDE)
    }


    fun setRequestedSize(
        reqWidth: Int,
        reqHeight: Int,
        reqSizeOptions: RequestSizeOptions,
    ): CropImageContractOptions {
        cropImageOptions.outputRequestWidth = reqWidth
        cropImageOptions.outputRequestHeight = reqHeight
        cropImageOptions.outputRequestSizeOptions = reqSizeOptions
        return this
    }


    fun setNoOutputImage(noOutputImage: Boolean): CropImageContractOptions {
        cropImageOptions.noOutputImage = noOutputImage
        return this
    }


    fun setInitialCropWindowRectangle(initialCropWindowRectangle: Rect?): CropImageContractOptions {
        cropImageOptions.initialCropWindowRectangle = initialCropWindowRectangle
        return this
    }


    fun setInitialRotation(initialRotation: Int): CropImageContractOptions {
        cropImageOptions.initialRotation = (initialRotation + DEGREES_360) % DEGREES_360
        return this
    }

    fun setAllowRotation(allowRotation: Boolean): CropImageContractOptions {
        cropImageOptions.allowRotation = allowRotation
        return this
    }


    fun setAllowFlipping(allowFlipping: Boolean): CropImageContractOptions {
        cropImageOptions.allowFlipping = allowFlipping
        return this
    }


    fun setAllowCounterRotation(allowCounterRotation: Boolean): CropImageContractOptions {
        cropImageOptions.allowCounterRotation = allowCounterRotation
        return this
    }


    fun setRotationDegrees(rotationDegrees: Int): CropImageContractOptions {
        cropImageOptions.rotationDegrees = (rotationDegrees + DEGREES_360) % DEGREES_360
        return this
    }

    fun setFlipHorizontally(flipHorizontally: Boolean): CropImageContractOptions {
        cropImageOptions.flipHorizontally = flipHorizontally
        return this
    }


    fun setFlipVertically(flipVertically: Boolean): CropImageContractOptions {
        cropImageOptions.flipVertically = flipVertically
        return this
    }


    fun setCropMenuCropButtonTitle(title: CharSequence?): CropImageContractOptions {
        cropImageOptions.cropMenuCropButtonTitle = title
        return this
    }


    fun setCropMenuCropButtonIcon(@DrawableRes drawableResource: Int): CropImageContractOptions {
        cropImageOptions.cropMenuCropButtonIcon = drawableResource
        return this
    }
}

fun CropOptions(
    uri: Uri? = null,
    builder: CropImageContractOptions.() -> (Unit) = {}
): CropImageContractOptions {
    val options = CropImageContractOptions(uri, CropImageOptions())
    options.run(builder)
    return options
}
