package com.example.sync

import android.content.Context
import android.net.ConnectivityManager
import android.net.NetworkCapabilities

class ConnectivityMonitor(context: Context) {

    private val connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

    fun getState(): ConnectivityState {
        val network = connectivityManager.activeNetwork ?: return ConnectivityState.OFFLINE
        val caps = connectivityManager.getNetworkCapabilities(network) ?: return ConnectivityState.OFFLINE
        return if (caps.hasCapability(NetworkCapabilities.NET_CAPABILITY_INTERNET)) {
            ConnectivityState.ONLINE
        } else {
            ConnectivityState.OFFLINE
        }
    }

    fun isOnline(): Boolean = getState() == ConnectivityState.ONLINE
}
