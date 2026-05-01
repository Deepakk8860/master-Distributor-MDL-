package com.android.masterdistributormdl.gskDistributor.view.onboarding

import android.app.Application
import android.util.Log
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.google.gson.JsonObject
import com.gsk.distributor.model.ApiResponse
import com.gsk.distributor.model.ErrorAlert
import com.gsk.distributor.model.StateResult
import com.android.masterdistributormdl.gskDistributor.model.User
import com.gsk.distributor.model.UserResult
import com.gsk.distributor.model.VideoKyc
import com.android.masterdistributormdl.gskDistributor.network.errorRetrofit
import com.android.masterdistributormdl.gskDistributor.network.getClient
import com.android.masterdistributormdl.gskDistributor.utils.TAG
import com.android.masterdistributormdl.gskDistributor.utils.gson
import com.android.masterdistributormdl.gskDistributor.utils.latitude
import com.android.masterdistributormdl.gskDistributor.utils.longitude
import com.android.masterdistributormdl.gskDistributor.utils.user_data
import com.android.masterdistributormdl.gskDistributor.utils.user_id
import com.android.masterdistributormdl.utils.SharedPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class OnboardModel(application: Application) : AndroidViewModel(application) {
    val sharedPreference = SharedPreference()
    var isLoaderVisible = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val retrofitError = MutableLiveData<ErrorAlert>()

 

    fun getUser(): User {
        return sharedPreference.getUserDist()!!
    }

    fun save(user: User) {
        sharedPreference.putString(user_data, gson.toJson(user))
    }

    fun setLatLong(lat: Double, lng: Double) {
        sharedPreference.putString(latitude, lat.toString())
        sharedPreference.putString(longitude, lng.toString())
        Log.d(TAG, "Lat-Long " + lat + "," + lng)
    }

    fun getUserProfile(result: (UserResult) -> Unit) {
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
                result.invoke(it)
            }.onFailure {
                isLoaderVisible.value = false
                retrofitError.postValue(errorRetrofit(it))
            }
        }
    }

    fun update_basic(param: JsonObject, result: (ApiResponse) -> Unit) {
        isLoaderVisible.value = true
        param.addProperty("uid", sharedPreference.getString(user_id))
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().update_basic(param).body()!!
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

    fun videoKycText(result: (VideoKyc) -> Unit) {
        val param=JsonObject()
        isLoaderVisible.value = true
        param.addProperty("uid", sharedPreference.getString(user_id))
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().videoKycText(param).body()!!
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


    //upload video for kyc
    fun uploadVideo(param: JsonObject,result: (ApiResponse) -> Unit) {
        isLoaderVisible.value = true
        param.addProperty("uid", sharedPreference.getString(user_id))
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().getUploadVideoKyc(param).body()!!
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

    fun showLoader(){
        isLoaderVisible.value = true
    }
    fun hideLoader(){
        isLoaderVisible.value = false
    }

    fun update_address(param: JsonObject, result: (ApiResponse) -> Unit) {
        isLoaderVisible.value = true
        param.addProperty("uid", sharedPreference.getString(user_id))
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().update_address(param).body()!!
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

    fun getPinLocation(pincode: String, result: (JsonObject) -> Unit) {
        isLoaderVisible.value = true
        val param = JsonObject()
        param.addProperty("uid", sharedPreference.getString(user_id))
        param.addProperty("pincode", pincode)
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().getPinLocation(param).body()!!
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

    fun statewisecode(result: (StateResult) -> Unit) {
        isLoaderVisible.value = true
        val param = JsonObject()
        param.addProperty("uid", sharedPreference.getString(user_id))
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().statewisecode(param).body()!!
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

    fun update_pan(param: JsonObject, result: (ApiResponse) -> Unit) {
        isLoaderVisible.value = true
        param.addProperty("uid", sharedPreference.getString(user_id))
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().update_pan(param).body()!!
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
    fun update_aadhaar(param: JsonObject, result: (ApiResponse) -> Unit) {
        isLoaderVisible.value = true
        param.addProperty("uid", sharedPreference.getString(user_id))
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().update_aadhaar(param).body()!!
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
    fun agreementText(param: JsonObject, result: (JsonObject) -> Unit) {
        isLoaderVisible.value = true
        param.addProperty("uid", sharedPreference.getString(user_id))
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().agreementText(param).body()!!
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

}
