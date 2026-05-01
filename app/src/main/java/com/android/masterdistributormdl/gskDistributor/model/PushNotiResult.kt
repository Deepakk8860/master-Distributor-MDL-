package com.gsk.distributor.model

import java.io.Serializable

data class PushNotiResult(
    val `data`: ArrayList<PushItem>, val message: String, val status: Int
) : Serializable

data class PushItem(
    val create_dt: String,
    val id: String,
    val message: String,
    var status: Int,
    val type: String,
    val type_id: String,
    val type_status: String
) :  Serializable