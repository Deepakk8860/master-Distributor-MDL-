package com.android.masterdistributormdl.gskDistributor.utils

import android.os.CountDownTimer
import android.util.Log

interface TimerListener {
    fun timerExpire()
}

object TimerUtil {

    private var listener: TimerListener? = null
    private const val timeOut = (60000*10).toLong()

    @Synchronized
    fun startTimer(listener: TimerListener) {
        TimerUtil.listener = listener
        timer.start()
    }

    @Synchronized
    fun stopTimer() {
        timer.cancel()
    }

    private val timer = object : CountDownTimer(timeOut, 1000) {
        override fun onTick(finished: Long) {
            val second = finished / 1000
            Log.d(TAG, "TIMER EXPIRE $second")
        }

        override fun onFinish() {
            listener?.timerExpire()
        }
    }
}