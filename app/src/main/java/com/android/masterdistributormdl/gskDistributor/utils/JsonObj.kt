package com.android.masterdistributormdl.gskDistributor.utils

import android.util.Log
import com.android.masterdistributormdl.gskDistributor.utils.TAG
import com.google.gson.JsonArray
import com.google.gson.JsonElement
import com.google.gson.JsonObject
import com.google.gson.JsonParser


class JsonObj(private val jsonObj: JsonObject) {
    constructor() :
            this(JsonObject())

    constructor(jsonElement: JsonElement) :
            this(jsonElement.asJsonObject)

    constructor(jsonArray: JsonArray, position: Int) :
            this(jsonArray.get(position).asJsonObject)

    constructor(jsonString: String) :
            this(JsonParser.parseString(jsonString))

    fun putJsonElement(key: String, value: JsonElement) {
        jsonObj.add(key, value)
    }

    fun putJsonArr(key: String, value: JsonArray) {
        jsonObj.add(key, value)
    }

    fun putString(key: String, value: String?) {
        jsonObj.addProperty(key, value)
    }

    fun putNumber(key: String, value: Number?) {
        jsonObj.addProperty(key, value)
    }

    fun putBoolean(key: String, value: Boolean) {
        jsonObj.addProperty(key, value)
    }

    fun getJsonObj(): JsonObject {
        return jsonObj
    }

    fun getJsonObj(key: String): JsonObject? {
        if (jsonObj.get(key).isJsonNull)
            return null
        else
            return jsonObj.get(key).asJsonObject
    }

    fun getJsonArray(key: String): JsonArray? {
        if (jsonObj.get(key).isJsonNull)
            return null
        else
            return jsonObj.get(key).asJsonArray
    }

    fun getString(key: String): String {
        if (jsonObj.get(key).isJsonNull)
            return ""
        else
            return jsonObj.get(key).asString
    }

    fun getInt(key: String): Int {
        if (jsonObj.get(key).isJsonNull)
            return 0
        else
            return jsonObj.get(key).asInt
    }

    fun getDouble(key: String): Double {
        if (jsonObj.get(key).isJsonNull)
            return 0.0
        else
            return jsonObj.get(key).asDouble
    }

    fun getBoolean(key: String): Boolean {
        if (jsonObj.get(key).isJsonNull)
            return false
        else
            return jsonObj.get(key).asBoolean
    }

    fun isJsonObject(key: String): Boolean {
        if (jsonObj.get(key).isJsonNull)
            return false
        else
            return jsonObj.get(key).isJsonObject
    }

    fun getFloat(key: String): Float {
        if (jsonObj.get(key).isJsonNull)
            return 0f
        else
            return jsonObj.get(key).asFloat
    }

    override fun toString(): String {
        return jsonObj.toString()
    }

    fun getKeyValues() {
        val entries: Set<Map.Entry<String, JsonElement>> = jsonObj.entrySet()
        val stringBuilder = StringBuilder()
        for ((key) in entries) {
            if (jsonObj.get(key).isJsonArray) {
                stringBuilder.append(key + " : " + getJsonArray(key) + "\n")
            } else {
                stringBuilder.append(key + " : " + getString(key) + "\n")
            }
        }
        Log.d(TAG, "Print Key & Values\n$stringBuilder")
    }
}