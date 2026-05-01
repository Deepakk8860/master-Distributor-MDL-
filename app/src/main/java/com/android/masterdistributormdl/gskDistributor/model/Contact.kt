package com.android.masterdistributormdl.gskDistributor.model

import android.widget.ImageView
import com.bumptech.glide.Glide
import com.bumptech.glide.request.RequestOptions

import com.google.gson.JsonArray
import com.google.gson.JsonObject
import java.io.Serializable

data class Contact(
    var name: String? = null,
    var phoneNumber: String? = null,
    var email:String?=null,
    var photoUri: String? = null
) : Serializable

class ContactArray:ArrayList<Contact>()

fun convertContactsToJsonArray(contacts: List<Contact>): JsonArray {
    val jsonArray = JsonArray()
    try{
        for (contact in contacts) {
            val jsonObject = JsonObject()
            jsonObject.addProperty("number", contact.phoneNumber)
            jsonObject.addProperty("email", contact.email)
            jsonObject.addProperty("name", contact.name)
            jsonArray.add(jsonObject)
        }

    }catch (e:Exception){
        print(e)
    }

    return jsonArray
}

fun loadImage1(view: ImageView, imageUrl: String?) {
    Glide.with(view.context)
        .load(imageUrl)
        .apply(RequestOptions.circleCropTransform())
        .into(view)
}