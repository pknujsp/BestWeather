package com.lifedawn.bestweather.ui.weathers.viewmodels;

import android.app.Application;
import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.util.ArrayMap;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestAccu;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestAqicn;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestKma;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestMet;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestOwmIndividual;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestOwmOneCall;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestWeatherSource;
import com.lifedawn.bestweather.commons.constants.LocationType;
import com.lifedawn.bestweather.commons.constants.ValueUnits;
import com.lifedawn.bestweather.commons.constants.WeatherProviderType;
import com.lifedawn.bestweather.data.MyApplication;
import com.lifedawn.bestweather.data.remote.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.data.remote.retrofit.parameters.openweathermap.onecall.OwmOneCallParameter;
import com.lifedawn.bestweather.data.remote.retrofit.responses.accuweather.currentconditions.AccuCurrentConditionsResponse;
import com.lifedawn.bestweather.data.remote.retrofit.responses.accuweather.dailyforecasts.AccuDailyForecastsResponse;
import com.lifedawn.bestweather.data.remote.retrofit.responses.accuweather.hourlyforecasts.AccuHourlyForecastsResponse;
import com.lifedawn.bestweather.data.remote.retrofit.responses.aqicn.AqiCnGeolocalizedFeedResponse;
import com.lifedawn.bestweather.data.remote.weather.kma.parser.model.ParsedKmaCurrentConditions;
import com.lifedawn.bestweather.data.remote.weather.kma.parser.model.ParsedKmaDailyForecast;
import com.lifedawn.bestweather.data.remote.weather.kma.parser.model.ParsedKmaHourlyForecast;
import com.lifedawn.bestweather.data.remote.retrofit.responses.kma.json.midlandfcstresponse.MidLandFcstResponse;
import com.lifedawn.bestweather.data.remote.retrofit.responses.kma.json.midtaresponse.MidTaResponse;
import com.lifedawn.bestweather.data.remote.retrofit.responses.kma.json.vilagefcstcommons.VilageFcstResponse;
import com.lifedawn.bestweather.data.remote.retrofit.responses.metnorway.locationforecast.LocationForecastResponse;
import com.lifedawn.bestweather.data.remote.retrofit.responses.openweathermap.individual.currentweather.OwmCurrentConditionsResponse;
import com.lifedawn.bestweather.data.remote.retrofit.responses.openweathermap.individual.dailyforecast.OwmDailyForecastResponse;
import com.lifedawn.bestweather.data.remote.retrofit.responses.openweathermap.individual.hourlyforecast.OwmHourlyForecastResponse;
import com.lifedawn.bestweather.data.remote.retrofit.responses.openweathermap.onecall.OwmOneCallResponse;
import com.lifedawn.bestweather.data.remote.retrofit.callback.MultipleWeatherRestApiCallback;
import com.lifedawn.bestweather.data.local.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.ui.weathers.WeatherFragment;
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.request.MainProcessing;
import com.lifedawn.bestweather.data.remote.weather.aqicn.AqicnResponseProcessor;
import com.lifedawn.bestweather.data.remote.weather.kma.KmaResponseProcessor;
import com.lifedawn.bestweather.data.remote.weather.metnorway.MetNorwayResponseProcessor;
import com.lifedawn.bestweather.data.remote.weather.owm.OwmResponseProcessor;
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.response.finaldata.kma.FinalCurrentConditions;
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.response.finaldata.kma.FinalDailyForecast;
import com.lifedawn.bestweather.data.remote.weather.dataprocessing.response.finaldata.kma.FinalHourlyForecast;
import com.lifedawn.bestweather.data.local.weather.models.AirQualityDto;
import com.lifedawn.bestweather.data.local.weather.models.CurrentConditionsDto;
import com.lifedawn.bestweather.data.local.weather.models.DailyForecastDto;
import com.lifedawn.bestweather.data.local.weather.models.HourlyForecastDto;
import com.lifedawn.bestweather.data.local.weather.models.WeatherDataDto;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class WeatherFragmentViewModel extends AndroidViewModel implements WeatherFragment.OnResumeFragment {
	public static final ConcurrentHashMap<String, WeatherFragment.WeatherResponseObj> FINAL_RESPONSE_MAP = new ConcurrentHashMap<>();
	public final MutableLiveData<Boolean> resumedFragmentObserver = new MutableLiveData<>();

	public DateTimeFormatter dateTimeFormatter;
	public FavoriteAddressDto selectedFavoriteAddressDto;
	public LocationType locationType;
	public WeatherFragment.ITextColor iTextColor;

	public WeatherProviderType mainWeatherProviderType;
	public Double latitude;
	public Double longitude;
	public String countryCode;
	public String addressName;
	public ZoneId zoneId;

	public FavoriteAddressDto favoriteAddressDto;

	public MultipleWeatherRestApiCallback multipleWeatherRestApiCallback;
	public Bundle arguments;

	private final int FRAGMENT_TOTAL_COUNTS = 7;
	private final AtomicBoolean needDrawFragments = new AtomicBoolean(true);
	private final AtomicInteger resumedFragmentCount = new AtomicInteger(0);

	public final MutableLiveData<WeatherFragment.ResponseResultObj> weatherDataResponse = new MutableLiveData<>();

	public WeatherFragmentViewModel(@NonNull Application application) {
		super(application);
	}

	@Override
	protected void onCleared() {
		super.onCleared();
		if (multipleWeatherRestApiCallback != null)
			multipleWeatherRestApiCallback.clear();
		multipleWeatherRestApiCallback = null;
		dateTimeFormatter = null;
		selectedFavoriteAddressDto = null;
		locationType = null;
		iTextColor = null;

		mainWeatherProviderType = null;
		latitude = null;
		longitude = null;
		countryCode = null;
		addressName = null;
		zoneId = null;
		favoriteAddressDto = null;
		arguments = null;
	}

	public static void clear() {
		for (WeatherFragment.WeatherResponseObj v : FINAL_RESPONSE_MAP.values()) {
			v.dataDownloadedDateTime = null;
			v.requestMainWeatherProviderType = null;

			v.multipleWeatherRestApiCallback.responseMap.clear();

			v.multipleWeatherRestApiCallback.callMap.clear();

			v.multipleWeatherRestApiCallback.valueMap.clear();

			v.requestWeatherProviderTypeSet.clear();
			v.multipleWeatherRestApiCallback = null;

		}

		FINAL_RESPONSE_MAP.clear();
	}

	public void requestNewData() {
		needDrawFragments.set(true);
		MyApplication.getExecutorService().submit(new Runnable() {
			@Override
			public void run() {
				//메인 날씨 제공사만 요청
				final Set<WeatherProviderType> weatherProviderTypeSet = new HashSet<>();
				weatherProviderTypeSet.add(mainWeatherProviderType);
				weatherProviderTypeSet.add(WeatherProviderType.AQICN);

				ArrayMap<WeatherProviderType, RequestWeatherSource> requestWeatherSources = new ArrayMap<>();
				setRequestWeatherSourceWithSourceTypes(weatherProviderTypeSet, requestWeatherSources);

				final WeatherFragment.ResponseResultObj responseResultObj = new WeatherFragment.ResponseResultObj(weatherProviderTypeSet, requestWeatherSources, mainWeatherProviderType);
				multipleWeatherRestApiCallback = new MultipleWeatherRestApiCallback() {
					@Override
					public void onResult() {
						multipleWeatherRestApiCallback = this;
						responseResultObj.multipleWeatherRestApiCallback = this;
						weatherDataResponse.postValue(responseResultObj);
					}

					@Override
					public void onCanceled() {

					}
				};

				multipleWeatherRestApiCallback.setZoneId(zoneId);
				MainProcessing.requestNewWeatherData(getApplication().getApplicationContext(), latitude,
						longitude,
						requestWeatherSources, multipleWeatherRestApiCallback);
			}
		});
	}

	public void requestNewDataWithAnotherWeatherSource(WeatherProviderType newWeatherProviderType) {
		needDrawFragments.set(true);
		MyApplication.getExecutorService().submit(new Runnable() {
			@Override
			public void run() {
				ArrayMap<WeatherProviderType, RequestWeatherSource> requestWeatherSources = new ArrayMap<>();
				//메인 날씨 제공사만 요청
				Set<WeatherProviderType> newWeatherProviderTypeSet = new HashSet<>();
				newWeatherProviderTypeSet.add(newWeatherProviderType);
				newWeatherProviderTypeSet.add(WeatherProviderType.AQICN);

				setRequestWeatherSourceWithSourceTypes(newWeatherProviderTypeSet, requestWeatherSources);

				final WeatherFragment.ResponseResultObj responseResultObj = new WeatherFragment.ResponseResultObj(newWeatherProviderTypeSet, requestWeatherSources, newWeatherProviderType);
				multipleWeatherRestApiCallback = new MultipleWeatherRestApiCallback() {
					@Override
					public void onResult() {
						multipleWeatherRestApiCallback = this;
						responseResultObj.multipleWeatherRestApiCallback = this;
						weatherDataResponse.postValue(responseResultObj);
					}

					@Override
					public void onCanceled() {

					}
				};

				multipleWeatherRestApiCallback.setZoneId(zoneId);
				MainProcessing.requestNewWeatherData(getApplication().getApplicationContext(), latitude,
						longitude, requestWeatherSources, multipleWeatherRestApiCallback);
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
			Set<OwmOneCallParameter.OneCallApis> excludes = new HashSet<>();
			excludes.add(OwmOneCallParameter.OneCallApis.minutely);
			excludes.add(OwmOneCallParameter.OneCallApis.alerts);
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

		if (countryCode.equals("KR")) {
			boolean kmaIsTopPriority = sharedPreferences.getBoolean(getApplication().getString(R.string.pref_key_kma_top_priority), true);
			if (kmaIsTopPriority) {
				mainWeatherProviderType = WeatherProviderType.KMA_WEB;
			}
		}

		return mainWeatherProviderType;
	}


	public WeatherDataDto createWeatherFragments(Set<WeatherProviderType> weatherProviderTypeSet, MultipleWeatherRestApiCallback multipleWeatherRestApiCallback,
	                                             Double latitude, Double longitude) {
		Map<WeatherProviderType, ArrayMap<RetrofitClient.ServiceType, MultipleWeatherRestApiCallback.ResponseResult>> responseMap = multipleWeatherRestApiCallback.getResponseMap();
		ArrayMap<RetrofitClient.ServiceType, MultipleWeatherRestApiCallback.ResponseResult> arrayMap;

		CurrentConditionsDto currentConditionsDto = null;
		List<HourlyForecastDto> hourlyForecastDtoList = null;
		List<DailyForecastDto> dailyForecastDtoList = null;

		String currentConditionsWeatherVal = null;
		Context context = getApplication().getApplicationContext();

		if (weatherProviderTypeSet.contains(WeatherProviderType.KMA_API)) {
			arrayMap = responseMap.get(WeatherProviderType.KMA_API);

			FinalCurrentConditions finalCurrentConditions = KmaResponseProcessor.getFinalCurrentConditionsByXML(
					(VilageFcstResponse) arrayMap.get(RetrofitClient.ServiceType.KMA_ULTRA_SRT_NCST).getResponseObj());
			FinalCurrentConditions yesterDayFinalCurrentConditions = KmaResponseProcessor.getFinalCurrentConditionsByXML(
					(VilageFcstResponse) arrayMap.get(RetrofitClient.ServiceType.KMA_YESTERDAY_ULTRA_SRT_NCST).getResponseObj());

			List<FinalHourlyForecast> finalHourlyForecastList = KmaResponseProcessor.getFinalHourlyForecastListByXML(
					(VilageFcstResponse) arrayMap.get(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST).getResponseObj(),
					(VilageFcstResponse) arrayMap.get(RetrofitClient.ServiceType.KMA_VILAGE_FCST).getResponseObj());

			List<FinalDailyForecast> finalDailyForecastList = KmaResponseProcessor.getFinalDailyForecastListByXML(
					(MidLandFcstResponse) arrayMap.get(RetrofitClient.ServiceType.KMA_MID_LAND_FCST).getResponseObj(),
					(MidTaResponse) arrayMap.get(RetrofitClient.ServiceType.KMA_MID_TA_FCST).getResponseObj(),
					Long.parseLong(multipleWeatherRestApiCallback.getValue("tmFc")));

			finalDailyForecastList = KmaResponseProcessor.getDailyForecastListByXML(finalDailyForecastList, finalHourlyForecastList);

			currentConditionsDto = KmaResponseProcessor.makeCurrentConditionsDtoOfXML(context, finalCurrentConditions,
					finalHourlyForecastList.get(0), latitude, longitude);
			currentConditionsDto.setYesterdayTemp(ValueUnits.convertTemperature(yesterDayFinalCurrentConditions.getTemperature(),
					MyApplication.VALUE_UNIT_OBJ.getTempUnit()) + MyApplication.VALUE_UNIT_OBJ.getTempUnitText());

			hourlyForecastDtoList = KmaResponseProcessor.makeHourlyForecastDtoListOfXML(context,
					finalHourlyForecastList, latitude, longitude);

			dailyForecastDtoList = KmaResponseProcessor.makeDailyForecastDtoListOfXML(finalDailyForecastList);

			String sky = finalHourlyForecastList.get(0).getSky();
			String pty = finalCurrentConditions.getPrecipitationType();

			currentConditionsWeatherVal = pty.equals("0") ? sky + "_sky" : pty + "_pty";
			mainWeatherProviderType = WeatherProviderType.KMA_API;

		} else if (weatherProviderTypeSet.contains(WeatherProviderType.KMA_WEB)) {
			arrayMap = responseMap.get(WeatherProviderType.KMA_WEB);

			ParsedKmaCurrentConditions parsedKmaCurrentConditions = (ParsedKmaCurrentConditions) arrayMap.get(RetrofitClient.ServiceType.KMA_WEB_CURRENT_CONDITIONS).getResponseObj();
			Object[] forecasts = (Object[]) arrayMap.get(RetrofitClient.ServiceType.KMA_WEB_FORECASTS).getResponseObj();

			ArrayList<ParsedKmaHourlyForecast> parsedKmaHourlyForecasts = (ArrayList<ParsedKmaHourlyForecast>) forecasts[0];
			ArrayList<ParsedKmaDailyForecast> parsedKmaDailyForecasts = (ArrayList<ParsedKmaDailyForecast>) forecasts[1];

			currentConditionsDto = KmaResponseProcessor.makeCurrentConditionsDtoOfWEB(context,
					parsedKmaCurrentConditions, parsedKmaHourlyForecasts.get(0), latitude, longitude);

			hourlyForecastDtoList = KmaResponseProcessor.makeHourlyForecastDtoListOfWEB(context,
					parsedKmaHourlyForecasts, latitude, longitude);

			dailyForecastDtoList = KmaResponseProcessor.makeDailyForecastDtoListOfWEB(parsedKmaDailyForecasts);

			String pty = parsedKmaCurrentConditions.getPty();

			currentConditionsWeatherVal = pty.isEmpty() ? parsedKmaHourlyForecasts.get(0).getWeatherDescription() : pty;
			mainWeatherProviderType = WeatherProviderType.KMA_WEB;

		} else if (weatherProviderTypeSet.contains(WeatherProviderType.ACCU_WEATHER)) {
			arrayMap = responseMap.get(WeatherProviderType.ACCU_WEATHER);

			AccuCurrentConditionsResponse accuCurrentConditionsResponse =
					(AccuCurrentConditionsResponse) arrayMap.get(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS).getResponseObj();

			AccuHourlyForecastsResponse accuHourlyForecastsResponse =
					(AccuHourlyForecastsResponse) arrayMap.get(RetrofitClient.ServiceType.ACCU_HOURLY_FORECAST).getResponseObj();

			AccuDailyForecastsResponse accuDailyForecastsResponse =
					(AccuDailyForecastsResponse) arrayMap.get(RetrofitClient.ServiceType.ACCU_DAILY_FORECAST).getResponseObj();

			currentConditionsDto = AccuWeatherResponseProcessor.makeCurrentConditionsDto(context, accuCurrentConditionsResponse.getItems().get(0)
			);

			hourlyForecastDtoList = AccuWeatherResponseProcessor.makeHourlyForecastDtoList(context, accuHourlyForecastsResponse.getItems()
			);

			dailyForecastDtoList = AccuWeatherResponseProcessor.makeDailyForecastDtoList(context,
					accuDailyForecastsResponse.getDailyForecasts());

			currentConditionsWeatherVal = accuCurrentConditionsResponse.getItems().get(0).getWeatherIcon();
			mainWeatherProviderType = WeatherProviderType.ACCU_WEATHER;

		} else if (weatherProviderTypeSet.contains(WeatherProviderType.OWM_ONECALL)) {
			arrayMap = responseMap.get(WeatherProviderType.OWM_ONECALL);

			OwmOneCallResponse owmOneCallResponse =
					(OwmOneCallResponse) arrayMap.get(RetrofitClient.ServiceType.OWM_ONE_CALL).getResponseObj();

			currentConditionsDto = OwmResponseProcessor.makeCurrentConditionsDtoOneCall(context, owmOneCallResponse, zoneId
			);

			hourlyForecastDtoList = OwmResponseProcessor.makeHourlyForecastDtoListOneCall(context, owmOneCallResponse, zoneId
			);

			dailyForecastDtoList = OwmResponseProcessor.makeDailyForecastDtoListOneCall(context, owmOneCallResponse, zoneId
			);

			currentConditionsWeatherVal = owmOneCallResponse.getCurrent().getWeather().get(0).getId();

			mainWeatherProviderType = WeatherProviderType.OWM_ONECALL;

		} else if (weatherProviderTypeSet.contains(WeatherProviderType.OWM_INDIVIDUAL)) {
			arrayMap = responseMap.get(WeatherProviderType.OWM_INDIVIDUAL);

			OwmCurrentConditionsResponse owmCurrentConditionsResponse =
					(OwmCurrentConditionsResponse) arrayMap.get(RetrofitClient.ServiceType.OWM_CURRENT_CONDITIONS).getResponseObj();
			OwmHourlyForecastResponse owmHourlyForecastResponse =
					(OwmHourlyForecastResponse) arrayMap.get(RetrofitClient.ServiceType.OWM_HOURLY_FORECAST).getResponseObj();
			OwmDailyForecastResponse owmDailyForecastResponse =
					(OwmDailyForecastResponse) arrayMap.get(RetrofitClient.ServiceType.OWM_DAILY_FORECAST).getResponseObj();

			currentConditionsDto = OwmResponseProcessor.makeCurrentConditionsDtoIndividual(context, owmCurrentConditionsResponse, zoneId
			);

			hourlyForecastDtoList = OwmResponseProcessor.makeHourlyForecastDtoListIndividual(context,
					owmHourlyForecastResponse, zoneId);

			dailyForecastDtoList = OwmResponseProcessor.makeDailyForecastDtoListIndividual(context,
					owmDailyForecastResponse, zoneId);

			currentConditionsWeatherVal = owmCurrentConditionsResponse.getWeather().get(0).getId();

			mainWeatherProviderType = WeatherProviderType.OWM_INDIVIDUAL;
		} else if (weatherProviderTypeSet.contains(WeatherProviderType.MET_NORWAY)) {
			arrayMap = responseMap.get(WeatherProviderType.MET_NORWAY);

			LocationForecastResponse locationForecastResponse =
					(LocationForecastResponse) arrayMap.get(RetrofitClient.ServiceType.MET_NORWAY_LOCATION_FORECAST).getResponseObj();

			currentConditionsDto = MetNorwayResponseProcessor.makeCurrentConditionsDto(context, locationForecastResponse
					, zoneId);

			hourlyForecastDtoList = MetNorwayResponseProcessor.makeHourlyForecastDtoList(context, locationForecastResponse, zoneId);

			dailyForecastDtoList = MetNorwayResponseProcessor.makeDailyForecastDtoList(context, locationForecastResponse, zoneId);

			currentConditionsWeatherVal = locationForecastResponse.getProperties().getTimeSeries().get(0)
					.getData().getNext_1_hours().getSummary().getSymbolCode().replace("day", "").replace("night", "")
					.replace("_", "");

			mainWeatherProviderType = WeatherProviderType.MET_NORWAY;
		}

		MultipleWeatherRestApiCallback.ResponseResult aqicnResponse = responseMap.get(WeatherProviderType.AQICN).get(
				RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED);
		AqiCnGeolocalizedFeedResponse airQualityResponse = null;

		if (aqicnResponse != null && aqicnResponse.isSuccessful()) {
			airQualityResponse = (AqiCnGeolocalizedFeedResponse) aqicnResponse.getResponseObj();
		}

		final AirQualityDto airQualityDto = AqicnResponseProcessor.makeAirQualityDto(airQualityResponse,
				ZonedDateTime.now(zoneId).getOffset());

		String precipitationVolume = "";
		if (currentConditionsDto.isHasPrecipitationVolume()) {
			precipitationVolume = currentConditionsDto.getPrecipitationVolume();
		} else if (currentConditionsDto.isHasRainVolume()) {
			precipitationVolume = currentConditionsDto.getRainVolume();
		} else if (currentConditionsDto.isHasSnowVolume()) {
			precipitationVolume = currentConditionsDto.getSnowVolume();
		}

		final WeatherDataDto weatherDataDTO = new WeatherDataDto(currentConditionsDto,
				(ArrayList<HourlyForecastDto>) hourlyForecastDtoList, (ArrayList<DailyForecastDto>) dailyForecastDtoList,
				airQualityDto,
				currentConditionsWeatherVal, latitude, longitude, addressName, countryCode, mainWeatherProviderType, zoneId,
				precipitationVolume,
				airQualityResponse);

		dateTimeFormatter = DateTimeFormatter.ofPattern(
				MyApplication.VALUE_UNIT_OBJ.getClockUnit() == ValueUnits.clock12 ? context.getString(R.string.datetime_pattern_clock12) :
						context.getString(R.string.datetime_pattern_clock24), Locale.getDefault());
		resumedFragmentCount.set(0);
		needDrawFragments.set(true);

		return weatherDataDTO;
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


	@Override
	public void onResumeWithAsync(Fragment fragment) {
		if (needDrawFragments.get() && resumedFragmentCount.incrementAndGet() == FRAGMENT_TOTAL_COUNTS) {
			resumedFragmentCount.set(0);
			needDrawFragments.set(false);
			resumedFragmentObserver.setValue(true);
		}
	}
}
