package com.android.masterdistributormdl.profile


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.masterdistributormdl.adapter.HomeAdapter
import com.android.masterdistributormdl.model.ActivityDetailsResult
import com.android.masterdistributormdl.model.AddLeadResult
import com.android.masterdistributormdl.model.ApiResponse
import com.android.masterdistributormdl.model.ErrorAlert
import com.android.masterdistributormdl.model.FollowUpListCount
import com.android.masterdistributormdl.model.LeadStageResult
import com.android.masterdistributormdl.model.LeadSummaryStats
import com.android.masterdistributormdl.model.UserResult
import com.android.masterdistributormdl.model.lead.ClientListResult
import com.android.masterdistributormdl.model.leadstatus.LeadStatusDetailsResult
import com.android.masterdistributormdl.model.profile.ProfileResult
import com.android.masterdistributormdl.network.errorRetrofit
import com.android.masterdistributormdl.network.getClient
import com.android.masterdistributormdl.utils.SharedPreference
import com.android.masterdistributormdl.utils.user_id
import com.google.gson.JsonObject
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ProfileModel(application: Application) : AndroidViewModel(application) {
    val sharedPreference = SharedPreference()
    val isLoaderVisible = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val retrofitError = MutableLiveData<ErrorAlert>()
    val timeLineAdapter = HomeAdapter(null, 2)
    val clientAdapter = HomeAdapter(null, 3)
    val followLeadAdapter = HomeAdapter(null, 4)

    init {

    }


    fun message(msg: String) {
        errorMessage.value = msg
    }

    //get lead list
    fun getProfile(param: JsonObject, result: (ProfileResult) -> Unit) {
        param.addProperty("uid", sharedPreference.getString(user_id))
        isLoaderVisible.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().getUserProfile(param).body()!!
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

    //edit profile
    fun editProfile(param: JsonObject, result: (ApiResponse) -> Unit) {
        param.addProperty("uid", sharedPreference.getString(user_id))
        isLoaderVisible.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().editProfile(param).body()!!
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


    //edit profile photo
    fun editProfilePhoto(base64: String, result: (ApiResponse) -> Unit) {
        isLoaderVisible.value = true
        val param = JsonObject()
        param.addProperty("uid", sharedPreference.getString(user_id))
        param.addProperty("profileimg", base64)
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().editProfilePhoto(param).body()!!
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


}


