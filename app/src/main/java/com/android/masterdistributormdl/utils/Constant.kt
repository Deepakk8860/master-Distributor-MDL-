package com.android.masterdistributormdl.utils

import android.annotation.SuppressLint
import android.app.Activity
import android.app.DatePickerDialog
import android.app.Dialog
import android.app.TimePickerDialog
import android.content.Context
import android.os.Build
import android.provider.Settings
import android.text.Html
import android.text.Spanned
import android.util.Base64
import android.util.Base64OutputStream
import android.util.TypedValue
import android.view.View
import android.view.Window
import android.view.inputmethod.InputMethodManager
import android.widget.EditText
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.annotation.RequiresApi
import androidx.core.widget.NestedScrollView
import androidx.core.widget.addTextChangedListener
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.load
import coil.request.ImageRequest
import coil.transform.CircleCropTransformation

import com.android.masterdistributormdl.R
import com.bumptech.glide.Glide
import com.google.gson.Gson
import okio.buffer
import okio.source
import java.io.ByteArrayOutputStream
import java.io.File
import java.io.IOException
import java.nio.charset.Charset
import java.text.DecimalFormat
import java.text.SimpleDateFormat
import java.time.LocalDate
import java.time.LocalDateTime
import java.time.format.DateTimeFormatter
import java.util.Calendar
import java.util.Locale
import java.util.regex.Pattern

//stage
//const val BASE_URL = "https://diststage.gstnregistration.com/"
//new live
//const val BASE_URL = "https://b2b.gstnregistration.com/"
//live
//const val BASE_URL = "https://b2b.gstsuvidhakendra.org.in/"
const val DASHBOARD_URL = "https://institutionaltradingsystem.com/dashboard/"
const val NOTIFICATION_URL = "https://institutionaltradingsystem.com/notifications"
val application = MyApplication.getMyApplication()
val sharedPreference = SharedPreference()
const val TAG = "ITS"
const val session_id = "session_id"
const val user_id = "user_id"
const val isLogin = "isLogin"
const val currency = "₹"
const val user_data = "user_data"
const val ROLE = "role"
const val WebUrl = "WebUrl"
const val isEnableBiometrics = "isEnableBiometrics"
const val latitude = "latitude"
const val longitude = "longitude"
const val OFFERS = "OFFERS"
const val MESSAGE = "MESSAGE"
const val HISTORY = "HISTORY"
const val SERVICES = "SERVICES"
const val STATUS_COLOR1 = "#EFEFEF"
const val STATUS_COLOR2 = "#25233C"
const val STATUS_WHITE = "#FFFFFFFF"
var isDateAndTimeUpdated=false
var followUpDate=""
const val USER_CONTACT = "USER_CONTACT"
const val SUBSCRIPTION_PIC = "subscription_pic"
val DEVICE_NAME = Build.DEVICE + " " + Build.MODEL + " " + Build.MANUFACTURER
val DEVICE_ID = getDeviceId()
var gson = Gson()

@SuppressLint("HardwareIds")
fun getDeviceId(): String {
    return Settings.Secure.getString(application.contentResolver, Settings.Secure.ANDROID_ID)
}


fun loadImage(imageView: ImageView, imageUrl: String?) {
    if (imageUrl.isNullOrEmpty()) {
        imageView.setImageResource(R.drawable.loader)
    } else {
        Glide.with(imageView.context)
            .load(imageUrl)
            .into(imageView)
    }
}

fun loadImageWithCoil(imageView: ImageView, imageUrl: String?) {
    imageView.load(imageUrl) {
        crossfade(true)
        placeholder(R.drawable.loader) // Optional: show while loading
        error(R.drawable.loader) // Optional: show if failed
        transformations(CircleCropTransformation())
    }
}

fun openDatePicker(requireContext: Context, txtDate: TextView, txtTime: TextView) {
    val calendar = Calendar.getInstance()
    val year = calendar.get(Calendar.YEAR)
    val month = calendar.get(Calendar.MONTH)
    val day = calendar.get(Calendar.DAY_OF_MONTH)

    val datePickerDialog =
        DatePickerDialog(requireContext, { _, selectedYear, selectedMonth, selectedDay ->
            // Format date: 12 May 2025
            val selectedCalendar = Calendar.getInstance().apply {
                set(Calendar.YEAR, selectedYear)
                set(Calendar.MONTH, selectedMonth)
                set(Calendar.DAY_OF_MONTH, selectedDay)
            }

            val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
            val formattedDate = dateFormat.format(selectedCalendar.time)
            txtDate.text = formattedDate

            // Open time picker after date selection
            openTimePicker(requireContext, txtTime,formattedDate)
        }, year, month, day)

    datePickerDialog.show()
}

@RequiresApi(Build.VERSION_CODES.O)
fun getFormattedDate(option: String): String {
    val now = LocalDateTime.now()
    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy, hh:mm a")
    // Example → 25 Sep 2025, 08:30 PM

    val calculatedDateTime = when (option) {
        "Today" -> now
        "Tomorrow" -> now.plusDays(1)
        "3 days from now" -> now.plusDays(3)
        "1 week from now" -> now.plusWeeks(1)
        "1 month from now" -> now.plusMonths(1)
        "Someday", "Select custom date and time", "No Follow Up" -> return ""
        else -> now
    }

    return calculatedDateTime.format(formatter)
}



//fun getFormattedDate(option: String): String {
//    val today = LocalDate.now()
//    val formatter = DateTimeFormatter.ofPattern("dd MMM yyyy")
//
//    val calculatedDate = when (option) {
//        "Today" -> today
//        "Tomorrow" -> today.plusDays(1)
//        "3 days from now" -> today.plusDays(3)
//        "1 week from now" -> today.plusWeeks(1)
//        "1 month from now" -> today.plusMonths(1)
//        "Someday", "Select custom date and time","No Follow Up" -> return ""
//        else -> today
//    }
//
//    return calculatedDate.format(formatter)
//}

@RequiresApi(Build.VERSION_CODES.O)
fun getFormattedDateTime(dateTimeStr: String): String {
    val inputFormatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")
    val outputFormatter = DateTimeFormatter.ofPattern("dd MMM yyyy hh:mm a")

    return try {
        val dateTime = LocalDateTime.parse(dateTimeStr, inputFormatter)
        dateTime.format(outputFormatter)
    } catch (e: Exception) {
        "" // If the input is invalid
    }
}

fun imageToBase64(imageFile: File): String {
    return "data:image/jpeg;base64," + fileToBase64(imageFile)
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





fun openTimePicker(requireContext: Context, txtTime: TextView, formattedDate: String) {
    val calendar = Calendar.getInstance()
    val hour = calendar.get(Calendar.HOUR_OF_DAY)
    val minute = calendar.get(Calendar.MINUTE)

    val timePickerDialog = TimePickerDialog(requireContext, { _, selectedHour, selectedMinute ->
        // Format time
        val amPm = if (selectedHour >= 12) "PM" else "AM"
        val hourFormatted =
            if (selectedHour > 12) selectedHour - 12 else if (selectedHour == 0) 12 else selectedHour
        val timeStr = "${hourFormatted.toString().padStart(2, '0')}:${
            selectedMinute.toString().padStart(2, '0')
        } $amPm"
        isDateAndTimeUpdated=true
        // Set time in txtTime
        txtTime.visibility=View.VISIBLE
        txtTime.text = timeStr
        followUpDate="$formattedDate $timeStr"


    }, hour, minute, false)

    timePickerDialog.show()
}

val formatter = DecimalFormat("##,##,##0.##")
fun getPriceFormat(amount: Double): String {
    formatter.format(amount)
    return currency + formatter.format(amount)
}

fun showToastShort(msg: String) {
    Toast.makeText(application, msg, Toast.LENGTH_SHORT).show()
}

fun String.capitalizeWords(): String {
    return this.trim().split("\\s+".toRegex())
        .joinToString(" ") { it.lowercase().replaceFirstChar { ch -> ch.uppercase() } }
}


fun scrollToPosition1(nested: NestedScrollView, view: View) {
    nested.scrollTo(1, view.bottom)
    view.requestFocus()
}

fun scrollToPosition(nested: NestedScrollView, nameEdt: EditText) {
    nested.scrollTo(1, nameEdt.bottom)
    // showToastShort("Please Enter Valid ${nameEdt.hint}")
    nameEdt.setError2()
    //  nameEdt.setError(msg)
}

fun checkMobileNo(mobile: String): Boolean {
    val regex = "^[6-9]{1}[0-9]{9}$"
    val pattern = Pattern.compile(regex)
    val matcher = pattern.matcher(mobile)
    return matcher.matches()
}


fun clearAllEditTextFocus(vararg editTexts: EditText) {

    for (editText in editTexts) {
        editText.setBackgroundResource(R.drawable.edt_normal)
        setEditText(editText)
    }
}

fun clearAllEditTextFocusError(vararg textViews: TextView) {
    for (editText in textViews) {
        editText.visibility = View.GONE
//        setEditTextError(editText)
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
    params.width = width - (width * 13 / 100)
    layout.layoutParams = params
    return dialog
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
                TypedValue.COMPLEX_UNIT_PX, editText.resources.getDimension(R.dimen._13sp)
            )
            editText.setBackgroundResource(R.drawable.edt_selected)
        }
    }
}

fun showToast(msg: String) {
    Toast.makeText(application, msg, Toast.LENGTH_LONG).show()
}

fun hideSoftKeyBoard(context: Context) {
    if (context is Activity) {
        val view = context.currentFocus
        hideSoftKeyBoard(view)
    }
}

fun hideSoftKeyBoard(view: View?) {
    view?.postDelayed({
        val imm = view.context.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
    }, 100)
    /* try {
         val imm =
             view?.context?.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
         imm.hideSoftInputFromWindow(view.windowToken, InputMethodManager.HIDE_NOT_ALWAYS)
     } catch (e: Exception) {
         e.printStackTrace()
     }*/
}

@SuppressLint("DiscouragedApi")
fun ImageView.setImageApp(icon: String) {
    val res = resources.getIdentifier(icon, "drawable", this.context.packageName)
    this.setImageResource(res)
}

fun getStringAssets(fileName: String): String? {
//    Log.d(TAG + "AssetsPath", fileName)
    var jsonSting: String? = null
    try {
        val source = application.assets.open(fileName).source().buffer()
        jsonSting = source.readByteString().string(Charset.forName("utf-8"))
    } catch (e: IOException) {
        e.printStackTrace()
    }
    return jsonSting
}

fun checkNullorEmpty(editText: EditText): Boolean {
    var check = false
    if (editText.text.toString().isNullOrEmpty()) {
        editText.setError2()
        check = true
    }
    return check
}

fun EditText.setError2() {
    setBackgroundResource(R.drawable.edt_error)
    requestFocus()
}

fun loadGif(imageView: ImageView, @DrawableRes gifResourceId: Int) {
    Glide.with(imageView.context)
        .asGif()
        .load(gifResourceId)
        .into(imageView)
}

fun ImageView.loadSvg(context: Context, url: String) {
    val imageLoader = ImageLoader.Builder(context)
        .components {
            add(SvgDecoder.Factory())
        }
        .build()

    val rotationAngle = when {
        url.contains("assign_w.svg", ignoreCase = true)  || url.contains("assign.svg", ignoreCase = true)-> 45f // ↘️ arrow
        // add more cases here if needed
        else -> 0f
    }

    val request = ImageRequest.Builder(context)
        .data(url)
        .crossfade(true)
        .target { drawable ->
            this.setImageDrawable(drawable)
            this.rotation = rotationAngle
        }
        .build()

    imageLoader.enqueue(request)
}


fun getHtmlSpanned(htmlText: String?): Spanned {
    return if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
        Html.fromHtml(htmlText, Html.FROM_HTML_MODE_LEGACY)
    } else {
        Html.fromHtml(htmlText)
    }
}