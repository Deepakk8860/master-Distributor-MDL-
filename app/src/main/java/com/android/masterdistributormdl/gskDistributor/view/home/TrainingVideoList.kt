package com.android.masterdistributormdl.gskDistributor.view.home


import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import com.android.masterdistributormdl.utils.SharedPreference
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.TrainingVideoBinding
import com.google.gson.JsonObject
import com.android.masterdistributormdl.gskDistributor.adapter.NewAdapter
import com.gsk.distributor.model.ErrorAlert
import com.gsk.distributor.model.TrainingVideoListData
import com.android.masterdistributormdl.gskDistributor.network.errorRetrofit
import com.android.masterdistributormdl.gskDistributor.network.getClient
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.Loading
import com.android.masterdistributormdl.gskDistributor.utils.STATUS_COLOR2
import com.android.masterdistributormdl.gskDistributor.utils.intentPassValue
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.android.masterdistributormdl.gskDistributor.utils.user_id
import com.android.masterdistributormdl.gskDistributor.view.MainActivity
import com.gsk.distributor.model.TrainingVideoListResult
import com.android.masterdistributormdl.gskDistributor.view.PlayerActivity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.util.*

class TrainingVideoList : Fragment() {
    lateinit var model: TrainingVideoListModel
    private lateinit var binding: TrainingVideoBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding=
            DataBindingUtil.inflate(inflater, R.layout.training_video, container, false)
        model = ViewModelProvider(this)[TrainingVideoListModel::class.java]
        binding.model = model
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        return binding.root
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            setHeader()
        }
    }

    private fun setHeader() {
        try {
            shooterFragment = this
            (activity as MainActivity).setHeader("", STATUS_COLOR2)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHeader()
        initView()
        initListener()
        getTrainingVideoList()
    }

    private fun initListener() {
        model.trainingVideoAdapter.setOnclickListener {
            it as TrainingVideoListData
            val bundle = bundleOf("videoLink" to it.url,"title" to it.title)
            intentPassValue(requireContext(), PlayerActivity(),bundle)
        }
    }

    private fun initView() {
        binding.back.setOnClickListener {
            requireActivity().onBackPressed()
        }
        model.retrofitError.observe(viewLifecycleOwner) {
            if (it.status == 0) {
                showToastShort(it.message)
            } else {
                InternetError.show(requireContext())
            }
        }
        model.errorMessage.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) showToastShort(it)
        }
        model.isLoaderVisible.observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = it
            Loading.showHide2(requireActivity(), it)
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            getTrainingVideoList()
        }
    }



    private fun getTrainingVideoList() {
        model.getTrainingVideoList {
            if (it.status == 0) {
                model.trainingVideoAdapter.updateAdapter(it.data as ArrayList<Any>)
            }
        }
    }
}

class TrainingVideoListModel(application: Application) : AndroidViewModel(application) {
    val sharedPreference = SharedPreference()
    val isLoaderVisible = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val retrofitError = MutableLiveData<ErrorAlert>()
    var user = sharedPreference.getUser()!!
    val trainingVideoAdapter = NewAdapter(this, 4)




    fun getTrainingVideoList(result: (TrainingVideoListResult) -> Unit) {
        isLoaderVisible.value = true
        val param=JsonObject()
        param.addProperty("uid",sharedPreference.getString(user_id))
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().getTrainingVideoList(param).body()!!
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





