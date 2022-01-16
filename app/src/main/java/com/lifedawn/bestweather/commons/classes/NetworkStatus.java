package com.lifedawn.bestweather.commons.classes;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.lifedawn.bestweather.R;

import org.jetbrains.annotations.NotNull;

public class NetworkStatus {
	private static NetworkStatus instance;
	private ConnectivityManager connectivityManager;
	private NetworkRequest networkRequest;

	public static NetworkStatus getInstance(Context context) {
		if (instance == null) {
			instance = new NetworkStatus(context);
		}
		return instance;
	}

	public NetworkStatus(Context context) {
		NetworkRequest.Builder builder = new NetworkRequest.Builder();
		builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
		builder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);

		networkRequest = builder.build();
		connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		connectivityManager.registerNetworkCallback(networkRequest, new ConnectivityManager.NetworkCallback() {
			@Override
			public void onAvailable(@NonNull Network network) {
				super.onAvailable(network);
			}

			@Override
			public void onLost(@NonNull Network network) {
				super.onLost(network);
			}
		});

	}


	public boolean networkAvailable() {
		NetworkInfo activeNetwork = connectivityManager.getActiveNetworkInfo();
		return activeNetwork != null;
	}

}