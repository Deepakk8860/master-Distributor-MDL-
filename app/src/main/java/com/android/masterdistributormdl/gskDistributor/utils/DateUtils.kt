package com.android.masterdistributormdl.gskDistributor.utils

import android.annotation.SuppressLint
import java.text.DateFormat
import java.text.SimpleDateFormat
import java.util.*

@SuppressLint("SimpleDateFormat")
fun calenderToDate(calendar: Calendar): String {
    val format = SimpleDateFormat("yyyy-MM-dd")
    val date = format.format(calendar.getTime())
    return date
}

fun getFormat(number: Int): String {
    if (number > 9) {
        return number.toString()
    }
    return "0" + number

}

fun getDateFormat(year: Int, month: Int, day: Int): String {
    return "" + year + "-" + getFormat(month) + "-" + getFormat(day)
}

fun printDataFormat(date: String): String {
    try {
        val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd")
        val date = dateFormat.parse(date)
        val formatter = SimpleDateFormat("dd MMM yyyy")
        val dateStr = formatter.format(date)
        return dateStr
    } catch (e: Exception) {
        return "N/A"
    }
}

fun indiaDate(dateTime: String): String {
    try {
        val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date = dateFormat.parse(dateTime)
        val formatter = SimpleDateFormat("dd MMM yyyy")
        val dateStr = formatter.format(date)
        return dateStr
    } catch (e: Exception) {
        return "N/A"
    }
}
@SuppressLint("SimpleDateFormat")
fun indiaDateComa(dateTime: String): String {
    try {
        val dateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date = dateFormat.parse(dateTime)
        val formatter = SimpleDateFormat("dd MMM, yyyy")
        val dateStr = formatter.format(date)
        return dateStr
    } catch (e: Exception) {
        return "N/A"
    }
}
fun indiaTimeFormat(dateTime: String): String {
    try {
        val dateFormat: DateFormat = SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        val date = dateFormat.parse(dateTime)
        val formatter = SimpleDateFormat("dd-MM-yyyy hh:mm aa")
        val dateStr = formatter.format(date)
        return dateStr
    } catch (e: Exception) {
        return "N/A"
    }
}