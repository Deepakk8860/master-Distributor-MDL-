package com.android.masterdistributormdl.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.masterdistributormdl.databinding.DropAdapterBinding
import com.android.masterdistributormdl.model.LeadStageData
import com.android.masterdistributormdl.utils.loadImage
import com.android.masterdistributormdl.utils.loadImageWithCoil

class DroupAdapter(val viewType: Int) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var arrayList = ArrayList<Any>()
    var selected = -1
    private var clickListener: ((Any) -> Unit)? = null
    private var originalList = ArrayList<Any>()
    fun setOnclickListener(clickListener: ((Any) -> Unit)) {
        this.clickListener = clickListener
    }

    override fun getItemViewType(position: Int): Int {

        return viewType

    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter() {
        notifyDataSetChanged()
    }

    fun clearSearch() {
        arrayList.clear()
        arrayList.addAll(originalList)
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(arrayList: ArrayList<Any>) {
        this.arrayList = arrayList
        notifyDataSetChanged()
    }
    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapterSearch(arrayList: ArrayList<Any>) {
        originalList.clear()
        this.arrayList = arrayList
        originalList.addAll(arrayList)
        notifyDataSetChanged()
    }

    override fun getItemCount() = arrayList.size

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecyclerView.ViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        var holder: RecyclerView.ViewHolder? = null
        if (viewType == 1) {
            holder = DroupHolder1(DropAdapterBinding.inflate(inflater, parent, false))
        } else if (viewType == 2) {
            holder = DroupHolder2(DropAdapterBinding.inflate(inflater, parent, false))
        }
        else if (viewType == 3) {
            holder = DroupHolder3(DropAdapterBinding.inflate(inflater, parent, false))
        } else if (viewType == 4) {
            holder = DroupHolder4(DropAdapterBinding.inflate(inflater, parent, false))
        }
        return holder!!
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is DroupHolder1) holder.bindHolder(position)
       else if (holder is DroupHolder2) holder.bindHolder(position)
       else if (holder is DroupHolder3) holder.bindHolder(position)
       else if (holder is DroupHolder4) holder.bindHolder(position)
    }




    inner class DroupHolder1(val binding: DropAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindHolder(position: Int) {
            val item = arrayList[position] as LeadStageData
            binding.name.text = item.name

            if (item.icon.isNotEmpty()){
                binding.ivIcon.visibility=View.VISIBLE
//                loadImage(binding.ivIcon,item.icon)
                loadImageWithCoil(binding.ivIcon,item.icon)
            }else{
                binding.ivIcon.visibility=View.GONE
            }

            binding.clik.setOnClickListener {
                clickListener?.invoke(item)
            }
        }
    }


    inner class DroupHolder3(val binding: DropAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindHolder(position: Int) {
            val item = arrayList[position] as LeadStageData
            binding.name.text = item.name

            if (item.icon.isNotEmpty()){
                binding.ivIcon.visibility=View.VISIBLE
                loadImage(binding.ivIcon,item.icon)
            }else{
                binding.ivIcon.visibility=View.GONE
            }

            binding.clik.setOnClickListener {
                clickListener?.invoke(item)
            }
        }
    }

    inner class DroupHolder4(val binding: DropAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindHolder(position: Int) {
            val item = arrayList[position] as LeadStageData
            binding.name.text = item.name
            binding.ivIcon.visibility=View.GONE

            binding.clik.setOnClickListener {
                clickListener?.invoke(item)
            }
        }
    }

    inner class DroupHolder2(val binding: DropAdapterBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindHolder(position: Int) {
            val item = arrayList[position] as LeadStageData
            binding.name.text = item.name
            binding.ivIcon.visibility=View.VISIBLE
            binding.ivIcon.setImageResource(item.icon.toInt())
            binding.clik.setOnClickListener {
                clickListener?.invoke(item)
            }
        }
    }


}