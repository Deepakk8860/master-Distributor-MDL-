package com.android.masterdistributormdl.network


import android.content.Intent
import android.util.Log
import androidx.localbroadcastmanager.content.LocalBroadcastManager

import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.gskDistributor.utils.BASE_URL
import com.android.masterdistributormdl.main.AUTH_RECEIVER
import com.android.masterdistributormdl.model.ErrorAlert
import com.android.masterdistributormdl.utils.TAG
import com.android.masterdistributormdl.utils.application
import com.google.gson.JsonSyntaxException
import com.google.gson.stream.MalformedJsonException
import kotlinx.coroutines.TimeoutCancellationException
import okhttp3.OkHttpClient
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.net.ProtocolException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.security.cert.CertPathValidatorException
import java.util.concurrent.TimeUnit
import java.util.concurrent.TimeoutException
import javax.net.ssl.SSLHandshakeException
import kotlin.coroutines.cancellation.CancellationException


fun getClient(): RetrofitUrl {
    val client = getOkHttpClient()
    return Retrofit.Builder().baseUrl(BASE_URL).client(client)
        .addConverterFactory(GsonConverterFactory.create()).build().create(RetrofitUrl::class.java)
}


fun getClient(apiUrl: String): RetrofitUrl {
    val client = getOkHttpClient()
    return Retrofit.Builder().baseUrl(apiUrl).client(client)
        .addConverterFactory(GsonConverterFactory.create()).build().create(RetrofitUrl::class.java)
}

fun getOkHttpClient(): OkHttpClient {
    return OkHttpClient.Builder()
        .connectTimeout(60, TimeUnit.SECONDS)
        .readTimeout(60, TimeUnit.SECONDS)
        .writeTimeout(60, TimeUnit.SECONDS)
        .addInterceptor(AppInterceptor())
        //   .addInterceptor(LoggingInterceptor())
        .cache(null).build()
}

fun errorRetrofit(it: Throwable): ErrorAlert {
    var openError = false
    val errorAlert: ErrorAlert
    if (it is TimeoutException || it is TimeoutCancellationException || it is SocketTimeoutException) {
        errorAlert = ErrorAlert(0, "Timeout Error")
    } else if (it is CertPathValidatorException || it is SSLHandshakeException) {
        errorAlert = ErrorAlert(
            0,
            "You are not connected to the internet or firewall is blocking you to access the content.\n" +
                    "Check your connection settings."
        )
    } else if (it is NullPointerException) {
        openError = true
        errorAlert = ErrorAlert(2, "Something went wrong, please try again")
    } else if (it is JsonSyntaxException) {
        openError = true
        errorAlert = ErrorAlert(2, "Response Parsing error")
    } else if (it is MalformedJsonException) {
        openError = true
        errorAlert = ErrorAlert(2, "Response Json body error")
    } else if (it is ProtocolException) {
        openError = true
        errorAlert = ErrorAlert(2, "Unexpected end of stream")
    } else if (it is UnknownHostException) {
        errorAlert = ErrorAlert(1, application.getString(R.string.internet_error))
    } else if (it is CancellationException) {
        errorAlert = ErrorAlert(0, "Task was cancelled")
    } else {
        openError = true
        errorAlert = ErrorAlert(1, "Something went wrong")
    }
    if (openError) {
        val intent = Intent(AUTH_RECEIVER)
        intent.putExtra("status", "Authentication Error")
        LocalBroadcastManager.getInstance(application).sendBroadcast(intent);
    }

    Log.e(TAG, "ApiError: " + it.localizedMessage?.toString())
    return errorAlert
}
