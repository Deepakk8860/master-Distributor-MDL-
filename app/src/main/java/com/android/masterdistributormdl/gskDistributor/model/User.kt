package com.android.masterdistributormdl.gskDistributor.model

import java.io.Serializable

data class User(
    val aadhaar_detail: AadhaarDetail,
    val alt_email: String,
    val alt_mobile: String,
    val branch_id: String,
    val company: String,
    val create_dt: String,
    val dob: String,
    val email: String,
    val territory: ArrayList<Territory>,
    val fullname: String,
    val gender: String,
    val gstin: String,
    val home_address: String,
    val home_city: String,
    val home_pincode: String,
    val home_state: String,
    val id: String,
    var video: Boolean,
    val kyc_status: KycStatus?,
    val mobile: String,
    val ofc_address: String,
    val ofc_city: String,
    val ofc_pincode: String,
    val ofc_state: String,
    val pan: String,
    val pancardimage: String,
    val profilephoto: String,
    val sameaddress: Boolean,
    val payout: Boolean,
    val usertype: String,
    val is_refer_enable: Boolean,
    val isApplyRetailer: Boolean
) : Serializable

data class KycStatus(
    val comment: String,
    val kyc: Boolean,
    var aadhaar_status: Boolean,
    var address_status: Boolean,
    var agreement_status: Boolean,
    var basic_status: Boolean,
    var pan_status: Boolean
) : Serializable

data class Territory(
    val pincode: String,
    var isChecked: Boolean
)

data class AadhaarDetail(
    val aadhaar: String,
    val aadhaarbackimage: String,
    val aadhaarfrontimage: String,
    val address: String,
    val city: String,
    val pincode: String,
    val state: String
) : Serializable