package com.android.masterdistributormdl.home


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
import com.android.masterdistributormdl.model.lead.ClientListResult
import com.android.masterdistributormdl.model.leadStatusList.LeadStatusFilterListResult
import com.android.masterdistributormdl.model.leadstatus.LeadStatusDetailsResult
import com.android.masterdistributormdl.model.preferedMessage.PreferedMessageListResult
import com.android.masterdistributormdl.model.territory.PincodeResult
import com.android.masterdistributormdl.network.errorRetrofit
import com.android.masterdistributormdl.network.getClient
import com.android.masterdistributormdl.utils.SharedPreference
import com.android.masterdistributormdl.utils.user_id
import com.google.gson.JsonObject
import com.gsk.distributor.model.UserResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class HomeModel(application: Application) : AndroidViewModel(application) {
    val sharedPreference = SharedPreference()
    val isLoaderVisible = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val retrofitError = MutableLiveData<ErrorAlert>()
    val retrofitErrorDist = MutableLiveData<com.gsk.distributor.model.ErrorAlert>()
    val timeLineAdapter = HomeAdapter(null, 2)
    val clientAdapter = HomeAdapter(null, 3)
    val followLeadAdapter = HomeAdapter(null, 4)
    var pincodeListAdapter = PincodeAdapter(this, 1)
    var pincodeListCityAdapter = PincodeAdapter(this, 4)
    val sharingListAdapter = PincodeAdapter(this, 2)
    var pincodeSelectedList=ArrayList<String>()
    private var type=0
    init {
    }


    fun message(msg: String) {
        errorMessage.value = msg
    }

    //get lead list
    fun getLeadStage(param: JsonObject, result: (LeadStageResult) -> Unit) {
        param.addProperty("uid", sharedPreference.getString(user_id))
        isLoaderVisible.value = true
        type=4
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().getLeadStage(param).body()!!
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

    fun getUserProfile(result: (UserResult) -> Unit) {
        isLoaderVisible.value=true
        type=1
        val param = JsonObject()
        param.addProperty("uid", sharedPreference.getString(com.android.masterdistributormdl.gskDistributor.utils.user_id))

        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    com.android.masterdistributormdl.gskDistributor.network.getClient().getUserProfile(param).body()!!
                }
            }.onSuccess {
                isLoaderVisible.value=false
                result.invoke(it)
            }.onFailure {
                isLoaderVisible.value=false
                retrofitErrorDist.postValue(
                    com.android.masterdistributormdl.gskDistributor.network.errorRetrofit(it))
            }
        }
    }

    fun getCityPlan(param: JsonObject,result: (com.gsk.distributor.model.PincodeResult) -> Unit) {
        isLoaderVisible.value=true
        param.addProperty("uid", sharedPreference.getString(com.android.masterdistributormdl.gskDistributor.utils.user_id))

        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().getPlanCity(param).body()!!
                }
            }.onSuccess {
                isLoaderVisible.value=false
                result.invoke(it)
            }.onFailure {
                isLoaderVisible.value=false
                retrofitError.postValue(errorRetrofit(it))
            }
        }
    }

    //get lead list
    fun getLeadStatusData(param: JsonObject, result: (LeadStatusDetailsResult) -> Unit) {
        param.addProperty("uid", sharedPreference.getString(user_id))
        isLoaderVisible.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().getLeadStatusDetails(param).body()!!
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


    //update lead status
    fun updateLeadStatus(param: JsonObject, result: (ApiResponse) -> Unit) {
        param.addProperty("uid", sharedPreference.getString(user_id))
        isLoaderVisible.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().updateLeads(param).body()!!
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

    //update lead status
    fun updateActivity(param: JsonObject, result: (ApiResponse) -> Unit) {
        param.addProperty("uid", sharedPreference.getString(user_id))
        isLoaderVisible.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().updateActivity(param).body()!!
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

    //get activity details
    fun getActivityDetails(param: JsonObject, result: (ActivityDetailsResult) -> Unit) {
        param.addProperty("uid", sharedPreference.getString(user_id))
        isLoaderVisible.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().getActivityDetails(param).body()!!
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


    //get client list manage
//    fun getClientList(param: JsonObject, result: (ClientListResult) -> Unit) {
//        param.addProperty("uid", sharedPreference.getString(user_id))
//        isLoaderVisible.value = true
//        viewModelScope.launch {
//            kotlin.runCatching {
//                withContext(Dispatchers.IO) {
//                    getClient().getClientListFilter(param).body()!!
//                }
//            }.onSuccess {
//                isLoaderVisible.value = false
//                result(it)
//            }.onFailure {
//                isLoaderVisible.value = false
//                retrofitError.postValue(errorRetrofit(it))
//
//            }
//        }
//    }//get client list

    //get client list manage
    fun getClientListFilter(param: JsonObject, result: (LeadStatusFilterListResult) -> Unit) {
        param.addProperty("uid", sharedPreference.getString(user_id))
        isLoaderVisible.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().getClientListFilter(param).body()!!
                }
            }.onSuccess {
                isLoaderVisible.value = false
                result(it)
            }.onFailure {
                isLoaderVisible.value = false
                retrofitError.postValue(errorRetrofit(it))

            }
        }
    }//get client list

    fun getPlanList(result: (LeadStageResult) -> Unit) {
        val param = JsonObject()
        param.addProperty("uid", sharedPreference.getString(user_id))
        isLoaderVisible.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().getPlanList(param).body()!!
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


    fun getPreferedMessageList(result: (PreferedMessageListResult) -> Unit) {
        val param = JsonObject()
        param.addProperty("uid", sharedPreference.getString(user_id))
        isLoaderVisible.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().getPreferedMessage(param).body()!!
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

    //get follow up list
    fun getFollowUpList(param: JsonObject, result: (ClientListResult) -> Unit) {
        param.addProperty("uid", sharedPreference.getString(user_id))
        isLoaderVisible.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().getFollowUpList(param).body()!!
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

    //get follow up list
    fun getLeadSummary(param: JsonObject, result: (LeadSummaryStats) -> Unit) {
        param.addProperty("uid", sharedPreference.getString(user_id))
        isLoaderVisible.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().getLeadSummary(param).body()!!
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


    //get follow up list count
    fun getFollowUpListCount(param: JsonObject, result: (FollowUpListCount) -> Unit) {
        param.addProperty("uid", sharedPreference.getString(user_id))
//        isLoaderVisible.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().getFollowUpListCount(param).body()!!
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


    //get pin location
    fun getPinLocation(param: JsonObject, result: (JsonObject) -> Unit) {
        param.addProperty("uid", sharedPreference.getString(user_id))
        isLoaderVisible.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().getPinLocation(param).body()!!
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


    //create payment link
    fun createPaymentLink(param: JsonObject, result: (ApiResponse) -> Unit) {
        param.addProperty("uid", sharedPreference.getString(user_id))
        isLoaderVisible.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().createPaymentLink(param).body()!!
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

    //add client lead
    fun addLead(param: JsonObject, result: (AddLeadResult) -> Unit) {
        param.addProperty("uid", sharedPreference.getString(user_id))
        isLoaderVisible.value = true
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().addLead(param).body()!!
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


