package com.lifedawn.bestweather.weathers.comparison.hourlyforecast;

import android.os.Bundle;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.responses.accuweather.twelvehoursofhourlyforecasts.TwelveHoursOfHourlyForecastsResponse;
import com.lifedawn.bestweather.retrofit.responses.kma.ultrasrtfcstresponse.UltraSrtFcstRoot;
import com.lifedawn.bestweather.retrofit.responses.kma.vilagefcstresponse.VilageFcstRoot;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.hourlyforecast.HourlyForecastResponse;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.onecall.OneCallResponse;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.weathers.comparison.base.BaseForecastComparisonFragment;
import com.lifedawn.bestweather.weathers.dataprocessing.request.MainProcessing;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AccuWeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalHourlyForecast;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Set;

import retrofit2.Response;

public class HourlyForecastComparisonFragment extends BaseForecastComparisonFragment {
	private Double latitude;
	private Double longitude;
	private String addressName;
	private String countryCode;
	private MainProcessing.WeatherSourceType mainWeatherSourceType;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		Bundle bundle = getArguments();
		latitude = bundle.getDouble(getString(R.string.bundle_key_latitude));
		longitude = bundle.getDouble(getString(R.string.bundle_key_longitude));
		addressName = bundle.getString(getString(R.string.bundle_key_address_name));
		countryCode = bundle.getString(getString(R.string.bundle_key_country_code));
		mainWeatherSourceType = (MainProcessing.WeatherSourceType) bundle.getSerializable(
				getString(R.string.bundle_key_main_weather_data_source));
	}
	
	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.toolbar.fragmentTitle.setText(R.string.comparison_hourly_forecast);
		binding.toolbar.backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getParentFragmentManager().popBackStackImmediate();
			}
		});
		
		binding.addressName.setText(addressName);
		loadForecasts();
	}
	
	@Override
	public void setValuesToViews() {
	
	}
	
	private void setValuesToViews(HourlyForecastResponse hourlyForecastResponse) {
		List<MainProcessing.WeatherSourceType> weatherSourceTypeList = new ArrayList<>();
		
		//날씨, 기온, 강수량, 강수확률
		if (hourlyForecastResponse.kmaHourlyForecastList != null) {
			weatherSourceTypeList.add(MainProcessing.WeatherSourceType.KMA);
		} else if (hourlyForecastResponse.kmaThrowable != null) {
		
		}
		
		if (hourlyForecastResponse.accuHourlyForecastsResponse != null) {
			weatherSourceTypeList.add(MainProcessing.WeatherSourceType.ACCU_WEATHER);
		} else if (hourlyForecastResponse.accuThrowable != null) {
		
		}
		
		if (hourlyForecastResponse.owmOneCallResponse != null) {
			weatherSourceTypeList.add(MainProcessing.WeatherSourceType.OPEN_WEATHER_MAP);
		} else if (hourlyForecastResponse.owmThrowable != null) {
		
		}
		
		addLabelIconViews(weatherSourceTypeList);
		
	}
	
	private void addLabelIconViews(List<MainProcessing.WeatherSourceType> weatherSourceTypeList) {
		//날씨, 기온, 강수확률, 강수량
		final int labelIconTopBottomMargin = (int) getResources().getDimension(R.dimen.label_icon_top_bottom_space);
		final int spaceBetweenValueTypeAndSource = (int) getResources().getDimension(
				R.dimen.space_between_value_type_icon_and_data_source_icon_in_comparison);
		final int leftRightMargin = (int) getResources().getDimension(R.dimen.label_icon_left_right_space);
		
		int[] valueTypeIcons = new int[]{R.drawable.temp_icon, R.drawable.temp_icon, R.drawable.temp_icon, R.drawable.temp_icon};
		String[] typeDescriptions = new String[]{getString(R.string.weather), getString(R.string.temperature),
				getString(R.string.probability_of_precipitation), getString(R.string.precipitation_volume)};
		
		int index = 0;
		for (int typeIconId : valueTypeIcons) {
			addLabelView(typeIconId, typeDescriptions[index++], labelIconTopBottomMargin, spaceBetweenValueTypeAndSource, leftRightMargin);
			addWeatherDataSourceIconView(weatherSourceTypeList.get(0), spaceBetweenValueTypeAndSource, labelIconTopBottomMargin,
					leftRightMargin);
			
			for (int wi = 1; wi < weatherSourceTypeList.size(); wi++) {
				addWeatherDataSourceIconView(weatherSourceTypeList.get(wi), labelIconTopBottomMargin, labelIconTopBottomMargin,
						leftRightMargin);
			}
		}
	}
	
	private void loadForecasts() {
		binding.customProgressView.onStartedProcessingData(getString(R.string.msg_refreshing_weather_data));
		
		Set<MainProcessing.WeatherSourceType> requestWeatherSourceTypeSet = new ArraySet<>();
		
		requestWeatherSourceTypeSet.add(MainProcessing.WeatherSourceType.ACCU_WEATHER);
		requestWeatherSourceTypeSet.add(MainProcessing.WeatherSourceType.OPEN_WEATHER_MAP);
		if (countryCode.equals("KR")) {
			requestWeatherSourceTypeSet.add(MainProcessing.WeatherSourceType.KMA);
		}
		Log.e(RetrofitClient.LOG_TAG, "비교 날씨 정보 요청, " + requestWeatherSourceTypeSet.toString());
		
		MainProcessing.downloadHourlyForecasts(getContext(), latitude, longitude, requestWeatherSourceTypeSet,
				new MultipleJsonDownloader<JsonElement>() {
					@Override
					public void onResult() {
						setTable(this, latitude, longitude);
					}
				});
		
	}
	
	private void setTable(MultipleJsonDownloader<JsonElement> multipleJsonDownloader, Double latitude, Double longitude) {
		Map<MainProcessing.WeatherSourceType, ArrayMap<RetrofitClient.ServiceType, MultipleJsonDownloader.ResponseResult<JsonElement>>> responseMap = multipleJsonDownloader.getResponseMap();
		ArrayMap<RetrofitClient.ServiceType, MultipleJsonDownloader.ResponseResult<JsonElement>> arrayMap;
		HourlyForecastResponse hourlyForecastResponse = new HourlyForecastResponse();
		
		//kma
		if (responseMap.containsKey(MainProcessing.WeatherSourceType.KMA)) {
			arrayMap = responseMap.get(MainProcessing.WeatherSourceType.KMA);
			MultipleJsonDownloader.ResponseResult<JsonElement> ultraSrtFcstResponse = arrayMap.get(
					RetrofitClient.ServiceType.ULTRA_SRT_FCST);
			MultipleJsonDownloader.ResponseResult<JsonElement> vilageFcstResponse = arrayMap.get(RetrofitClient.ServiceType.VILAGE_FCST);
			
			if (ultraSrtFcstResponse.getT() == null && vilageFcstResponse.getT() == null) {
				UltraSrtFcstRoot ultraSrtFcstRoot = KmaResponseProcessor.getUltraSrtFcstObjFromJson(
						ultraSrtFcstResponse.getResponse().body().toString());
				VilageFcstRoot vilageFcstRoot = KmaResponseProcessor.getVilageFcstObjFromJson(
						vilageFcstResponse.getResponse().body().toString());
				
				String successfulCode = "00";
				if (ultraSrtFcstRoot.getResponse().getHeader().getResultCode().equals(
						successfulCode) && vilageFcstRoot.getResponse().getHeader().getResultCode().equals(successfulCode)) {
					hourlyForecastResponse.kmaHourlyForecastList = KmaResponseProcessor.getFinalHourlyForecastList(ultraSrtFcstRoot,
							vilageFcstRoot);
				}
			} else {
				if (ultraSrtFcstResponse.getT() != null) {
					hourlyForecastResponse.kmaThrowable = ultraSrtFcstResponse.getT();
				} else if (vilageFcstResponse.getT() != null) {
					hourlyForecastResponse.kmaThrowable = vilageFcstResponse.getT();
				}
			}
		}
		
		//accu
		if (responseMap.containsKey(MainProcessing.WeatherSourceType.ACCU_WEATHER)) {
			arrayMap = responseMap.get(MainProcessing.WeatherSourceType.ACCU_WEATHER);
			MultipleJsonDownloader.ResponseResult<JsonElement> geoCodingResponse = arrayMap.get(
					RetrofitClient.ServiceType.ACCU_GEOPOSITION_SEARCH);
			MultipleJsonDownloader.ResponseResult<JsonElement> accuHourlyForecastResponse = arrayMap.get(
					RetrofitClient.ServiceType.ACCU_12_HOURLY);
			
			if (geoCodingResponse.getT() == null && accuHourlyForecastResponse.getT() == null) {
				
				hourlyForecastResponse.accuHourlyForecastsResponse = AccuWeatherResponseProcessor.getHourlyForecastObjFromJson(
						arrayMap.get(RetrofitClient.ServiceType.ACCU_12_HOURLY).getResponse().body());
			} else {
				hourlyForecastResponse.accuThrowable = geoCodingResponse.getT() != null ? geoCodingResponse.getT() : accuHourlyForecastResponse.getT();
			}
		}
		//owm
		if (responseMap.containsKey(MainProcessing.WeatherSourceType.OPEN_WEATHER_MAP)) {
			arrayMap = responseMap.get(MainProcessing.WeatherSourceType.OPEN_WEATHER_MAP);
			MultipleJsonDownloader.ResponseResult<JsonElement> responseResult = arrayMap.get(RetrofitClient.ServiceType.OWM_ONE_CALL);
			
			if (responseResult.getT() == null) {
				hourlyForecastResponse.owmOneCallResponse = OpenWeatherMapResponseProcessor.getOneCallObjFromJson(
						responseResult.getResponse().body().toString());
			} else {
				hourlyForecastResponse.owmThrowable = responseResult.getT();
			}
		}
		
		if (getActivity() != null) {
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					setValuesToViews(hourlyForecastResponse);
					binding.customProgressView.onSuccessfulProcessingData();
				}
			});
			
		}
	}
	
	static class HourlyForecastResponse {
		List<FinalHourlyForecast> kmaHourlyForecastList;
		TwelveHoursOfHourlyForecastsResponse accuHourlyForecastsResponse;
		OneCallResponse owmOneCallResponse;
		
		Throwable kmaThrowable;
		Throwable accuThrowable;
		Throwable owmThrowable;
	}
	
}
