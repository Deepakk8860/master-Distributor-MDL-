package com.android.masterdistributormdl.gskDistributor.utils

import android.text.Editable
import android.text.TextUtils
import android.text.TextWatcher

open class AddTextWatcher() : TextWatcher {
    override fun onTextChanged(s: CharSequence, start: Int, before: Int, count: Int) {

    }

    override fun beforeTextChanged(s: CharSequence, start: Int, count: Int, after: Int) {}
    override fun afterTextChanged(s: Editable) {

        if (s.isNotEmpty() && s.length % 5 == 0) {
            val c = s[s.length - 1]
            if (space == c) {
                s.delete(s.length - 1, s.length)
            }
        }

        if (s.isNotEmpty() && s.length % 5 == 0) {
            val c = s[s.length - 1]

            if (Character.isDigit(c) && TextUtils.split(s.toString(), space.toString()).size <= 3) {
                s.insert(s.length - 1, space.toString())

            }
        }


    }

    fun isAadhaarValid(aadhaarNo: String): Boolean {
        //Removed extra characters
        val newAadhaarNo = aadhaarNo.replace(" ", "", true)

        if (newAadhaarNo.length != 12
            || newAadhaarNo.toBigIntegerOrNull() == null
            || newAadhaarNo.first().toString().toInt() in 0..1
        ) {
            return false
        }

        val firstMatrix = arrayOf(
            intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9),
            intArrayOf(1, 2, 3, 4, 0, 6, 7, 8, 9, 5),
            intArrayOf(2, 3, 4, 0, 1, 7, 8, 9, 5, 6),
            intArrayOf(3, 4, 0, 1, 2, 8, 9, 5, 6, 7),
            intArrayOf(4, 0, 1, 2, 3, 9, 5, 6, 7, 8),
            intArrayOf(5, 9, 8, 7, 6, 0, 4, 3, 2, 1),
            intArrayOf(6, 5, 9, 8, 7, 1, 0, 4, 3, 2),
            intArrayOf(7, 6, 5, 9, 8, 2, 1, 0, 4, 3),
            intArrayOf(8, 7, 6, 5, 9, 3, 2, 1, 0, 4),
            intArrayOf(9, 8, 7, 6, 5, 4, 3, 2, 1, 0)
        )
        val secondMatrix = arrayOf(
            intArrayOf(0, 1, 2, 3, 4, 5, 6, 7, 8, 9),
            intArrayOf(1, 5, 7, 6, 2, 8, 3, 0, 9, 4),
            intArrayOf(5, 8, 0, 3, 7, 9, 6, 1, 4, 2),
            intArrayOf(8, 9, 1, 6, 0, 4, 3, 5, 2, 7),
            intArrayOf(9, 4, 5, 3, 1, 2, 6, 8, 7, 0),
            intArrayOf(4, 2, 8, 6, 5, 7, 3, 9, 0, 1),
            intArrayOf(2, 7, 9, 3, 8, 0, 6, 4, 1, 5),
            intArrayOf(7, 0, 4, 6, 9, 1, 3, 2, 5, 8)
        )

        var isValid = 0

        //Created reverse array
        val reversedIntArray =
            newAadhaarNo.chunked(1).map { it.toInt() }.toIntArray().reversedArray()

        //Apply Verhoeff algorithm
        for (i in reversedIntArray.indices) {
            isValid = firstMatrix[isValid][secondMatrix[i % 8][reversedIntArray[i]]]
        }
        return isValid == 0
    }

    companion object {
        private const val space = ' '
    }

}