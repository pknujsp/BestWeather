package com.lifedawn.bestweather.alarm.alarmnotifications;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.google.gson.JsonElement;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.RequestWeatherDataType;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.databinding.FragmentWeatherForAlarmBinding;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.responses.accuweather.currentconditions.CurrentConditionsResponse;
import com.lifedawn.bestweather.retrofit.responses.accuweather.twelvehoursofhourlyforecasts.TwelveHoursOfHourlyForecastsResponse;
import com.lifedawn.bestweather.retrofit.responses.aqicn.GeolocalizedFeedResponse;
import com.lifedawn.bestweather.retrofit.responses.kma.json.vilagefcstcommons.VilageFcstResponse;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.onecall.OneCallResponse;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.room.dto.AlarmDto;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AccuWeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalCurrentConditions;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalHourlyForecast;
import com.lifedawn.bestweather.weathers.dataprocessing.util.SunRiseSetUtil;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WeatherRequestUtil;
import com.lifedawn.bestweather.weathers.simplefragment.accuweather.hourlyforecast.AccuSimpleHourlyForecastFragment;
import com.lifedawn.bestweather.weathers.simplefragment.base.BaseSimpleForecastFragment;
import com.lifedawn.bestweather.weathers.simplefragment.kma.hourlyforecast.KmaSimpleHourlyForecastFragment;
import com.lifedawn.bestweather.weathers.simplefragment.openweathermap.hourlyforecast.OwmSimpleHourlyForecastFragment;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Calendar;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TimeZone;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import retrofit2.Response;


public class WeatherForAlarmFragment extends Fragment {
	private FragmentWeatherForAlarmBinding binding;
	private AlarmDto alarmDto;
	private ExecutorService executorService = Executors.newSingleThreadExecutor();

	private ValueUnits tempUnit;
	private ValueUnits windUnit;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		alarmDto = (AlarmDto) getArguments().getSerializable(AlarmDto.class.getName());

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		tempUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_temp), ValueUnits.celsius.name()));
		windUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_wind), ValueUnits.mPerSec.name()));
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
	                         Bundle savedInstanceState) {
		binding = FragmentWeatherForAlarmBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.progressResultView.setContentView(binding.weatherContentsLayout);

		binding.addressName.setText(alarmDto.getLocationAddressName());

		Set<RequestWeatherDataType> requestWeatherDataTypeSet = new HashSet<>();
		requestWeatherDataTypeSet.add(RequestWeatherDataType.currentConditions);
		requestWeatherDataTypeSet.add(RequestWeatherDataType.hourlyForecast);
		requestWeatherDataTypeSet.add(RequestWeatherDataType.airQuality);

		WeatherRequestUtil.loadWeatherData(getContext(), executorService, alarmDto.getLocationCountryCode(),
				Double.parseDouble(alarmDto.getLocationLatitude()),
				Double.parseDouble(alarmDto.getLocationLongitude()), requestWeatherDataTypeSet,
				new MultipleJsonDownloader() {
					@Override
					public void onResult() {
						setWeatherFragments(this);
					}

					@Override
					public void onCanceled() {

					}
				}, null);
	}


	private void setWeatherFragments(MultipleJsonDownloader multipleJsonDownloader) {
		final WeatherSourceType requestWeatherSourceType = WeatherRequestUtil.getMainWeatherSourceType(getContext(),
				alarmDto.getLocationCountryCode());
		Map<WeatherSourceType, ArrayMap<RetrofitClient.ServiceType, MultipleJsonDownloader.ResponseResult>> responseMap = multipleJsonDownloader.getResponseMap();

		ArrayMap<RetrofitClient.ServiceType, MultipleJsonDownloader.ResponseResult> arrayMap = responseMap.get(
				requestWeatherSourceType);

		boolean successful = false;

		switch (requestWeatherSourceType) {
			case KMA:
				MultipleJsonDownloader.ResponseResult ultraSrtNcstResponseResult = arrayMap.get(RetrofitClient.ServiceType.ULTRA_SRT_NCST);
				MultipleJsonDownloader.ResponseResult ultraSrtFcstResponseResult = arrayMap.get(RetrofitClient.ServiceType.ULTRA_SRT_FCST);
				MultipleJsonDownloader.ResponseResult vilageFcstResponseResult = arrayMap.get(RetrofitClient.ServiceType.VILAGE_FCST);

				if (ultraSrtFcstResponseResult.isSuccessful() && ultraSrtNcstResponseResult.isSuccessful() && vilageFcstResponseResult.isSuccessful()) {
					successful = true;
				}
				break;
			case ACCU_WEATHER:
				MultipleJsonDownloader.ResponseResult currentConditionsResponseResult =
						arrayMap.get(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS);
				MultipleJsonDownloader.ResponseResult hourlyForecastResponseResult =
						arrayMap.get(RetrofitClient.ServiceType.ACCU_12_HOURLY);

				if (currentConditionsResponseResult.isSuccessful() && hourlyForecastResponseResult.isSuccessful()) {
					successful = true;
				}
				break;
			case OPEN_WEATHER_MAP:
				MultipleJsonDownloader.ResponseResult owmResponseResult = arrayMap.get(RetrofitClient.ServiceType.OWM_ONE_CALL);

				if (owmResponseResult.isSuccessful()) {
					successful = true;
				}

				break;
		}

		if (successful) {
			setCurrentConditions(multipleJsonDownloader, requestWeatherSourceType);
			setHourlyForecast(multipleJsonDownloader, requestWeatherSourceType);
		} else {
			if (getActivity() != null) {
				getActivity().runOnUiThread(new Runnable() {
					@Override
					public void run() {
						binding.progressResultView.onFailed(getString(R.string.msg_failed_update));
					}
				});
			}
		}

	}


	private void setCurrentConditions(MultipleJsonDownloader multipleJsonDownloader,
	                                  WeatherSourceType weatherSourceType) {
		ZoneId zoneId = null;
		ArrayMap<RetrofitClient.ServiceType, MultipleJsonDownloader.ResponseResult> arrayMap = multipleJsonDownloader.getResponseMap().get(
				weatherSourceType);

		int weatherIcon = 0;
		String temp = null;
		String airQuality = getString(R.string.air_quality) + " : ";
		String precipitation = getString(R.string.precipitation) + " : ";

		final Double latitude = Double.parseDouble(alarmDto.getLocationLatitude());
		final Double longitude = Double.parseDouble(alarmDto.getLocationLongitude());

		MultipleJsonDownloader.ResponseResult aqicnResponse = multipleJsonDownloader.getResponseMap().get(WeatherSourceType.AQICN).get(
				RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED);

		if (aqicnResponse.isSuccessful()) {
			GeolocalizedFeedResponse airQualityResponse = AqicnResponseProcessor.getAirQualityObjFromJson((Response<JsonElement>) aqicnResponse.getResponse());

			if (airQualityResponse != null) {
				if (airQualityResponse.getStatus().equals("ok")) {
					GeolocalizedFeedResponse.Data.IAqi iAqi = airQualityResponse.getData().getIaqi();
					int val = Integer.MIN_VALUE;

					if (iAqi.getO3() != null) {
						val = Math.max(val, (int) Double.parseDouble(iAqi.getO3().getValue()));
					}
					if (iAqi.getPm25() != null) {
						val = Math.max(val, (int) Double.parseDouble(iAqi.getPm25().getValue()));
					}
					if (iAqi.getPm10() != null) {
						val = Math.max(val, (int) Double.parseDouble(iAqi.getPm10().getValue()));
					}
					if (iAqi.getNo2() != null) {
						val = Math.max(val, (int) Double.parseDouble(iAqi.getNo2().getValue()));
					}
					if (iAqi.getSo2() != null) {
						val = Math.max(val, (int) Double.parseDouble(iAqi.getSo2().getValue()));
					}
					if (iAqi.getCo() != null) {
						val = Math.max(val, (int) Double.parseDouble(iAqi.getCo().getValue()));
					}
					if (iAqi.getDew() != null) {
						val = Math.max(val, (int) Double.parseDouble(iAqi.getDew().getValue()));
					}

					if (val == Integer.MIN_VALUE) {
						airQuality += getString(R.string.not_data);
					} else {
						airQuality += AqicnResponseProcessor.getGradeDescription(val);
					}
				} else {
					airQuality += getString(R.string.not_data);
				}
			} else {
				airQuality += getString(R.string.not_data);
			}
		}

		switch (weatherSourceType) {
			case KMA:
				MultipleJsonDownloader.ResponseResult ultraSrtNcstResponseResult = arrayMap.get(RetrofitClient.ServiceType.ULTRA_SRT_NCST);
				MultipleJsonDownloader.ResponseResult ultraSrtFcstResponseResult = arrayMap.get(RetrofitClient.ServiceType.ULTRA_SRT_FCST);
				MultipleJsonDownloader.ResponseResult vilageFcstResponseResult = arrayMap.get(RetrofitClient.ServiceType.VILAGE_FCST);

				FinalCurrentConditions finalCurrentConditions = KmaResponseProcessor.getFinalCurrentConditions(
						(VilageFcstResponse) ultraSrtNcstResponseResult.getResponse().body());
				List<FinalHourlyForecast> finalHourlyForecastList = KmaResponseProcessor.getFinalHourlyForecastList(
						(VilageFcstResponse) ultraSrtFcstResponseResult.getResponse().body(),
						(VilageFcstResponse) vilageFcstResponseResult.getResponse().body());

				zoneId = KmaResponseProcessor.getZoneId();

				precipitation += KmaResponseProcessor.getWeatherPtyIconDescription(finalCurrentConditions.getPrecipitationType());
				if (Double.parseDouble(finalCurrentConditions.getPrecipitation1Hour()) > 0.0) {
					precipitation += ", " + finalCurrentConditions.getPrecipitation1Hour() + "mm";
				}

				SunriseSunsetCalculator calculator = new SunriseSunsetCalculator(new Location(latitude, longitude),
						TimeZone.getTimeZone(zoneId.getId()));

				Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(KmaResponseProcessor.getZoneId().getId()));
				Calendar sunRise = calculator.getOfficialSunriseCalendarForDate(calendar);
				Calendar sunSet = calculator.getOfficialSunsetCalendarForDate(calendar);

				weatherIcon = KmaResponseProcessor.getWeatherSkyIconImg(finalHourlyForecastList.get(0).getSky(),
						SunRiseSetUtil.isNight(calendar, sunRise, sunSet));
				temp = ValueUnits.convertTemperature(finalCurrentConditions.getTemperature(),
						tempUnit) + ValueUnits.convertToStr(getContext(), tempUnit);
				break;
			case ACCU_WEATHER:
				MultipleJsonDownloader.ResponseResult currentConditionsResponseResult =
						arrayMap.get(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS);

				CurrentConditionsResponse currentConditionsResponse = AccuWeatherResponseProcessor.getCurrentConditionsObjFromJson(
						(JsonElement) currentConditionsResponseResult.getResponse().body());

				zoneId = ZonedDateTime.parse(currentConditionsResponse.getItems().get(0).getLocalObservationDateTime()).getZone();

				List<CurrentConditionsResponse.Item> items = currentConditionsResponse.getItems();
				CurrentConditionsResponse.Item item = items.get(0);
				if (item.getHasPrecipitation().equals("true")) {
					// precipitation type 값 종류 : Rain, Snow, Ice, Null(Not), or Mixed
					String precipitationUnit = "mm";

					precipitation += AccuWeatherResponseProcessor.getPty(
							item.getPrecipitationType()) + ", " + item.getPrecip1hr().getMetric().getValue() + precipitationUnit;
				} else {
					precipitation += getString(R.string.not_precipitation);
				}

				weatherIcon = AccuWeatherResponseProcessor.getWeatherIconImg(item.getWeatherIcon());
				temp = ValueUnits.convertTemperature(item.getTemperature().getMetric().getValue(), tempUnit) + ValueUnits.convertToStr(
						getContext(), tempUnit);
				break;
			case OPEN_WEATHER_MAP:
				MultipleJsonDownloader.ResponseResult owmResponseResult = arrayMap.get(RetrofitClient.ServiceType.OWM_ONE_CALL);
				OneCallResponse oneCallResponse = OpenWeatherMapResponseProcessor.getOneCallObjFromJson(
						owmResponseResult.getResponse().body().toString());
				zoneId = OpenWeatherMapResponseProcessor.getZoneId(oneCallResponse);

				OneCallResponse.Current current = oneCallResponse.getCurrent();
				if (current.getRain() != null || current.getSnow() != null) {
					String precipitationUnit = "mm";

					if (current.getRain() != null && current.getSnow() != null) {
						precipitation += getString(
								R.string.owm_icon_616_rain_and_snow) + ", " + current.getRain().getPrecipitation1Hour() + precipitationUnit + ", " + current.getSnow().getPrecipitation1Hour() + precipitationUnit;
					} else if (current.getRain() != null) {
						precipitation += getString(R.string.rain) + ", " + current.getRain().getPrecipitation1Hour() + precipitationUnit;
					} else {
						precipitation += getString(R.string.snow) + ", " + current.getSnow().getPrecipitation1Hour() + precipitationUnit;
					}
				} else {
					precipitation += getString(R.string.not_precipitation);
				}

				weatherIcon = OpenWeatherMapResponseProcessor.getWeatherIconImg(current.getWeather().get(0).getId(), current.getWeather().get(0).getIcon().contains("n"));
				temp = ValueUnits.convertTemperature(current.getTemp(), tempUnit).toString() + ValueUnits.convertToStr(getContext(),
						tempUnit);
				break;
		}

		if (getActivity() != null) {
			int finalWeatherIcon = weatherIcon;
			String finalTemp = temp;
			String finalAirQuality = airQuality;
			String finalPrecipitation = precipitation;

			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					binding.weatherIcon.setImageResource(finalWeatherIcon);
					binding.temperature.setText(finalTemp);
					binding.airQuality.setText(finalAirQuality);
					binding.precipitation.setText(finalPrecipitation);
				}
			});
		}
	}

	private void setHourlyForecast(MultipleJsonDownloader multipleJsonDownloader,
	                               WeatherSourceType weatherSourceType) {
		ZoneId zoneId = null;
		final Double latitude = Double.parseDouble(alarmDto.getLocationLatitude());
		final Double longitude = Double.parseDouble(alarmDto.getLocationLongitude());

		ArrayMap<RetrofitClient.ServiceType, MultipleJsonDownloader.ResponseResult> arrayMap = multipleJsonDownloader.getResponseMap().get(
				weatherSourceType);

		BaseSimpleForecastFragment hourlyForecastFragment = null;

		switch (weatherSourceType) {
			case KMA:
				MultipleJsonDownloader.ResponseResult ultraSrtFcstResponseResult = arrayMap.get(RetrofitClient.ServiceType.ULTRA_SRT_FCST);
				MultipleJsonDownloader.ResponseResult vilageFcstResponseResult = arrayMap.get(RetrofitClient.ServiceType.VILAGE_FCST);

				List<FinalHourlyForecast> finalHourlyForecastList = KmaResponseProcessor.getFinalHourlyForecastList(
						(VilageFcstResponse) ultraSrtFcstResponseResult.getResponse().body(),
						(VilageFcstResponse) vilageFcstResponseResult.getResponse().body());

				KmaSimpleHourlyForecastFragment kmaSimpleHourlyForecastFragment = new KmaSimpleHourlyForecastFragment();
				kmaSimpleHourlyForecastFragment.setFinalHourlyForecastList(finalHourlyForecastList);
				kmaSimpleHourlyForecastFragment.setHeaderVisibility(View.GONE);
				hourlyForecastFragment = kmaSimpleHourlyForecastFragment;

				zoneId = KmaResponseProcessor.getZoneId();
				break;
			case ACCU_WEATHER:
				MultipleJsonDownloader.ResponseResult hourlyForecastResponseResult =
						arrayMap.get(RetrofitClient.ServiceType.ACCU_12_HOURLY);

				TwelveHoursOfHourlyForecastsResponse twelveHoursOfHourlyForecastsResponse = AccuWeatherResponseProcessor.getHourlyForecastObjFromJson(
						(JsonElement) hourlyForecastResponseResult.getResponse().body());

				AccuSimpleHourlyForecastFragment accuSimpleHourlyForecastFragment = new AccuSimpleHourlyForecastFragment();
				accuSimpleHourlyForecastFragment.setTwelveHoursOfHourlyForecastsResponse(twelveHoursOfHourlyForecastsResponse);
				accuSimpleHourlyForecastFragment.setHeaderVisibility(View.GONE);

				hourlyForecastFragment = accuSimpleHourlyForecastFragment;

				zoneId = ZonedDateTime.parse(twelveHoursOfHourlyForecastsResponse.getItems().get(0).getDateTime()).getZone();
				break;
			case OPEN_WEATHER_MAP:
				MultipleJsonDownloader.ResponseResult owmResponseResult = arrayMap.get(RetrofitClient.ServiceType.OWM_ONE_CALL);
				OneCallResponse oneCallResponse = OpenWeatherMapResponseProcessor.getOneCallObjFromJson(
						owmResponseResult.getResponse().body().toString());

				OwmSimpleHourlyForecastFragment owmSimpleHourlyForecastFragment = new OwmSimpleHourlyForecastFragment();
				owmSimpleHourlyForecastFragment.setOneCallResponse(oneCallResponse);
				owmSimpleHourlyForecastFragment.setHeaderVisibility(View.GONE);

				hourlyForecastFragment = owmSimpleHourlyForecastFragment;

				zoneId = OpenWeatherMapResponseProcessor.getZoneId(oneCallResponse);
				break;
		}

		final Bundle defaultBundle = new Bundle();
		defaultBundle.putDouble(BundleKey.Latitude.name(), latitude);
		defaultBundle.putDouble(BundleKey.Longitude.name(), longitude);
		defaultBundle.putString(BundleKey.AddressName.name(), alarmDto.getLocationAddressName());
		defaultBundle.putString(BundleKey.CountryCode.name(), alarmDto.getLocationCountryCode());
		defaultBundle.putSerializable(BundleKey.WeatherDataSource.name(), weatherSourceType);
		defaultBundle.putSerializable(BundleKey.TimeZone.name(), zoneId);

		hourlyForecastFragment.setArguments(defaultBundle);

		Map<WeatherDataType, Integer> textSizeMap = new HashMap<>();
		textSizeMap.put(WeatherDataType.date, 15);
		textSizeMap.put(WeatherDataType.time, 15);
		textSizeMap.put(WeatherDataType.pop, 14);
		textSizeMap.put(WeatherDataType.rainVolume, 14);
		textSizeMap.put(WeatherDataType.snowVolume, 14);
		textSizeMap.put(WeatherDataType.temp, 18);

		Map<WeatherDataType, Integer> textColorMap = new HashMap<>();
		textColorMap.put(WeatherDataType.date, Color.WHITE);
		textColorMap.put(WeatherDataType.time, Color.WHITE);
		textColorMap.put(WeatherDataType.pop, Color.WHITE);
		textColorMap.put(WeatherDataType.rainVolume, Color.WHITE);
		textColorMap.put(WeatherDataType.snowVolume, Color.WHITE);
		textColorMap.put(WeatherDataType.temp, Color.WHITE);

		hourlyForecastFragment.setTextSizeMap(textSizeMap);
		hourlyForecastFragment.setTextColorMap(textColorMap);
		hourlyForecastFragment.setCardBackgroundColor(ContextCompat.getColor(getContext(), R.color.transparent));

		if (getActivity() != null) {
			final Fragment finalHourlyForecastFragment = hourlyForecastFragment;

			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					getChildFragmentManager().beginTransaction()
							.add(binding.hourlyForecastContainer.getId(), finalHourlyForecastFragment).commit();
				}
			});
		}
	}
}