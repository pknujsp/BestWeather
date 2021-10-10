package com.lifedawn.bestweather.commons.classes;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.lifedawn.bestweather.R;

import java.util.List;

public class Geocoding {

	public static void reverseGeocoding(Context context, String query, ReverseGeocodingCallback callback) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				Geocoder geocoder = new Geocoder(context);

				try {
					List<Address> addressList = geocoder.getFromLocationName(query, 20);
					callback.onReverseGeocodingResult(addressList);
				} catch (Exception e) {

				}

			}
		}).start();
	}

	public static void geocoding(Context context, Double latitude, Double longitude, GeocodingCallback callback) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				Geocoder geocoder = new Geocoder(context);

				try {
					List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 20);
					callback.onGeocodingResult(addressList);
				} catch (Exception e) {

				}

			}
		}).start();
	}

	public interface GeocodingCallback {
		void onGeocodingResult(List<Address> addressList);
	}

	public interface ReverseGeocodingCallback {
		void onReverseGeocodingResult(List<Address> addressList);
	}
}
