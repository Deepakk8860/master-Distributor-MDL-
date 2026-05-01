package com.android.masterdistributormdl.utils

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import androidx.drawerlayout.widget.DrawerLayout
import com.android.masterdistributormdl.gskDistributor.utils.onAlertSnackbar
import com.android.masterdistributormdl.gskDistributor.utils.onSnackbar

object NetworkAlert {
    var drawerLayout: DrawerLayout? = null
    fun init(context: Context) {
        val connectivityManager =
            context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.requestNetwork(networkRequest, networkCallback)
    }

    private val networkRequest: NetworkRequest =
        NetworkRequest.Builder()
            .addCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR).build()

    private val networkCallback = object : ConnectivityManager.NetworkCallback() {

        override fun onAvailable(network: Network) {
            super.onAvailable(network)
            if (drawerLayout != null)
                onSnackbar(drawerLayout!!, "Connected to Internet")
        }

        override fun onCapabilitiesChanged(
            network: Network, networkCapabilities: NetworkCapabilities
        ) {
            super.onCapabilitiesChanged(network, networkCapabilities)
            val unmetered =
                networkCapabilities.hasCapability(NetworkCapabilities.NET_CAPABILITY_NOT_METERED)
        }

        override fun onLost(network: Network) {
            super.onLost(network)
            if (drawerLayout != null)
                onAlertSnackbar(drawerLayout!!, "Not Connected to Internet")
        }
    }

}