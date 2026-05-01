package com.android.masterdistributormdl.model.preferedMessage

data class PreferedMessageListResult(
    val message: String,
    val messages: List<Message>,
    val status: Int
)