package com.android.masterdistributormdl.doc

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.OpenableColumns
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.core.os.bundleOf
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.DocSharingBinding
import com.android.masterdistributormdl.home.HomeModel
import com.android.masterdistributormdl.main.MainActivity
import com.android.masterdistributormdl.model.doc.Data
import com.android.masterdistributormdl.utils.AlertError
import com.android.masterdistributormdl.utils.InternetError
import com.android.masterdistributormdl.utils.Loading
import com.android.masterdistributormdl.utils.STATUS_COLOR1
import com.android.masterdistributormdl.utils.addFragment
import com.android.masterdistributormdl.utils.shooterFragment
import com.android.masterdistributormdl.utils.showToastShort
import com.google.gson.JsonObject

class DocSharing : Fragment() {
    private lateinit var binding: DocSharingBinding
    lateinit var model: DocModel
    private lateinit var pdfPickerLauncher: ActivityResultLauncher<Intent>

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.doc_sharing, container, false)
        model = ViewModelProvider(this)[DocModel::class.java]
        binding.model = model
        binding.lifecycleOwner = this
        binding.executePendingBindings()
        return binding.root
    }

    @RequiresApi(Build.VERSION_CODES.O)
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        setHeader()
        handleRetrofitMessage()
        initView()
        initListener()
        getDocList()
    }

    private fun getDocList() {
        model.docList{
            if (it.status==0){
                binding.recDocUpload.visibility=View.VISIBLE
                binding.llContent.visibility=View.GONE
                model.docSharingAdapter.updateAdapter(it.data as ArrayList<Any>)
                binding.recDocUpload.adapter=model.docSharingAdapter
            }else{
                binding.recDocUpload.visibility=View.GONE
                binding.llContent.visibility=View.VISIBLE
            }
        }
    }


    fun sharePdfUrl(pdfUrl: String) {
        val shareIntent = Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_SUBJECT, "Shared PDF")
            putExtra(Intent.EXTRA_TEXT, "Hey, check out this PDF:\n$pdfUrl")
        }

        val chooser = Intent.createChooser(shareIntent, "Share PDF using")
        if (shareIntent.resolveActivity(requireContext().packageManager) != null) {
            startActivity(chooser)
        } else {
            Toast.makeText(requireContext(), "No app available to share", Toast.LENGTH_SHORT).show()
        }
    }


    private fun initView() {
        binding.swipeRefreshLayout.setOnRefreshListener {
            getDocList()
        }


        pdfPickerLauncher =
            registerForActivityResult(ActivityResultContracts.StartActivityForResult()) { result ->
                if (result.resultCode == Activity.RESULT_OK) {
                    val pdfUri = result.data?.data
                    if (pdfUri != null) {
                        val fileName = getFileNameFromUri(pdfUri)
                        encodePdfToBase64(pdfUri, fileName)
                    } else {
                        Toast.makeText(requireContext(), "PDF selection failed", Toast.LENGTH_SHORT).show()
                    }
                }
            }
    }

    private fun initListener() {
        binding.backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        model.docSharingAdapter.setOnclickListener {
            it as Data
            if (it.isShare){
                sharePdfUrl(it.file_url)
            }else{
                addFragment(requireActivity(),PdfPreview(), bundleOf("pdfUrl" to it.file_url))
            }
        }


        binding.fabUpload.setOnClickListener {
            pickPdfFromDevice()
        }
    }

    private fun pickPdfFromDevice() {
        val intent = Intent(Intent.ACTION_GET_CONTENT)
        intent.type = "application/pdf"
        intent.addCategory(Intent.CATEGORY_OPENABLE)
        pdfPickerLauncher.launch(Intent.createChooser(intent, "Select PDF"))
    }

    @SuppressLint("Range")
    fun getFileNameFromUri(uri: Uri): String {
        var result: String? = null
        if (uri.scheme == "content") {
            val cursor = requireContext().contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    result = it.getString(it.getColumnIndex(OpenableColumns.DISPLAY_NAME))
                }
            }
        }
        if (result == null) {
            result = uri.path
            val cut = result?.lastIndexOf('/')
            if (cut != -1) {
                result = result?.substring(cut!! + 1)
            }
        }
        return result ?: "unknown_file.pdf"
    }

    private fun encodePdfToBase64(uri: Uri, fileName: String) {
        val base64WithMime = pdfToBase64WithMimePrefix(uri)
        if (base64WithMime != null) {
            Log.d("Base64PDF", base64WithMime.take(100)) // Optional: preview
            uploadPdfBase64(base64WithMime, fileName)
        } else {
            Toast.makeText(requireContext(), "Failed to encode PDF", Toast.LENGTH_SHORT).show()
        }
    }

    private fun pdfToBase64WithMimePrefix(uri: Uri): String? {
        return try {
            val inputStream = requireContext().contentResolver.openInputStream(uri)
            val bytes = inputStream?.readBytes()
            inputStream?.close()
            if (bytes != null) {
                "data:application/pdf;base64," + Base64.encodeToString(bytes, Base64.NO_WRAP)
            } else {
                null
            }
        } catch (e: Exception) {
            e.printStackTrace()
            null
        }
    }

    private fun uploadPdfBase64(base64Pdf: String, fileName: String) {
        val param = JsonObject().apply {
            addProperty("document_file", base64Pdf)
            addProperty("document_name", fileName)
        }

        model.uploadDoc(param) {
            if (it.status == 0) {
                showToastShort(it.message)
                getDocList()
            }
        }
    }

    private fun handleRetrofitMessage() {
        model.retrofitError.observe(viewLifecycleOwner) {
            if (it.status == 0) {
                showToastShort(it.message)
            } else if (it.status == 1) {
                InternetError.show(requireContext())
            }
        }
        model.errorMessage.observe(viewLifecycleOwner) {
            if (!it.isNullOrEmpty()) AlertError.show(requireContext(), it) {}
        }
        model.isLoaderVisible.observe(viewLifecycleOwner) { it ->
            binding.swipeRefreshLayout.isRefreshing=it
            Loading.showHide(requireActivity(), it)
        }
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
            (activity as MainActivity).setHeader2("", STATUS_COLOR1)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
