package com.lifedawn.bestweather.weathers;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentResultListener;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.Geocoding;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.interfaces.IGps;
import com.lifedawn.bestweather.commons.views.ProgressDialog;
import com.lifedawn.bestweather.databinding.FragmentWeatherBinding;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.responses.accuweather.currentconditions.CurrentConditionsResponse;
import com.lifedawn.bestweather.retrofit.responses.aqicn.GeolocalizedFeedResponse;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.onecall.OneCallResponse;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.weathers.dataprocessing.request.MainProcessing;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AccuWeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
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
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;

import retrofit2.Response;


public class WeatherFragment extends Fragment {
	private FragmentWeatherBinding binding;
	private FavoriteAddressDto selectedFavoriteAddressDto;
	private LocationType locationType;
	private WeatherViewModel.ILoadImgOfCurrentConditions iLoadImgOfCurrentConditions;
	private WeatherViewModel weatherViewModel;

	private MainProcessing.WeatherSourceType mainWeatherSourceType;
	private Double latitude;
	private Double longitude;
	private String countryCode;
	private String addressName;
	private SharedPreferences sharedPreferences;

	private IGps iGps;

	public static final Map<String, MultipleJsonDownloader<JsonElement>> finalResponseMap = new HashMap<>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
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
		getParentFragmentManager().setFragmentResultListener(getString(R.string.key_current_location), this, new FragmentResultListener() {
			@Override
			public void onFragmentResult(@NonNull @NotNull String requestKey, @NonNull @NotNull Bundle result) {
				getParentFragmentManager().clearFragmentResultListener(requestKey);
				getParentFragmentManager().clearFragmentResult(requestKey);

				locationType = LocationType.CurrentLocation;
				sharedPreferences.edit().putString(getString(R.string.pref_key_last_selected_favorite_address_id), "")
						.putString(getString(R.string.pref_key_last_selected_location_type), locationType.name()).apply();
				iGps = (IGps) result.getSerializable(getString(R.string.bundle_key_igps));

				latitude = Double.parseDouble(
						sharedPreferences.getString(getString(R.string.pref_key_last_current_location_latitude), "0.0"));
				longitude = Double.parseDouble(
						sharedPreferences.getString(getString(R.string.pref_key_last_current_location_longitude), "0.0"));

				if (latitude == 0.0 && longitude == 0.0) {
					//최근에 현재위치로 잡힌 위치가 없으므로 현재 위치 요청
					iGps.requestCurrentLocation();
				} else {
					//위/경도에 해당하는 지역명을 불러오고, 날씨 데이터 다운로드
					//이미 존재하는 날씨 데이터면 다운로드X
					requestAddressOfLocation(latitude, longitude, true);
				}
			}
		});
		getParentFragmentManager().setFragmentResultListener(getString(R.string.key_selected_location), this, new FragmentResultListener() {
			@Override
			public void onFragmentResult(@NonNull @NotNull String requestKey, @NonNull @NotNull Bundle result) {
				getParentFragmentManager().clearFragmentResultListener(requestKey);
				getParentFragmentManager().clearFragmentResult(requestKey);

				locationType = LocationType.SelectedAddress;
				selectedFavoriteAddressDto = (FavoriteAddressDto) result.getSerializable(getString(R.string.bundle_key_selected_address_dto));
				iGps = (IGps) result.getSerializable(getString(R.string.bundle_key_igps));

				sharedPreferences.edit().putString(getString(R.string.pref_key_last_selected_favorite_address_id), selectedFavoriteAddressDto.getId().toString())
						.putString(getString(R.string.pref_key_last_selected_location_type), locationType.name()).apply();

				mainWeatherSourceType = getMainWeatherSourceType(selectedFavoriteAddressDto.getCountryCode());
				countryCode = selectedFavoriteAddressDto.getCountryCode();
				addressName = selectedFavoriteAddressDto.getAddress();
				latitude = Double.parseDouble(selectedFavoriteAddressDto.getLatitude());
				longitude = Double.parseDouble(selectedFavoriteAddressDto.getLongitude());

				binding.addressName.setText(addressName);
				refresh(latitude, longitude);
			}
		});
	}

	private boolean containWeatherData(Double latitude, Double longitude) {
		return finalResponseMap.containsKey(latitude.toString() + longitude.toString());
	}


	private void requestAddressOfLocation(Double latitude, Double longitude, boolean refresh) {
		Geocoding.geocoding(getContext(), latitude, longitude, new Geocoding.GeocodingCallback() {
			@Override
			public void onGeocodingResult(List<Address> addressList) {
				setAddressNameOfLocation(addressList);
				if (refresh) {
					refresh(latitude, longitude);
				}
			}
		});
	}

	private void setAddressNameOfLocation(List<Address> addressList) {
		if (addressList.isEmpty()) {
			//검색 결과가 없으면 미 표시
		} else {
			Address address = addressList.get(0);
			addressName = address.getAddressLine(0);
			mainWeatherSourceType = getMainWeatherSourceType(address.getCountryCode());
			countryCode = address.getCountryCode();

			if (getActivity() != null) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						binding.addressName.setText(addressName);
						weatherViewModel.setCurrentLocationAddressName(addressName);
					}
				});

			}
		}
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
		if (latitude == 0.0 || longitude == 0.0) {
			return;
		}
		refresh(latitude, longitude);
	}

	public void onChangedCurrentLocation(Location currentLocation) {
		this.latitude = currentLocation.getLatitude();
		this.longitude = currentLocation.getLongitude();
		requestAddressOfLocation(latitude, longitude, true);
	}

	public void forceRefresh() {
		finalResponseMap.remove(latitude.toString() + longitude.toString());
		refresh(latitude, longitude);
	}

	private void refresh(Double latitude, Double longitude) {
		String latLon = latitude.toString() + longitude.toString();
		if (containWeatherData(latitude, longitude)) {
			setWeatherFragments(finalResponseMap.get(latLon), latitude, longitude, null);
		} else {
			AlertDialog dialog = ProgressDialog.show(getActivity(), getString(R.string.msg_refreshing_weather_data));

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
											dialog.dismiss();
											refresh(latitude, longitude);
											return;
										}
									}
								}
							}
							finalResponseMap.put(latLon, this);
							setWeatherFragments(this, latitude, longitude, dialog);
						}
					});

		}


	}

	private void setWeatherFragments(MultipleJsonDownloader<JsonElement> multipleJsonDownloader, Double latitude, Double longitude,
	                                 @Nullable AlertDialog dialog) {
		Map<MainProcessing.WeatherSourceType, ArrayMap<RetrofitClient.ServiceType, MultipleJsonDownloader.ResponseResult<JsonElement>>> responseMap = multipleJsonDownloader.getResponseMap();

		ArrayMap<RetrofitClient.ServiceType, MultipleJsonDownloader.ResponseResult<JsonElement>> aqiArrayMap = responseMap.get(
				MainProcessing.WeatherSourceType.AQICN);
		MultipleJsonDownloader.ResponseResult<JsonElement> aqicnResponse = aqiArrayMap.get(
				RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED);
		GeolocalizedFeedResponse airQualityResponse = AqicnResponseProcessor.getAirQualityObjFromJson((Response<JsonElement>) aqicnResponse.getResponse());

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
					timeZone = AccuWeatherResponseProcessor.getTimeZone(
							currentConditionsResponse.getItems().get(0).getLocalObservationDateTime());
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

				timeZone = OpenWeatherMapResponseProcessor.getTimeZone(oneCallResponse);
				break;
		}
		iLoadImgOfCurrentConditions.loadImgOfCurrentConditions(mainWeatherSourceType, currentConditionsWeatherVal, latitude, longitude,
				timeZone);


		final Bundle defaultBundle = new Bundle();
		defaultBundle.putDouble(getString(R.string.bundle_key_latitude), this.latitude);
		defaultBundle.putDouble(getString(R.string.bundle_key_longitude), this.longitude);
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
					DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(
							clockUnit == ValueUnits.clock12 ? "M.d E a h:mm" : "M.d E HH:mm", Locale.getDefault());
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

					if (dialog != null) {
						dialog.dismiss();
					}
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
									refresh(latitude, longitude);
								}
								dialogInterface.dismiss();
							}
						}).create().show();
			}
		});
	}

	public LocationType getLocationType() {
		return locationType;
	}

	public FavoriteAddressDto getSelectedFavoriteAddressDto() {
		return selectedFavoriteAddressDto;
	}

}