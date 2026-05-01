package com.android.masterdistributormdl.model.doc

data class Data(
    val created_at: String,
    val document_name: String,
    val document_type: String,
    val file_name: String,
    val file_url: String,
    var isShare: Boolean,
    val id: String,
    val uid: String
)