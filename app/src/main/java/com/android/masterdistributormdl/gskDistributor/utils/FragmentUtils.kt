package com.android.masterdistributormdl.gskDistributor.utils

import android.os.Bundle
import android.util.Log

import androidx.core.os.bundleOf
import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.fragment.app.FragmentManager
import com.android.masterdistributormdl.R


var shooterFragment: Fragment? = null

fun getAppFragmentManager(activity: FragmentActivity): FragmentManager {
    return activity.supportFragmentManager
}


fun addFragmentFade(activity: FragmentActivity, fragment: Fragment, bundle: Bundle) {
    fragment.arguments = bundle
    addFragmentFade(activity, fragment)
}

fun addFragmentFade(activity: FragmentActivity, fragment: Fragment) {
    try {
        val transaction = getAppFragmentManager(activity).beginTransaction()
        if (shooterFragment != null) {
            transaction.hide(shooterFragment!!)
        }
        transaction.addToBackStack(fragment.tag)
        transaction.add(R.id.main_content, fragment, fragment.tag)
        transaction.commit()
    } catch (e: Exception) {
        Log.d(TAG, "FragmentException : " + e.localizedMessage)
    }


}

fun addFragment(activity: FragmentActivity, fragment: Fragment, bundle: Bundle) {
    fragment.arguments = bundle
    addFragment(activity, fragment)
}

fun addFragment(activity: FragmentActivity, fragment: Fragment) {
    try {
        val transaction = getAppFragmentManager(activity).beginTransaction()
        transaction.setCustomAnimations(
            R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit
        )
        val tag = fragment.tag
        if (shooterFragment != null) {
            transaction.hide(shooterFragment!!)
        }
        transaction.addToBackStack(tag)
        transaction.add(R.id.main_content, fragment, tag)
        transaction.commit()
    } catch (e: Exception) {
        Log.d(TAG, "FragmentException : " + e.localizedMessage)
    }
}

fun replaceFragmentFade(activity: FragmentActivity, fragment: Fragment, bundle: Bundle) {
    fragment.arguments = bundle
    replaceFragmentFade(activity, fragment)
}

fun replaceFragmentFade(activity: FragmentActivity, fragment: Fragment) {
    try {
        val transaction = getAppFragmentManager(activity).beginTransaction()
        transaction.setCustomAnimations(
            R.anim.fade_in, R.anim.fade_out
        )
        transaction.replace(R.id.main_content, fragment)
        transaction.commit()
    } catch (e: Exception) {
        Log.d(TAG, "FragmentException : " + e.localizedMessage)
    }
}


fun replaceFragment(activity: FragmentActivity, fragment: Fragment, bundle: Bundle) {
    fragment.arguments = bundle
    replaceFragment(activity, fragment)
}

fun replaceFragment(activity: FragmentActivity, fragment: Fragment) {
    try {
        val transaction = getAppFragmentManager(activity).beginTransaction()
        transaction.replace(R.id.main_content, fragment)
        transaction.commit()
    } catch (e: Exception) {
        Log.d(TAG, "FragmentException : " + e.localizedMessage)
    }
}

fun clearFragments(activity: FragmentActivity) {
    try {
        val fragmentManager = getAppFragmentManager(activity)
        while (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStackImmediate()
        }
    } catch (e: Exception) {
        Log.d(TAG, "FragmentException : " + e.localizedMessage)
    }
}

fun setOnBackResult(activity: FragmentActivity, key: String) {
    setOnBackResult(activity, key, bundleOf())
}

fun setOnBackResult(activity: FragmentActivity, key: String, bundle: Bundle) {
    try {
        getAppFragmentManager(activity).setFragmentResult(key, bundle)
        getAppFragmentManager(activity).popBackStack()
    } catch (e: Exception) {
        Log.d(TAG, "FragmentException : " + e.localizedMessage)
    }
}

