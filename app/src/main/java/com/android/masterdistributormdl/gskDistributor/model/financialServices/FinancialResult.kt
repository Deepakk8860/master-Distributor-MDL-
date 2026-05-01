package com.android.masterdistributormdl.gskDistributor.model.financialServices

import com.android.masterdistributormdl.gskDistributor.model.financialServices.Data

data class FinancialResult(
    val `data`: List<Data>,
    val message: String,
    val status: Int
)