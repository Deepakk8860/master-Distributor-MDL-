package com.android.masterdistributormdl.utils

import android.content.Context
import android.content.SharedPreferences
import com.android.masterdistributormdl.model.User


class SharedPreference {
    private val appSharedPrefsUser: SharedPreferences =
        MyApplication.getMyApplication()
            .getSharedPreferences("appSharedPrefs", Context.MODE_PRIVATE)
    private val prefsEditor: SharedPreferences.Editor = appSharedPrefsUser.edit()

    fun clearSharedPrefernce() {
        prefsEditor.clear()
        prefsEditor.commit()
    }

    fun getInteger(key: String?): Int {
        return appSharedPrefsUser.getInt(key, 0)
    }

    fun putInteger(key: String, value: Int) {
        prefsEditor.putInt(key, value)
        prefsEditor.commit()
    }

    fun getString(key: String): String? {
        return appSharedPrefsUser.getString(key, "")
    }

    fun putString(key: String, value: String) {
        prefsEditor.putString(key, value)
        prefsEditor.commit()
    }

    fun putDouble(key: String, value: Double) {
        prefsEditor.putString(key, value.toString())
        prefsEditor.commit()
    }

    fun getDouble(key: String): Double {
        val value = appSharedPrefsUser.getString(key, "0.0")
        return value!!.toDouble()
    }

    fun putFloat(key: String, value: Float) {
        prefsEditor.putFloat(key, value!!)
        prefsEditor.commit()
    }

    fun getFloat(key: String): Float {
        return appSharedPrefsUser.getFloat(key, 0.0.toFloat())
    }

    fun getBoolean(key: String): Boolean {
        return appSharedPrefsUser.getBoolean(key, false)
    }

    fun putBoolean(key: String, value: Boolean) {
        prefsEditor.putBoolean(key, value)
        prefsEditor.commit()
    }

    fun getUserDist(): com.android.masterdistributormdl.gskDistributor.model.User? {
        var user: com.android.masterdistributormdl.gskDistributor.model.User? = null
        val json = appSharedPrefsUser.getString(user_data, "")
        if (json!!.isNotEmpty()) {
            user = gson.fromJson(json, com.android.masterdistributormdl.gskDistributor.model.User::class.java)

        }
        return user
    }

    fun getUser(): User? {
        var user: User? = null
        val json = appSharedPrefsUser.getString(user_data, "")
        if (json!!.isNotEmpty()) {
            user = gson.fromJson(json, User::class.java)
        }
        return user
    }




}