package com.seanmahaffey.bapops

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import android.net.wifi.WifiInfo
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

object WifiChecker {

    suspend fun getCurrentSsid(context: Context): String {
        val connectivityManager = context.applicationContext
            .getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager

        val request = NetworkRequest.Builder()
            .addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
            .build()

        return suspendCancellableCoroutine { continuation ->
            var resumed = false

            val callback = object : ConnectivityManager.NetworkCallback(FLAG_INCLUDE_LOCATION_INFO) {
                override fun onCapabilitiesChanged(network: Network, capabilities: NetworkCapabilities) {
                    if (resumed) return
                    val transportInfo = capabilities.transportInfo
                    val ssid = if (transportInfo is WifiInfo) {
                        transportInfo.ssid?.trim('"') ?: ""
                    } else {
                        ""
                    }
                    resumed = true
                    connectivityManager.unregisterNetworkCallback(this)
                    if (continuation.isActive) continuation.resume(ssid)
                }
            }

            connectivityManager.registerNetworkCallback(request, callback)

            continuation.invokeOnCancellation {
                try {
                    connectivityManager.unregisterNetworkCallback(callback)
                } catch (_: Exception) {
                }
            }
        }
    }

    suspend fun isOnTargetNetwork(context: Context, targetSsid: String): Boolean {
        if (targetSsid.isBlank()) return false
        return getCurrentSsid(context).equals(targetSsid, ignoreCase = true)
    }
}