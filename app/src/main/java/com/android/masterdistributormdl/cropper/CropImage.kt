package com.android.masterdistributormdl.cropper

import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Paint
import android.graphics.PorterDuff
import android.graphics.PorterDuffXfermode
import android.graphics.Rect
import android.graphics.RectF
import android.net.Uri
import android.os.Environment
import android.os.Parcel
import android.os.Parcelable
import android.provider.MediaStore
import androidx.core.content.FileProvider
import com.android.masterdistributormdl.cropper.CropImageView.CropResult
import com.android.masterdistributormdl.cropper.CommonVersionCheck.isAtLeastQ29
import java.io.File


@Suppress("unused", "MemberVisibilityCanBePrivate")
object CropImage {

    const val CROP_IMAGE_EXTRA_SOURCE = "CROP_IMAGE_EXTRA_SOURCE"

    const val CROP_IMAGE_EXTRA_OPTIONS = "CROP_IMAGE_EXTRA_OPTIONS"

    const val CROP_IMAGE_EXTRA_BUNDLE = "CROP_IMAGE_EXTRA_BUNDLE"

    const val CROP_IMAGE_EXTRA_RESULT = "CROP_IMAGE_EXTRA_RESULT"


    const val PICK_IMAGE_CHOOSER_REQUEST_CODE = 200


    const val PICK_IMAGE_PERMISSIONS_REQUEST_CODE = 201

    const val CAMERA_CAPTURE_PERMISSIONS_REQUEST_CODE = 2011


    const val CROP_IMAGE_ACTIVITY_REQUEST_CODE = 203

    const val CROP_IMAGE_ACTIVITY_RESULT_ERROR_CODE = 204


    fun toOvalBitmap(bitmap: Bitmap): Bitmap {
        val width = bitmap.width
        val height = bitmap.height
        val output = Bitmap.createBitmap(width, height, Bitmap.Config.ARGB_8888)
        val canvas = Canvas(output)
        val color = -0xbdbdbe
        val paint = Paint()
        paint.isAntiAlias = true
        canvas.drawARGB(0, 0, 0, 0)
        paint.color = color
        val rect = RectF(0f, 0f, width.toFloat(), height.toFloat())
        canvas.drawOval(rect, paint)
        paint.xfermode = PorterDuffXfermode(PorterDuff.Mode.SRC_IN)
        canvas.drawBitmap(bitmap, 0f, 0f, paint)
        bitmap.recycle()
        return output
    }


    fun getCaptureImageOutputUriContent(context: Context): Uri {
        val outputFileUri: Uri
        val getImage: File?

        if (isAtLeastQ29()) {
            getImage = context.getExternalFilesDir(Environment.DIRECTORY_PICTURES)
            outputFileUri = try {
                FileProvider.getUriForFile(
                    context,
                    context.packageName + CommonValues.authority,
                    File(getImage!!.path, "pickImageResult.jpeg")
                )
            } catch (e: Exception) {
                Uri.fromFile(File(getImage!!.path, "pickImageResult.jpeg"))
            }
        } else {
            getImage = context.externalCacheDir
            outputFileUri = Uri.fromFile(File(getImage!!.path, "pickImageResult.jpeg"))
        }
        return outputFileUri
    }


    fun getCaptureImageOutputUriFilePath(context: Context, uniqueName: Boolean = true): String =
        getFilePathFromUri(context, getCaptureImageOutputUriContent(context), uniqueName)


    @JvmStatic
    fun getPickImageResultUriContent(context: Context, data: Intent?): Uri {
        var isCamera = true
        val uri = data?.data
        if (uri != null) {
            val action = data.action
            isCamera = action != null && action == MediaStore.ACTION_IMAGE_CAPTURE
        }
        return if (isCamera || uri == null) getCaptureImageOutputUriContent(context)
        else uri
    }

    @JvmStatic
    fun getPickImageResultUriFilePath(
        context: Context,
        data: Intent?,
        uniqueName: Boolean = true
    ): String =
        getFilePathFromUri(context, getPickImageResultUriContent(context, data), uniqueName)

    open class ActivityResult : CropResult, Parcelable {

        constructor(
            originalUri: Uri?,
            uriContent: Uri?,
            error: Exception?,
            cropPoints: FloatArray?,
            cropRect: Rect?,
            rotation: Int,
            wholeImageRect: Rect?,
            sampleSize: Int
        ) : super(
            originalBitmap = null,
            originalUri = originalUri,
            bitmap = null,
            uriContent = uriContent,
            error = error,
            cropPoints = cropPoints!!,
            cropRect = cropRect,
            wholeImageRect = wholeImageRect,
            rotation = rotation,
            sampleSize = sampleSize
        )

        protected constructor(`in`: Parcel) : super(
            originalBitmap = null,
            originalUri = `in`.readParcelable<Parcelable>(Uri::class.java.classLoader) as Uri?,
            bitmap = null,
            uriContent = `in`.readParcelable<Parcelable>(Uri::class.java.classLoader) as Uri?,
            error = `in`.readSerializable() as Exception?,
            cropPoints = `in`.createFloatArray()!!,
            cropRect = `in`.readParcelable<Parcelable>(Rect::class.java.classLoader) as Rect?,
            wholeImageRect = `in`.readParcelable<Parcelable>(Rect::class.java.classLoader) as Rect?,
            rotation = `in`.readInt(),
            sampleSize = `in`.readInt()
        )

        override fun writeToParcel(dest: Parcel, flags: Int) {
            dest.writeParcelable(originalUri, flags)
            dest.writeParcelable(uriContent, flags)
            dest.writeSerializable(error)
            dest.writeFloatArray(cropPoints)
            dest.writeParcelable(cropRect, flags)
            dest.writeParcelable(wholeImageRect, flags)
            dest.writeInt(rotation)
            dest.writeInt(sampleSize)
        }

        override fun describeContents(): Int = 0

        companion object {

            @JvmField
            val CREATOR: Parcelable.Creator<ActivityResult?> =
                object : Parcelable.Creator<ActivityResult?> {
                    override fun createFromParcel(`in`: Parcel): ActivityResult =
                        ActivityResult(`in`)

                    override fun newArray(size: Int): Array<ActivityResult?> = arrayOfNulls(size)
                }
        }
    }

    object CancelledResult : CropImageView.CropResult(
        originalBitmap = null,
        originalUri = null,
        bitmap = null,
        uriContent = null,
        error = Exception("cropping has been cancelled by the user"),
        cropPoints = floatArrayOf(),
        cropRect = null,
        wholeImageRect = null,
        rotation = 0,
        sampleSize = 0
    )
}
