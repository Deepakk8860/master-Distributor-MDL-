package com.android.masterdistributormdl.adapter

import android.annotation.SuppressLint
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.android.masterdistributormdl.databinding.ItemLeadBinding
import com.android.masterdistributormdl.model.TaskItem

class LeadAdapter(val viewType: Int) : RecyclerView.Adapter<RecyclerView.ViewHolder>() {
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
            holder = LeadHolder(ItemLeadBinding.inflate(inflater, parent, false))
        }
        return holder!!
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is LeadHolder) holder.bindHolder(position)

    }

    inner class LeadHolder(val binding: ItemLeadBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindHolder(position: Int) {
            val item = arrayList[position] as TaskItem
            binding.taskTitle.text=item.title
            binding.taskCheckBox.isChecked=item.isChecked
        }
    }



}