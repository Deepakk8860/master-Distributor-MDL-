package com.android.masterdistributormdl.gskDistributor.view.home

import android.app.Application
import android.content.ContentValues
import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.provider.ContactsContract
import android.view.LayoutInflater
import com.android.masterdistributormdl.utils.SharedPreference
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf

import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.ConsultantDetailsBinding
import com.android.masterdistributormdl.databinding.SupportBinding
import com.android.masterdistributormdl.gskDistributor.model.consultant.Consultant
import com.android.masterdistributormdl.gskDistributor.model.consultant.ConsultantResult
import com.android.masterdistributormdl.gskDistributor.model.consultant.EscalationManager
import com.android.masterdistributormdl.gskDistributor.model.consultant.Manager
import com.android.masterdistributormdl.gskDistributor.model.consultant.Trainer
import com.google.gson.JsonObject
import com.gsk.distributor.model.ApiResponse
import com.gsk.distributor.model.TicketTypeResult
import com.gsk.distributor.model.ErrorAlert
import com.android.masterdistributormdl.gskDistributor.network.errorRetrofit
import com.android.masterdistributormdl.gskDistributor.network.getClient
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.Loading
import com.android.masterdistributormdl.gskDistributor.utils.STATUS_COLOR2
import com.android.masterdistributormdl.gskDistributor.utils.addFragment
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.android.masterdistributormdl.gskDistributor.view.MainActivity

import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class ConsultantDetails : Fragment() {
    private lateinit var binding: ConsultantDetailsBinding
    lateinit var model: ConsultantDetailModel
    private var consultantPhone=""
    private var consultantName=""
    private var consultantManager=""
    private var consultantManagerName=""
    private var consultantTrainer=""
    private var consultantTrainerName=""
    private var consultantEscalation=""
    private var consultantEscalationName=""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.consultant_details, container, false)
        model = ViewModelProvider(this)[ConsultantDetailModel::class.java]
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
            Loading.showHide(requireActivity(), it)
        }

        binding.back.setOnClickListener { requireActivity().onBackPressed()}
        getConsultantDetails()
        initListener()
    }

    private fun initListener() {
        binding.addContactConsultant.setOnClickListener {
            addContact(consultantPhone,consultantName)
        }

        binding.addContactManager.setOnClickListener {
            addContact(consultantManager,consultantManagerName)
        }

        binding.addContactTrainer.setOnClickListener {
            addContact(consultantTrainer,consultantTrainerName)
        }

        binding.addContactEscalation.setOnClickListener {
            addContact(consultantEscalation,consultantEscalationName)
        }
    }

    private fun addContact(phone: String, name: String) {
        val intent = Intent(ContactsContract.Intents.Insert.ACTION)
        intent.type = ContactsContract.RawContacts.CONTENT_TYPE
        intent.putExtra(ContactsContract.Intents.Insert.NAME, name)
        val data = ArrayList<ContentValues>()

        val row1 = ContentValues()
        row1.put(
            ContactsContract.Contacts.Data.MIMETYPE,
            ContactsContract.CommonDataKinds.Phone.CONTENT_ITEM_TYPE
        )
        row1.put(ContactsContract.CommonDataKinds.Phone.NUMBER, phone)
        row1.put(
            ContactsContract.CommonDataKinds.Phone.TYPE,
            ContactsContract.CommonDataKinds.Phone.TYPE_HOME
        )
        data.add(row1)

        intent.putParcelableArrayListExtra(ContactsContract.Intents.Insert.DATA, data)
        startActivity(intent)
    }

    private fun getConsultantDetails() {
        model.getConsultantDetails { 
            if(it.status==0){
                if (it.consultant !=null){
                    binding.cvConsultant.visibility=View.VISIBLE
                    setConsultantDetails(it.consultant)
                }else{
                    binding.cvConsultant.visibility=View.GONE
                }

                if (it.trainer != null){
                    binding.cvTrainer.visibility=View.VISIBLE
                    setTrainerDetails(it.trainer)
                }else{
                    binding.cvTrainer.visibility=View.GONE
                }

                if (it.manager != null){
                    binding.cvManager.visibility=View.VISIBLE
                    setManagerDetails(it.manager)
                }else{
                    binding.cvManager.visibility=View.GONE
                }
                if (it.escalation_manager != null){
                    binding.cvEscalation.visibility=View.VISIBLE
                    setEscalationDetails(it.escalation_manager)
                }else{
                    binding.cvEscalation.visibility=View.GONE
                }
            }
        }
    }

    private fun setEscalationDetails(it: EscalationManager) {
        binding.txtEscalationName.text=it.name
        binding.txtEscalationEmail.text=it.email
        binding.txtEscalationMobile.text=it.phone
        consultantEscalation=it.phone
        consultantEscalationName=it.name
    }


    private fun setManagerDetails(it: Manager) {
        binding.txtManagerName.text=it.name
        binding.txtManagerEmail.text=it.email
        binding.txtManagerMobile.text=it.phone
        consultantManager=it.phone
        consultantManagerName=it.name
    }

    private fun setTrainerDetails(it: Trainer) {
        binding.txtAgentName.text=it.name
        binding.txtAgentEmail.text=it.email
        binding.txtAgentMobile.text=it.phone
        consultantTrainer=it.phone
        consultantTrainerName=it.name
    }

    private fun setConsultantDetails(it: Consultant) {
        binding.txtConsutantName.text=it.name
        binding.txtConsutantEmail.text=it.email
        binding.txtConsutantMobile.text=it.phone
        consultantPhone=it.phone
        consultantName=it.name
    }


}

class ConsultantDetailModel(application: Application) : AndroidViewModel(application) {
    val sharedPreference = SharedPreference()
    val isLoaderVisible = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val retrofitError = MutableLiveData<ErrorAlert>()
    val user = sharedPreference.getUser()!!

    fun getConsultantDetails(result: (ConsultantResult) -> Unit) {
        isLoaderVisible.value = true
        val param = JsonObject()
        param.addProperty("uid", user.id)
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().getConsultantDetails(param).body()!!
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

    fun createTicket(param: JsonObject, result: (ApiResponse) -> Unit) {
        isLoaderVisible.value = true
        param.addProperty("uid", user.id)
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().createTicket(param).body()!!
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



