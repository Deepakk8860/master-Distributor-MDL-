package com.android.masterdistributormdl.adapter

import android.annotation.SuppressLint
import android.content.Intent
import android.graphics.BitmapFactory
import android.graphics.Color
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.android.masterdistributormdl.addLead.PreferedMessageSharing
import com.android.masterdistributormdl.databinding.AdapterDocBinding
import com.android.masterdistributormdl.databinding.AdapterSharingBinding
import com.android.masterdistributormdl.databinding.BottomAdapterBinding
import com.android.masterdistributormdl.databinding.ItemPincodeBinding
import com.android.masterdistributormdl.databinding.ManageClientAdapterBinding
import com.android.masterdistributormdl.databinding.ManageClientBinding
import com.android.masterdistributormdl.databinding.ManageFollowUpAdapterBinding
import com.android.masterdistributormdl.databinding.TimelineItemBinding
import com.android.masterdistributormdl.gskDistributor.model.Territory
import com.android.masterdistributormdl.home.HomeModel
import com.android.masterdistributormdl.main.MainModel
import com.android.masterdistributormdl.model.BottomMenu
import com.android.masterdistributormdl.model.LeadStageData
import com.android.masterdistributormdl.model.doc.DocResult
import com.android.masterdistributormdl.model.lead.Data
import com.android.masterdistributormdl.model.leadstatus.Activity
import com.android.masterdistributormdl.model.leadstatus.LeadStatusDetailsResult
import com.android.masterdistributormdl.model.preferedMessage.Message
import com.android.masterdistributormdl.model.preferedMessage.PreferedMessageListResult
import com.android.masterdistributormdl.utils.SharedPreference
import com.android.masterdistributormdl.utils.capitalizeWords
import com.android.masterdistributormdl.utils.loadImage
import com.android.masterdistributormdl.utils.loadImageWithCoil
import com.android.masterdistributormdl.utils.loadSvg
import com.android.masterdistributormdl.utils.setImageApp

import java.util.Random


class PincodeAdapter(val model: HomeModel?, var viewType: Int) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var arrayList = ArrayList<Any>()
    var selected = -1
    val sharedPreference = SharedPreference()
    private var selectedPosition = -1
    private var clickListener: ((Any) -> Unit?)? = null

    private var clickListener2: ((Territory) -> Unit)? = null

    fun setOnClickListener(listener: (Territory) -> Unit) {
        clickListener2 = listener
    }

    fun setOnclickListener(clickListener: ((Any) -> Unit?)) {
        this.clickListener = clickListener
    }

    override fun getItemViewType(position: Int): Int {
        return viewType
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter() {
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(arrayList: ArrayList<Any>) {
        this.arrayList = arrayList
        notifyDataSetChanged()
    }

    override fun getItemCount() = arrayList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        var holder: RecyclerView.ViewHolder? = null
         if (viewType == 1) {
            holder = PinCodeListHolder(ItemPincodeBinding.inflate(inflater, parent, false))
        } else if (viewType == 2) {
            holder = SharingHolder(AdapterSharingBinding.inflate(inflater, parent, false))
        }else if (viewType == 3) {
            holder = DocSharingHolder(AdapterDocBinding.inflate(inflater, parent, false))
        }
        return holder!!
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
         if (holder is PinCodeListHolder) holder.bindHolder(position)
         else if (holder is SharingHolder) holder.bindHolder(position)
         else if (holder is DocSharingHolder) holder.bindHolder(position)

    }



    inner class PinCodeListHolder(val binding: ItemPincodeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindHolder(position: Int) {
            val item = arrayList[position] as Territory
            binding.textPincode.text=item.pincode
            model!!.pincodeSelectedList.clear()
            binding.checkBoxPinCode.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked){
                    model.pincodeSelectedList.add(item.pincode)
                }else{
                    model!!.pincodeSelectedList.remove(item.pincode)
                }
            }

        }
    }

    inner class SharingHolder(val binding: AdapterSharingBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindHolder(position: Int) {
            val item = arrayList[position] as Message
            binding.txtPreferredMsg.text=item.message
            // Set checkbox state based on selectedPosition
            binding.checkBox.isChecked = selectedPosition == position

            // Click listener for checkbox
            binding.checkBox.setOnClickListener {
                val oldPosition = selectedPosition
                selectedPosition = if (selectedPosition == position) -1 else position
                notifyItemChanged(oldPosition)
                notifyItemChanged(position)

                // Share the selected message text if checkbox is checked
                if (binding.checkBox.isChecked) {
                    shareText(item.message)
                }
            }

            // Optional: Whole layout click also selects checkbox
            binding.click.setOnClickListener {
                binding.checkBox.performClick()
            }

        }

        private fun shareText(message: String) {
            val intent = Intent().apply {
                action = Intent.ACTION_SEND
                putExtra(Intent.EXTRA_TEXT, message)
                type = "text/plain"
            }
            binding.txtPreferredMsg.context.startActivity(Intent.createChooser(intent, "Share via"))
        }
    }


    inner class DocSharingHolder(val binding: AdapterDocBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bindHolder(position: Int) {
            val item = arrayList[position] as com.android.masterdistributormdl.model.doc.Data
            binding.txtFileName.text=item.document_name

            binding.ivShare.setOnClickListener {
                item.isShare=true
                selected = position
                clickListener?.invoke(item)
            }

            binding.ivPreview.setOnClickListener {
                item.isShare=false
                selected = position
                clickListener?.invoke(item)
            }
        }

    }
}