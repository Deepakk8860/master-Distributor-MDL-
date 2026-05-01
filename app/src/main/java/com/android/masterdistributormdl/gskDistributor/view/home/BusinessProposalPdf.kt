package com.android.masterdistributormdl.gskDistributor.view.home

import android.app.Application
import android.graphics.Bitmap
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.pdf.PdfRenderer
import android.os.Bundle
import android.os.ParcelFileDescriptor
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.BusinessProposalPdfBinding
import com.google.gson.Gson
import com.google.gson.JsonObject
import com.android.masterdistributormdl.gskDistributor.adapter.HomeAdapter

import com.android.masterdistributormdl.gskDistributor.download.DownloadFIle
import com.android.masterdistributormdl.gskDistributor.download.DownloadListener
import com.android.masterdistributormdl.gskDistributor.download.DownloadTask
import com.gsk.distributor.model.ErrorAlert
import com.gsk.distributor.model.Images
import com.gsk.distributor.model.PdfImages
import com.android.masterdistributormdl.gskDistributor.network.errorRetrofit
import com.android.masterdistributormdl.gskDistributor.network.getClient
import com.android.masterdistributormdl.gskDistributor.utils.FileOpen
import com.android.masterdistributormdl.gskDistributor.utils.InternetError
import com.android.masterdistributormdl.gskDistributor.utils.Loading
import com.android.masterdistributormdl.gskDistributor.utils.STATUS_COLOR2
import com.android.masterdistributormdl.gskDistributor.utils.shooterFragment
import com.android.masterdistributormdl.gskDistributor.utils.showToastShort
import com.android.masterdistributormdl.gskDistributor.utils.user_id
import com.android.masterdistributormdl.gskDistributor.view.MainActivity
import com.android.masterdistributormdl.utils.SharedPreference
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream


class BusinessProposalPdf : Fragment() {
  private lateinit var binding: BusinessProposalPdfBinding
    lateinit var model: BusinessProposalPdfModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.business_proposal_pdf, container, false)
        model = ViewModelProvider(this)[BusinessProposalPdfModel::class.java]
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
        binding.back.setOnClickListener { requireActivity().onBackPressed() }
        model.errorMessage.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) showToastShort(it)
        }
        model.isLoaderVisible.observe(viewLifecycleOwner) {
            Loading.showHide2(requireContext(), it)
            binding.swipeRefreshLayout.isRefreshing = it
        }
        binding.swipeRefreshLayout.setOnRefreshListener {
            model.generateshopCerticate()
        }
        model.generateshopCerticate()
        model.pdfImages.observe(viewLifecycleOwner) {
            model.earnsAdapter.updateAdapter(it as ArrayList<Any>)
        }
        binding.share.setOnClickListener {
            if (model.pdfFile.exists()) {
                FileOpen.shareFIle(requireContext(), model.pdfFile)
            } else {
                showToastShort("Please reload again shop certificate")
            }
        }
        binding.download.setOnClickListener {
            if (model.url.isNullOrEmpty()) {
                showToastShort("certificate not created")
            } else {
                val downloader = DownloadFIle()
                downloader.download(requireContext(), model.url!!)
            }
        }
    }


}

class BusinessProposalPdfModel(application: Application) : AndroidViewModel(application) {
    val sharedPreference = SharedPreference()
    val isLoaderVisible = MutableLiveData<Boolean>()
    val errorMessage = MutableLiveData<String>()
    val retrofitError = MutableLiveData<ErrorAlert>()
    val user = sharedPreference.getUser()!!
    val id = user.id
    val pdfImagesFolder = File(application.filesDir, "GSK_Proposal")
    val pdfFile = File(application.filesDir, "business_proposal-$id.pdf")
    val pdfImages = MutableLiveData<PdfImages>()
    val earnsAdapter = HomeAdapter(this, 22)
    var url: String? = null
    val context = application

    init {
        val json = sharedPreference.getString("proposal_images")
        if (json!!.isNotEmpty()) {
            pdfImages.value = (Gson().fromJson(json, PdfImages::class.java))
        }

    }

    fun generateshopCerticate() {
        isLoaderVisible.value = true
        val param = JsonObject()
        param.addProperty("uid", sharedPreference.getString(user_id))
        param.addProperty("type", "SHOP")

        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    getClient().generateBusinessProposal(param).body()!!
                }
            }.onSuccess {
                isLoaderVisible.value = false
                val uri = it.proposal_url
                if (uri.isNullOrEmpty()) {
                    showToastShort(it.message)
                    sharedPreference.putString("proposal_images", "")
                    pdfImages.postValue(PdfImages())
                    showToastShort(it.message)
                    if (pdfImagesFolder.exists()) {
                        pdfImagesFolder.delete()
                    }
                    if (pdfFile.exists())
                        pdfFile.delete()
                } else {
                    url = uri
                    downloadFIle()
                }
            }.onFailure {
                isLoaderVisible.value = false
                retrofitError.postValue(errorRetrofit(it))
            }
        }
    }

    private fun downloadFIle() {
        DownloadTask(context, url!!, pdfFile, object : DownloadListener {
            override fun onProgress(progress: Int) {
                isLoaderVisible.value = false
            }

            override fun onFinish(file: File?) {
                getImagesFromPDF()
            }

            override fun onFailed(error: String) {
                isLoaderVisible.value = false
            }
        })

    }

    fun getImagesFromPDF() {
        viewModelScope.launch {
            kotlin.runCatching {
                withContext(Dispatchers.IO) {
                    convertFiletoPdf()
                }
            }.onSuccess {
                isLoaderVisible.value = false
            }.onFailure {
                isLoaderVisible.value = false
            }
        }
    }

    private fun convertFiletoPdf() {
        if (pdfImagesFolder.exists()) {
            pdfImagesFolder.delete()
        }
        pdfImagesFolder.mkdirs()
        val pdfImage = PdfImages()
        val fileDescriptor = ParcelFileDescriptor.open(pdfFile, ParcelFileDescriptor.MODE_READ_ONLY)
        val renderer = PdfRenderer(fileDescriptor)
        val pageCount = renderer.pageCount
        for (i in 0 until pageCount) {
            val page = renderer.openPage(i)
            val bitmap = Bitmap.createBitmap(page.width, page.height, Bitmap.Config.ARGB_8888)
            val canvas = Canvas(bitmap)
            canvas.drawColor(Color.WHITE)
            canvas.drawBitmap(bitmap, 0f, 0f, null)
            page.render(bitmap, null, null, PdfRenderer.Page.RENDER_MODE_FOR_DISPLAY)
            page.close()
            val file = File(pdfImagesFolder.absolutePath, "business_image$i.png")
            try {
                val out = FileOutputStream(file)
                bitmap.compress(Bitmap.CompressFormat.PNG, 100, out)
                Log.v("Saved Image - ", file.absolutePath)
                out.flush()
                out.close()
                pdfImage.add(Images((i + 1), file.absolutePath))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
      
        sharedPreference.putString("proposal_images", Gson().toJson(pdfImage))
        pdfImages.postValue(pdfImage)
    }


}



