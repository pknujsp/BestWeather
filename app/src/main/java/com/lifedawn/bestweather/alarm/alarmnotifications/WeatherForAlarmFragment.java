package com.lifedawn.bestweather.alarm.alarmnotifications;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import android.util.ArrayMap;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.constants.BundleKey;
import com.lifedawn.bestweather.commons.constants.WeatherDataType;
import com.lifedawn.bestweather.commons.constants.ValueUnits;
import com.lifedawn.bestweather.commons.constants.WeatherProviderType;
import com.lifedawn.bestweather.commons.constants.WeatherValueType;
import com.lifedawn.bestweather.databinding.FragmentWeatherForAlarmBinding;
import com.lifedawn.bestweather.data.MyApplication;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.responses.accuweather.currentconditions.AccuCurrentConditionsResponse;
import com.lifedawn.bestweather.retrofit.responses.aqicn.AqiCnGeolocalizedFeedResponse;
import com.lifedawn.bestweather.retrofit.responses.kma.json.vilagefcstcommons.VilageFcstResponse;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.onecall.OwmOneCallResponse;
import com.lifedawn.bestweather.retrofit.util.WeatherRestApiDownloader;
import com.lifedawn.bestweather.room.dto.AlarmDto;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AccuWeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalCurrentConditions;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalHourlyForecast;
import com.lifedawn.bestweather.weathers.dataprocessing.util.SunRiseSetUtil;
import com.lifedawn.bestweather.weathers.dataprocessing.util.WeatherRequestUtil;
import com.lifedawn.bestweather.weathers.simplefragment.base.BaseSimpleForecastFragment;
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


public class WeatherForAlarmFragment extends Fragment {
	private FragmentWeatherForAlarmBinding binding;
	private AlarmDto alarmDto;
	private ExecutorService executorService = Executors.newSingleThreadExecutor();

	private ValueUnits tempUnit;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		alarmDto = (AlarmDto) getArguments().getSerializable(AlarmDto.class.getName());

		tempUnit = MyApplication.VALUE_UNIT_OBJ.getTempUnit();
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

		Set<WeatherDataType> weatherDataTypeSet = new HashSet<>();
		weatherDataTypeSet.add(WeatherDataType.currentConditions);
		weatherDataTypeSet.add(WeatherDataType.hourlyForecast);
		weatherDataTypeSet.add(WeatherDataType.airQuality);

		WeatherRequestUtil.loadWeatherData(getContext(), executorService,
				Double.parseDouble(alarmDto.getLocationLatitude()),
				Double.parseDouble(alarmDto.getLocationLongitude()), weatherDataTypeSet,
				new WeatherRestApiDownloader() {
					@Override
					public void onResult() {
						setWeatherFragments(this);
					}

					@Override
					public void onCanceled() {

					}
				}, null, null);
	}


	private void setWeatherFragments(WeatherRestApiDownloader weatherRestApiDownloader) {
		final WeatherProviderType requestWeatherProviderType = WeatherRequestUtil.getMainWeatherSourceType(getContext(),
				alarmDto.getLocationCountryCode());
		Map<WeatherProviderType, ArrayMap<RetrofitClient.ServiceType, WeatherRestApiDownloader.ResponseResult>> responseMap = weatherRestApiDownloader.getResponseMap();

		ArrayMap<RetrofitClient.ServiceType, WeatherRestApiDownloader.ResponseResult> arrayMap = responseMap.get(
				requestWeatherProviderType);

		boolean successful = false;

		switch (requestWeatherProviderType) {
			case KMA_WEB:
				WeatherRestApiDownloader.ResponseResult ultraSrtNcstResponseResult = arrayMap.get(RetrofitClient.ServiceType.KMA_ULTRA_SRT_NCST);
				WeatherRestApiDownloader.ResponseResult ultraSrtFcstResponseResult = arrayMap.get(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST);
				WeatherRestApiDownloader.ResponseResult vilageFcstResponseResult = arrayMap.get(RetrofitClient.ServiceType.KMA_VILAGE_FCST);

				if (ultraSrtFcstResponseResult.isSuccessful() && ultraSrtNcstResponseResult.isSuccessful() && vilageFcstResponseResult.isSuccessful()) {
					successful = true;
				}
				break;
			case ACCU_WEATHER:
				WeatherRestApiDownloader.ResponseResult currentConditionsResponseResult =
						arrayMap.get(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS);
				WeatherRestApiDownloader.ResponseResult hourlyForecastResponseResult =
						arrayMap.get(RetrofitClient.ServiceType.ACCU_HOURLY_FORECAST);

				if (currentConditionsResponseResult.isSuccessful() && hourlyForecastResponseResult.isSuccessful()) {
					successful = true;
				}
				break;
			case OWM_ONECALL:
				WeatherRestApiDownloader.ResponseResult owmResponseResult = arrayMap.get(RetrofitClient.ServiceType.OWM_ONE_CALL);

				if (owmResponseResult.isSuccessful()) {
					successful = true;
				}

				break;
		}

		if (successful) {
			setCurrentConditions(weatherRestApiDownloader, requestWeatherProviderType);
			setHourlyForecast(weatherRestApiDownloader, requestWeatherProviderType);
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


	private void setCurrentConditions(WeatherRestApiDownloader weatherRestApiDownloader,
	                                  WeatherProviderType weatherProviderType) {
		ZoneId zoneId = null;
		ArrayMap<RetrofitClient.ServiceType, WeatherRestApiDownloader.ResponseResult> arrayMap = weatherRestApiDownloader.getResponseMap().get(
				weatherProviderType);

		int weatherIcon = 0;
		String temp = null;
		String airQuality = getString(R.string.air_quality) + " : ";
		String precipitation = getString(R.string.precipitation) + " : ";

		final Double latitude = Double.parseDouble(alarmDto.getLocationLatitude());
		final Double longitude = Double.parseDouble(alarmDto.getLocationLongitude());

		WeatherRestApiDownloader.ResponseResult aqicnResponse = weatherRestApiDownloader.getResponseMap().get(WeatherProviderType.AQICN).get(
				RetrofitClient.ServiceType.AQICN_GEOLOCALIZED_FEED);

		if (aqicnResponse.isSuccessful()) {
			AqiCnGeolocalizedFeedResponse airQualityResponse = (AqiCnGeolocalizedFeedResponse) aqicnResponse.getResponseObj();

			if (airQualityResponse != null) {
				if (airQualityResponse.getStatus().equals("ok")) {
					AqiCnGeolocalizedFeedResponse.Data.IAqi iAqi = airQualityResponse.getData().getIaqi();
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
						airQuality += getString(R.string.noData);
					} else {
						airQuality += AqicnResponseProcessor.getGradeDescription(val);
					}
				} else {
					airQuality += getString(R.string.noData);
				}
			} else {
				airQuality += getString(R.string.noData);
			}
		}

		switch (weatherProviderType) {
			case KMA_WEB:
				WeatherRestApiDownloader.ResponseResult ultraSrtNcstResponseResult = arrayMap.get(RetrofitClient.ServiceType.KMA_ULTRA_SRT_NCST);
				WeatherRestApiDownloader.ResponseResult ultraSrtFcstResponseResult = arrayMap.get(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST);
				WeatherRestApiDownloader.ResponseResult vilageFcstResponseResult = arrayMap.get(RetrofitClient.ServiceType.KMA_VILAGE_FCST);

				FinalCurrentConditions finalCurrentConditions = KmaResponseProcessor.getFinalCurrentConditionsByXML(
						(VilageFcstResponse) ultraSrtNcstResponseResult.getResponseObj());
				List<FinalHourlyForecast> finalHourlyForecastList = KmaResponseProcessor.getFinalHourlyForecastListByXML(
						(VilageFcstResponse) ultraSrtFcstResponseResult.getResponseObj(),
						(VilageFcstResponse) vilageFcstResponseResult.getResponseObj());

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
						tempUnit) + MyApplication.VALUE_UNIT_OBJ.getTempUnitText();
				break;
			case ACCU_WEATHER:
				WeatherRestApiDownloader.ResponseResult currentConditionsResponseResult =
						arrayMap.get(RetrofitClient.ServiceType.ACCU_CURRENT_CONDITIONS);

				AccuCurrentConditionsResponse accuCurrentConditionsResponse =
						(AccuCurrentConditionsResponse) currentConditionsResponseResult.getResponseObj();

				zoneId = ZonedDateTime.parse(accuCurrentConditionsResponse.getItems().get(0).getLocalObservationDateTime()).getZone();

				List<AccuCurrentConditionsResponse.Item> items = accuCurrentConditionsResponse.getItems();
				AccuCurrentConditionsResponse.Item item = items.get(0);
				if (item.getHasPrecipitation().equals("true")) {
					// precipitation type 값 종류 : Rain, Snow, Ice, Null(Not), or Mixed
					String precipitationUnit = "mm";

					precipitation += AccuWeatherResponseProcessor.getPty(
							item.getPrecipitationType()) + ", " + item.getPrecip1hr().getMetric().getValue() + precipitationUnit;
				} else {
					precipitation += getString(R.string.not_precipitation);
				}

				weatherIcon = AccuWeatherResponseProcessor.getWeatherIconImg(item.getWeatherIcon());
				temp =
						ValueUnits.convertTemperature(item.getTemperature().getMetric().getValue(), tempUnit) + MyApplication.VALUE_UNIT_OBJ.getTempUnitText();
				break;
			case OWM_ONECALL:
				WeatherRestApiDownloader.ResponseResult owmResponseResult = arrayMap.get(RetrofitClient.ServiceType.OWM_ONE_CALL);
				OwmOneCallResponse owmOneCallResponse =
						(OwmOneCallResponse) owmResponseResult.getResponseObj();
				zoneId = OpenWeatherMapResponseProcessor.getZoneId(owmOneCallResponse);

				OwmOneCallResponse.Current current = owmOneCallResponse.getCurrent();
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
				temp = ValueUnits.convertTemperature(current.getTemp(), tempUnit) + MyApplication.VALUE_UNIT_OBJ.getTempUnitText();
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

	private void setHourlyForecast(WeatherRestApiDownloader weatherRestApiDownloader,
	                               WeatherProviderType weatherProviderType) {
		ZoneId zoneId = null;
		final Double latitude = Double.parseDouble(alarmDto.getLocationLatitude());
		final Double longitude = Double.parseDouble(alarmDto.getLocationLongitude());

		ArrayMap<RetrofitClient.ServiceType, WeatherRestApiDownloader.ResponseResult> arrayMap = weatherRestApiDownloader.getResponseMap().get(
				weatherProviderType);

		BaseSimpleForecastFragment hourlyForecastFragment = null;


		final Bundle defaultBundle = new Bundle();
		defaultBundle.putDouble(BundleKey.Latitude.name(), latitude);
		defaultBundle.putDouble(BundleKey.Longitude.name(), longitude);
		defaultBundle.putString(BundleKey.AddressName.name(), alarmDto.getLocationAddressName());
		defaultBundle.putString(BundleKey.CountryCode.name(), alarmDto.getLocationCountryCode());
		defaultBundle.putSerializable(BundleKey.WeatherProvider.name(), weatherProviderType);
		defaultBundle.putSerializable(BundleKey.TimeZone.name(), zoneId);

		hourlyForecastFragment.setArguments(defaultBundle);

		Map<WeatherValueType, Integer> textSizeMap = new HashMap<>();
		textSizeMap.put(WeatherValueType.date, 15);
		textSizeMap.put(WeatherValueType.time, 15);
		textSizeMap.put(WeatherValueType.pop, 14);
		textSizeMap.put(WeatherValueType.rainVolume, 14);
		textSizeMap.put(WeatherValueType.snowVolume, 14);
		textSizeMap.put(WeatherValueType.temp, 18);

		Map<WeatherValueType, Integer> textColorMap = new HashMap<>();
		textColorMap.put(WeatherValueType.date, Color.WHITE);
		textColorMap.put(WeatherValueType.time, Color.WHITE);
		textColorMap.put(WeatherValueType.pop, Color.WHITE);
		textColorMap.put(WeatherValueType.rainVolume, Color.WHITE);
		textColorMap.put(WeatherValueType.snowVolume, Color.WHITE);
		textColorMap.put(WeatherValueType.temp, Color.WHITE);

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