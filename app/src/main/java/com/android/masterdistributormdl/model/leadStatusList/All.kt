package com.android.masterdistributormdl.model.leadStatusList

data class All(
    val client_name: String,
    val created_date: String,
    val is_pending_system_lead: Int,
    val last_activity: String,
    val last_activity_icon: String,
    val lead_id: String,
    val lead_source_type: String,
    val lead_status: String,
    val notes: String,
    val pincode: String,
    val profession: String
)