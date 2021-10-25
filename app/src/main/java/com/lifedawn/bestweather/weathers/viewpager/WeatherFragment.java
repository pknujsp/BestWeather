package com.lifedawn.bestweather.weathers.viewpager;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;

import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.Geocoding;
import com.lifedawn.bestweather.commons.classes.Gps;
import com.lifedawn.bestweather.commons.enums.FavoriteAddressType;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.interfaces.IGps;
import com.lifedawn.bestweather.databinding.FragmentWeatherBinding;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.responses.accuweather.currentconditions.CurrentConditionsResponse;
import com.lifedawn.bestweather.retrofit.responses.aqicn.GeolocalizedFeedResponse;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.onecall.OneCallResponse;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.weathers.dataprocessing.request.MainProcessing;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AccuWeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalCurrentConditions;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalDailyForecast;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalHourlyForecast;
import com.lifedawn.bestweather.weathers.detailfragment.accuweather.currentconditions.AccuDetailCurrentConditionsFragment;
import com.lifedawn.bestweather.weathers.detailfragment.kma.currentconditions.KmaDetailCurrentConditionsFragment;
import com.lifedawn.bestweather.weathers.detailfragment.openweathermap.currentconditions.OwmDetailCurrentConditionsFragment;
import com.lifedawn.bestweather.weathers.simplefragment.accuweather.currentconditions.AccuSimpleCurrentConditionsFragment;
import com.lifedawn.bestweather.weathers.simplefragment.accuweather.dailyforecast.AccuSimpleDailyForecastFragment;
import com.lifedawn.bestweather.weathers.simplefragment.accuweather.hourlyforecast.AccuSimpleHourlyForecastFragment;
import com.lifedawn.bestweather.weathers.simplefragment.aqicn.SimpleAirQualityFragment;
import com.lifedawn.bestweather.weathers.simplefragment.kma.currentconditions.KmaSimpleCurrentConditionsFragment;
import com.lifedawn.bestweather.weathers.simplefragment.kma.dailyforecast.KmaSimpleDailyForecastFragment;
import com.lifedawn.bestweather.weathers.simplefragment.kma.hourlyforecast.KmaSimpleHourlyForecastFragment;
import com.lifedawn.bestweather.weathers.simplefragment.openweathermap.currentconditions.OwmSimpleCurrentConditionsFragment;
import com.lifedawn.bestweather.weathers.simplefragment.openweathermap.dailyforecast.OwmSimpleDailyForecastFragment;
import com.lifedawn.bestweather.weathers.simplefragment.openweathermap.hourlyforecast.OwmSimpleHourlyForecastFragment;
import com.lifedawn.bestweather.weathers.simplefragment.sunsetrise.SunsetriseFragment;
import com.lifedawn.bestweather.weathers.viewmodels.WeatherViewModel;

import org.jetbrains.annotations.NotNull;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;


public class WeatherFragment extends Fragment implements IGps {
	private FragmentWeatherBinding binding;
	private FavoriteAddressDto selectedFavoriteAddressDto;
	private FavoriteAddressType favoriteAddressType;
	private Gps gps;
	private WeatherViewModel.ILoadImgOfCurrentConditions iLoadImgOfCurrentConditions;
	private WeatherViewModel weatherViewModel;
	private MainProcessing.WeatherSourceType mainWeatherSourceType;
	private Double currentLocationLatitude;
	private Double currentLocationLongitude;
	private String countryCode;
	private String addressName;
	private SharedPreferences sharedPreferences;

	public static final Map<String, MultipleJsonDownloader<JsonElement>> finalResponseMap = new HashMap<>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

		Bundle bundle = getArguments();
		favoriteAddressType = (FavoriteAddressType) bundle.getSerializable(getString(R.string.bundle_key_favorite_address_type));
		String newSelectedAddressId = null;

		if (favoriteAddressType == FavoriteAddressType.SelectedAddress) {
			selectedFavoriteAddressDto = (FavoriteAddressDto) bundle.getSerializable(getString(R.string.bundle_key_selected_address));
			newSelectedAddressId = selectedFavoriteAddressDto.getId().toString();
		} else {
			gps = new Gps();
			newSelectedAddressId = FavoriteAddressType.CurrentLocation.name();
		}

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		sharedPreferences.edit().putString(getString(R.string.pref_key_last_selected_favorite_address_id), newSelectedAddressId).apply();

		weatherViewModel = new ViewModelProvider(getActivity()).get(WeatherViewModel.class);
		iLoadImgOfCurrentConditions = weatherViewModel.getiLoadImgOfCurrentConditions();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentWeatherBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.customProgressView.setContentView(binding.scrollView);

		if (favoriteAddressType == FavoriteAddressType.SelectedAddress) {
			binding.addressName.setText(selectedFavoriteAddressDto.getAddress());
			mainWeatherSourceType = getMainWeatherSourceType(selectedFavoriteAddressDto.getCountryCode());
			countryCode = selectedFavoriteAddressDto.getCountryCode();
			addressName = selectedFavoriteAddressDto.getAddress();
			this.currentLocationLatitude = Double.parseDouble(selectedFavoriteAddressDto.getLatitude());
			this.currentLocationLongitude = Double.parseDouble(selectedFavoriteAddressDto.getLongitude());
			refreshForSelectedLocation();
		} else if (favoriteAddressType == FavoriteAddressType.CurrentLocation) {
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
			final Double lastLatitude = Double.parseDouble(
					sharedPreferences.getString(getString(R.string.pref_key_last_current_location_latitude), "0"));
			final Double lastLongitude = Double.parseDouble(
					sharedPreferences.getString(getString(R.string.pref_key_last_current_location_longitude), "0"));

			if (lastLatitude == 0 && lastLongitude == 0) {
				requestCurrentLocation();
			} else {
				this.currentLocationLatitude = lastLatitude;
				this.currentLocationLongitude = lastLongitude;
				setCurrentLocationAddressName(lastLatitude, lastLongitude, true);
			}
		}
	}


	private void setCurrentLocationAddressName(Double latitude, Double longitude, boolean refresh) {
		Geocoding.geocoding(getContext(), latitude, longitude, new Geocoding.GeocodingCallback() {
			@Override
			public void onGeocodingResult(List<Address> addressList) {
				if (addressList.isEmpty()) {

				} else {
					Address address = addressList.get(0);
					String addressName = getString(R.string.current_location) + ", " + address.getAddressLine(0);
					if (getActivity() != null) {
						getActivity().runOnUiThread(new Runnable() {
							@Override
							public void run() {
								binding.addressName.setText(addressName);
								weatherViewModel.setCurrentLocationAddressName(addressName);
								TimeZone timeZone = TimeZone.getDefault();

								if (refresh) {
									mainWeatherSourceType = getMainWeatherSourceType(address.getCountryCode());
									countryCode = address.getCountryCode();
									WeatherFragment.this.addressName = address.getAddressLine(0);
									refresh(latitude, longitude);
								}
							}
						});

					}
				}

			}
		});
	}

	private MainProcessing.WeatherSourceType getMainWeatherSourceType(@NonNull String countryCode) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		MainProcessing.WeatherSourceType mainWeatherSourceType = null;

		if (sharedPreferences.getBoolean(getString(R.string.pref_key_accu_weather), true)) {
			mainWeatherSourceType = MainProcessing.WeatherSourceType.ACCU_WEATHER;
		} else {
			mainWeatherSourceType = MainProcessing.WeatherSourceType.OPEN_WEATHER_MAP;
		}

		if (countryCode.equals("KR")) {
			boolean kmaIsTopPriority = sharedPreferences.getBoolean(getString(R.string.pref_key_kma_top_priority), true);
			if (kmaIsTopPriority) {
				mainWeatherSourceType = MainProcessing.WeatherSourceType.KMA;
			}
		}

		return mainWeatherSourceType;
	}

	public void reDraw() {
		if (getChildFragmentManager().getFragments().size() > 0) {

			if (favoriteAddressType == FavoriteAddressType.SelectedAddress) {
				refreshForSelectedLocation();
			} else {
				refresh(currentLocationLatitude, currentLocationLongitude);
			}
		}
	}

	public void refresh() {
		if (favoriteAddressType == FavoriteAddressType.SelectedAddress) {
			finalResponseMap.remove(selectedFavoriteAddressDto.getLatitude() + selectedFavoriteAddressDto.getLongitude());
			refreshForSelectedLocation();
		} else {
			finalResponseMap.remove(currentLocationLatitude.toString() + currentLocationLongitude.toString());
			refresh(currentLocationLatitude, currentLocationLongitude);
		}
	}

	public void refreshForCurrentLocation(Location currentLocation) {
		if (currentLocationLatitude != null) {
			finalResponseMap.remove(currentLocationLatitude.toString() + currentLocationLongitude.toString());
		}

		this.currentLocationLatitude = currentLocation.getLatitude();
		this.currentLocationLongitude = currentLocation.getLongitude();
		setCurrentLocationAddressName(currentLocation.getLatitude(), currentLocation.getLongitude(), true);
	}

	public void refreshForSelectedLocation() {
		refresh(Double.parseDouble(selectedFavoriteAddressDto.getLatitude()),
				Double.parseDouble(selectedFavoriteAddressDto.getLongitude()));
	}

	private void refresh(Double latitude, Double longitude) {
		String latLon = latitude.toString() + longitude.toString();
		if (finalResponseMap.containsKey(latLon)) {
			setWeatherFragments(finalResponseMap.get(latLon), latitude, longitude);
		} else {
			binding.customProgressView.onStartedProcessingData(getString(R.string.msg_refreshing_weather_data));

			Set<MainProcessing.WeatherSourceType> weatherSourceTypeSet = new ArraySet<>();
			weatherSourceTypeSet.add(MainProcessing.WeatherSourceType.AQICN);
			weatherSourceTypeSet.add(mainWeatherSourceType);

			//메인 날씨 제공사만 요청
			MainProcessing.WeatherSourceType secondWeatherSourceType = null;
			switch (mainWeatherSourceType) {
				case KMA:
					MainProcessing.WeatherSourceType defaultWeatherSourceType = getMainWeatherSourceType("");
					secondWeatherSourceType = defaultWeatherSourceType;
					break;
				case OPEN_WEATHER_MAP:
					secondWeatherSourceType = MainProcessing.WeatherSourceType.ACCU_WEATHER;
					break;
				case ACCU_WEATHER:
					secondWeatherSourceType = MainProcessing.WeatherSourceType.OPEN_WEATHER_MAP;
					break;
			}
			final MainProcessing.WeatherSourceType finalSecondWeatherSourceType = secondWeatherSourceType;

			Log.e(RetrofitClient.LOG_TAG, "날씨 정보 요청, " + weatherSourceTypeSet.toString());

			MainProcessing.downloadAllWeatherData(getContext(), latitude.toString(), longitude.toString(), weatherSourceTypeSet,
					new MultipleJsonDownloader<JsonElement>() {
						@Override
						public void onResult() {
							Set<Map.Entry<MainProcessing.WeatherSourceType, ArrayMap<RetrofitClient.ServiceType, ResponseResult<JsonElement>>>> entrySet = responseMap.entrySet();
							//메인 날씨 제공사의 데이터가 정상이면 메인 날씨 제공사의 프래그먼트들을 설정하고 값을 표시한다.
							//메인 날씨 제공사의 응답이 불량이면 정상인 다른 날씨 제공사에 데이터를 재 요청 한다.

							for (Map.Entry<MainProcessing.WeatherSourceType, ArrayMap<RetrofitClient.ServiceType, ResponseResult<JsonElement>>> entry : entrySet) {
								MainProcessing.WeatherSourceType weatherSourceType = entry.getKey();

								if (weatherSourceType == mainWeatherSourceType) {
									for (ResponseResult<JsonElement> responseResult : entry.getValue().values()) {
										if (responseResult.getResponse() == null) {
											mainWeatherSourceType = finalSecondWeatherSourceType;
											refresh(latitude, longitude);
											return;
										}
									}
								}
							}

							if (getActivity() != null) {
								finalResponseMap.put(latLon, this);
								setWeatherFragments(this, latitude, longitude);

								getActivity().runOnUiThread(new Runnable() {
									@Override
									public void run() {
										binding.customProgressView.onSuccessfulProcessingData();
									}
								});
							}
						}
					});

		}


	}

	private void setWeatherFragments(MultipleJsonDownloader<JsonElement> multipleJsonDownloader, Double latitude, Double longitude) {
		Map<MainProcessing.WeatherSourceType, ArrayMap<RetrofitClient.ServiceType, MultipleJsonDownloader.ResponseResult<JsonElement>>> responseMap = multipleJsonDownloader.getResponseMap();
		Gson gson = new Gson();

		ArrayMap<RetrofitClient.ServiceType, MultipleJsonDownloader.ResponseResult<JsonElement>> aqiArrayMap = responseMap.get(
				MainProcessing.WeatherSourceType.AQICN);
		MultipleJsonDownloader.ResponseResult<JsonElement> aqicnResponse = aqiArrayMap.get(
				RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED);
		GeolocalizedFeedResponse airQualityResponse = gson.fromJson(aqicnResponse.getResponse().body().toString(),
				GeolocalizedFeedResponse.class);

		ArrayMap<RetrofitClient.ServiceType, MultipleJsonDownloader.ResponseResult<JsonElement>> arrayMap = responseMap.get(
				mainWeatherSourceType);

		Fragment simpleCurrentConditionsFragment = null;
		Fragment simpleHourlyForecastFragment = null;
		Fragment simpleDailyForecastFragment = null;
		Fragment detailCurrentConditionsFragment = null;

		String currentConditionsWeatherVal = null;

		TimeZone timeZone = TimeZone.getDefault();

		switch (mainWeatherSourceType) {
			case KMA:
				FinalCurrentConditions finalCurrentConditions = KmaResponseProcessor.getFinalCurrentConditions(
						KmaResponseProcessor.getUltraSrtNcstObjFromJson(
								arrayMap.get(RetrofitClient.ServiceType.ULTRA_SRT_NCST).getResponse().body().toString()));
				List<FinalHourlyForecast> finalHourlyForecastList = KmaResponseProcessor.getFinalHourlyForecastList(
						KmaResponseProcessor.getUltraSrtFcstObjFromJson(
								arrayMap.get(RetrofitClient.ServiceType.ULTRA_SRT_FCST).getResponse().body().toString()),
						KmaResponseProcessor.getVilageFcstObjFromJson(
								arrayMap.get(RetrofitClient.ServiceType.VILAGE_FCST).getResponse().body().toString()));
				List<FinalDailyForecast> finalDailyForecastList = KmaResponseProcessor.getFinalDailyForecastList(
						KmaResponseProcessor.getMidLandObjFromJson(
								arrayMap.get(RetrofitClient.ServiceType.MID_LAND_FCST).getResponse().body().toString()),
						KmaResponseProcessor.getMidTaObjFromJson(
								arrayMap.get(RetrofitClient.ServiceType.MID_TA_FCST).getResponse().body().toString()),
						Long.parseLong(multipleJsonDownloader.get("tmFc")));

				KmaSimpleCurrentConditionsFragment kmaSimpleCurrentConditionsFragment = new KmaSimpleCurrentConditionsFragment();
				KmaSimpleHourlyForecastFragment kmaSimpleHourlyForecastFragment = new KmaSimpleHourlyForecastFragment();
				KmaSimpleDailyForecastFragment kmaSimpleDailyForecastFragment = new KmaSimpleDailyForecastFragment();
				KmaDetailCurrentConditionsFragment kmaDetailCurrentConditionsFragment = new KmaDetailCurrentConditionsFragment();

				kmaSimpleCurrentConditionsFragment.setFinalCurrentConditions(finalCurrentConditions).setFinalHourlyForecast(
						finalHourlyForecastList.get(0)).setAirQualityResponse(airQualityResponse);
				kmaSimpleHourlyForecastFragment.setFinalHourlyForecastList(finalHourlyForecastList);
				kmaSimpleDailyForecastFragment.setFinalDailyForecastList(finalDailyForecastList);
				kmaDetailCurrentConditionsFragment.setFinalCurrentConditions(finalCurrentConditions);

				simpleCurrentConditionsFragment = kmaSimpleCurrentConditionsFragment;
				simpleHourlyForecastFragment = kmaSimpleHourlyForecastFragment;
				simpleDailyForecastFragment = kmaSimpleDailyForecastFragment;
				detailCurrentConditionsFragment = kmaDetailCurrentConditionsFragment;

				String sky = finalHourlyForecastList.get(0).getSky();
				String pty = finalCurrentConditions.getPrecipitationType();

				currentConditionsWeatherVal = pty.equals("0") ? sky + "_sky" : pty + "_pty";

				timeZone = TimeZone.getTimeZone("Asia/Seoul");
				break;
			case ACCU_WEATHER:
				AccuSimpleCurrentConditionsFragment accuSimpleCurrentConditionsFragment = new AccuSimpleCurrentConditionsFragment();
				AccuSimpleHourlyForecastFragment accuSimpleHourlyForecastFragment = new AccuSimpleHourlyForecastFragment();
				AccuSimpleDailyForecastFragment accuSimpleDailyForecastFragment = new AccuSimpleDailyForecastFragment();
				AccuDetailCurrentConditionsFragment accuDetailCurrentConditionsFragment = new AccuDetailCurrentConditionsFragment();

				CurrentConditionsResponse currentConditionsResponse = AccuWeatherResponseProcessor.getCurrentConditionsObjFromJson(
						arrayMap.get(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS).getResponse().body());

				accuSimpleCurrentConditionsFragment.setCurrentConditionsResponse(currentConditionsResponse).setAirQualityResponse(
						airQualityResponse);
				accuSimpleHourlyForecastFragment.setTwelveHoursOfHourlyForecastsResponse(
						AccuWeatherResponseProcessor.getHourlyForecastObjFromJson(
								arrayMap.get(RetrofitClient.ServiceType.ACCU_12_HOURLY).getResponse().body()));
				accuSimpleDailyForecastFragment.setFiveDaysOfDailyForecastsResponse(
						AccuWeatherResponseProcessor.getDailyForecastObjFromJson(
								arrayMap.get(RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY).getResponse().body().toString()));
				accuDetailCurrentConditionsFragment.setCurrentConditionsResponse(currentConditionsResponse);

				simpleCurrentConditionsFragment = accuSimpleCurrentConditionsFragment;
				simpleHourlyForecastFragment = accuSimpleHourlyForecastFragment;
				simpleDailyForecastFragment = accuSimpleDailyForecastFragment;
				detailCurrentConditionsFragment = accuDetailCurrentConditionsFragment;

				currentConditionsWeatherVal = currentConditionsResponse.getItems().get(0).getWeatherIcon();

				try {
					timeZone =
							AccuWeatherResponseProcessor.getTimeZone(currentConditionsResponse.getItems().get(0).getLocalObservationDateTime());
				} catch (ParseException e) {
					e.printStackTrace();
				}
				break;
			case OPEN_WEATHER_MAP:
				OwmSimpleCurrentConditionsFragment owmSimpleCurrentConditionsFragment = new OwmSimpleCurrentConditionsFragment();
				OwmSimpleHourlyForecastFragment owmSimpleHourlyForecastFragment = new OwmSimpleHourlyForecastFragment();
				OwmSimpleDailyForecastFragment owmSimpleDailyForecastFragment = new OwmSimpleDailyForecastFragment();
				OwmDetailCurrentConditionsFragment owmDetailCurrentConditionsFragment = new OwmDetailCurrentConditionsFragment();

				OneCallResponse oneCallResponse = OpenWeatherMapResponseProcessor.getOneCallObjFromJson(
						arrayMap.get(RetrofitClient.ServiceType.OWM_ONE_CALL).getResponse().body().toString());

				owmSimpleCurrentConditionsFragment.setOneCallResponse(oneCallResponse).setAirQualityResponse(airQualityResponse);
				owmSimpleHourlyForecastFragment.setOneCallResponse(oneCallResponse);
				owmSimpleDailyForecastFragment.setOneCallResponse(oneCallResponse);
				owmDetailCurrentConditionsFragment.setOneCallResponse(oneCallResponse);

				simpleCurrentConditionsFragment = owmSimpleCurrentConditionsFragment;
				simpleHourlyForecastFragment = owmSimpleHourlyForecastFragment;
				simpleDailyForecastFragment = owmSimpleDailyForecastFragment;
				detailCurrentConditionsFragment = owmDetailCurrentConditionsFragment;

				currentConditionsWeatherVal = oneCallResponse.getCurrent().getWeather().get(0).getId();

				timeZone =
						OpenWeatherMapResponseProcessor.getTimeZone(oneCallResponse);
				break;
		}
		iLoadImgOfCurrentConditions.loadImgOfCurrentConditions(mainWeatherSourceType, currentConditionsWeatherVal, latitude, longitude, timeZone);


		final Bundle defaultBundle = new Bundle();
		defaultBundle.putDouble(getString(R.string.bundle_key_latitude), currentLocationLatitude);
		defaultBundle.putDouble(getString(R.string.bundle_key_longitude), currentLocationLongitude);
		defaultBundle.putString(getString(R.string.bundle_key_address_name), addressName);
		defaultBundle.putString(getString(R.string.bundle_key_country_code), countryCode);
		defaultBundle.putSerializable(getString(R.string.bundle_key_main_weather_data_source), mainWeatherSourceType);
		defaultBundle.putSerializable(getString(R.string.bundle_key_timezone), timeZone);


		SimpleAirQualityFragment simpleAirQualityFragment = new SimpleAirQualityFragment();
		simpleAirQualityFragment.setGeolocalizedFeedResponse(airQualityResponse);
		simpleAirQualityFragment.setArguments(defaultBundle);

		Fragment sunSetRiseFragment = new SunsetriseFragment();
		sunSetRiseFragment.setArguments(defaultBundle);

		if (getActivity() != null) {
			Fragment finalSimpleDailyForecastFragment = simpleDailyForecastFragment;
			Fragment finalSimpleHourlyForecastFragment = simpleHourlyForecastFragment;
			Fragment finalSimpleCurrentConditionsFragment = simpleCurrentConditionsFragment;
			Fragment finalDetailCurrentConditionsFragment = detailCurrentConditionsFragment;

			finalSimpleHourlyForecastFragment.setArguments(defaultBundle);
			finalSimpleDailyForecastFragment.setArguments(defaultBundle);
			finalSimpleCurrentConditionsFragment.setArguments(defaultBundle);
			finalDetailCurrentConditionsFragment.setArguments(defaultBundle);

			ValueUnits clockUnit = ValueUnits.enumOf(
					sharedPreferences.getString(getString(R.string.pref_key_unit_clock), ValueUnits.clock12.name()));
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					createWeatherDataSourcePicker(countryCode);
					LocalDateTime localDateTime = multipleJsonDownloader.getLocalDateTime();
					DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(clockUnit == ValueUnits.clock12 ? "M.d E a h:mm"
							: "M.d E HH:mm", Locale.getDefault());
					binding.updatedDatetime.setText(localDateTime.format(dateTimeFormatter));

					getChildFragmentManager().beginTransaction().replace(binding.simpleCurrentConditions.getId(),
							finalSimpleCurrentConditionsFragment, getString(R.string.tag_simple_current_conditions_fragment)).replace(
							binding.simpleHourlyForecast.getId(), finalSimpleHourlyForecastFragment,
							getString(R.string.tag_simple_hourly_forecast_fragment)).replace(binding.simpleDailyForecast.getId(),
							finalSimpleDailyForecastFragment, getString(R.string.tag_simple_daily_forecast_fragment)).replace(
							binding.detailCurrentConditions.getId(), finalDetailCurrentConditionsFragment,
							getString(R.string.tag_detail_current_conditions_fragment)).replace(binding.simpleAirQuality.getId(),
							simpleAirQualityFragment, getString(R.string.tag_simple_air_quality_fragment)).replace(
							binding.sunSetRise.getId(), sunSetRiseFragment, getString(R.string.tag_sun_set_rise_fragment)).commit();
				}
			});

		}
	}

	private void createWeatherDataSourcePicker(String countryCode) {
		switch (mainWeatherSourceType) {
			case KMA:
				binding.datasource.setText(R.string.kma);
				break;
			case ACCU_WEATHER:
				binding.datasource.setText(R.string.accu_weather);
				break;
			case OPEN_WEATHER_MAP:
				binding.datasource.setText(R.string.owm);
				break;
		}

		binding.datasource.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				CharSequence[] items = new CharSequence[countryCode.equals("KR") ? 3 : 2];
				int checkedItemIdx = 0;

				if (countryCode.equals("KR")) {
					items[0] = getString(R.string.kma);
					items[1] = getString(R.string.accu_weather);
					items[2] = getString(R.string.owm);

					checkedItemIdx = (mainWeatherSourceType == MainProcessing.WeatherSourceType.KMA) ? 0 : (mainWeatherSourceType == MainProcessing.WeatherSourceType.ACCU_WEATHER) ? 1 : 2;
				} else {
					items[0] = getString(R.string.accu_weather);
					items[1] = getString(R.string.owm);
					checkedItemIdx = mainWeatherSourceType == MainProcessing.WeatherSourceType.ACCU_WEATHER ? 0 : 1;
				}
				final int finalCheckedItemIdx = checkedItemIdx;

				new MaterialAlertDialogBuilder(getActivity()).setTitle(R.string.title_pick_weather_data_source).setSingleChoiceItems(items,
						checkedItemIdx, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int index) {
								if (finalCheckedItemIdx != index) {
									if (!items[index].equals(getString(R.string.kma))) {
										// 선택된 제공사가 accu, owm 둘 중 하나이면 우선순위 변경
										boolean accu = items[index].equals(getString(R.string.accu_weather));

										SharedPreferences.Editor editor = sharedPreferences.edit();
										editor.putBoolean(getString(R.string.pref_key_accu_weather), accu);
										editor.putBoolean(getString(R.string.pref_key_open_weather_map), !accu);
										editor.apply();

										mainWeatherSourceType = accu ? MainProcessing.WeatherSourceType.ACCU_WEATHER : MainProcessing.WeatherSourceType.OPEN_WEATHER_MAP;
									} else {
										mainWeatherSourceType = MainProcessing.WeatherSourceType.KMA;
									}
									refresh();
								}
								dialogInterface.dismiss();
							}
						}).create().show();
			}
		});
	}

	public FavoriteAddressType getFavoriteAddressType() {
		return favoriteAddressType;
	}

	public FavoriteAddressDto getSelectedFavoriteAddressDto() {
		return selectedFavoriteAddressDto;
	}

	public boolean isFragmentUsingCurrentLocation() {
		return favoriteAddressType == FavoriteAddressType.CurrentLocation;
	}

	@Override
	public void requestCurrentLocation() {
		if (!gps.isProcessing()) {
			binding.customProgressView.onStartedProcessingData(getString(R.string.msg_finding_current_location));
			gps.runGps(requireActivity(), locationCallback, requestOnGpsLauncher, requestLocationPermissionLauncher);
		}
	}

	private final ActivityResultCallback<ActivityResult> requestOnGpsResultCallback = new ActivityResultCallback<ActivityResult>() {
		@Override
		public void onActivityResult(ActivityResult result) {
			requestCurrentLocation();
		}
	};

	private final ActivityResultLauncher<Intent> requestOnGpsLauncher = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(), requestOnGpsResultCallback);

	private final ActivityResultLauncher<String> requestLocationPermissionLauncher = registerForActivityResult(
			new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
				@Override
				public void onActivityResult(Boolean isGranted) {
					onResultLocationPermission(isGranted);
				}
			});

	protected void onResultLocationPermission(boolean isGranted) {
		if (isGranted) {
			gps.clear();
			requestCurrentLocation();
		} else {
			Toast.makeText(getContext(), R.string.message_needs_location_permission, Toast.LENGTH_SHORT).show();
			locationCallback.onFailed();
		}
	}

	private final Gps.LocationCallback locationCallback = new Gps.LocationCallback() {
		@Override
		public void onSuccessful(Location location) {
			SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(getContext()).edit();
			editor.putString(getString(R.string.pref_key_last_current_location_latitude), String.valueOf(location.getLatitude())).putString(
					getString(R.string.pref_key_last_current_location_longitude), String.valueOf(location.getLongitude())).apply();

			refreshForCurrentLocation(location);
		}

		@Override
		public void onFailed() {
			binding.customProgressView.onFailedProcessingData(getString(R.string.update_failed));
		}
	};

}