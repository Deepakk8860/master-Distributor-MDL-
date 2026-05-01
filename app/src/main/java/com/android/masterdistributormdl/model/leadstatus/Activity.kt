package com.android.masterdistributormdl.model.leadstatus

data class Activity(
    val created_on: String,
    val activity_id: String,
    val last_activity_ago: String,
    val last_activity_icon: String,
    val remark: String,
    val sub_remark: String,
    val type: String
)