package com.android.masterdistributormdl.model.territory

data class PincodeResult(
    val message: String,
    val status: Int,
    val territory: List<Territory>
)