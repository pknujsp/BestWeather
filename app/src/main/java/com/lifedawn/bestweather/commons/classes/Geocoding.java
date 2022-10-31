package com.lifedawn.bestweather.commons.classes;

import android.content.Context;
import android.location.Address;
import android.location.Geocoder;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.nominatim.GeocodeParameter;
import com.lifedawn.bestweather.retrofit.parameters.nominatim.ReverseGeocodeParameter;
import com.lifedawn.bestweather.retrofit.responses.nominatim.GeocodeResponse;
import com.lifedawn.bestweather.retrofit.responses.nominatim.ReverseGeocodeResponse;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class Geocoding {

	public static void androidGeocoding(Context context, String query, GeocodingCallback callback) {
		MyApplication.getExecutorService().submit(new Runnable() {
			@Override
			public void run() {
				if (query.isEmpty()) {
					callback.onGeocodingResult(new ArrayList<>());
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
						addressDtoList.add(new AddressDto(address.getLatitude(), address.getLongitude(),
								address.getAddressLine(0),
								address.getCountryName(), address.getCountryCode()));
					}

					callback.onGeocodingResult(addressDtoList);
				} catch (Exception e) {

				}
			}
		});
	}

	public static void nominatimGeocoding(Context context, String query, GeocodingCallback callback) {
		GeocodeParameter parameter = new GeocodeParameter(query);
		Call<JsonElement> call =
				RetrofitClient.getApiService(RetrofitClient.ServiceType.NOMINATIM).nominatimGeocode(parameter.getMap(),
						MyApplication.locale.toLanguageTag());

		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				if (response.isSuccessful()) {
					GeocodeResponse geocodeResponse = new Gson().fromJson(response.body(),
							GeocodeResponse.class);

					List<AddressDto> addressDtoList = new ArrayList<>();

					for (GeocodeResponse.Features features : geocodeResponse.getFeatures()) {
						GeocodeResponse.Properties properties = features.getProperties();
						String editedDisplayName = convertDisplayName(properties.getDisplayName());

						addressDtoList.add(new AddressDto(features.getGeometry().getCoordinates().get(1),
								features.getGeometry().getCoordinates().get(0),
								editedDisplayName, properties.getAddress().getCountry(),
								properties.getAddress().getCountryCode().toUpperCase()
						));
					}
					callback.onGeocodingResult(addressDtoList);
				} else {
					androidGeocoding(context, query, callback);
				}
			}

			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				androidGeocoding(context, query, callback);
			}
		});
	}

	public static void androidReverseGeocoding(Context context, Double latitude, Double longitude, ReverseGeocodingCallback callback) {
		Geocoder geocoder = new Geocoder(context);
		try {
			List<Address> addressList = geocoder.getFromLocation(latitude, longitude, 20);

			if (addressList.size() > 0) {
				AddressDto addressDto = new AddressDto(latitude, longitude,
						addressList.get(0).getAddressLine(0),
						addressList.get(0).getCountryName(), addressList.get(0).getCountryCode()
				);

				callback.onReverseGeocodingResult(addressDto);
			} else {
				callback.onReverseGeocodingResult(null);
			}
		} catch (Exception e) {

		}
	}

	public static void nominatimReverseGeocoding(Context context, Double latitude, Double longitude, ReverseGeocodingCallback callback) {
		ReverseGeocodeParameter parameter = new ReverseGeocodeParameter(latitude, longitude);
		Call<JsonElement> call =
				RetrofitClient.getApiService(RetrofitClient.ServiceType.NOMINATIM).nominatimReverseGeocode(parameter.getMap(),
						MyApplication.locale.toLanguageTag());

		call.enqueue(new Callback<JsonElement>() {
			@Override
			public void onResponse(Call<JsonElement> call, Response<JsonElement> response) {
				if (response.isSuccessful()) {
					ReverseGeocodeResponse reverseGeocodeResponse = new Gson().fromJson(response.body().toString(),
							ReverseGeocodeResponse.class);

					ReverseGeocodeResponse.Properties properties = reverseGeocodeResponse.getFeatures().get(0).getProperties();

					String editedDisplayName = convertDisplayName(properties.getDisplayName());

					AddressDto addressDto = new AddressDto(latitude, longitude,
							editedDisplayName, properties.getAddress().getCountry(),
							properties.getAddress().getCountryCode().toUpperCase()
					);

					callback.onReverseGeocodingResult(addressDto);
				} else {
					androidReverseGeocoding(context, latitude, longitude, callback);
				}
			}

			@Override
			public void onFailure(Call<JsonElement> call, Throwable t) {
				androidReverseGeocoding(context, latitude, longitude, callback);
			}
		});
	}

	private static String convertDisplayName(String originalDisplayName) {
		String[] separatedDisplayNames = originalDisplayName.split(", ");
		if (separatedDisplayNames.length > 2) {
			StringBuilder stringBuilder = new StringBuilder();
			final int lastIdx = separatedDisplayNames.length - 2;
			for (int i = 0; i <= lastIdx; i++) {
				stringBuilder.append(separatedDisplayNames[i]);
				if (i < lastIdx) {
					stringBuilder.append(", ");
				}
			}
			return stringBuilder.toString();
		} else {
			return originalDisplayName;
		}
	}


	public interface ReverseGeocodingCallback {
		void onReverseGeocodingResult(AddressDto addressDto);
	}

	public interface GeocodingCallback {
		void onGeocodingResult(List<AddressDto> addressList);
	}

	public static class AddressDto {
		public final Double latitude;
		public final Double longitude;
		public final String displayName;
		public final String country;
		public final String countryCode;

		public AddressDto(Double latitude, Double longitude, String displayName, String country, String countryCode) {
			this.latitude = latitude;
			this.longitude = longitude;
			this.displayName = displayName;
			this.country = country;
			this.countryCode = countryCode;
		}

	}
}
