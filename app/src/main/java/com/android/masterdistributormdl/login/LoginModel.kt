package com.android.masterdistributormdl.login


import android.app.Application
import android.content.ActivityNotFoundException
import android.content.Intent
import android.net.Uri
import androidx.fragment.app.FragmentActivity
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.masterdistributormdl.gskDistributor.view.MainActivity
import com.android.masterdistributormdl.home.Home
import com.android.masterdistributormdl.model.ApiResponse
import com.android.masterdistributormdl.model.ErrorAlert
import com.android.masterdistributormdl.model.User
import com.android.masterdistributormdl.model.auth.LoginResult
import com.android.masterdistributormdl.model.profile.Data
import com.android.masterdistributormdl.model.profile.ProfileResult
import com.android.masterdistributormdl.network.errorRetrofit
import com.android.masterdistributormdl.network.getClient
import com.android.masterdistributormdl.utils.SharedPreference
import com.android.masterdistributormdl.utils.gson
import com.android.masterdistributormdl.utils.replaceFragment
import com.android.masterdistributormdl.utils.session_id
import com.android.masterdistributormdl.utils.user_data
import com.android.masterdistributormdl.utils.user_id
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class LoginModel(application: Application) : AndroidViewModel(application) {
    val sharedPreference = SharedPreference()
    val isLoaderVisible = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val retrofitError = MutableLiveData<ErrorAlert>()
    var user = sharedPreference.getUser()

    fun save(userId: String, sessionId: String) {
        sharedPreference.putString(user_id, userId)
        sharedPreference.putString(session_id, sessionId)
    }

    fun save(user: Data) {
        sharedPreference.putString(user_data, gson.toJson(user))
        getUser()
    }

    fun getUser() {
        user = sharedPreference.getUser()
    }

    //send otp with email and password
    fun loginUser(param: JsonObject, result: (ApiResponse) -> Unit) {
        isLoaderVisible.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().sendOtpToMail(param).body()!!
                }
            }.onSuccess {
                isLoaderVisible.value = false
                result(it)
            }.onFailure {
                isLoaderVisible.value = false
                retrofitError.postValue(errorRetrofit(it))

            }
        }
    }

    fun showLoader(){
        isLoaderVisible.value=true
    }

    //verify by new api
    fun verifyOTP(
        param: JsonObject,
        result: (LoginResult) -> Unit
    ) {
        isLoaderVisible.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().verifyOtp(param).body()!!
                }
            }.onSuccess {
                isLoaderVisible.value = false
                result.invoke(it)
            }.onFailure {
                isLoaderVisible.value = false
                retrofitError.postValue(errorRetrofit(it))
            }
        }
    }


    //verify by new api
    fun verifyByGoogleLogin(
        param: JsonObject,
        result: (LoginResult) -> Unit
    ) {
        isLoaderVisible.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().verifyByGoogleLogin(param).body()!!
                }
            }.onSuccess {
                isLoaderVisible.value = false
                result.invoke(it)
            }.onFailure {
                isLoaderVisible.value = false
                retrofitError.postValue(errorRetrofit(it))
            }
        }
    }

    fun getUserProfile(activity: FragmentActivity) {
        isLoaderVisible.value = true
        val param = JsonObject()
        param.addProperty("uid", sharedPreference.getString(user_id))
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().getUserProfile(param).body()!!

                }
            }.onSuccess {
                isLoaderVisible.value = false
                if (it.status == 0) {
                    save(it.data)
                    val intent =
                        Intent(activity, MainActivity::class.java)
                    activity.startActivity(intent)
                }
            }.onFailure {
                isLoaderVisible.value = false
                retrofitError.postValue(errorRetrofit(it))
            }
        }
    }






}


