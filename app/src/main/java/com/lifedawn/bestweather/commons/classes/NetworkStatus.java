package com.lifedawn.bestweather.commons.classes;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkCapabilities;
import android.net.NetworkRequest;
import android.widget.Toast;

import com.lifedawn.bestweather.R;

public final class NetworkStatus {
	private ConnectivityManager.NetworkCallback networkCallback;
	private ConnectivityManager connectivityManager;
	private Context context;
	
	public NetworkStatus(Context context, ConnectivityManager.NetworkCallback networkCallback) {
		this.context = context;
		this.networkCallback = networkCallback;
		setNetworkCallback();
	}
	
	public void unregisterNetworkCallback() {
		connectivityManager.unregisterNetworkCallback(networkCallback);
	}
	
	public void setNetworkCallback() {
		NetworkRequest.Builder builder = new NetworkRequest.Builder();
		builder.addTransportType(NetworkCapabilities.TRANSPORT_WIFI);
		builder.addTransportType(NetworkCapabilities.TRANSPORT_CELLULAR);
		
		connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
		connectivityManager.registerNetworkCallback(builder.build(), networkCallback);
	}
	
	public boolean networkAvailable() {
		if (connectivityManager.getActiveNetwork() == null) {
			Toast.makeText(context, context.getString(R.string.disconnected_network), Toast.LENGTH_SHORT).show();
			return false;
		} else {
			NetworkCapabilities nc = connectivityManager.getNetworkCapabilities(connectivityManager.getActiveNetwork());
			
			if (nc.hasTransport(NetworkCapabilities.TRANSPORT_CELLULAR) || nc.hasTransport(NetworkCapabilities.TRANSPORT_WIFI)) {
				return true;
			} else {
				Toast.makeText(context, context.getString(R.string.disconnected_network), Toast.LENGTH_SHORT).show();
				return false;
			}
		}
	}
	
	public void showToastDisconnected() {
		Toast.makeText(context, R.string.disconnected_network, Toast.LENGTH_SHORT).show();
	}
	
	public void showToastConnected() {
		Toast.makeText(context, R.string.connected_network, Toast.LENGTH_SHORT).show();
	}
}