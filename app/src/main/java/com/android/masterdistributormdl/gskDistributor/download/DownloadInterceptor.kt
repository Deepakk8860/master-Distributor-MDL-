package com.android.masterdistributormdl.gskDistributor.download
import com.android.masterdistributormdl.BuildConfig
import com.android.masterdistributormdl.gskDistributor.network.api_key
import com.android.masterdistributormdl.gskDistributor.network.app_ver
import com.android.masterdistributormdl.gskDistributor.network.printRequest
import com.android.masterdistributormdl.gskDistributor.utils.DEVICE_ID
import com.android.masterdistributormdl.gskDistributor.utils.DEVICE_NAME
import com.android.masterdistributormdl.gskDistributor.utils.latitude
import com.android.masterdistributormdl.gskDistributor.utils.longitude
import com.android.masterdistributormdl.gskDistributor.utils.session_id
import com.android.masterdistributormdl.utils.SharedPreference
import okhttp3.Interceptor
import okhttp3.Response

class DownloadInterceptor(private val listener: DownloadListener) : Interceptor {
    val sharedPreference = SharedPreference()
    override fun intercept(chain: Interceptor.Chain): Response {
        var request = chain.request()
        val method = request.method
        if (method.equals("POST", true)) {
            val header = request.newBuilder().apply {
                val sessionId = sharedPreference.getString(session_id)
                var latitude = sharedPreference.getString(latitude)
                var longitude = sharedPreference.getString(longitude)
                if (latitude.isNullOrEmpty()){
                    latitude="0"
                    longitude="0"
                }
                if (!sessionId.isNullOrEmpty()) header("sessionid", sessionId)
                header("appkey", api_key)
                header("appver", app_ver.toString())
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
        }
        if (BuildConfig.DEBUG) {
            printRequest(request)
        }
        val response = chain.proceed(request)
        if (response.code != 200) {
            throw Exception("FIle not found")
        }
        return response.newBuilder().body(DownloadResponse(response.body, listener)).build()
    }
}