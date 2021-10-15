package com.lifedawn.bestweather.weathers.viewpager;

import android.content.SharedPreferences;
import android.location.Address;
import android.location.Location;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentTransaction;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.Geocoding;
import com.lifedawn.bestweather.commons.enums.FavoriteAddressType;
import com.lifedawn.bestweather.commons.interfaces.IGps;
import com.lifedawn.bestweather.databinding.FragmentWeatherBinding;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.responses.accuweather.currentconditions.CurrentConditionsResponse;
import com.lifedawn.bestweather.retrofit.responses.aqicn.GeolocalizedFeedResponse;
import com.lifedawn.bestweather.retrofit.responses.kma.midlandfcstresponse.MidLandFcstRoot;
import com.lifedawn.bestweather.retrofit.responses.kma.midtaresponse.MidTaRoot;
import com.lifedawn.bestweather.retrofit.responses.kma.ultrasrtfcstresponse.UltraSrtFcstRoot;
import com.lifedawn.bestweather.retrofit.responses.kma.ultrasrtncstresponse.UltraSrtNcstRoot;
import com.lifedawn.bestweather.retrofit.responses.kma.vilagefcstresponse.VilageFcstRoot;
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

import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Response;


public class WeatherFragment extends Fragment {
	private FragmentWeatherBinding binding;
	private FavoriteAddressDto selectedAddress;
	private FavoriteAddressType favoriteAddressType;
	private IGps iGps;
	private WeatherViewModel.ILoadImgOfCurrentConditions iLoadImgOfCurrentConditions;
	private WeatherViewModel weatherViewModel;
	private MainProcessing.WeatherSourceType mainWeatherSourceType;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		Bundle bundle = getArguments();
		favoriteAddressType = (FavoriteAddressType) bundle.getSerializable(getString(R.string.bundle_key_favorite_address_type));
		
		if (favoriteAddressType == FavoriteAddressType.SelectedAddress) {
			selectedAddress = (FavoriteAddressDto) bundle.getSerializable(getString(R.string.bundle_key_selected_address));
		}
		
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
			binding.addressName.setText(selectedAddress.getAddress());
			mainWeatherSourceType = getMainWeatherSourceType(selectedAddress.getCountryCode());
			refreshForSelectedLocation();
		} else if (favoriteAddressType == FavoriteAddressType.CurrentLocation) {
			SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
			final Double lastLatitude = Double.parseDouble(
					sharedPreferences.getString(getString(R.string.pref_key_last_current_location_latitude), "0"));
			final Double lastLongitude = Double.parseDouble(
					sharedPreferences.getString(getString(R.string.pref_key_last_current_location_longitude), "0"));
			
			if (lastLatitude == 0 && lastLongitude == 0) {
				iGps.setUseGps();
			} else {
				setCurrentLocationAddressName(lastLatitude, lastLongitude, true);
			}
		}
	}
	
	
	public void setiGps(IGps iGps) {
		this.iGps = iGps;
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
								if (refresh) {
									mainWeatherSourceType = getMainWeatherSourceType(address.getCountryCode());
									refresh(latitude, longitude);
								}
							}
						});
					}
				}
			}
		});
	}
	
	private MainProcessing.WeatherSourceType getMainWeatherSourceType(String countryCode) {
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
	
	
	public void refreshForCurrentLocation(Location currentLocation) {
		setCurrentLocationAddressName(currentLocation.getLatitude(), currentLocation.getLongitude(), true);
	}
	
	public void refreshForSelectedLocation() {
		refresh(Double.parseDouble(selectedAddress.getLatitude()), Double.parseDouble(selectedAddress.getLongitude()));
	}
	
	private void refresh(Double latitude, Double longitude) {
		binding.customProgressView.onStartedProcessingData(getString(R.string.msg_refreshing_weather_data));
		
		Set<MainProcessing.WeatherSourceType> weatherSourceTypeSet = new ArraySet<>();
		weatherSourceTypeSet.add(MainProcessing.WeatherSourceType.AQICN);
		
		weatherSourceTypeSet.add(mainWeatherSourceType);
		//메인 날씨 제공사만 요청
		MainProcessing.WeatherSourceType secondWeatherSourceType = null;
		switch (mainWeatherSourceType) {
			case KMA:
			case OPEN_WEATHER_MAP:
				secondWeatherSourceType = MainProcessing.WeatherSourceType.ACCU_WEATHER;
				break;
			case ACCU_WEATHER:
				secondWeatherSourceType = MainProcessing.WeatherSourceType.OPEN_WEATHER_MAP;
				break;
		}
		final MainProcessing.WeatherSourceType finalSecondWeatherSourceType = secondWeatherSourceType;
		
		Log.e(RetrofitClient.LOG_TAG, "날씨 정보 요청, " + weatherSourceTypeSet.toString());
		iLoadImgOfCurrentConditions.loadImgOfCurrentConditions(null);
		
		MainProcessing.downloadWeatherData(getContext(), latitude.toString(), longitude.toString(), weatherSourceTypeSet,
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
									if (!responseResult.getResponse().isSuccessful()) {
										mainWeatherSourceType = finalSecondWeatherSourceType;
										refresh(latitude, longitude);
										return;
									}
								}
							}
						}
						
						if (getActivity() != null) {
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
				
				break;
			case ACCU_WEATHER:
				AccuSimpleCurrentConditionsFragment accuSimpleCurrentConditionsFragment = new AccuSimpleCurrentConditionsFragment();
				AccuSimpleHourlyForecastFragment accuSimpleHourlyForecastFragment = new AccuSimpleHourlyForecastFragment();
				AccuSimpleDailyForecastFragment accuSimpleDailyForecastFragment = new AccuSimpleDailyForecastFragment();
				AccuDetailCurrentConditionsFragment accuDetailCurrentConditionsFragment = new AccuDetailCurrentConditionsFragment();
				
				CurrentConditionsResponse currentConditionsResponse = AccuWeatherResponseProcessor.getCurrentConditionsObjFromJson(
						arrayMap.get(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS).getResponse().body().toString());
				
				accuSimpleCurrentConditionsFragment.setCurrentConditionsResponse(currentConditionsResponse).setAirQualityResponse(
						airQualityResponse);
				accuSimpleHourlyForecastFragment.setTwelveHoursOfHourlyForecastsResponse(
						AccuWeatherResponseProcessor.getHourlyForecastObjFromJson(
								arrayMap.get(RetrofitClient.ServiceType.ACCU_12_HOURLY).getResponse().body().toString()));
				accuSimpleDailyForecastFragment.setFiveDaysOfDailyForecastsResponse(
						AccuWeatherResponseProcessor.getDailyForecastObjFromJson(
								arrayMap.get(RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY).getResponse().body().toString()));
				accuDetailCurrentConditionsFragment.setCurrentConditionsResponse(currentConditionsResponse);
				
				simpleCurrentConditionsFragment = accuSimpleCurrentConditionsFragment;
				simpleHourlyForecastFragment = accuSimpleHourlyForecastFragment;
				simpleDailyForecastFragment = accuSimpleDailyForecastFragment;
				detailCurrentConditionsFragment = accuDetailCurrentConditionsFragment;
				
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
				break;
		}
		SimpleAirQualityFragment simpleAirQualityFragment = new SimpleAirQualityFragment();
		simpleAirQualityFragment.setGeolocalizedFeedResponse(airQualityResponse);
		
		Bundle sunSetRiseBundle = new Bundle();
		sunSetRiseBundle.putDouble("latitude", latitude);
		sunSetRiseBundle.putDouble("longitude", longitude);
		Fragment sunSetRiseFragment = new SunsetriseFragment();
		sunSetRiseFragment.setArguments(sunSetRiseBundle);
		
		if (getActivity() != null) {
			Fragment finalSimpleDailyForecastFragment = simpleDailyForecastFragment;
			Fragment finalSimpleHourlyForecastFragment = simpleHourlyForecastFragment;
			Fragment finalSimpleCurrentConditionsFragment = simpleCurrentConditionsFragment;
			Fragment finalDetailCurrentConditionsFragment = detailCurrentConditionsFragment;
			
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					FragmentTransaction fragmentTransaction = getChildFragmentManager().beginTransaction();
					fragmentTransaction.add(binding.simpleCurrentConditions.getId(), finalSimpleCurrentConditionsFragment,
							getString(R.string.tag_simple_current_conditions_fragment)).add(binding.simpleHourlyForecast.getId(),
							finalSimpleHourlyForecastFragment, getString(R.string.tag_simple_hourly_forecast_fragment)).add(
							binding.simpleDailyForecast.getId(), finalSimpleDailyForecastFragment,
							getString(R.string.tag_simple_daily_forecast_fragment)).add(binding.detailCurrentConditions.getId(),
							finalDetailCurrentConditionsFragment, getString(R.string.tag_detail_current_conditions_fragment)).add(
							binding.simpleAirQuality.getId(), simpleAirQualityFragment,
							getString(R.string.tag_simple_air_quality_fragment)).add(binding.sunSetRise.getId(), sunSetRiseFragment,
							getString(R.string.tag_sun_set_rise_fragment)).commit();
				}
			});
			
		}
	}
	
	
	public FavoriteAddressType getFavoriteAddressType() {
		return favoriteAddressType;
	}
	
	public boolean isFragmentUsingCurrentLocation() {
		return favoriteAddressType == FavoriteAddressType.CurrentLocation;
	}
}