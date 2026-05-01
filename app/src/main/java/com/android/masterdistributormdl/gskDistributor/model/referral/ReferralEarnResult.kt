package com.android.masterdistributormdl.gskDistributor.model.referral

data class ReferralEarnResult(
    val `data`: ArrayList<Data>,
    val earning: String,
    val message: String,
    val status: Int
)