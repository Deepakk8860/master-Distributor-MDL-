package com.android.masterdistributormdl.adapter

import android.annotation.SuppressLint
import android.graphics.BitmapFactory
import android.graphics.Color
import android.text.Html
import android.util.Log
import android.view.Gravity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.os.bundleOf
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.android.masterdistributormdl.databinding.BottomAdapterBinding
import com.android.masterdistributormdl.databinding.ItemPincodeBinding
import com.android.masterdistributormdl.databinding.ManageClientAdapterBinding
import com.android.masterdistributormdl.databinding.ManageClientBinding
import com.android.masterdistributormdl.databinding.ManageFollowUpAdapterBinding
import com.android.masterdistributormdl.databinding.TimelineItemBinding
import com.android.masterdistributormdl.gskDistributor.model.Territory
import com.android.masterdistributormdl.main.MainModel
import com.android.masterdistributormdl.model.BottomMenu
import com.android.masterdistributormdl.model.LeadStageData
import com.android.masterdistributormdl.model.lead.Data
import com.android.masterdistributormdl.model.leadstatus.Activity
import com.android.masterdistributormdl.model.leadstatus.LeadStatusDetailsResult
import com.android.masterdistributormdl.utils.SharedPreference
import com.android.masterdistributormdl.utils.capitalizeWords
import com.android.masterdistributormdl.utils.loadImage
import com.android.masterdistributormdl.utils.loadImageWithCoil
import com.android.masterdistributormdl.utils.loadSvg
import com.android.masterdistributormdl.utils.setImageApp

import java.util.Random


class HomeAdapter(val model: ViewModel?, var viewType: Int) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var arrayList = ArrayList<Any>()
    var selected = -1
    val sharedPreference = SharedPreference()

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
            holder = BottomHolder(BottomAdapterBinding.inflate(inflater, parent, false))
        }else if (viewType == 2) {
            holder = TimeLineHolder(TimelineItemBinding.inflate(inflater, parent, false))
        }else if (viewType == 3) {
            holder = ClientListHolder(ManageClientAdapterBinding.inflate(inflater, parent, false))
        }else if (viewType == 4) {
            holder = FollowLeadListHolder(ManageFollowUpAdapterBinding.inflate(inflater, parent, false))
        }
        return holder!!
    }


    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is BottomHolder) holder.bindHolder(position)
        else if (holder is TimeLineHolder) holder.bindHolder(position)
        else if (holder is ClientListHolder) holder.bindHolder(position)
        else if (holder is FollowLeadListHolder) holder.bindHolder(position)

    }


    inner class BottomHolder(val binding: BottomAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindHolder(position: Int) {
            val item = arrayList[position] as BottomMenu
            val count = (model as MainModel).notiCount
            if (count > 0 && position == 1) {
                binding.count.text = "$count"
                binding.count.visibility = View.VISIBLE
            } else {
                binding.count.text = ""
                binding.count.visibility = View.GONE
            }
            binding.name.text = item.name
            if (selected == item.id) {
                binding.image.setImageApp(item.icon + "1")
                binding.name.setTextColor(Color.parseColor("#F86202"))
            } else {
                binding.image.setImageApp(item.icon)
                binding.name.setTextColor(Color.parseColor("#FFFFFF"))
            }
            binding.click.setOnClickListener {
                selected = item.id
                updateAdapter()
                clickListener?.invoke(item)
            }
        }
    }


    inner class TimeLineHolder(val binding: TimelineItemBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindHolder(position: Int) {
            val item = arrayList[position] as Activity
            binding.txtDateTime.text=item.last_activity_ago
            binding.title.text=item.remark
            binding.description.text=item.sub_remark
//            loadImageWithCoil(binding.icon,item.last_activity_icon)
            binding.icon.loadSvg(binding.icon.context,item.last_activity_icon)
            val lastPosition = arrayList.size - 1

            if (position==lastPosition){
                binding.line.visibility=View.GONE
            }else{
                binding.line.visibility=View.VISIBLE
            }

            binding.click.setOnClickListener {
                selected = position
                clickListener?.invoke(item)
            }
        }
    }


    inner class ClientListHolder(val binding: ManageClientAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindHolder(position: Int) {
            val item = arrayList[position] as Data
            binding.nameTxt.text = item.client_name.capitalizeWords()
            binding.txtDes.text=item.last_activity
            binding.txtStatus.text=item.lead_status
            binding.txtPincode.text= Html.fromHtml("<b>PinCode:</b> ${item.pincode}", Html.FROM_HTML_MODE_LEGACY)
            if (item.profession.isNotEmpty()){
                binding.txtInvestmentSize.visibility=View.VISIBLE
                binding.txtInvestmentSize.text=Html.fromHtml("<b>Investment Size:</b> ${item.profession}", Html.FROM_HTML_MODE_LEGACY)
            }
            val rnd = Random()
            val color1 = Color.argb(200, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
            DrawableCompat.setTint(binding.logoTxt.background, color1)
//            val names = item.name!!.split(" ")
            val names = item.client_name.trim().split("\\s+".toRegex()) ?: emptyList()
            if (names.isNotEmpty() && names[0].isNotEmpty()) {
                var name = names[0].take(1) // Safely get the first character
                if (names.size > 1 && names[1].isNotEmpty()) name += names[1].take(1)
                binding.logoTxt.text = name
            } else {
                binding.logoTxt.text = ""
            }
            if (item.notes.isNotEmpty()){
                binding.txtNotes.visibility= View.VISIBLE
                binding.txtNotes.text=item.notes
            }else{
                binding.txtNotes.visibility= View.GONE
            }


//            loadImage(binding.ivDesIcon,item.last_activity_icon)
            binding.ivDesIcon.loadSvg(binding.ivDesIcon.context,item.last_activity_icon)
            binding.click.setOnClickListener {
                selected = position
                clickListener?.invoke(item)
            }

        }
    }


    inner class FollowLeadListHolder(val binding: ManageFollowUpAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindHolder(position: Int) {
            val item = arrayList[position] as Data
            binding.ivDesIcon.visibility=View.GONE
            binding.nameTxt.text = item.client_name.capitalizeWords()
            binding.txtDes.text=item.last_activity
            binding.txtStatus.text=item.lead_status
            binding.txtPincode.text= Html.fromHtml("<b>PinCode:</b> ${item.pincode}", Html.FROM_HTML_MODE_LEGACY)
            if (item.profession.isNotEmpty()){
                binding.txtInvestmentSize.visibility=View.VISIBLE
                binding.txtInvestmentSize.text=Html.fromHtml("<b>Investment Size:</b> ${item.profession}", Html.FROM_HTML_MODE_LEGACY)
            }
            val rnd = Random()
            val color1 = Color.argb(200, rnd.nextInt(256), rnd.nextInt(256), rnd.nextInt(256))
            DrawableCompat.setTint(binding.logoTxt.background, color1)
//            val names = item.name!!.split(" ")
            val names = item.client_name.trim().split("\\s+".toRegex()) ?: emptyList()
            if (names.isNotEmpty() && names[0].isNotEmpty()) {
                var name = names[0].take(1) // Safely get the first character
                if (names.size > 1 && names[1].isNotEmpty()) name += names[1].take(1)
                binding.logoTxt.text = name
            } else {
                binding.logoTxt.text = ""
            }

            binding.click.setOnClickListener {
                selected = position
                clickListener?.invoke(item)
            }

        }
    }
}