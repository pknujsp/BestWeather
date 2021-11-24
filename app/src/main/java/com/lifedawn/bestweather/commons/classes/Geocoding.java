package com.lifedawn.bestweather.commons.classes;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.lifedawn.bestweather.R;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public class Geocoding {

	public static void reverseGeocoding(Context context, ExecutorService executorService, String query, ReverseGeocodingCallback callback) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				Geocoder geocoder = new Geocoder(context);

				try {
					List<Address> addressList = geocoder.getFromLocationName(query, 20);
					List<Integer> errors = new ArrayList<>();
					for (int i = addressList.size() - 1; i >= 0; i--) {
						if (addressList.get(i).getCountryName() == null || addressList.get(i).getCountryCode() == null) {
							errors.add(i);
						}
					}

					for (int errorIdx : errors) {
						addressList.remove(errorIdx);
					}

					callback.onReverseGeocodingResult(addressList);
				} catch (Exception e) {

				}
			}
		});
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
