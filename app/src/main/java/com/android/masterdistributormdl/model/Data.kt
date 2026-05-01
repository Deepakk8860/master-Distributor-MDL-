package com.android.masterdistributormdl.model

import java.io.Serializable

data class ErrorAlert(val status: Int, val message: String)
data class TaskItem(val title: String, var isChecked: Boolean = false)

data class BottomMenu(
    val id: Int, val name: String, val icon: String
) : Serializable

data class ApiResponse(
    val status: Int, val message: String
) : Serializable


data class UserResult(
    val `data`: User, val message: String, val version: Boolean,val status: Int
) : Serializable


data class User(
    val id: String,
    val branch_id: String,
    val address: String,
    val city: String,
    val district: String,
    var email: String,
    val fullname: String,
    val profilephoto: String,
    val landmark: String,
    val mobile: String,
    val pincode: String,
    val state: String,
    val aadhaar: String,
    var isApplyDist: Boolean,
    val isApproveDist: Boolean,
    var isApplyRetail: Boolean,
    var isApproveRetail: Boolean,
    var isApplyQR: Boolean,
    val isApproveQR: Boolean,
    val parent_id: String,
    val pan: String,
    val shopid: String,
    val gstin: String,
    var is_rate: Boolean,
    val gateway: String?
) : Serializable


data class LeadStageResult(
    val status: Int,
    val message: String,
    val data: ArrayList<LeadStageData>
)

data class AddLeadResult(
    val status: Int,
    val message: String,
    val data: AddLeadDataResult
)

data class FollowUpListCount(
    val status: Int,
    val data: FollowUpListCountData
)

data class FollowUpListCountData(
    val overdue: Int,
    val today: String,
    val upcoming: String,
    val manual: String,
    val system_total: String,
    val manual_total: String,
    val system: String,
    val someday: String
) : Serializable

data class LeadSummaryStats(
    val status: Int,
    val data: LeadSummaryStatsData
)

data class LeadSummaryStatsData(
    val leads_assigned: String,
    val leads_contacted: String,
    val new_leads: String,
    val response_time: String
) : Serializable

data class ActivityDetailsResult(
    val status: Int,
    val message: String,
    val data: ActivityDetailsData
)

data class ActivityDetailsData(
    val activity_id: String,
    val lead_id: String,
    val type: String,
    val remark: String,
    val sub_remark: String,
    val activity_time: String,
    val create_dt: String,
) : Serializable



data class AddLeadDataResult(
    val lead_id: String
) : Serializable

data class LeadStageData(
    val name: String, val icon: String,val id: String,
    val pincode_count: String="",
    val type:String=""
) : Serializable