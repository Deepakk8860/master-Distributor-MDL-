package com.android.masterdistributormdl.gskDistributor.download

import okhttp3.RequestBody
import okhttp3.ResponseBody
import retrofit2.Call
import retrofit2.http.*

interface DownloadService {
    @Streaming
    @GET
    fun downloadUrl(@Url url: String): Call<ResponseBody>

    @Streaming
    @POST
    @Headers("Content-Type: application/json")
    fun downloadUrl(@Url url: String, @Body body: RequestBody): Call<ResponseBody>
}