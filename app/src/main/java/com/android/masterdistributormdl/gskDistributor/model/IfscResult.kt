package com.android.masterdistributormdl.gskDistributor.model

import com.android.masterdistributormdl.gskDistributor.model.BankData

data class IfscResult(
    val bank_data: BankData,
    val message: String,
    val status: Int
)