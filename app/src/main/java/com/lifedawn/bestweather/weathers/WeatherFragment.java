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
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.gson.JsonElement;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.Geocoding;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestAccu;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestAqicn;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestKma;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestOwm;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestWeatherSource;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.commons.interfaces.IGps;
import com.lifedawn.bestweather.commons.views.ProgressDialog;
import com.lifedawn.bestweather.databinding.FragmentWeatherBinding;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.openweathermap.OneCallParameter;
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
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
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

	private WeatherSourceType mainWeatherSourceType;
	private Double latitude;
	private Double longitude;
	private String countryCode;
	private String addressName;
	private SharedPreferences sharedPreferences;
	private IGps iGps;

	public static final Map<String, WeatherResponseObj> finalResponseMap = new HashMap<>();

	static class WeatherResponseObj {
		final MultipleJsonDownloader<JsonElement> multipleJsonDownloader;
		final WeatherSourceType requestWeatherSourceType;

		public WeatherResponseObj(MultipleJsonDownloader<JsonElement> multipleJsonDownloader, WeatherSourceType requestWeatherSourceType) {
			this.multipleJsonDownloader = multipleJsonDownloader;
			this.requestWeatherSourceType = requestWeatherSourceType;
		}
	}

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
		MobileAds.initialize(getContext());
		AdRequest adRequest = new AdRequest.Builder().build();
		binding.adViewBelowAirQuality.loadAd(adRequest);
		binding.adViewBottom.loadAd(adRequest);

		getParentFragmentManager().setFragmentResultListener(getString(R.string.key_current_location), this, new FragmentResultListener() {
			@Override
			public void onFragmentResult(@NonNull @NotNull String requestKey, @NonNull @NotNull Bundle result) {
				getParentFragmentManager().clearFragmentResultListener(requestKey);
				getParentFragmentManager().clearFragmentResult(requestKey);

				locationType = LocationType.CurrentLocation;
				sharedPreferences.edit().putInt(getString(R.string.pref_key_last_selected_favorite_address_id), -1).putString(
						getString(R.string.pref_key_last_selected_location_type), locationType.name()).apply();
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
					requestAddressOfLocation(latitude, longitude, !containWeatherData(latitude, longitude));
				}
			}
		});
		getParentFragmentManager().setFragmentResultListener(getString(R.string.key_selected_location), this, new FragmentResultListener() {
			@Override
			public void onFragmentResult(@NonNull @NotNull String requestKey, @NonNull @NotNull Bundle result) {
				getParentFragmentManager().clearFragmentResultListener(requestKey);
				getParentFragmentManager().clearFragmentResult(requestKey);

				locationType = LocationType.SelectedAddress;
				selectedFavoriteAddressDto = (FavoriteAddressDto) result.getSerializable(
						getString(R.string.bundle_key_selected_address_dto));
				iGps = (IGps) result.getSerializable(getString(R.string.bundle_key_igps));

				sharedPreferences.edit().putInt(getString(R.string.pref_key_last_selected_favorite_address_id),
						selectedFavoriteAddressDto.getId()).putString(getString(R.string.pref_key_last_selected_location_type),
						locationType.name()).apply();

				mainWeatherSourceType = getMainWeatherSourceType(selectedFavoriteAddressDto.getCountryCode());
				countryCode = selectedFavoriteAddressDto.getCountryCode();
				addressName = selectedFavoriteAddressDto.getAddress();
				latitude = Double.parseDouble(selectedFavoriteAddressDto.getLatitude());
				longitude = Double.parseDouble(selectedFavoriteAddressDto.getLongitude());

				binding.addressName.setText(addressName);

				if (containWeatherData(latitude, longitude)) {
					//기존 데이터 표시
					mainWeatherSourceType = finalResponseMap.get(latitude.toString() + longitude.toString()).requestWeatherSourceType;
					reDraw();
				} else {
					requestNewData();
				}
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
				if (getActivity() != null) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							if (addressList.isEmpty()) {
								//검색 결과가 없으면 주소 정보 미 표시하고 데이터 로드
								mainWeatherSourceType = getMainWeatherSourceType("");
								countryCode = "";
								addressName = getString(R.string.unknown_address);

								String addressStr = getString(R.string.current_location) + " : " + addressName;
								binding.addressName.setText(addressStr);
								weatherViewModel.setCurrentLocationAddressName(addressName);
							} else {
								Address address = addressList.get(0);
								addressName = address.getAddressLine(0);
								mainWeatherSourceType = getMainWeatherSourceType(address.getCountryCode());
								countryCode = address.getCountryCode();

								String addressStr = getString(R.string.current_location) + " : " + addressName;
								binding.addressName.setText(addressStr);
								weatherViewModel.setCurrentLocationAddressName(addressName);
							}


							if (refresh) {
								requestNewData();
							} else {
								//이미 데이터가 있으면 다시 그림
								mainWeatherSourceType = finalResponseMap.get(
										latitude.toString() + longitude.toString()).requestWeatherSourceType;
								reDraw();
							}
						}
					});

				}

			}
		});
	}

	private WeatherSourceType getMainWeatherSourceType(@NonNull String countryCode) {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		WeatherSourceType mainWeatherSourceType = null;

		if (sharedPreferences.getBoolean(getString(R.string.pref_key_accu_weather), true)) {
			mainWeatherSourceType = WeatherSourceType.ACCU_WEATHER;
		} else {
			mainWeatherSourceType = WeatherSourceType.OPEN_WEATHER_MAP;
		}

		if (countryCode.equals("KR")) {
			boolean kmaIsTopPriority = sharedPreferences.getBoolean(getString(R.string.pref_key_kma_top_priority), true);
			if (kmaIsTopPriority) {
				mainWeatherSourceType = WeatherSourceType.KMA;
			}
		}

		return mainWeatherSourceType;
	}


	public void reDraw() {
		setWeatherFragments(mainWeatherSourceType, finalResponseMap.get(latitude.toString() + longitude.toString()).multipleJsonDownloader,
				latitude, longitude, null);
	}

	public void onChangedCurrentLocation(Location currentLocation) {
		this.latitude = currentLocation.getLatitude();
		this.longitude = currentLocation.getLongitude();
		finalResponseMap.remove(latitude.toString() + longitude.toString());
		requestAddressOfLocation(latitude, longitude, true);
	}


	public void requestNewData() {
		AlertDialog loadingDialog = ProgressDialog.show(getActivity(), getString(R.string.msg_refreshing_weather_data), null);

		ArrayMap<WeatherSourceType, RequestWeatherSource> requestWeatherSources = new ArrayMap<>();
		RequestAqicn requestAqicn = new RequestAqicn();
		requestAqicn.addRequestServiceType(RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED);
		requestWeatherSources.put(WeatherSourceType.AQICN, requestAqicn);

		//메인 날씨 제공사만 요청
		WeatherSourceType requestWeatherSourceType = mainWeatherSourceType;
		setRequestWeatherSourceWithSourceType(requestWeatherSourceType, requestWeatherSources);

		final JsonResultObj jsonResultObj = new JsonResultObj(requestWeatherSourceType, requestWeatherSources);

		MultipleJsonDownloader<JsonElement> multipleJsonDownloader = new MultipleJsonDownloader<JsonElement>() {
			@Override
			public void onResult() {
				jsonResultObj.multipleJsonDownloader = this;
				processOnResult(jsonResultObj);
			}
		};
		multipleJsonDownloader.setLoadingDialog(loadingDialog);

		MainProcessing.requestNewWeatherData(getContext(), latitude, longitude, requestWeatherSources, multipleJsonDownloader);
	}

	private void requestNewDataWithAnotherWeatherSource(WeatherSourceType newWeatherSourceType, WeatherSourceType lastWeatherSourceType) {
		AlertDialog loadingDialog = ProgressDialog.show(getActivity(), getString(R.string.msg_refreshing_weather_data), null);

		ArrayMap<WeatherSourceType, RequestWeatherSource> requestWeatherSources = new ArrayMap<>();
		RequestAqicn requestAqicn = new RequestAqicn();
		requestAqicn.addRequestServiceType(RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED);
		requestWeatherSources.put(WeatherSourceType.AQICN, requestAqicn);

		//메인 날씨 제공사만 요청
		setRequestWeatherSourceWithSourceType(newWeatherSourceType, requestWeatherSources);

		final JsonResultObj jsonResultObj = new JsonResultObj(newWeatherSourceType, requestWeatherSources);
		MultipleJsonDownloader<JsonElement> multipleJsonDownloader = new MultipleJsonDownloader<JsonElement>() {
			@Override
			public void onResult() {
				jsonResultObj.multipleJsonDownloader = this;
				processOnResult(jsonResultObj);
			}
		};
		multipleJsonDownloader.setLoadingDialog(loadingDialog);

		MainProcessing.requestNewWeatherData(getContext(), latitude, longitude, requestWeatherSources, multipleJsonDownloader);
	}

	private void processOnResult(JsonResultObj jsonResultObj) {
		Set<Map.Entry<WeatherSourceType, ArrayMap<RetrofitClient.ServiceType, MultipleJsonDownloader.ResponseResult<JsonElement>>>> entrySet = jsonResultObj.multipleJsonDownloader.getResponseMap().entrySet();
		//메인 날씨 제공사의 데이터가 정상이면 메인 날씨 제공사의 프래그먼트들을 설정하고 값을 표시한다.
		//메인 날씨 제공사의 응답이 불량이면 재 시도, 취소 중 택1 다이얼로그 표시
		for (Map.Entry<WeatherSourceType, ArrayMap<RetrofitClient.ServiceType, MultipleJsonDownloader.ResponseResult<JsonElement>>> entry : entrySet) {
			WeatherSourceType weatherSourceType = entry.getKey();

			if (weatherSourceType == jsonResultObj.requestWeatherSource) {
				for (MultipleJsonDownloader.ResponseResult<JsonElement> responseResult : entry.getValue().values()) {
					if (responseResult.getT() != null) {

						if (getActivity() != null) {
							getActivity().runOnUiThread(new Runnable() {
								@Override
								public void run() {
									//다시시도, 취소 중 택1
									jsonResultObj.multipleJsonDownloader.getLoadingDialog().dismiss();

									Set<WeatherSourceType> otherTypes = getOtherWeatherSourceTypes(jsonResultObj.requestWeatherSource,
											mainWeatherSourceType);

									final String[] failedDialogItems = new String[otherTypes.size() + 2];
									failedDialogItems[0] = getString(R.string.cancel);
									failedDialogItems[1] = getString(R.string.again);

									final WeatherSourceType[] weatherSourceTypeArr = new WeatherSourceType[otherTypes.size()];
									int arrIndex = 2;

									if (otherTypes.contains(WeatherSourceType.KMA)) {
										weatherSourceTypeArr[arrIndex - 2] = WeatherSourceType.KMA;
										failedDialogItems[arrIndex++] = getString(R.string.kma) + ", " + getString(
												R.string.rerequest_another_weather_datasource);
									}
									if (otherTypes.contains(WeatherSourceType.ACCU_WEATHER)) {
										weatherSourceTypeArr[arrIndex - 2] = WeatherSourceType.ACCU_WEATHER;
										failedDialogItems[arrIndex++] = getString(R.string.accu_weather) + ", " + getString(
												R.string.rerequest_another_weather_datasource);
									}
									if (otherTypes.contains(WeatherSourceType.OPEN_WEATHER_MAP)) {
										weatherSourceTypeArr[arrIndex - 2] = WeatherSourceType.OPEN_WEATHER_MAP;
										failedDialogItems[arrIndex] = getString(R.string.owm) + ", " + getString(
												R.string.rerequest_another_weather_datasource);
									}


									AlertDialog failedDialog = new AlertDialog.Builder(getActivity()).setCancelable(false).setTitle(
											R.string.update_failed).setItems(failedDialogItems, new DialogInterface.OnClickListener() {
										@Override
										public void onClick(DialogInterface dialog, int which) {
											if (which == 1) {
												//다시 시도
												jsonResultObj.multipleJsonDownloader.setLoadingDialog(
														reRefreshBySameWeatherSource(jsonResultObj));
											} else if (which == 0) {
												//취소
												if (!containWeatherData(latitude, longitude)) {
													getActivity().finish();
												}
											} else if (which >= 2) {
												jsonResultObj.requestWeatherSource = weatherSourceTypeArr[which - 2];
												jsonResultObj.multipleJsonDownloader.setLoadingDialog(
														reRefreshByAnotherWeatherSource(mainWeatherSourceType, jsonResultObj));
											}
											dialog.dismiss();
										}
									}).create();
									failedDialog.show();

								}
							});
						}

						return;
					}
				}
			}
		}
		//응답 성공 하면
		WeatherResponseObj weatherResponseObj = new WeatherResponseObj(jsonResultObj.multipleJsonDownloader,
				jsonResultObj.requestWeatherSource);

		finalResponseMap.put(latitude.toString() + longitude.toString(), weatherResponseObj);
		setWeatherFragments(jsonResultObj.requestWeatherSource, jsonResultObj.multipleJsonDownloader, latitude, longitude,
				jsonResultObj.multipleJsonDownloader.getLoadingDialog());
	}

	/**
	 * kma, accu, owm
	 * 요청 : kma, 현재 : owm ->  accu
	 * 요청 : kma, 현재 : accu ->  owm
	 * 요청 : kma, 현재 : kma ->  owm, accu
	 * <p>
	 * 요청 : accu, 현재 : accu ->  owm
	 * 요청 : accu, 현재 : accu ->  owm, kma (only kr)
	 * 요청 : accu, 현재 : owm ->  미 표시
	 * 요청 : accu, 현재 : owm ->  kma (only kr)
	 * 요청 : accu, 현재 : kma ->  owm
	 * <p>
	 * 요청 : owm, 현재 : owm ->  accu
	 * 요청 : owm, 현재 : owm ->  accu, kma (only kr)
	 * 요청 : owm, 현재 : accu ->  미 표시
	 * 요청 : owm, 현재 : accu ->  kma (only kr)
	 * 요청 : owm, 현재 : kma ->  accu
	 */
	private Set<WeatherSourceType> getOtherWeatherSourceTypes(WeatherSourceType requestWeatherSourceType,
	                                                          WeatherSourceType lastWeatherSourceType) {
		Set<WeatherSourceType> others = new HashSet<>();

		if (requestWeatherSourceType == WeatherSourceType.KMA) {

			if (lastWeatherSourceType == WeatherSourceType.OPEN_WEATHER_MAP) {
				others.add(WeatherSourceType.ACCU_WEATHER);
			} else if (lastWeatherSourceType == WeatherSourceType.ACCU_WEATHER) {
				others.add(WeatherSourceType.OPEN_WEATHER_MAP);
			} else {
				others.add(WeatherSourceType.OPEN_WEATHER_MAP);
				others.add(WeatherSourceType.ACCU_WEATHER);
			}
		} else if (requestWeatherSourceType == WeatherSourceType.ACCU_WEATHER) {

			if (lastWeatherSourceType == WeatherSourceType.ACCU_WEATHER) {
				if (countryCode.equals("KR")) {
					others.add(WeatherSourceType.OPEN_WEATHER_MAP);
					others.add(WeatherSourceType.KMA);
				} else {
					others.add(WeatherSourceType.OPEN_WEATHER_MAP);
				}
			} else if (lastWeatherSourceType == WeatherSourceType.OPEN_WEATHER_MAP) {
				if (countryCode.equals("KR")) {
					others.add(WeatherSourceType.KMA);
				}
			} else {
				others.add(WeatherSourceType.OPEN_WEATHER_MAP);
			}
		} else if (requestWeatherSourceType == WeatherSourceType.OPEN_WEATHER_MAP) {

			if (lastWeatherSourceType == WeatherSourceType.OPEN_WEATHER_MAP) {
				if (countryCode.equals("KR")) {
					others.add(WeatherSourceType.ACCU_WEATHER);
					others.add(WeatherSourceType.KMA);
				} else {
					others.add(WeatherSourceType.ACCU_WEATHER);
				}
			} else if (lastWeatherSourceType == WeatherSourceType.ACCU_WEATHER) {
				if (countryCode.equals("KR")) {
					others.add(WeatherSourceType.KMA);
				}
			} else {
				others.add(WeatherSourceType.ACCU_WEATHER);
			}
		}

		return others;
	}

	private AlertDialog reRefreshBySameWeatherSource(JsonResultObj jsonResultObj) {
		final AlertDialog loadingDialog = ProgressDialog.show(getActivity(), getString(R.string.msg_refreshing_weather_data), null);
		ArrayMap<RetrofitClient.ServiceType, MultipleJsonDownloader.ResponseResult<JsonElement>> result = jsonResultObj.multipleJsonDownloader.getResponseMap().get(
				jsonResultObj.requestWeatherSource);

		ArrayMap<WeatherSourceType, RequestWeatherSource> newRequestWeatherSources = new ArrayMap<>();
		//요청한 날씨 제공사만 가져옴
		RequestWeatherSource failedRequestWeatherSource = jsonResultObj.requestWeatherSources.get(jsonResultObj.requestWeatherSource);
		newRequestWeatherSources.put(jsonResultObj.requestWeatherSource, failedRequestWeatherSource);
		failedRequestWeatherSource.getRequestServiceTypes().clear();

		//실패한 자료만 재 요청
		for (int i = 0; i < result.size(); i++) {
			if (result.valueAt(i).getT() != null) {
				failedRequestWeatherSource.addRequestServiceType(result.keyAt(i));
			}
		}

		MainProcessing.reRequestWeatherDataBySameWeatherSourceIfFailed(getContext(), latitude, longitude, newRequestWeatherSources,
				jsonResultObj.multipleJsonDownloader);
		return loadingDialog;
	}

	private AlertDialog reRefreshByAnotherWeatherSource(WeatherSourceType lastWeatherSourceType, JsonResultObj jsonResultObj) {
		final AlertDialog loadingDialog = ProgressDialog.show(getActivity(), getString(R.string.msg_refreshing_weather_data), null);

		ArrayMap<WeatherSourceType, RequestWeatherSource> newRequestWeatherSources = new ArrayMap<>();
		setRequestWeatherSourceWithSourceType(jsonResultObj.requestWeatherSource, newRequestWeatherSources);
		jsonResultObj.requestWeatherSources = newRequestWeatherSources;

		MainProcessing.reRequestWeatherDataByAnotherWeatherSourceIfFailed(getContext(), latitude, longitude, lastWeatherSourceType,
				newRequestWeatherSources, jsonResultObj.multipleJsonDownloader);
		return loadingDialog;
	}

	private void setRequestWeatherSourceWithSourceType(WeatherSourceType weatherSourceType,
	                                                   ArrayMap<WeatherSourceType, RequestWeatherSource> newRequestWeatherSources) {
		switch (weatherSourceType) {
			case KMA:
				RequestKma requestKma = new RequestKma();
				requestKma.addRequestServiceType(RetrofitClient.ServiceType.ULTRA_SRT_NCST).addRequestServiceType(
						RetrofitClient.ServiceType.ULTRA_SRT_FCST).addRequestServiceType(
						RetrofitClient.ServiceType.VILAGE_FCST).addRequestServiceType(
						RetrofitClient.ServiceType.MID_LAND_FCST).addRequestServiceType(RetrofitClient.ServiceType.MID_TA_FCST);
				newRequestWeatherSources.put(WeatherSourceType.KMA, requestKma);
				break;
			case OPEN_WEATHER_MAP:
				RequestOwm requestOwm = new RequestOwm();
				requestOwm.addRequestServiceType(RetrofitClient.ServiceType.OWM_ONE_CALL);
				Set<OneCallParameter.OneCallApis> excludes = new HashSet<>();
				excludes.add(OneCallParameter.OneCallApis.minutely);
				excludes.add(OneCallParameter.OneCallApis.alerts);
				requestOwm.setExcludeApis(excludes);

				newRequestWeatherSources.put(WeatherSourceType.OPEN_WEATHER_MAP, requestOwm);
				break;
			case ACCU_WEATHER:
				RequestAccu requestAccu = new RequestAccu();
				requestAccu.addRequestServiceType(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS).addRequestServiceType(
						RetrofitClient.ServiceType.ACCU_12_HOURLY).addRequestServiceType(RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY);

				newRequestWeatherSources.put(WeatherSourceType.ACCU_WEATHER, requestAccu);
				break;
			default:
				break;
		}
	}


	private void setWeatherFragments(WeatherSourceType requestWeatherSourceType, MultipleJsonDownloader<JsonElement> multipleJsonDownloader,
	                                 Double latitude, Double longitude, @Nullable AlertDialog loadingDialog) {
		Map<WeatherSourceType, ArrayMap<RetrofitClient.ServiceType, MultipleJsonDownloader.ResponseResult<JsonElement>>> responseMap = multipleJsonDownloader.getResponseMap();
		MultipleJsonDownloader.ResponseResult<JsonElement> aqicnResponse = responseMap.get(WeatherSourceType.AQICN).get(
				RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED);

		GeolocalizedFeedResponse airQualityResponse = null;
		if (aqicnResponse.getResponse() != null) {
			airQualityResponse = AqicnResponseProcessor.getAirQualityObjFromJson((Response<JsonElement>) aqicnResponse.getResponse());
		}

		ArrayMap<RetrofitClient.ServiceType, MultipleJsonDownloader.ResponseResult<JsonElement>> arrayMap = responseMap.get(
				requestWeatherSourceType);

		Fragment simpleCurrentConditionsFragment = null;
		Fragment simpleHourlyForecastFragment = null;
		Fragment simpleDailyForecastFragment = null;
		Fragment detailCurrentConditionsFragment = null;

		String currentConditionsWeatherVal = null;
		TimeZone timeZone = TimeZone.getDefault();

		switch (requestWeatherSourceType) {
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
					ZoneId zoneId = AccuWeatherResponseProcessor.getTimeZone(
							currentConditionsResponse.getItems().get(0).getLocalObservationDateTime());
					timeZone = TimeZone.getDefault();
					timeZone.setID(zoneId.toString());
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
		mainWeatherSourceType = requestWeatherSourceType;
		iLoadImgOfCurrentConditions.loadImgOfCurrentConditions(mainWeatherSourceType, currentConditionsWeatherVal, latitude, longitude,
				timeZone);

		if (getActivity() != null) {
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

					if (loadingDialog != null) {
						loadingDialog.dismiss();
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

					checkedItemIdx = (mainWeatherSourceType == WeatherSourceType.KMA) ? 0 : (mainWeatherSourceType == WeatherSourceType.ACCU_WEATHER) ? 1 : 2;
				} else {
					items[0] = getString(R.string.accu_weather);
					items[1] = getString(R.string.owm);
					checkedItemIdx = mainWeatherSourceType == WeatherSourceType.ACCU_WEATHER ? 0 : 1;
				}
				final int finalCheckedItemIdx = checkedItemIdx;

				new MaterialAlertDialogBuilder(getActivity()).setTitle(R.string.title_pick_weather_data_source).setSingleChoiceItems(items,
						checkedItemIdx, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialogInterface, int index) {
								WeatherSourceType lastWeatherSourceType = mainWeatherSourceType;
								WeatherSourceType newWeatherSourceType;

								if (finalCheckedItemIdx != index) {
									if (!items[index].equals(getString(R.string.kma))) {
										// 선택된 제공사가 accu, owm 둘 중 하나이면 우선순위 변경
										boolean accu = items[index].equals(getString(R.string.accu_weather));

										SharedPreferences.Editor editor = sharedPreferences.edit();
										editor.putBoolean(getString(R.string.pref_key_accu_weather), accu);
										editor.putBoolean(getString(R.string.pref_key_open_weather_map), !accu);
										editor.apply();

										newWeatherSourceType = accu ? WeatherSourceType.ACCU_WEATHER : WeatherSourceType.OPEN_WEATHER_MAP;
									} else {
										newWeatherSourceType = WeatherSourceType.KMA;
									}
									requestNewDataWithAnotherWeatherSource(newWeatherSourceType, lastWeatherSourceType);
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

	static class JsonResultObj {
		MultipleJsonDownloader<JsonElement> multipleJsonDownloader;
		WeatherSourceType requestWeatherSource;
		ArrayMap<WeatherSourceType, RequestWeatherSource> requestWeatherSources;

		public JsonResultObj(WeatherSourceType requestWeatherSource,
		                     ArrayMap<WeatherSourceType, RequestWeatherSource> requestWeatherSources) {
			this.requestWeatherSource = requestWeatherSource;
			this.requestWeatherSources = requestWeatherSources;
		}
	}
}