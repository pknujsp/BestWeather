package com.lifedawn.bestweather.weathers.viewmodels;

import android.app.Application;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.FusedLocation;
import com.lifedawn.bestweather.commons.classes.LocationLifeCycleObserver;
import com.lifedawn.bestweather.commons.classes.NetworkStatus;
import com.lifedawn.bestweather.commons.classes.WeatherViewController;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestAccu;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestAqicn;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestKma;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestMet;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestOwmIndividual;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestOwmOneCall;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestWeatherSource;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.commons.interfaces.OnResultFragmentListener;
import com.lifedawn.bestweather.commons.views.ProgressDialog;
import com.lifedawn.bestweather.databinding.FragmentWeatherBinding;
import com.lifedawn.bestweather.flickr.FlickrViewModel;
import com.lifedawn.bestweather.main.IRefreshFavoriteLocationListOnSideNav;
import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.openweathermap.onecall.OneCallParameter;
import com.lifedawn.bestweather.retrofit.util.WeatherRestApiDownloader;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.weathers.WeatherFragment;
import com.lifedawn.bestweather.weathers.dataprocessing.request.MainProcessing;

import java.time.Instant;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.HashSet;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.TimeUnit;

public class WeatherFragmentViewModel extends AndroidViewModel {
	public static final Map<String, WeatherFragment.WeatherResponseObj> FINAL_RESPONSE_MAP = new ConcurrentHashMap<>();

	public DateTimeFormatter dateTimeFormatter;
	public FavoriteAddressDto selectedFavoriteAddressDto;
	public LocationType locationType;

	public WeatherProviderType mainWeatherProviderType;
	public Double latitude;
	public Double longitude;
	public String countryCode;
	public String addressName;
	public SharedPreferences sharedPreferences;
	public ZoneId zoneId;

	public FavoriteAddressDto favoriteAddressDto;

	public WeatherRestApiDownloader weatherRestApiDownloader;
	public Bundle arguments;

	public final MutableLiveData<WeatherFragment.ResponseResultObj> weatherDataLiveData = new MutableLiveData<>();

	public WeatherFragmentViewModel(@NonNull Application application) {
		super(application);
	}

	public void requestNewData() {
		MyApplication.getExecutorService().execute(new Runnable() {
			@Override
			public void run() {
				//메인 날씨 제공사만 요청
				final Set<WeatherProviderType> weatherProviderTypeSet = new HashSet<>();
				weatherProviderTypeSet.add(mainWeatherProviderType);
				weatherProviderTypeSet.add(WeatherProviderType.AQICN);

				ArrayMap<WeatherProviderType, RequestWeatherSource> requestWeatherSources = new ArrayMap<>();
				setRequestWeatherSourceWithSourceTypes(weatherProviderTypeSet, requestWeatherSources);

				final WeatherFragment.ResponseResultObj responseResultObj = new WeatherFragment.ResponseResultObj(weatherProviderTypeSet, requestWeatherSources, mainWeatherProviderType);

				weatherRestApiDownloader = new WeatherRestApiDownloader() {
					@Override
					public void onResult() {
						weatherRestApiDownloader = this;
						responseResultObj.weatherRestApiDownloader = this;
						weatherDataLiveData.postValue(responseResultObj);
					}

					@Override
					public void onCanceled() {

					}
				};

				weatherRestApiDownloader.setZoneId(zoneId);
				MainProcessing.requestNewWeatherData(getApplication().getApplicationContext(), latitude,
						longitude,
						requestWeatherSources, weatherRestApiDownloader);
			}
		});
	}

	public void requestNewDataWithAnotherWeatherSource(WeatherProviderType newWeatherProviderType,
	                                                   WeatherProviderType lastWeatherProviderType) {
		MyApplication.getExecutorService().execute(new Runnable() {
			@Override
			public void run() {
				ArrayMap<WeatherProviderType, RequestWeatherSource> requestWeatherSources = new ArrayMap<>();
				//메인 날씨 제공사만 요청
				Set<WeatherProviderType> newWeatherProviderTypeSet = new HashSet<>();
				newWeatherProviderTypeSet.add(newWeatherProviderType);
				newWeatherProviderTypeSet.add(WeatherProviderType.AQICN);

				setRequestWeatherSourceWithSourceTypes(newWeatherProviderTypeSet, requestWeatherSources);

				final WeatherFragment.ResponseResultObj responseResultObj = new WeatherFragment.ResponseResultObj(newWeatherProviderTypeSet, requestWeatherSources, newWeatherProviderType);
				weatherRestApiDownloader = new WeatherRestApiDownloader() {
					@Override
					public void onResult() {
						weatherRestApiDownloader = this;
						responseResultObj.weatherRestApiDownloader = this;
						weatherDataLiveData.postValue(responseResultObj);
					}

					@Override
					public void onCanceled() {

					}
				};

				weatherRestApiDownloader.setZoneId(zoneId);
				MainProcessing.requestNewWeatherData(getApplication().getApplicationContext(), latitude,
						longitude, requestWeatherSources, weatherRestApiDownloader);
			}
		});
	}

	private void setRequestWeatherSourceWithSourceTypes(Set<WeatherProviderType> weatherProviderTypeSet,
	                                                    ArrayMap<WeatherProviderType, RequestWeatherSource> newRequestWeatherSources) {
		if (weatherProviderTypeSet.contains(WeatherProviderType.KMA_WEB)) {
			RequestKma requestKma = new RequestKma();
			requestKma.addRequestServiceType(RetrofitClient.ServiceType.KMA_ULTRA_SRT_NCST).addRequestServiceType(
							RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST).addRequestServiceType(
							RetrofitClient.ServiceType.KMA_VILAGE_FCST).addRequestServiceType(
							RetrofitClient.ServiceType.KMA_MID_LAND_FCST).addRequestServiceType(RetrofitClient.ServiceType.KMA_MID_TA_FCST)
					.addRequestServiceType(RetrofitClient.ServiceType.KMA_YESTERDAY_ULTRA_SRT_NCST);
			newRequestWeatherSources.put(WeatherProviderType.KMA_WEB, requestKma);
		}
		if (weatherProviderTypeSet.contains(WeatherProviderType.KMA_WEB)) {
			RequestKma requestKma = new RequestKma();
			requestKma.addRequestServiceType(RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS).addRequestServiceType(
					RetrofitClient.ServiceType.KMA_WEB_FORECASTS);
			newRequestWeatherSources.put(WeatherProviderType.KMA_WEB, requestKma);
		}
		if (weatherProviderTypeSet.contains(WeatherProviderType.OWM_ONECALL)) {
			RequestOwmOneCall requestOwmOneCall = new RequestOwmOneCall();
			requestOwmOneCall.addRequestServiceType(RetrofitClient.ServiceType.OWM_ONE_CALL);
			Set<OneCallParameter.OneCallApis> excludes = new HashSet<>();
			excludes.add(OneCallParameter.OneCallApis.minutely);
			excludes.add(OneCallParameter.OneCallApis.alerts);
			requestOwmOneCall.setExcludeApis(excludes);

			newRequestWeatherSources.put(WeatherProviderType.OWM_ONECALL, requestOwmOneCall);
		}
		if (weatherProviderTypeSet.contains(WeatherProviderType.MET_NORWAY)) {
			RequestMet requestMet = new RequestMet();
			requestMet.addRequestServiceType(RetrofitClient.ServiceType.MET_NORWAY_LOCATION_FORECAST);

			newRequestWeatherSources.put(WeatherProviderType.MET_NORWAY, requestMet);
		}
		if (weatherProviderTypeSet.contains(WeatherProviderType.ACCU_WEATHER)) {
			RequestAccu requestAccu = new RequestAccu();
			requestAccu.addRequestServiceType(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS).addRequestServiceType(
					RetrofitClient.ServiceType.ACCU_HOURLY_FORECAST).addRequestServiceType(RetrofitClient.ServiceType.ACCU_DAILY_FORECAST);

			newRequestWeatherSources.put(WeatherProviderType.ACCU_WEATHER, requestAccu);
		}
		if (weatherProviderTypeSet.contains(WeatherProviderType.OWM_INDIVIDUAL)) {
			RequestOwmIndividual requestOwmIndividual = new RequestOwmIndividual();
			requestOwmIndividual.addRequestServiceType(RetrofitClient.ServiceType.OWM_CURRENT_CONDITIONS).addRequestServiceType(
					RetrofitClient.ServiceType.OWM_HOURLY_FORECAST).addRequestServiceType(RetrofitClient.ServiceType.OWM_DAILY_FORECAST);

			newRequestWeatherSources.put(WeatherProviderType.OWM_INDIVIDUAL, requestOwmIndividual);
		}
		if (weatherProviderTypeSet.contains(WeatherProviderType.AQICN)) {
			RequestAqicn requestAqicn = new RequestAqicn();
			requestAqicn.addRequestServiceType(RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED);

			newRequestWeatherSources.put(WeatherProviderType.AQICN, requestAqicn);
		}

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
	public Set<WeatherProviderType> getOtherWeatherSourceTypes(WeatherProviderType requestWeatherProviderType,
	                                                           WeatherProviderType lastWeatherProviderType) {
		Set<WeatherProviderType> others = new HashSet<>();

		if (requestWeatherProviderType == WeatherProviderType.KMA_WEB) {

			if (lastWeatherProviderType == WeatherProviderType.OWM_ONECALL) {
				others.add(WeatherProviderType.MET_NORWAY);
			} else if (lastWeatherProviderType == WeatherProviderType.MET_NORWAY) {
				others.add(WeatherProviderType.OWM_ONECALL);
			} else {
				others.add(WeatherProviderType.OWM_ONECALL);
				others.add(WeatherProviderType.MET_NORWAY);
			}
		} else if (requestWeatherProviderType == WeatherProviderType.KMA_API) {

			if (lastWeatherProviderType == WeatherProviderType.OWM_ONECALL) {
				others.add(WeatherProviderType.MET_NORWAY);
			} else if (lastWeatherProviderType == WeatherProviderType.MET_NORWAY) {
				others.add(WeatherProviderType.OWM_ONECALL);
			} else {
				others.add(WeatherProviderType.OWM_ONECALL);
				others.add(WeatherProviderType.MET_NORWAY);
			}
		} else if (requestWeatherProviderType == WeatherProviderType.MET_NORWAY) {

			if (lastWeatherProviderType == WeatherProviderType.MET_NORWAY) {
				if (countryCode != null && countryCode.equals("KR")) {
					others.add(WeatherProviderType.OWM_ONECALL);
					others.add(WeatherProviderType.KMA_WEB);
				} else {
					others.add(WeatherProviderType.OWM_ONECALL);
				}
			} else if (lastWeatherProviderType == WeatherProviderType.OWM_ONECALL) {
				if (countryCode != null && countryCode.equals("KR")) {

					others.add(WeatherProviderType.KMA_WEB);
				}
			} else {
				others.add(WeatherProviderType.OWM_ONECALL);
			}
		} else if (requestWeatherProviderType == WeatherProviderType.OWM_ONECALL) {

			if (lastWeatherProviderType == WeatherProviderType.OWM_ONECALL) {
				if (countryCode != null && countryCode.equals("KR")) {

					others.add(WeatherProviderType.MET_NORWAY);
					others.add(WeatherProviderType.KMA_WEB);
				} else {
					others.add(WeatherProviderType.MET_NORWAY);
				}
			} else if (lastWeatherProviderType == WeatherProviderType.MET_NORWAY) {
				if (countryCode != null && countryCode.equals("KR")) {

					others.add(WeatherProviderType.KMA_WEB);
				}
			} else {
				others.add(WeatherProviderType.MET_NORWAY);
			}
		} else if (requestWeatherProviderType == WeatherProviderType.OWM_INDIVIDUAL) {

			if (lastWeatherProviderType == WeatherProviderType.OWM_INDIVIDUAL) {
				if (countryCode != null && countryCode.equals("KR")) {

					others.add(WeatherProviderType.MET_NORWAY);
					others.add(WeatherProviderType.KMA_WEB);
				} else {
					others.add(WeatherProviderType.MET_NORWAY);
				}
			} else if (lastWeatherProviderType == WeatherProviderType.MET_NORWAY) {
				if (countryCode != null && countryCode.equals("KR")) {

					others.add(WeatherProviderType.KMA_WEB);
				}
			} else {
				others.add(WeatherProviderType.MET_NORWAY);
			}
		}

		return others;
	}

	public WeatherProviderType getMainWeatherSourceType(@NonNull String countryCode) {
		if (arguments.containsKey("anotherProvider")) {
			WeatherProviderType weatherProviderType = (WeatherProviderType) arguments.getSerializable("anotherProvider");
			arguments.remove("anotherProvider");
			return weatherProviderType;
		}

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplication().getApplicationContext());
		WeatherProviderType mainWeatherProviderType = sharedPreferences.getBoolean(getApplication().getString(R.string.pref_key_met),
				true) ?
				WeatherProviderType.MET_NORWAY : WeatherProviderType.OWM_ONECALL;

		if (countryCode != null && countryCode.equals("KR")) {
			boolean kmaIsTopPriority = sharedPreferences.getBoolean(getApplication().getString(R.string.pref_key_kma_top_priority), true);
			if (kmaIsTopPriority) {
				mainWeatherProviderType = WeatherProviderType.KMA_WEB;
			}
		}

		return mainWeatherProviderType;
	}


	public boolean containWeatherData(Double latitude, Double longitude) {
		return FINAL_RESPONSE_MAP.containsKey(latitude.toString() + longitude.toString());
	}

	public void removeOldDownloadedData(Double latitude, Double longitude) {
		FINAL_RESPONSE_MAP.remove(latitude.toString() + longitude.toString());
	}

	public boolean isOldDownloadedData(Double latitude, Double longitude) {
		if (!FINAL_RESPONSE_MAP.containsKey(latitude.toString() + longitude.toString()))
			return false;

		long diff = ChronoUnit.MINUTES.between(
				FINAL_RESPONSE_MAP.get(latitude.toString() + longitude).dataDownloadedDateTime,
				LocalDateTime.now());
		return diff >= 30;
	}


}
