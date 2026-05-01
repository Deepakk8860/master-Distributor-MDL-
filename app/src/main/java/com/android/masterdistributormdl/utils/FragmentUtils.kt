package com.android.masterdistributormdl.utils

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
        transaction.hide(shooterFragment!!)
        transaction.addToBackStack(fragment.tag)
        transaction.add(R.id.main_content, fragment, fragment.tag)
        transaction.commit()
    } catch (e: Exception) {
        Log.e(TAG, "Fragment : " + e.localizedMessage)
    }
}

fun addFragment(activity: FragmentActivity, fragment: Fragment, bundle: Bundle) {
    fragment.arguments = bundle
    addFragment(activity, fragment)
}

fun restartFragment(activity: FragmentActivity, fragment: Fragment) {
    val fragmentManager = getAppFragmentManager(activity)
    val fragmentTransaction = fragmentManager.beginTransaction()

    // Create a new instance of the fragment class
    val newFragment = fragment::class.java.newInstance()

    // Create a bundle and put the reference string into it
    val args = Bundle()
    args.putString("reference", "")

    // Set the bundle as arguments to the new fragment
    newFragment.arguments = args

    // Replace the current fragment with the new instance
    fragmentTransaction.replace(R.id.main_content, newFragment, newFragment.tag)

    // Optional: Clear the back stack to prevent the old fragment instance from being reachable
    fragmentManager.popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE)

    // Commit the transaction
    fragmentTransaction.commit()
}


fun addFragment(activity: FragmentActivity, fragment: Fragment) {
    try {
        val transaction = getAppFragmentManager(activity).beginTransaction()
        transaction.setCustomAnimations(
            R.anim.enter, R.anim.exit, R.anim.pop_enter, R.anim.pop_exit
        )
        var tag = fragment.tag
        // Check if tag is null, generate a tag based on class name if null
        Log.d(TAG, "addFragment: $tag")
        if (tag == null) {
            transaction.hide(shooterFragment!!)
            transaction.addToBackStack(tag)
            transaction.add(R.id.main_content, fragment, tag)
            transaction.commit()
        }

    }
    catch (_: Exception) {
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
        Log.e(TAG, "Fragment : " + e.localizedMessage)
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
        Log.e(TAG, "Fragment : " + e.localizedMessage)
    }
}

fun clearFragments(activity: FragmentActivity) {
    try {
        val fragmentManager = getAppFragmentManager(activity)
        while (fragmentManager.backStackEntryCount > 0) {
            fragmentManager.popBackStackImmediate()
        }
    } catch (e: Exception) {
        Log.e(TAG, "Fragment : " + e.localizedMessage)
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
        Log.e(TAG, "Fragment : " + e.localizedMessage)
    }
}


fun onBackResult(result_key: String, activity: FragmentActivity, result: (Bundle) -> Unit) {
    getAppFragmentManager(activity).setFragmentResultListener(
        result_key, activity
    ) { requestKey, bundle ->
        result.invoke(bundle)
    }
}
