package com.android.masterdistributormdl.gskDistributor.adapter

import android.annotation.SuppressLint
import android.content.res.ColorStateList
import android.graphics.BitmapFactory
import android.net.Uri
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.lifecycle.ViewModel
import androidx.recyclerview.widget.RecyclerView
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.databinding.AdapterImagesBinding
import com.android.masterdistributormdl.databinding.AdapterNotificationListBinding
import com.android.masterdistributormdl.databinding.AdapterTrainingVideoBinding
import com.android.masterdistributormdl.databinding.AdapterVideoBinding
import com.android.masterdistributormdl.databinding.CertificateImageBinding
import com.android.masterdistributormdl.gskDistributor.model.NotificationType
import com.gsk.distributor.model.*
import com.android.masterdistributormdl.gskDistributor.utils.fetchThumbnail
import com.android.masterdistributormdl.gskDistributor.utils.loadImage


class NewAdapter(val model: ViewModel, var viewType: Int) :
    RecyclerView.Adapter<RecyclerView.ViewHolder>() {
    var arrayList = ArrayList<Any>()
    var selected = -1
    private var clickListener: ((Any) -> Unit?)? = null

    fun setOnclickListener(clickListener: ((Any) -> Unit?)) {
        this.clickListener = clickListener
    }



    private var clickListener2: ((Any) -> Unit?)? = null
    fun setOnSwitchStateChangeListener(listener: (Any) -> Unit?) {
        this.clickListener2 = listener
    }

    private var originalList = ArrayList<Any>()



    override fun getItemViewType(position: Int): Int {
        return viewType
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter() {
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter(arrayList: java.util.ArrayList<Any>) {
        this.arrayList = arrayList
        notifyDataSetChanged()
    }

    @SuppressLint("NotifyDataSetChanged")
    fun updateAdapter1(arrayList: ArrayList<Any>) {
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
            holder = PromotionalViewHolder(AdapterImagesBinding.inflate(inflater, parent, false))
        }
        else if (viewType == 2) {
            holder = PromotionalVideoViewHolder(AdapterVideoBinding.inflate(inflater, parent, false))
        }
        else if (viewType == 3) {
            holder = NotificationSettingsViewHolder(AdapterNotificationListBinding.inflate(inflater, parent, false))
        }
         else if (viewType == 4) {
             holder = TrainingVideoViewHolder(AdapterTrainingVideoBinding.inflate(inflater, parent, false))
         }
         else if (viewType == 22) {
             holder = CertificateHolder(CertificateImageBinding.inflate(inflater, parent, false))
         }
        return holder!!
    }

    override fun onBindViewHolder(holder: RecyclerView.ViewHolder, position: Int) {
        if (holder is PromotionalViewHolder) holder.bindHolder(position)
        else if (holder is PromotionalVideoViewHolder) holder.bindHolder(position)
        else if (holder is NotificationSettingsViewHolder) holder.bindHolder(position)
        else if (holder is TrainingVideoViewHolder) holder.bindHolder(position)
        else if (holder is CertificateHolder) holder.bindHolder(position)
    }

    inner class CertificateHolder(val binding: CertificateImageBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindHolder(position: Int) {
            val item = arrayList[position] as Images
            val bitmap = BitmapFactory.decodeFile(item.path)
            binding.image.setImageBitmap(bitmap)
        }
    }


    inner class PromotionalViewHolder(val binding: AdapterImagesBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindHolder(position: Int) {
            val item = arrayList[position] as PromotionalImagesData
            loadImage(binding.circularImageView,item.link)
            binding.titleTextView.text=item.title
            binding.circularImageView.setOnClickListener {
                clickListener?.invoke(item)
            }
        }
    }

    inner class PromotionalVideoViewHolder(val binding: AdapterVideoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindHolder(position: Int) {
            val item = arrayList[position] as PromotionalImagesData
            // Play video
            val uri = Uri.parse(item.link)
            fetchThumbnail(item.link,binding.ivThubnail)
            binding.playButton.setOnClickListener {
                clickListener?.invoke(item)
            }

        }
    }


    inner class NotificationSettingsViewHolder(val binding: AdapterNotificationListBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindHolder(position: Int) {
            val item = arrayList[position] as NotificationType
            binding.title.text=item.type

            if (item.push=="Y"){
                binding.switchPush.isEnabled=true
            }else{
                binding.switchPush.isEnabled=false
            }

            if (item.email=="Y"){
                binding.switchEmail.isEnabled=true
            }else{
                binding.switchEmail.isEnabled=false
            }

            if (item.pushnotify=="Y"){
                binding.switchPush.thumbTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.switchPush.context, R.color.switch_on_thumb_color))
                binding.switchPush.trackTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.switchPush.context, R.color.switch_on_track_color))
                binding.switchPush.isChecked=true
            }else{
                binding.switchPush.isChecked=false
            }


            if (item.emailnotify=="Y"){
                binding.switchPush.thumbTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.switchPush.context, R.color.switch_on_thumb_color))
                binding.switchPush.trackTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.switchPush.context, R.color.switch_on_track_color))
                binding.switchEmail.isChecked=true
            }else{
                binding.switchEmail.isChecked=false
            }
            // Listener for SwitchCompat 1
            binding.switchPush.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // Change the color of the thumb and track when the switch is checked
                    binding.switchPush.thumbTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.switchPush.context, R.color.switch_on_thumb_color))
                    binding.switchPush.trackTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.switchPush.context, R.color.switch_on_track_color))
                } else {
                    // Change the color of the thumb and track when the switch is unchecked
                    binding.switchPush.thumbTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.switchPush.context, R.color.switch_off_thumb_color))
                    binding.switchPush.trackTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.switchPush.context, R.color.switch_off_track_color))
                }
                val state = mapOf(
                    "item" to item,
                    "switch1State" to isChecked,
                    "switch2State" to binding.switchEmail.isChecked
                )
                clickListener2?.invoke(state)
            }

            // Listener for SwitchCompat 2
            binding.switchEmail.setOnCheckedChangeListener { _, isChecked ->
                if (isChecked) {
                    // Change the color of the thumb and track when the switch is checked
                    binding.switchEmail.thumbTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.switchPush.context, R.color.switch_on_thumb_color))
                    binding.switchEmail.trackTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.switchPush.context, R.color.switch_on_track_color))
                } else {
                    // Change the color of the thumb and track when the switch is unchecked
                    binding.switchEmail.thumbTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.switchPush.context, R.color.switch_off_thumb_color))
                    binding.switchEmail.trackTintList = ColorStateList.valueOf(ContextCompat.getColor(binding.switchPush.context, R.color.switch_off_track_color))
                }
                val state = mapOf(
                    "item" to item,
                    "switch1State" to binding.switchPush.isChecked,
                    "switch2State" to isChecked
                )
                clickListener2?.invoke(state)
            }
        }
    }


    inner class TrainingVideoViewHolder(val binding: AdapterTrainingVideoBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindHolder(position: Int) {
            val item = arrayList[position] as TrainingVideoListData
//            fetchThumbnail(item.url,binding.ivVideoBanner)
            binding.txtTitle.text=item.title
            loadImage(binding.ivVideoBanner,item.thumbnail)
            binding.ivPlay.setOnClickListener {
                clickListener?.invoke(item)
            }

        }
    }


}