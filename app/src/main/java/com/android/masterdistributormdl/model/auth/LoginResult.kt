package com.android.masterdistributormdl.model.auth

data class LoginResult(
    val `data`: Data,
    val message: String,
    val status: Int
)