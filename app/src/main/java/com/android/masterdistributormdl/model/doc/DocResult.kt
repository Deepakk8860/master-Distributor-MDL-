package com.android.masterdistributormdl.model.doc

data class DocResult(
    val `data`: List<Data>,
    val message: String,
    val status: Int
)