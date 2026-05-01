package com.android.masterdistributormdl.model.lead

data class Data(
    val client_name: String,
    val last_activity: String,
    val lead_status: String,
    val profession: String,
    val pincode: String,
    val last_activity_icon: String,
    val is_pending_system_lead: String,
    val lead_source_type: String,
    val lead_id: String,
    val notes: String
)