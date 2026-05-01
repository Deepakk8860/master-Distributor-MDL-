package com.android.masterdistributormdl.utils

import android.app.Activity
import android.content.pm.ActivityInfo
import android.hardware.SensorManager
import android.view.OrientationEventListener

class AutoRotateHelper(private val activity: Activity) {

    private var orientationListener: OrientationEventListener? = null
    private var lastOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED

    fun enable() {
        orientationListener = object : OrientationEventListener(activity, SensorManager.SENSOR_DELAY_NORMAL) {
            override fun onOrientationChanged(orientation: Int) {
                if (orientation == ORIENTATION_UNKNOWN) return

                val newOrientation = when {
                    orientation in 60..140 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_LANDSCAPE
                    orientation in 220..300 -> ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE
                    orientation in 140..220 -> ActivityInfo.SCREEN_ORIENTATION_REVERSE_PORTRAIT
                    else -> ActivityInfo.SCREEN_ORIENTATION_PORTRAIT
                }

                if (newOrientation != lastOrientation) {
                    lastOrientation = newOrientation
                    activity.requestedOrientation = newOrientation
                }
            }
        }

        if (orientationListener?.canDetectOrientation() == true) {
            orientationListener?.enable()
        }
    }

    fun disable() {
        orientationListener?.disable()
        orientationListener = null
        activity.requestedOrientation = ActivityInfo.SCREEN_ORIENTATION_UNSPECIFIED
    }
}
