package com.android.masterdistributormdl.gskDistributor.adapter


import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup

import android.widget.LinearLayout
import androidx.viewpager.widget.PagerAdapter
import com.android.masterdistributormdl.databinding.HomeSliderAdapterBinding
import com.android.masterdistributormdl.gskDistributor.utils.loadImage

import com.gsk.distributor.model.OfferItem

import java.util.*


class HomeSlider() : PagerAdapter() {
    var arrayList = ArrayList<Any>()
    private var clickListener: ((Any) -> Unit?)? = null

    fun setOnclickListener(clickListener: ((Any) -> Unit?)) {
        this.clickListener = clickListener
    }

    fun updateAdapter(arrayList: ArrayList<Any>) {
        this.arrayList = arrayList
        notifyDataSetChanged()
    }

    override fun getCount() = arrayList.size


    override fun isViewFromObject(view: View, `object`: Any): Boolean {
        return view === `object` as LinearLayout
    }


    override fun instantiateItem(container: ViewGroup, position: Int): Any {
        val inflater = LayoutInflater.from(container.context)
        val binding = HomeSliderAdapterBinding.inflate(inflater, container, false)
        val item = arrayList[position] as OfferItem
        loadImage(binding.image, item.home_banner)
        Objects.requireNonNull(container).addView(binding.root)
        binding.image.setOnClickListener {
            clickListener?.invoke(item)
        }
        return binding.root
    }


    override fun destroyItem(container: ViewGroup, position: Int, `object`: Any) {
        container.removeView(`object` as LinearLayout)
    }
}
