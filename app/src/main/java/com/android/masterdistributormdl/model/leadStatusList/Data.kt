package com.android.masterdistributormdl.model.leadStatusList

import com.android.masterdistributormdl.model.lead.Data

data class Data(
    val all: ArrayList<Data>,
    val current_month: ArrayList<Data>,
    val last_month: ArrayList<Data>,
    val new: ArrayList<Data>,
    val today: ArrayList<Data>
)