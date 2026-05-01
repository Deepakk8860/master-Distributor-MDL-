package com.android.masterdistributormdl.model.leadStatusList

data class LeadStatusFilterListResult(
    val current_month_count: Int,
    val `data`: Data,
    val last_month_count: Int,
    val message: String,
    val new_count: Int,
    val status: Int,
    val today_count: Int,
    val total_count: Int
)