package com.android.masterdistributormdl.doc


import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.viewModelScope
import com.android.masterdistributormdl.adapter.HomeAdapter
import com.android.masterdistributormdl.adapter.PincodeAdapter
import com.android.masterdistributormdl.model.ActivityDetailsResult
import com.android.masterdistributormdl.model.AddLeadResult
import com.android.masterdistributormdl.model.ApiResponse
import com.android.masterdistributormdl.model.ErrorAlert
import com.android.masterdistributormdl.model.FollowUpListCount
import com.android.masterdistributormdl.model.LeadStageResult
import com.android.masterdistributormdl.model.LeadSummaryStats
import com.android.masterdistributormdl.model.User
import com.android.masterdistributormdl.model.doc.DocResult
import com.android.masterdistributormdl.model.lead.ClientListResult
import com.android.masterdistributormdl.model.leadstatus.LeadStatusDetailsResult
import com.android.masterdistributormdl.model.preferedMessage.PreferedMessageListResult
import com.android.masterdistributormdl.network.errorRetrofit
import com.android.masterdistributormdl.network.getClient
import com.android.masterdistributormdl.utils.SharedPreference
import com.android.masterdistributormdl.utils.user_id
import com.google.android.gms.common.api.Api
import com.google.gson.JsonObject
import com.gsk.distributor.model.UserResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class DocModel(application: Application) : AndroidViewModel(application) {
    val sharedPreference = SharedPreference()
    val isLoaderVisible = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val retrofitError = MutableLiveData<ErrorAlert>()
    val retrofitErrorDist = MutableLiveData<com.gsk.distributor.model.ErrorAlert>()
    val docSharingAdapter = PincodeAdapter(null, 3)



    init {

    }


    fun message(msg: String) {
        errorMessage.value = msg
    }

    //get lead list
    fun uploadDoc(param: JsonObject, result: (ApiResponse) -> Unit) {
        param.addProperty("uid", sharedPreference.getString(user_id))
        isLoaderVisible.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().uploadDoc(param).body()!!
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


    //get lead list
    fun docList(result: (DocResult) -> Unit) {
        val param=JsonObject()
        param.addProperty("uid", sharedPreference.getString(user_id))
        isLoaderVisible.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().docList(param).body()!!
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


