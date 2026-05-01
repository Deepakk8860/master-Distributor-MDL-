package com.android.masterdistributormdl.model.profile

data class AadhaarDetail(
    val aadhaar: String,
    val aadhaarbackimage: String,
    val aadhaarfrontimage: String,
    val address: String,
    val city: String,
    val pincode: String,
    val state: String
)