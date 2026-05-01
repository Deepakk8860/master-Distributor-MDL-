package com.android.masterdistributormdl.gskDistributor.view.home

import android.app.Application
import android.graphics.BitmapFactory
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import com.android.masterdistributormdl.utils.SharedPreference
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.cropper.CropImageContract
import com.android.masterdistributormdl.cropper.CropOptions
import com.android.masterdistributormdl.databinding.TicketInfoBinding
import com.google.gson.JsonObject
import com.android.masterdistributormdl.gskDistributor.adapter.HomeAdapter
import com.gsk.distributor.model.ApiResponse
import com.gsk.distributor.model.TicketItem
import com.gsk.distributor.model.TicketReplies
import com.gsk.distributor.model.TicketResult
import com.gsk.distributor.model.ErrorAlert
import com.android.masterdistributormdl.gskDistributor.network.errorRetrofit
import com.android.masterdistributormdl.gskDistributor.network.getClient
import com.android.masterdistributormdl.gskDistributor.view.MainActivity
import com.android.masterdistributormdl.gskDistributor.download.DownloadFIle
import com.android.masterdistributormdl.gskDistributor.utils.AlertError
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.Loading
import com.android.masterdistributormdl.gskDistributor.utils.STATUS_COLOR2
import com.android.masterdistributormdl.gskDistributor.utils.addFragment
import com.android.masterdistributormdl.gskDistributor.utils.hideSoftKeyBoard
import com.android.masterdistributormdl.gskDistributor.utils.imageToBase64
import com.android.masterdistributormdl.gskDistributor.utils.indiaDate
import com.android.masterdistributormdl.gskDistributor.utils.setTextColor2
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File


class TicketInfo : Fragment() {
    private lateinit var binding: TicketInfoBinding

    lateinit var model: TicketInfoModel
    private var attachmentTicket=""

    var item: TicketItem? = null
    var base64 = ""
    var isViewMoreShow = false
    var ticket_id = ""

    val arrayReplies = ArrayList<TicketReplies>()
    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding=
            DataBindingUtil.inflate(inflater, R.layout.ticket_info, container, false)
        model = ViewModelProvider(this).get(TicketInfoModel::class.java)
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

        item = requireArguments().getSerializable("data") as TicketItem?
        setHeader()
        binding.back.setOnClickListener {
            hideSoftKeyBoard(requireContext())
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
        model.isDialogVisible.observe(viewLifecycleOwner) {
            Loading.showHide(requireActivity(), it)
        }
        model.isLoaderVisible.observe(viewLifecycleOwner) {
            binding.swipeRefreshLayout.isRefreshing = it
            Loading.showHide2(requireActivity(), it)
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            getTicketInfo()
        }
        binding.attachment.setOnClickListener {
            startCrop()
        }
        binding.sendChat.setOnClickListener {
            if (binding.etMessage.length() < 25) {
                showToastShort("Please enter minimum 25 characters")
            } else {
                sendChat()
            }
        }
        binding.chatImageLay.visibility = View.GONE
        binding.close.setOnClickListener {
            base64 = ""
            binding.chatImageLay.visibility = View.GONE
        }
        model.chatAdapter.setOnclickListener {
            val replies = it as TicketReplies

            startDownloading(replies.attachment)
        }
        binding.viewMore.visibility = View.GONE
        binding.attachmentLay.setOnClickListener {
            addFragment(requireActivity(), ViewProfilePicture(),
                bundleOf("profilePicture" to attachmentTicket)
            )
//            startDownloading(item!!.attachment)
        }
        binding.viewMore.setOnClickListener {
            binding.viewMore.visibility = View.GONE
            isViewMoreShow = true
            setChatView()
        }
        binding.scrollViewLay.visibility = View.GONE

        if (item == null) {
            ticket_id = requireArguments().getString("ticket_id")!!
        } else {
            setData()
        }
        getTicketInfo()
    }

    private fun setData() {
        val item = this.item!!
        ticket_id = item.id
        binding.ticketId.text = "Ticket ID: " + item.ticket_id
        binding.type.text = item.issue
        binding.subject.text = item.subject
        binding.description.text = item.message

        binding.date.text = "Created Date: " + indiaDate(item.create_dt)
        val status_ = item.status
        if (status_.equals("2")) {
            binding.status.text = "Closed"
            binding.status.setTextColor2("#F82002")
            binding.chatLay.visibility = View.GONE
        } else {
            binding.status.text = "Active"
            binding.status.setTextColor2("#34A853")
            binding.chatLay.visibility = View.VISIBLE
        }
        if (item.attachment.startsWith("http")) {
            binding.attachmentLay.visibility = View.VISIBLE
        } else {
            binding.attachmentLay.visibility = View.GONE
        }
        binding.scrollViewLay.visibility = View.VISIBLE


    }

    private fun getTicketInfo() {
        val param = JsonObject()
        param.addProperty("ticket_id", ticket_id)
        param.addProperty("type", 0)
        model.getTicketInfo(param) {
            arrayReplies.clear()
            if (it.status == 0) {
                item = it.ticketdata
                setData()
                attachmentTicket=it.ticketdata.attachment
                arrayReplies.addAll(it.ticketdetail)
                setChatView()
            } else {
                AlertError.show(requireContext(), it.message) {
                    requireActivity().onBackPressed()
                }
            }
        }
    }

    private fun setChatView() {
        if (arrayReplies.size > 1 && !isViewMoreShow) {
            binding.viewMore.visibility = View.VISIBLE
            val array2 = ArrayList<TicketReplies>()
            array2.add(arrayReplies[0])
            model.updateAdapter(array2)
        } else {
            //  arrayReplies.reverse()
            model.updateAdapter(arrayReplies)
            //  scrollViewLay.post { scrollViewLay.fullScroll(ScrollView.FOCUS_DOWN) }
        }

    }

    private val cropImage = registerForActivityResult(CropImageContract()) { result ->
        if (result.isSuccessful) {
            val uri = result.uriContent!!
            val filePath = result.getUriFilePath(requireContext())
            val file = File(filePath)
            binding.chatImageLay.visibility = View.VISIBLE
            val bitmap = BitmapFactory.decodeFile(filePath)
            binding.chatImage.setImageBitmap(bitmap)
            base64 = imageToBase64(file)
            file.delete()

        } else {
            val exception = result.error

        }
    }

    private fun startCrop() {
        cropImage.launch(CropOptions {
            /*  setAspectRatio(500, 500)*/
            setActivityTitle("Pick Image")
            setRequestedSize(500, 500)
            setAllowFlipping(true)
            setAllowRotation(true)
            setImageSource(includeGallery = true, includeCamera = false)
        })
    }

    private fun sendChat() {
        val param = JsonObject()
        param.addProperty("ticket_id", ticket_id)
        param.addProperty("replycomment", binding.etMessage.text.toString())
        param.addProperty("attachment", base64)
        model.sendChat(param) {
            if (it.status == 0) {
                base64 = ""
                binding.chatImage.setImageBitmap(null)
                binding.chatImageLay.visibility = View.GONE
                binding.etMessage.setText("")
                getTicketInfo()
                //   SuccessAlert.show(requireContext(), it.message) { }
            } else {
                AlertError.show(requireContext(), it.message) {}
            }
        }
    }


    private fun startDownloading(url: String) {
        val downloader = DownloadFIle()
        downloader.download(requireContext(), url)
    }

}

class TicketInfoModel(application: Application) : AndroidViewModel(application) {
    val sharedPreference = SharedPreference()
    val isLoaderVisible = MutableLiveData<Boolean>()
    val isDialogVisible = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val retrofitError = MutableLiveData<ErrorAlert>()
    val user = sharedPreference.getUser()!!
    val chatAdapter = HomeAdapter(this, 15)

    init {

    }

    fun updateAdapter(array: ArrayList<TicketReplies>) {
        chatAdapter.updateAdapter(array as ArrayList<Any>)
    }

    fun sendChat(param: JsonObject, result: (ApiResponse) -> Unit) {
        isDialogVisible.value = true
        param.addProperty("uid", user.id)
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().sendChat(param).body()!!
                }
            }.onSuccess {
                isDialogVisible.value = false
                result.invoke(it)
            }.onFailure {
                isDialogVisible.value = false
                retrofitError.postValue(errorRetrofit(it))
            }
        }
    }

    fun getTicketInfo(param: JsonObject, result: (TicketResult) -> Unit) {
        isLoaderVisible.value = true
        param.addProperty("uid", user.id)
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().getTicketInfo(param).body()!!
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



