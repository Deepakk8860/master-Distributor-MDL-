package com.android.masterdistributormdl.gskDistributor.network

import android.content.Intent
import android.os.Handler
import android.os.Looper
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager
import com.android.masterdistributormdl.BuildConfig
import com.android.masterdistributormdl.gskDistributor.utils.DEVICE_ID
import com.android.masterdistributormdl.gskDistributor.utils.DEVICE_NAME
import com.android.masterdistributormdl.gskDistributor.utils.TAG
import com.android.masterdistributormdl.gskDistributor.utils.latitude
import com.android.masterdistributormdl.gskDistributor.utils.longitude
import com.android.masterdistributormdl.gskDistributor.utils.session_id
import com.android.masterdistributormdl.gskDistributor.view.AUTH_RECEIVER
import com.android.masterdistributormdl.gskDistributor.view.LOGOUT_RECEIVER
import com.android.masterdistributormdl.gskDistributor.view.MaintenanceMsgDistributorActivity
import com.android.masterdistributormdl.main.MaintenanceMsgActivity
import com.android.masterdistributormdl.utils.SharedPreference
import com.android.masterdistributormdl.utils.application
import okhttp3.Interceptor
import okhttp3.Request
import okhttp3.Response
import okhttp3.ResponseBody.Companion.toResponseBody
import okio.Buffer
import org.json.JSONObject

//const val api_key = "24e9acacee84be2a54cee79fefdaf94e"
const val api_key = "00f538ca7e1924095c35684a6d2b27d5"
const val app_ver = "2.0"

class AppInterceptor : Interceptor {
    val sharedPreference = SharedPreference()
    private var maintenanceShown = false

    override fun intercept(chain: Interceptor.Chain): Response {
        val sessionId = sharedPreference.getString(session_id)
        var latitude = sharedPreference.getString(latitude)
        var longitude = sharedPreference.getString(longitude)
        if (latitude.isNullOrEmpty()){
            latitude="0"
            longitude="0"
        }
        var request = chain.request()
        val header = request.newBuilder().apply {
            if (!sessionId.isNullOrEmpty()) header("sessionid", sessionId)
            header("appkey", api_key)
            header("appver", app_ver)
            header("os", "an")
            header("latitude", latitude.toString())
            header("longitude", longitude.toString())
            header("devicename", DEVICE_NAME)
            header("deviceid", DEVICE_ID)
            header("logintype", "Dist")
            header("codever", BuildConfig.VERSION_NAME)
            header("appcode", BuildConfig.VERSION_CODE.toString())
        }
        request = header.build()
        if (BuildConfig.DEBUG) {
            printRequest(request)
        }
        val response = chain.proceed(request)
        val responseStrValue = response.peekBody(Long.MAX_VALUE).string()

        var alreadyRedirected = sharedPreference.getBoolean("MAINTENANCE_SHOWN")
        try {
            val json = JSONObject(responseStrValue)
            var status = json.optInt("status", -1)
            // ✅ Maintenance Check: If status == 100
//            sharedPreference.putBoolean("MAINTENANCE_SHOWN", false)
            if (status == 100 && !alreadyRedirected) {
                sharedPreference.putBoolean("MAINTENANCE_SHOWN", true)
                Handler(Looper.getMainLooper()).post {
                    val intent = Intent(application, MaintenanceMsgDistributorActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    application.startActivity(intent)

                }
            }else if (status==101){
                Handler(Looper.getMainLooper()).post {
                    val intent = Intent(LOGOUT_RECEIVER)
                    LocalBroadcastManager.getInstance(application).sendBroadcast(intent);
                }
            }

        } catch (e: Exception) {
            e.printStackTrace()
        }
        if (response.code == 401) {
            val intent = Intent(AUTH_RECEIVER)
            intent.putExtra("status", "auth")
            LocalBroadcastManager.getInstance(application).sendBroadcast(intent);
        }
        if (BuildConfig.DEBUG) {
            val responseStr = getResponse(response)
            return response.newBuilder()
                .body(responseStr?.toResponseBody((response.body)?.contentType())).build()
        } else {
            return response
        }


    }


}
fun printRequest(request: Request?) {
    val builder = StringBuilder()
    builder.append("Url-->" + request?.url + "\n")
    builder.append("Method-->" + request?.method + "\n")
    builder.append("Headers-->\n" + request?.headers + "\n")
    builder.append(bodyToString(request))
    Log.d(TAG, "---\nApiLogRequest---------------------\n" + builder.toString())
}

fun getResponse(response: Response): String? {
    val body = response.body?.string()
    val builder = StringBuilder()
    builder.append("-->Url " + response.code + " " + response.request.url + "\n")
    //  builder.append("-->Headers\n" + response.headers + "\n")
    builder.append(body)
    Log.d(TAG, "--\nApiLogResponse---------------------\n" + builder.toString())
    return body
}

fun bodyToString(request: Request?): String? {
    return try {
        val copy = request?.newBuilder()?.build()
        val buffer = Buffer()
        copy?.body!!.writeTo(buffer)
        buffer.readUtf8()
    } catch (e: Exception) {
        "No body"
    }
}