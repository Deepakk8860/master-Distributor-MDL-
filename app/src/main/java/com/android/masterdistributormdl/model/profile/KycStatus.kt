package com.android.masterdistributormdl.model.profile

data class KycStatus(
    val aadhaar_status: Boolean,
    val address_status: Boolean,
    val agreement_status: Boolean,
    val basic_status: Boolean,
    val comment: String,
    val is_crm: Boolean,
    val kyc: Boolean,
    val pan_status: Boolean
)