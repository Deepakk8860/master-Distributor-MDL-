package com.android.masterdistributormdl.gskDistributor.adapter

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.BaseAdapter
import android.widget.TextView
import com.android.masterdistributormdl.R
import com.android.masterdistributormdl.gskDistributor.utils.getHtmlSpanned


class SpinnerAdapter(val context: Context, var list: List<String>) : BaseAdapter() {
    private val inflater: LayoutInflater =
        context.getSystemService(Context.LAYOUT_INFLATER_SERVICE) as LayoutInflater

    override fun getView(position: Int, convertView: View?, parent: ViewGroup?): View {
        val view: View
        val vh: ItemHolder
        if (convertView == null) {
            view = inflater.inflate(R.layout.spinner_adapter, parent, false)
            vh = ItemHolder(view)
            view?.tag = vh
        } else {
            view = convertView
            vh = view.tag as ItemHolder
        }
        vh.name.text = getHtmlSpanned(list.get(position))


        return view
    }

    override fun getCount(): Int {
        return list.size
    }

    override fun getItem(position: Int): Any {
        return list.get(position)
    }

    override fun getItemId(position: Int): Long {
        return position.toLong()
    }

    private class ItemHolder(view: View) {
        val name: TextView

        init {
            name = view.findViewById(R.id.name)
        }
    }

}