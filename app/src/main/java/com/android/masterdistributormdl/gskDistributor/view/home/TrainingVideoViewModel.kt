package com.android.masterdistributormdl.gskDistributor.view.home

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import com.android.masterdistributormdl.utils.SharedPreference
import com.gsk.distributor.model.ErrorAlert


class TrainingVideoViewModel(application: Application) : AndroidViewModel(application) {
    val isLoaderVisible = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    private val sharedPreference= SharedPreference()
    val retrofitError = MutableLiveData<ErrorAlert>()




//    //get list of training video
//    fun getTrainingVideoList(result: (TrainingVideoResult) -> Unit) {
//        isLoaderVisible.value = true
//        val param = UserIdRequest(uid = sharedPreference.getString(user_id))
//        viewModelScope.launch {
//            kotlin.runCatching {
//                withContext(Dispatchers.IO) {
//                    getClient().getTrainingVideo(param).body()!!
//                }
//            }.onSuccess {
//                isLoaderVisible.value = false
//                result.invoke(it)
//            }.onFailure {
//                com.prologic.profin.utils.isLoaderVisible.value = false
//                retrofitError.postValue(errorRetrofit(it))
//            }
//        }
//    }

}