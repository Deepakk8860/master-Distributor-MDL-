package com.android.masterdistributormdl.model.leadstatus

data class Data(
    val activities: List<Activity>,
    val city: String,
    val client_name: String,
    val created_date: String,
    val display_name: String,
    val pincode_allowed: String,
    val email: String,
    val followup: Any,
    val followup_date: String,
    val lead_id: String,
    val lead_stage: String,
    val mobile: String,
    val notes: String,
    val opportunity_size: String,
    val opportunity_id: String,
    val pincode: String,
    val state: String,
    val whatsapp_num: String
)