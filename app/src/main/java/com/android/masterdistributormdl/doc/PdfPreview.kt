package com.android.masterdistributormdl.doc

import android.annotation.SuppressLint
import android.app.Activity
import android.content.Context
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
import android.webkit.WebChromeClient
import android.webkit.WebView
import android.webkit.WebViewClient
import android.widget.Toast
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.annotation.RequiresApi
import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.DocSharingBinding
import com.android.masterdistributormdl.databinding.PdfPreviewBinding
import com.android.masterdistributormdl.home.HomeModel
import com.android.masterdistributormdl.main.MainActivity
import com.android.masterdistributormdl.model.doc.Data
import com.android.masterdistributormdl.utils.AlertError
import com.android.masterdistributormdl.utils.InternetError
import com.android.masterdistributormdl.utils.Loading
import com.android.masterdistributormdl.utils.STATUS_COLOR1
import com.android.masterdistributormdl.utils.shooterFragment
import com.android.masterdistributormdl.utils.showToastShort
import com.google.gson.JsonObject
import java.io.File
import java.io.FileOutputStream
import java.net.URL

class PdfPreview : Fragment() {
    private lateinit var binding: PdfPreviewBinding
    lateinit var model: DocModel
    private lateinit var pdfPickerLauncher: ActivityResultLauncher<Intent>
    private var pdfUrl=""

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding = DataBindingUtil.inflate(inflater, R.layout.pdf_preview, container, false)
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
    }

    private fun initListener() {
        binding.backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }

        binding.relShareLayout.setOnClickListener {
            sharePdfUrl(pdfUrl)
        }
    }


    private fun sharePdfUrl(pdfUrl: String) {
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
        pdfUrl = requireArguments().getString("pdfUrl") ?: ""
        previewPdfFromUrl(pdfUrl)
    }

    private fun previewPdfFromUrl(pdfUrl: String) {
        downloadPdfToFile(requireContext(), pdfUrl) { file ->
            if (file != null && file.exists()) {
                binding.pdfView.fromFile(file)
                    .enableSwipe(true)
                    .enableDoubletap(true)
                    .defaultPage(0)
                    .spacing(10)
                    .load()
            } else {
                Toast.makeText(requireContext(), "Failed to load PDF", Toast.LENGTH_SHORT).show()
            }
        }
    }

    fun downloadPdfToFile(context: Context, pdfUrl: String, onDownloaded: (File?) -> Unit) {
        Thread {
            try {
                val url = URL(pdfUrl)
                val connection = url.openConnection()
                connection.connect()

                val inputStream = connection.getInputStream()
                val file = File(context.cacheDir, "downloaded_preview.pdf")

                val outputStream = FileOutputStream(file)
                inputStream.copyTo(outputStream)

                outputStream.close()
                inputStream.close()

                (context as Activity).runOnUiThread {
                    onDownloaded(file)
                }
            } catch (e: Exception) {
                e.printStackTrace()
                (context as Activity).runOnUiThread {
                    onDownloaded(null)
                }
            }
        }.start()
    }


//    private fun initView() {
//        val pdfUrl = "https://diststage.gstsuvidhakendra.org.in//upload_files//share_user_doc//509_sharedocument_1942175837685e57e314ca7.pdf"
//        val googleDocsUrl = "https://docs.google.com/gview?embedded=true&url=$pdfUrl"
//
//        binding.pdfWebView.settings.javaScriptEnabled = true
//        binding.pdfWebView.webChromeClient = WebChromeClient()
//        binding.pdfWebView.webViewClient = object : WebViewClient() {
//            override fun onPageStarted(view: WebView?, url: String?, favicon: android.graphics.Bitmap?) {
//                binding.progressBar.visibility = View.VISIBLE
//            }
//
//            override fun onPageFinished(view: WebView?, url: String?) {
//                binding.progressBar.visibility = View.GONE
//            }
//        }
//
//        binding.pdfWebView.loadUrl(googleDocsUrl)
//    }


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
            (activity as MainActivity).setHeader("", STATUS_COLOR1)
        } catch (e: Exception) {
            e.printStackTrace()
        }
    }
}
