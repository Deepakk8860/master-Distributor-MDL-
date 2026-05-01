package com.android.masterdistributormdl.gskDistributor.model

data class NotificationSettingsModel(
    val message: String,
    val notification_type: List<NotificationType>,
    val status: Int
)