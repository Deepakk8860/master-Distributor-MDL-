package com.android.masterdistributormdl.addLead


import android.content.Intent
import android.os.Build
import android.os.Bundle
import android.util.Log

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.RequiresApi
import androidx.appcompat.widget.AppCompatImageView
import androidx.appcompat.widget.AppCompatTextView
import androidx.collection.emptyLongSet
import androidx.core.content.ContextCompat
import androidx.core.os.bundleOf
import androidx.core.widget.addTextChangedListener

import androidx.databinding.DataBindingUtil
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.DividerItemDecoration
import androidx.recyclerview.widget.RecyclerView
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.adapter.DroupAdapter
import com.android.masterdistributormdl.databinding.AddClientBinding
import com.android.masterdistributormdl.databinding.CreatePaymentLinkBinding
import com.android.masterdistributormdl.databinding.CustomMessageSharingBinding
import com.android.masterdistributormdl.gskDistributor.model.Territory
import com.android.masterdistributormdl.gskDistributor.utils.SuccessAlert
import com.android.masterdistributormdl.home.Home

import com.android.masterdistributormdl.home.HomeModel
import com.android.masterdistributormdl.main.MainActivity
import com.android.masterdistributormdl.model.LeadStageData
import com.android.masterdistributormdl.utils.AlertError
import com.android.masterdistributormdl.utils.InternetError
import com.android.masterdistributormdl.utils.Loading
import com.android.masterdistributormdl.utils.STATUS_COLOR1
import com.android.masterdistributormdl.utils.addFragment
import com.android.masterdistributormdl.utils.checkMobileNo
import com.android.masterdistributormdl.utils.checkNullorEmpty
import com.android.masterdistributormdl.utils.clearAllEditTextFocus
import com.android.masterdistributormdl.utils.clearAllEditTextFocusError
import com.android.masterdistributormdl.utils.followUpDate
import com.android.masterdistributormdl.utils.getFormattedDate
import com.android.masterdistributormdl.utils.hideSoftKeyBoard
import com.android.masterdistributormdl.utils.loadImage
import com.android.masterdistributormdl.utils.replaceFragment
import com.android.masterdistributormdl.utils.scrollToPosition
import com.android.masterdistributormdl.utils.scrollToPosition1
import com.android.masterdistributormdl.utils.setEditText
import com.android.masterdistributormdl.utils.setError2
import com.android.masterdistributormdl.utils.shooterFragment
import com.android.masterdistributormdl.utils.showToastShort
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.gson.Gson
import com.google.gson.JsonArray
import com.google.gson.JsonObject
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext


class PreferedMessageSharing : Fragment() {
    private lateinit var binding: CustomMessageSharingBinding
    lateinit var model: HomeModel

    override fun onCreateView(
        inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?
    ): View {
        binding =
            DataBindingUtil.inflate(inflater, R.layout.custom_message_sharing, container, false)
        model = ViewModelProvider(this)[HomeModel::class.java]
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
        initListener()
        getPreferedMessage()
    }

    private fun initListener() {
        binding.llCustom.setOnClickListener {
            openBottomSheetMessage()
        }

        binding.backButton.setOnClickListener {
            requireActivity().onBackPressed()
        }
    }

    private fun openBottomSheetMessage() {
        val dialog = BottomSheetDialog(requireContext(), R.style.TransparentBottomSheet)
        val view = layoutInflater.inflate(R.layout.bottom_sheet_notes, null)
        val edtNotes = view.findViewById<EditText>(R.id.edtNotes)
        val title = view.findViewById<TextView>(R.id.tvTitle)
        title.text="Custom Sharing Message"
        edtNotes.hint="Type message..."
        edtNotes.requestFocus()
        val btnSave = view.findViewById<Button>(R.id.btnSave)
        btnSave.text="SHARE"
        btnSave.setOnClickListener {
            dialog.dismiss()
            shareText(edtNotes.text.toString())
        }


        dialog.setContentView(view)

        dialog.show()

        view.findViewById<ImageView>(R.id.ivClose).setOnClickListener {
            dialog.dismiss()
        }

    }

    private fun shareText(message: String) {
        val intent = Intent().apply {
            action = Intent.ACTION_SEND
            putExtra(Intent.EXTRA_TEXT, message)
            type = "text/plain"
        }
        startActivity(Intent.createChooser(intent, "Share via"))
    }

    private fun getPreferedMessage() {
        model.getPreferedMessageList {
            if (it.status == 0) {
                model.sharingListAdapter.updateAdapter(it.messages as ArrayList<Any>)
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


