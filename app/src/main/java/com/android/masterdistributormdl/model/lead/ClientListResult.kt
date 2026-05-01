package com.android.masterdistributormdl.model.lead

data class ClientListResult(
    val `data`: ArrayList<Data>,
    val message: String,
    val lead_count: String,
    val new_count: String,
    val today_count: String,
    val last_month_count: String,
    val status: Int
)