package com.lifedawn.bestweather.commons.classes

import android.content.Context
import android.net.ConnectivityManager
import android.net.Network
import android.net.NetworkCapabilities
import android.net.NetworkRequest
import javax.inject.Inject

class NetworkStatus @Inject constructor(context: Context) {
    private val connectivityManager: ConnectivityManager
    private val networkRequest: NetworkRequest

    init {
        val builder = NetworkRequest.Builder()
        builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI)
        builder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR)

        networkRequest = builder.build()
        connectivityManager = context.getSystemService(Context.CONNECTIVITY_SERVICE) as ConnectivityManager
        connectivityManager.registerNetworkCallback(networkRequest, object : ConnectivityManager.NetworkCallback() {
            override fun onAvailable(network: Network) {
                super.onAvailable(network)
            }

            override fun onLost(network: Network) {
                super.onLost(network)
            }
        })
    }

    fun networkAvailable() =
        connectivityManager.activeNetwork != null

}