package com.android.masterdistributormdl.gskDistributor.view.home

import android.app.Application
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.FragmentAgentListBinding
import com.android.masterdistributormdl.gskDistributor.adapter.HomeAdapter
import com.android.masterdistributormdl.gskDistributor.network.errorRetrofit
import com.android.masterdistributormdl.gskDistributor.network.getClient
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.Loading
import com.android.masterdistributormdl.gskDistributor.utils.STATUS_COLOR2
import com.android.masterdistributormdl.gskDistributor.utils.addFragment
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.android.masterdistributormdl.gskDistributor.utils.user_id
import com.android.masterdistributormdl.gskDistributor.view.MainActivity
import com.android.masterdistributormdl.utils.SharedPreference
import com.google.gson.JsonObject
import com.gsk.distributor.model.AgentItem
import com.gsk.distributor.model.AgentListResult
import com.gsk.distributor.model.ErrorAlert
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

class AgentListScreen : Fragment() {
    private lateinit var model: AgentListModel
    private lateinit var binding: FragmentAgentListBinding

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.fragment_agent_list, container, false)
        model = ViewModelProvider(this)[AgentListModel::class.java]
        binding.lifecycleOwner = viewLifecycleOwner
        return binding.root
    }

    override fun onHiddenChanged(hidden: Boolean) {
        super.onHiddenChanged(hidden)
        if (!hidden) {
            setHeader()
            getAgentList()
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

        binding.back.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.recyclerView.adapter = model.agentAdapter

        // Set add agent click
        binding.cardAddAgent.setOnClickListener {
            addFragment(requireActivity(), AddEditAgentScreen())
        }

        // Set click listener on adapter items for editing
        model.agentAdapter.setOnclickListener {
            val agent = it as AgentItem
            val bundle = bundleOf(
                "agent_id" to (agent.id ?: ""),
                "name" to (agent.name ?: ""),
                "email" to (agent.email ?: ""),
                "mobile" to (agent.mobile ?: ""),
                "pincode" to (agent.pincode ?: ""),
                "address" to (agent.address ?: "")
            )
            addFragment(requireActivity(), AddEditAgentScreen(), bundle)
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
            getAgentList()
        }

        binding.edtSearch.addTextChangedListener {
            val query = it.toString()
            if (query.isNotEmpty()) {
                model.agentAdapter.searchFilter(query)
                if (model.agentAdapter.arrayList.size > 0) {
                    binding.txtNoDataFound.visibility = View.GONE
                } else {
                    binding.txtNoDataFound.visibility = View.VISIBLE
                }
            } else {
                model.agentAdapter.clearSearch()
                binding.txtNoDataFound.visibility = View.GONE
            }
        }

        getAgentList()
    }

    fun getAgentList() {
        model.getAgentList { response ->
            if (response.status == 0) {
                if (response.data.isNullOrEmpty()) {
                    binding.txtNoDataFound.visibility = View.VISIBLE
                } else {
                    binding.txtNoDataFound.visibility = View.GONE
                }
                binding.edtSearch.visibility = View.VISIBLE
                binding.count.text = "(${response.data.size} Agents)"
                model.agentAdapter.updateAdapter1(response.data as ArrayList<Any>)
            } else {
                binding.count.text = "(0 Agents)"
                model.agentAdapter.updateAdapter1(ArrayList())
                binding.edtSearch.visibility = View.GONE
                binding.txtNoDataFound.visibility = View.VISIBLE
                binding.txtNoDataFound.text = response.message
            }
        }
    }
}

class AgentListModel(application: Application) : AndroidViewModel(application) {
    val sharedPreference = SharedPreference()
    val isLoaderVisible = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val retrofitError = MutableLiveData<ErrorAlert>()
    val agentAdapter = HomeAdapter(this, 30)

    fun getAgentList(result: (AgentListResult) -> Unit) {
        isLoaderVisible.value = true
        val param = JsonObject()
        param.addProperty("uid", sharedPreference.getString(user_id))
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().agentList(param).body()!!
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
