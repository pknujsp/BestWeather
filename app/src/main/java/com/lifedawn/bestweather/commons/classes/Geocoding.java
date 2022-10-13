package com.lifedawn.bestweather.commons.classes;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.retrofit.client.Queries;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.nominatim.ReverseGeocodeParameter;
import com.lifedawn.bestweather.retrofit.responses.nominatim.ReverseGeocodeResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Geocoding {

	public static void reverseGeocoding(Context context, String query, ReverseGeocodingCallback callback) {
		MyApplication.getExecutorService().execute(new Runnable() {
			@Override
			public void run() {
				if (query.isEmpty()) {
					callback.onReverseGeocodingResult(new ArrayList<>());
					return;
				}

				boolean containKr = query.matches(".*[ㄱ-ㅎㅏ-ㅣ가-힣]+.*");
				Geocoder geocoder = new Geocoder(context, containKr ? Locale.KOREA : Locale.US);

				try {
					List<Address> addressList = geocoder.getFromLocationName(query, 5);
					List<Integer> errors = new ArrayList<>();

					for (int i = addressList.size() - 1; i >= 0; i--) {
						if (addressList.get(i).getCountryName() == null || addressList.get(i).getCountryCode() == null) {
							errors.add(i);
						}
					}

					for (int errorIdx : errors) {
						addressList.remove(errorIdx);
					}

					List<AddressDto> addressDtoList = new ArrayList<>();
					for (Address address : addressList) {
						addressDtoList.add(new AddressDto(address.getLatitude(), address.getLongitude(), address.getAddressLine(0),
								address.getAddressLine(0),
								address.getCountryName(), address.getCountryCode(), address.getAdminArea()));
					}

					callback.onReverseGeocodingResult(addressDtoList);
				} catch (Exception e) {

				}
			}
		});
	}

	public static void geocoding(Context context, Double latitude, Double longitude, GeocodingCallback callback) {
		MyApplication.getExecutorService().execute(new Runnable() {
			@Override
			public void run() {
				Geocoder geocoder = new Geocoder(context);
				try {
					List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 20);

					if (addressList.size() > 0) {
						AddressDto addressDto = new AddressDto(latitude, longitude, addressList.get(0).getAddressLine(0),
								addressList.get(0).getAddressLine(0),
								addressList.get(0).getCountryName(), addressList.get(0).getCountryCode(),
								addressList.get(0).getAdminArea());

						callback.onGeocodingResult(addressDto);
					} else {
						callback.onGeocodingResult(null);
					}
				} catch (Exception e) {

				}
			}
		});
	}

	public static void nominatimGeocoding(Context context, Double latitude, Double longitude, GeocodingCallback callback) {
		ReverseGeocodeParameter parameter = new ReverseGeocodeParameter(latitude, longitude);
		Call<JsonElement> call = RetrofitClient.getApiService(RetrofitClient.ServiceType.NOMINATIM_REVERSE).nominatimReverseGeocode(parameter.getMap());

		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				if (response.isSuccessful()) {
					ReverseGeocodeResponse reverseGeocodeResponse = new Gson().fromJson(response.body().toString(),
							ReverseGeocodeResponse.class);

					ReverseGeocodeResponse.Properties properties = reverseGeocodeResponse.getFeatures().get(0).getProperties();
					AddressDto addressDto = new AddressDto(latitude, longitude, properties.getName(),
							properties.getDisplayName(), properties.getAddress().getCountry(),
							properties.getAddress().getCountryCode().toUpperCase(),
							properties.getAddress().getCity() != null ? properties.getAddress().getCity() :
									properties.getAddress().getState());

					callback.onGeocodingResult(addressDto);
				} else {
					geocoding(context, latitude, longitude, callback);
				}
			}

			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				geocoding(context, latitude, longitude, callback);
			}
		});
	}


	public interface GeocodingCallback {
		void onGeocodingResult(AddressDto addressDto);
	}

	public interface ReverseGeocodingCallback {
		void onReverseGeocodingResult(List<AddressDto> addressList);
	}

	public static class AddressDto {
		public final Double latitude;
		public final Double longitude;
		public final String simpleName;
		public final String displayName;
		public final String country;
		public final String countryCode;
		public final String cityOrState;

		public AddressDto(Double latitude, Double longitude, String simpleName, String displayName, String country, String countryCode,
		                  String cityOrState) {
			this.latitude = latitude;
			this.longitude = longitude;
			this.simpleName = simpleName;
			this.displayName = displayName;
			this.country = country;
			this.countryCode = countryCode;
			this.cityOrState = cityOrState;
		}

		public String toName() {
			return simpleName + ", " + cityOrState + ", " + country;
		}
	}
}
