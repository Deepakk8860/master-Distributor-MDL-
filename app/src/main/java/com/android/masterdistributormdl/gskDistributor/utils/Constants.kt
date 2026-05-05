package com.android.masterdistributormdl.gskDistributor.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.Dialog
import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.media.MediaMetadataRetriever
import android.net.ConnectivityManager
import android.net.NetworkCapabilities
import android.os.AsyncTask

import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.text.Html
import android.text.InputType
import android.text.Spanned
import android.util.*
import android.view.View
import android.view.ViewGroup
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.*
import androidx.core.content.res.ResourcesCompat
import androidx.core.view.marginBottom
import androidx.core.view.marginLeft
import androidx.core.view.marginRight
import androidx.core.view.marginTop
import androidx.core.widget.addTextChangedListener
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.utils.MyApplication
import com.google.android.material.snackbar.Snackbar
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

import com.google.gson.Gson
import com.google.gson.JsonObject
import com.google.mlkit.vision.common.InputImage
import com.google.mlkit.vision.text.TextRecognition
import com.google.mlkit.vision.text.latin.TextRecognizerOptions

import com.gsk.distributor.model.PANData
import com.squareup.picasso.Picasso
import com.squareup.picasso.Target

import kotlinx.coroutines.*
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.MultipartBody
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.asRequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.buffer
import okio.source
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.FileInputStream
import java.io.FileOutputStream
import java.io.IOException
import java.nio.charset.Charset
import java.text.DateFormat
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.util.Locale
import java.util.UUID
import java.util.concurrent.TimeUnit
import java.util.regex.Pattern

//stage
const val BASE_URL = "https://diststage.gstnregistration.com/"

//new live
//const val BASE_URL = "https://b2b.gstnregistration.com/"
//
const val URL_PATH = "masterdistributor"
const val URL_PATH_START = "appapi"

//live
//const val BASE_URL = "https://b2b.gstsuvidhakendra.org.in/"



//const val BASE_URL = "https://b2b.gstsuvidhakendra.org.in/"
//const val URL_PATH = "masterfranchise"


//stage url with url path
//const val BASE_URL = "https://staging.paynuke.com"
//const val BASE_URL = "https://diststage.gstsuvidhakendra .org.in"
//const val URL_PATH = "masterfranchise"

//const val URL_PATH = "gskuser"

var is_sales = false
val APP_TYPE = "MD"   // GSK or MF01

// Replace with your Conversion ID and Label

/*-----------------------------------------------------*/
const val SHARE_URL = "https://paynuke.in/app-refer?request="
const val OFFERS = "OFFERS"
const val m_pin = "m_pin"
var is_pin_open = false
const val device_token = "device_token"
const val TAG = "PayNuke-Log"
const val session_id = "session_id"
const val user_id = "user_id"
val application = MyApplication.getMyApplication()
const val file_ext = ".csv"
const val file_ext_2 = ".xlsx"
const val file_ext_csv = ".csv"
const val user_data = "user_data"
const val currency = "₹"
const val latitude = "latitude"
const val longitude = "longitude"
const val is_login = "is_login"
val gson = Gson()
val DEVICE_NAME = Build.DEVICE + " " + Build.MODEL + " " + Build.MANUFACTURER
val DEVICE_ID = getDeviceId()
const val STATUS_COLOR1 = "#FFF9F3"
const val STATUS_COLOR2 = "#132D5F"
const val STATUS_WHITE = "#FFFFFFFF"
const val NOTIFICATIONS_RECEIVER = "NOTIFICATIONS_RECEIVER"
const val dash_count = "dash_count2"

@SuppressLint("HardwareIds")
fun getDeviceId(): String {
    val deviceId =
        Settings.Secure.getString(application.contentResolver, Settings.Secure.ANDROID_ID)
    return deviceId;
}


fun imageToBase64(imageFile: File): String {
    return "data:image/jpeg;base64," + fileToBase64(imageFile)
}

fun stringToBase64(str: String): String {
    val encodeStr = Base64.encode(str.toByteArray(), Base64.DEFAULT).toString(Charsets.UTF_8)
    return encodeStr
}

fun setEditText(editText: EditText) {
    editText.addTextChangedListener {
        if (it.isNullOrEmpty()) {
            editText.setTextSize(
                TypedValue.COMPLEX_UNIT_PX, editText.resources.getDimension(R.dimen._13sp)
            )
            editText.setBackgroundResource(R.drawable.edt_normal)
        } else {
            editText.setTextSize(
                TypedValue.COMPLEX_UNIT_PX,
                editText.resources.getDimension(R.dimen._13sp)
            )
            editText.setBackgroundResource(R.drawable.edt_selected)
        }
    }
}

fun intentPass(context: Context, activity: Activity) {
    val intentData = Intent(context, activity::class.java)
    context.startActivity(intentData)
}

 fun formatPrice(price: Double?): String {
    return if (price == null || price == 0.00) "-" else getPriceFormat(price)
}

fun videoToBase64(filePath: String): String {
    return "data:video/mp4;base64," + videoFileBase64(filePath)
}

//video to base64
//fun videoFileBase64(filePath: String): String? {
//    return try {
//        // Create a File object for the video
//        val videoFile = File(filePath)
//
//        // Read the video file into a byte array
//        val fileInputStream = FileInputStream(videoFile)
//        val byteArray = fileInputStream.readBytes()
//        fileInputStream.close()
//
//        // Encode the byte array to Base64
//        Base64.encodeToString(byteArray, Base64.DEFAULT)
//    } catch (e: Exception) {
//        e.printStackTrace()
//        null
//    }
//}

fun videoFileBase64(filePath: String): String? {
    val bufferSize = 1024 * 4 // Process in chunks of 4 KB
    return try {
        val videoFile = File(filePath)
        val fileInputStream = FileInputStream(videoFile)
        val outputStream = ByteArrayOutputStream()
        val buffer = ByteArray(bufferSize)

        val base64OutputStream = Base64OutputStream(outputStream, Base64.DEFAULT)

        var bytesRead: Int
        while (fileInputStream.read(buffer).also { bytesRead = it } != -1) {
            base64OutputStream.write(buffer, 0, bytesRead)
        }

        fileInputStream.close()
        base64OutputStream.close()

        // Convert the Base64 encoded bytes to a string
        outputStream.toString()
    } catch (e: Exception) {
        e.printStackTrace()
        null
    }
}

fun intentPassValue(context: Context, activity: Activity, bundle: Bundle) {
    val intentData = Intent(context, activity::class.java)
    intentData.putExtras(bundle)
    context.startActivity(intentData)
//    context.startActivity(intentData,
//        ActivityOptions.makeSceneTransitionAnimation(context as Activity).toBundle())
}

// Function to compress a video file
fun compressVideo(inputPath: String, outputPath: String, targetBitrate: Int) {
    // Placeholder for actual compression logic
    // For demonstration, we copy the original video to the outputPath as is
    val inputFile = File(inputPath)
    val outputFile = File(outputPath)

    FileInputStream(inputFile).use { input ->
        FileOutputStream(outputFile).use { output ->
            input.copyTo(output)
        }
    }
}

// Function to dynamically generate a path for the compressed video
fun getCompressedVideoPath(context: Context, originalPath: String): String {
    // Create a directory for compressed videos
    val compressedVideoDir = File(context.cacheDir, "compressed_videos")
    if (!compressedVideoDir.exists()) {
        compressedVideoDir.mkdirs() // Create the directory if it doesn't exist
    }

    // Generate a new file name based on the original file name
    val originalFile = File(originalPath)
    val originalFileName = originalFile.nameWithoutExtension
    val newFileName = "$originalFileName-compressed.mp4"

    // Return the full path for the compressed video
    return File(compressedVideoDir, newFileName).absolutePath
}


// Main function to compress a video and convert it to Base64
fun compressAndConvertToBase64(
    context: Context,
    dynamicVideoPath: String,
    targetBitrate: Int
): String? {
    return try {
        // Generate the path for the compressed video
        val compressedVideoPath = getCompressedVideoPath(context, dynamicVideoPath)

        // Compress the video
        compressVideo(dynamicVideoPath, compressedVideoPath, targetBitrate)

        // Convert the compressed video to Base64
        videoToBase64(compressedVideoPath)
    } catch (e: OutOfMemoryError) {
        // Handle OutOfMemoryError
        e.printStackTrace()
        null
    } catch (e: Exception) {
        // Handle other exceptions
        e.printStackTrace()
        Toast.makeText(context, "An error occurred while processing the video.", Toast.LENGTH_LONG)
            .show()
        null
    }
}


fun String.capitalizeFirstLetter(): String {
    if (isEmpty()) {
        return this
    }
    return substring(0, 1).uppercase(Locale.getDefault()) + substring(1)
}

fun bitmapToBase64(bitmap: Bitmap): String {
    val baos = ByteArrayOutputStream()
    bitmap.compress(Bitmap.CompressFormat.PNG, 100, baos)
    val b = baos.toByteArray()
    return "data:image/jpeg;base64," + Base64.encodeToString(b, Base64.DEFAULT)
}

fun fileToBase64(file: File): String {
    return ByteArrayOutputStream().use { outputStream ->
        Base64OutputStream(outputStream, Base64.DEFAULT).use { base64 ->
            file.inputStream().use { inputStream ->
                inputStream.copyTo(base64)
            }
        }
        return@use outputStream.toString()
    }
}

fun setClearError(textInputLayout: TextInputLayout, editText: TextInputEditText) {
    editText.addTextChangedListener {
        textInputLayout.error = null
        if (it.isNullOrEmpty()) {
            editText.setTextSize(
                TypedValue.COMPLEX_UNIT_PX, editText.resources.getDimension(R.dimen._12sp)
            )
        } else {
            editText.setTextSize(
                TypedValue.COMPLEX_UNIT_PX, editText.resources.getDimension(R.dimen._14sp)
            )
        }
    }
}

@SuppressLint("DiscouragedApi")
fun ImageView.setImageApp(icon: String) {
    val res = resources.getIdentifier(icon, "drawable", this.context.packageName)
    this.setImageResource(res)
}

fun TextView.setTextColor2(color: Int) {
    setTextColor(resources.getColor(color))
}

fun TextView.setTextColor2(color: String) {
    setTextColor(Color.parseColor(color))
}

fun getTextImage(context: Context, bitmap: Bitmap, result: (String) -> Unit) {
    val recognizer = TextRecognition.getClient(TextRecognizerOptions.DEFAULT_OPTIONS)
    val image = InputImage.fromBitmap(bitmap, 0)
    Loading.show(context)
    recognizer.process(image).addOnSuccessListener {
        Loading.dismiss()
        result(it.text)
    }.addOnFailureListener { e ->
        Loading.dismiss()
        AlertError.show(
            context, "Something went wrong\nError - " + e.localizedMessage
        ) {}
    }
}

fun getPanNoFromText(string: String, result: (PANData) -> Unit) {
    val delimeter = "-"
    val str = string.replace("[^/A-Z0-9]".toRegex(), delimeter)
    val strings = str.split(delimeter)
    val item = PANData("", "")
    strings.forEach {
        if (it.length == 10 && checkPANNo(it)) {
            item.pan_no = it
        } else if (it.length == 10 && checkPANDate(it)) {
            item.dob = it
        }
    }
    result.invoke(item)
}

fun getAadhaarNoFromText(string: String, result: (String) -> Unit) {
    Log.d(TAG, "ScanText1 :\n$string")
    val str = string.replace("[^\n0-9]".toRegex(), "")
    Log.d(TAG, "ScanText2 : " + str)
    val strings = str.split("\n")
    strings.forEach {
        if (it.length == 12 && checkAadharNo(it)) {
            result.invoke(it)
        }
    }
}

fun checkPincode(pincode: String): Boolean {
    val regex = "^[1-9]{1}[0-9]{5}$"
    val pattern = Pattern.compile(regex)
    val matcher = pattern.matcher(pincode)
    return matcher.matches()
}

fun checkAadharNo(aadhar: String): Boolean {
    val regex = "^[2-9]{1}[0-9]{3}[0-9]{4}[0-9]{4}$"
    val pattern = Pattern.compile(regex)
    val matcher = pattern.matcher(aadhar)
    return matcher.matches()
}

fun checkPANNo(panNo: String): Boolean {
    val regex = "[A-Z]{5}[0-9]{4}[A-Z]{1}"
    val pattern = Pattern.compile(regex)
    val matcher = pattern.matcher(panNo)
    return matcher.matches()
}

fun checkPANDate(date: String): Boolean {
    val regex = "[0-9]{1}[0-9]{1}/[0-9]{1}[0-9]{1}/[0-9]{4}"
    val pattern = Pattern.compile(regex)
    val matcher = pattern.matcher(date)
    return matcher.matches()
}

fun TextView.setFontFamily(fontFamily: Int) {
    val typeface = ResourcesCompat.getFont(context, fontFamily)
    setTypeface(typeface)
}

fun getNumberFormat(amount: Double): String {
    return try {
        val formatter = DecimalFormat("##,##,##0.##").format(amount)
        return formatter.format(amount)
    } catch (e: Exception) {
        currency + "0"
    }

}

fun getPriceFormat(amount: Double): String {
    return try {
        val formatter = DecimalFormat("##,##,##0.##").format(amount)
        currency + formatter.format(amount)
    } catch (e: Exception) {
        currency + "0"
    }
}


fun TextView.textSize(dimen: Int) {
    setTextSize(TypedValue.COMPLEX_UNIT_PX, getResources().getDimension(dimen))
}

fun getStringBodyFromJson(value: JsonObject): RequestBody {
    return value.toString().toRequestBody("application/json".toMediaTypeOrNull())
}

fun getStringBody(value: String): RequestBody {
    return value.toRequestBody("text/plain".toMediaTypeOrNull())
}

fun getFIleBody(key: String, filePath: String): MultipartBody.Part? {
    val file = File(filePath)
    return MultipartBody.Part.createFormData(
        key, file.name, file.asRequestBody(("image/jpeg").toMediaTypeOrNull())
    )
}

fun View.setMargins(
    left: Int = this.marginLeft,
    top: Int = this.marginTop,
    right: Int = this.marginRight,
    bottom: Int = this.marginBottom,
) {
    layoutParams = (layoutParams as ViewGroup.MarginLayoutParams).apply {
        setMargins(left, top, right, bottom)
    }
}

fun showHide(it: String, editText: EditText) {
    if (it.equals("Hide")) {
        editText.inputType = InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
    } else {
        editText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
    }
    editText.setSelection(editText.length())
    editText.typeface = ResourcesCompat.getFont(editText.context, R.font.normal)
}

fun setTextColor(textView: TextView, color: Int) {
    textView.setTextColor(textView.context.resources.getColor(color))
}


fun getStringAssets(fileName: String): String? {
    Log.d(TAG + "AssetsPath", fileName)
    var jsonSting: String? = null
    try {
        val source = application.assets.open(fileName).source().buffer()
        jsonSting = source.readByteString().string(Charset.forName("utf-8"))
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return jsonSting
}

fun getString(id: Int): String {
    return application.resources.getString(id)
}


fun hideSoftKeyBoard(context: Context) {
    if (context is Activity) {
        val view = context.currentFocus
        hideSoftKeyBoard(view)
    }
}

fun copyText(context: Context, string: String?) {
    val clipboardManager = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
    val clipData = ClipData.newPlainText("label", string)
    clipboardManager.setPrimaryClip(clipData)
}

fun hideSoftKeyBoard(view: View?) {
    view?.postDelayed({
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }, 100)/* try {
         val imm =
             view?.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
         imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
     } catch (e: Exception) {
         e.printStackTrace()
     }*/
}

fun convertDateTime(dateTime: String): String {
    try {
        val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss")
        val date = dateFormat.parse(dateTime)
        val formatter = SimpleDateFormat("dd-MMM-yyyy hh:mm aa")
        val dateStr = formatter.format(date)
        return dateStr
    } catch (e: Exception) {
        return "N/A"
    }

}

fun getDialog(context: Context, layoutId: Int): Dialog {
    val dialog = Dialog(context)
    dialog.requestWindowFeature(Window.FEATURE_NO_TITLE)
    dialog.window?.setBackgroundDrawableResource(R.color.trans)
    val width = Utils.getScreenWidth(context)
    dialog.setContentView(layoutId)
    val layout = dialog.findViewById<LinearLayout>(R.id.main_layout)
    val params = layout.layoutParams
    params.width = width - (width * 10 / 100)
    layout.layoutParams = params
    return dialog
}

fun isNetworkAvailable(context: Context): Boolean {
    val manager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
        val capabilities = manager.getNetworkCapabilities(manager.activeNetwork)
        if (capabilities == null) return false
        else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR)) return true
        else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) return true
        else if (capabilities.hasTransport(NetworkCapabilities.TRANSPORT_ETHERNET)) return true
    } else {
        val activeNetworkInfo = manager.activeNetworkInfo
        if (activeNetworkInfo != null && activeNetworkInfo.isConnected) {
            return true
        }
    }
    return false
}

fun showToast(msg: String) {
    Toast.makeText(application, msg, Toast.LENGTH_LONG).show()
}

fun showToastShort(msg: String) {
    Toast.makeText(application, msg, Toast.LENGTH_SHORT).show()
}

fun fetchThumbnail(videoUrl: String, imageView: ImageView) {
    AsyncTask.execute {
        try {
            // Initialize MediaMetadataRetriever
            val retriever = MediaMetadataRetriever()
            retriever.setDataSource(videoUrl, HashMap())

            // Get a frame at 1 second (1000000 microseconds)
            val bitmap: Bitmap? =
                retriever.getFrameAtTime(1000000, MediaMetadataRetriever.OPTION_CLOSEST_SYNC)

            retriever.release()

            // Update the ImageView on the main thread
            imageView.post {
                if (bitmap != null) {
                    imageView.setImageBitmap(bitmap)
                } else {
                    imageView.setImageResource(R.drawable.iv_video_icon) // Fallback
                }
            }
        } catch (e: Exception) {
            e.printStackTrace()
            // Handle errors and set a fallback image
            imageView.post {
                imageView.setImageResource(R.drawable.iv_video_icon)
            }
        }
    }
}


fun loadImage(imageView: ImageView, imageUrl: String?, default: Int) {
    if (imageUrl.isNullOrEmpty()) {
        imageView.setImageResource(default)
    } else {
        Picasso.get().load(imageUrl).placeholder(default).error(default).into(imageView)
        imageView.setOnLongClickListener {
            ImageDialog(imageView.context, imageUrl)
            true
        }
    }
}


fun loadImage(imageView: ImageView, imageUrl: String?) {
    if (imageUrl.isNullOrEmpty()) {
        imageView.setImageResource(R.drawable.loader)
    } else {
        Picasso.get().load(imageUrl).into(imageView)

        imageView.setOnLongClickListener {
            ImageDialog(imageView.context, imageUrl)
            true
        }
    }
}

fun checkUpiId(upi: String): Boolean {
    val regex = "[a-zA-Z0-9_]{3,}@[a-zA-Z]{3,}"
    val pattern = Pattern.compile(regex)
    val matcher = pattern.matcher(upi)
    return matcher.matches()
}

fun checkIfsc(upi: String): Boolean {
    val regex = "^[A-Z]{4}0[A-Z0-9]{6}$"
    val pattern = Pattern.compile(regex)
    val matcher = pattern.matcher(upi)
    return matcher.matches()
}

fun bitmapToFile(context: Context, bitmap: Bitmap): File {
    val dir = context.getDir("Images", Context.MODE_PRIVATE)
    val file = File(dir, "${UUID.randomUUID()}.png")
    try {
        val stream = FileOutputStream(file)
        bitmap.compress(Bitmap.CompressFormat.PNG, 100, stream)
        stream.flush()
        stream.close()
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return file
}

fun loadImage(url: String, result: (Bitmap?) -> Unit) {


    Picasso.get().load(url).into(object : Target {

        override fun onBitmapLoaded(bitmap: Bitmap, from: Picasso.LoadedFrom) {
            result.invoke(bitmap)
        }

        override fun onPrepareLoad(placeHolderDrawable: Drawable?) {
            result.invoke(null)
        }

        override fun onBitmapFailed(e: Exception?, errorDrawable: Drawable?) {
            result.invoke(null)
        }

    })
}


fun checkGstinNo(gstin_no: String): Boolean {
    val regex = "^[0-9]{2}[A-Z]{5}[0-9]{4}[A-Z]{1}[1-9A-Z]{1}Z[0-9A-Z]{1}$"
    val pattern = Pattern.compile(regex)
    val matcher = pattern.matcher(gstin_no)
    return matcher.matches()
}


fun isEmailValid(email: String): Boolean {
    return Patterns.EMAIL_ADDRESS.matcher(email).matches()
}

fun checkNumeric(mobile: String): Boolean {
    val regex = "^[0-9]{10}$"
    val pattern = Pattern.compile(regex)
    val matcher = pattern.matcher(mobile)
    return matcher.matches()
}

fun checkMobileNo(mobile: String): Boolean {
    val regex = "^[6-9]{1}[0-9]{9}$"
    val pattern = Pattern.compile(regex)
    val matcher = pattern.matcher(mobile)
    return matcher.matches()
}

fun getHtmlSpanned(htmlText: String?): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(htmlText)
    }
}


fun number2digits(number: String?): String {
    if (number.isNullOrEmpty()) return ""
    val value = String.format("%.2f", number.toDouble())
    return value
}

fun number2digits(number: Double): String {
    val value = String.format("%.2f", number)
    return value
}

fun stringToArrayList(string: String): ArrayList<String> {
    val list = ArrayList<String>()
    for (item in string.trim(' ').split(",")) {
        list.add(item)
    }
    return list
}

fun onAlertSnackbar(view: View, msg: String) {
    try {
        val snackbar = Snackbar.make(view, msg, Snackbar.LENGTH_SHORT)
        val snackView = snackbar.view
        snackView.setBackgroundColor(Color.parseColor("#1562B6"))
        snackbar.show()
    } catch (e: Exception) {
        showToastShort(msg)
    }
}

fun onSnackbar(view: View, msg: String) {
    try {
        val snackbar = Snackbar.make(view, msg, Snackbar.LENGTH_SHORT)
        val snackView = snackbar.view
        snackView.setBackgroundColor(Color.parseColor("#DD1212"))
        snackbar.show()
    } catch (e: Exception) {
        showToastShort(msg)
    }
}


fun setButtonEnabled(view: View) {
    view.isClickable = false
    CoroutineScope(Dispatchers.IO).launch {
        delay(TimeUnit.MILLISECONDS.toMillis(1000))
        withContext(Dispatchers.Main) {
            view.isClickable = true
        }
    }
}

 
