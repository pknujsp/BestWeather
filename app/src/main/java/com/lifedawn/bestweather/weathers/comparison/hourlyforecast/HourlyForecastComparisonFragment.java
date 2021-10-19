package com.lifedawn.bestweather.weathers.comparison.hourlyforecast;

import android.os.Bundle;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

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
import com.lifedawn.bestweather.weathers.view.ClockView;
import com.lifedawn.bestweather.weathers.view.DateView;
import com.lifedawn.bestweather.weathers.view.TextValueView;
import com.lifedawn.bestweather.weathers.view.WeatherIconView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
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

		List<ForecastObj<FinalHourlyForecast>> kmaFinalHourlyForecasts = null;
		List<ForecastObj<TwelveHoursOfHourlyForecastsResponse.Item>> accuFinalHourlyForecasts = null;
		List<ForecastObj<OneCallResponse.Hourly>> owmFinalHourlyForecasts = null;

		if (hourlyForecastResponse.kmaHourlyForecastList != null) {
			kmaFinalHourlyForecasts = new ArrayList<>();
			for (FinalHourlyForecast finalHourlyForecast : hourlyForecastResponse.kmaHourlyForecastList) {
				kmaFinalHourlyForecasts.add(new ForecastObj<>(finalHourlyForecast.getFcstDateTime(), finalHourlyForecast));
			}

			weatherSourceTypeList.add(MainProcessing.WeatherSourceType.KMA);
		} else if (hourlyForecastResponse.kmaThrowable != null) {

		}

		if (hourlyForecastResponse.accuHourlyForecastsResponse != null) {
			accuFinalHourlyForecasts = new ArrayList<>();
			for (TwelveHoursOfHourlyForecastsResponse.Item item : hourlyForecastResponse.accuHourlyForecastsResponse.getItems()) {
				accuFinalHourlyForecasts.add(new ForecastObj<>(new Date(Long.parseLong(item.getEpochDateTime()) * 1000L), item));
			}
			weatherSourceTypeList.add(MainProcessing.WeatherSourceType.ACCU_WEATHER);
		} else if (hourlyForecastResponse.accuThrowable != null) {

		}

		if (hourlyForecastResponse.owmOneCallResponse != null) {
			owmFinalHourlyForecasts = new ArrayList<>();
			for (OneCallResponse.Hourly hourly : hourlyForecastResponse.owmOneCallResponse.getHourly()) {
				owmFinalHourlyForecasts.add(new ForecastObj<>(new Date(Long.parseLong(hourly.getDt()) * 1000L), hourly));
			}
			weatherSourceTypeList.add(MainProcessing.WeatherSourceType.OPEN_WEATHER_MAP);
		} else if (hourlyForecastResponse.owmThrowable != null) {

		}

		binding.labels.removeAllViews();
		binding.forecastView.removeAllViews();
		addLabelIconViews(weatherSourceTypeList);

		Calendar calendar = Calendar.getInstance();
		calendar.add(Calendar.DATE, 2);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		Date firstDateTime = calendar.getTime();
		calendar.add(Calendar.DATE, -4);
		Date lastDateTime = calendar.getTime();

		if (kmaFinalHourlyForecasts != null) {
			if (kmaFinalHourlyForecasts.get(0).dateTime.before(firstDateTime)) {
				firstDateTime = kmaFinalHourlyForecasts.get(0).dateTime;
			}
			if (kmaFinalHourlyForecasts.get(kmaFinalHourlyForecasts.size() - 1).dateTime.after(lastDateTime)) {
				lastDateTime = kmaFinalHourlyForecasts.get(kmaFinalHourlyForecasts.size() - 1).dateTime;
			}
		}
		if (accuFinalHourlyForecasts != null) {
			if (accuFinalHourlyForecasts.get(0).dateTime.before(firstDateTime)) {
				firstDateTime.setTime(accuFinalHourlyForecasts.get(0).dateTime.getTime());
			}
			if (accuFinalHourlyForecasts.get(accuFinalHourlyForecasts.size() - 1).dateTime.after(lastDateTime)) {
				lastDateTime.setTime(accuFinalHourlyForecasts.get(accuFinalHourlyForecasts.size() - 1).dateTime.getTime());
			}
		}
		if (owmFinalHourlyForecasts != null) {
			if (owmFinalHourlyForecasts.get(0).dateTime.before(firstDateTime)) {
				firstDateTime.setTime(owmFinalHourlyForecasts.get(0).dateTime.getTime());
			}
			if (owmFinalHourlyForecasts.get(owmFinalHourlyForecasts.size() - 1).dateTime.after(lastDateTime)) {
				lastDateTime.setTime(owmFinalHourlyForecasts.get(owmFinalHourlyForecasts.size() - 1).dateTime.getTime());
			}
		}


		List<Date> dateTimeList = new ArrayList<>();
		//firstDateTime부터 lastDateTime까지 추가
		calendar.setTime(firstDateTime);
		while (!calendar.getTime().after(lastDateTime)) {
			dateTimeList.add(calendar.getTime());
			calendar.add(Calendar.HOUR_OF_DAY, 1);
		}

		final int columnsCount = dateTimeList.size();

		final int viewTopBottomMargin = (int) getResources().getDimension(R.dimen.label_icon_top_bottom_space);
		final int itemMinHeight = (int) getResources().getDimension(R.dimen.label_icon_min_height);

		final int columnWidth = (int) getResources().getDimension(R.dimen.column_width_in_comparison_hourly_forecast_view);
		final int viewWidth = columnWidth * columnsCount;

		final int dateRowHeight = (int) getResources().getDimension(R.dimen.date_row_height_in_simple_forecast_view);
		final int clockRowHeight = (int) getResources().getDimension(R.dimen.clock_row_height_in_simple_forecast_view);
		final int weatherRowHeight = (int) getResources().getDimension(R.dimen.weather_icon_row_height_in_simple_forecast_view);
		final int defaultTextRowHeight = (int) getResources().getDimension(R.dimen.default_row_height_in_simple_forecast_view);

		//날짜, 시각, 날씨, 기온, 강수량, 강수확률
		dateRow = new DateView(getContext(), viewWidth, dateRowHeight, columnWidth);
		ClockView clockRow = new ClockView(getContext(), viewWidth, clockRowHeight, columnWidth);
		WeatherIconView[] weatherIconRows = new WeatherIconView[columnsCount];
		TextValueView[] tempRows = new TextValueView[columnsCount];
		TextValueView[] precipitationVolumeRows = new TextValueView[columnsCount];
		TextValueView[] probabilityOfPrecipitationRows = new TextValueView[columnsCount];

		for (int i = 0; i < columnsCount; i++) {
			weatherIconRows[i] = new WeatherIconView(getContext(), viewWidth, weatherRowHeight, columnWidth);
			tempRows[i] = new TextValueView(getContext(), viewWidth, defaultTextRowHeight, columnWidth);
			precipitationVolumeRows[i] = new TextValueView(getContext(), viewWidth, defaultTextRowHeight, columnWidth);
			probabilityOfPrecipitationRows[i] = new TextValueView(getContext(), viewWidth, defaultTextRowHeight, columnWidth);
		}

		//날짜


		dateRow.init(dateTimeList);
		clockRow.setClockList(dateTimeList);

		LinearLayout.LayoutParams rowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		rowLayoutParams.topMargin = viewTopBottomMargin;
		rowLayoutParams.bottomMargin = viewTopBottomMargin;

		binding.forecastView.addView(dateRow, rowLayoutParams);
		binding.forecastView.addView(clockRow, rowLayoutParams);
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

	static class ForecastObj<T> {
		final Date dateTime;
		final T e;

		public ForecastObj(Date dateTime, T e) {
			this.dateTime = dateTime;
			this.e = e;
		}
	}
}
