package com.android.masterdistributormdl.gskDistributor.network


import android.util.Log
import com.android.masterdistributormdl.R
import com.google.gson.JsonSyntaxException
import com.google.gson.stream.MalformedJsonException
import com.gsk.distributor.model.ErrorAlert
import com.android.masterdistributormdl.gskDistributor.utils.BASE_URL
import com.android.masterdistributormdl.gskDistributor.utils.TAG
import com.android.masterdistributormdl.gskDistributor.utils.application
import kotlinx.coroutines.TimeoutCancellationException
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import retrofit2.converter.scalars.ScalarsConverterFactory
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.security.cert.CertPathValidatorException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.net.ssl.SSLHandshakeException





fun getClient(): RetrofitUrl {
    val client = getOkHttpClient()
    return Retrofit.Builder()
        .baseUrl(BASE_URL)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(RetrofitUrl::class.java)
}


fun getClient(apiUrl: String): RetrofitUrl {
    val client = getOkHttpClient()
    return Retrofit.Builder()
        .baseUrl(apiUrl)
        .client(client)
        .addConverterFactory(GsonConverterFactory.create())
        .build()
        .create(RetrofitUrl::class.java)
}

fun getClient2(): RetrofitUrl {
    val client = getOkHttpClient() // Assuming you have a method to get OkHttpClient
    return Retrofit.Builder()
        .baseUrl(BASE_URL) // Replace with your actual base URL
        .client(client)
        .addConverterFactory(ScalarsConverterFactory.create()) // Use ScalarsConverterFactory for plain-text responses
        .build()
        .create(RetrofitUrl::class.java)
}

fun getOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
   .addInterceptor(AppInterceptor())
        .cache(null)
        .build()
}

fun errorRetrofit(it: Throwable): ErrorAlert {
    val errorAlert: ErrorAlert
    if (it is TimeoutException || it is TimeoutCancellationException || it is SocketTimeoutException) {
        errorAlert = ErrorAlert(0, "Timeout Error")
    } else if (it is CertPathValidatorException || it is SSLHandshakeException) {
        errorAlert = ErrorAlert(0, "You are not connected to the internet or firewall is blocking you to access the content.\n" +
                "Check your connection settings.")
    } else if (it is NullPointerException) {
        errorAlert = ErrorAlert(0, "Something went wrong, please try again")
    } else if (it is JsonSyntaxException) {
        errorAlert = ErrorAlert(0, "Response Parsing error")
    } else if (it is MalformedJsonException) {
        errorAlert = ErrorAlert(0, "Response Json body error")
    } else if (it is UnknownHostException) {
        errorAlert = ErrorAlert(1, application.getString(R.string.internet_error))
    } else {
        errorAlert = ErrorAlert(0, "Something went wrong")
    }
    Log.e(TAG, "ApiError: " + it.localizedMessage?.toString())
    return errorAlert
}
