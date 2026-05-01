package com.android.masterdistributormdl.gskDistributor.model

data class NotificationType(
    val email: String="",
    val emailnotify: String="",
    val id: String="",
    val push: String="",
    val pushnotify: String="",
    val type: String=""
)

data class NotifyKeys(val push: String, val email: String)