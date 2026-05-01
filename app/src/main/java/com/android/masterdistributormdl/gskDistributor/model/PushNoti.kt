package com.android.masterdistributormdl.gskDistributor.model

data class PushNoti(
    val event: String?,
    val userdata: PushData?
)

data class PushData(
    val event: String?, val id: String?
)