package com.lifedawn.bestweather.commons.classes;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.lifecycle.LifecycleOwner;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.Observer;

import com.lifedawn.bestweather.R;

import org.jetbrains.annotations.NotNull;

public class NetworkStatus extends LiveData<Boolean> {
	private static NetworkStatus instance;
	private ConnectivityManager connectivityManager;
	private NetworkRequest networkRequest;
	private Context context;

	public static NetworkStatus getInstance(Context context) {
		if (instance == null) {
			instance = new NetworkStatus(context);
		}
		return instance;
	}

	public NetworkStatus(Context context) {
		this.context = context;
		NetworkRequest.Builder builder = new NetworkRequest.Builder();
		builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
		builder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);

		networkRequest = builder.build();
		connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		connectivityManager.registerNetworkCallback(networkRequest, new ConnectivityManager.NetworkCallback() {
			@Override
			public void onAvailable(@NonNull Network network) {
				super.onAvailable(network);
				postValue(true);
			}

			@Override
			public void onLost(@NonNull Network network) {
				super.onLost(network);
				postValue(false);
			}
		});
	}

	public boolean networkAvailable() {
		if (connectivityManager.getActiveNetwork() == null) {
			return false;
		} else {
			NetworkCapabilities nc = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());

			if (nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
				return true;
			} else {
				return false;
			}
		}
	}

}