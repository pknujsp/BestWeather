package com.lifedawn.bestweather.weathers.comparison.hourlyforecast;

import android.content.Context;
import android.os.Bundle;
import android.util.ArrayMap;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;

import com.google.gson.JsonElement;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.ForecastObj;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestAccu;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestKma;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestOwm;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestWeatherSource;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.commons.views.ProgressDialog;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.openweathermap.OneCallParameter;
import com.lifedawn.bestweather.retrofit.responses.accuweather.twelvehoursofhourlyforecasts.TwelveHoursOfHourlyForecastsResponse;
import com.lifedawn.bestweather.retrofit.responses.kma.ultrasrtfcstresponse.UltraSrtFcstRoot;
import com.lifedawn.bestweather.retrofit.responses.kma.vilagefcstresponse.VilageFcstRoot;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.onecall.OneCallResponse;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.weathers.comparison.base.BaseForecastComparisonFragment;
import com.lifedawn.bestweather.weathers.dataprocessing.request.MainProcessing;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AccuWeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalHourlyForecast;
import com.lifedawn.bestweather.weathers.dataprocessing.util.SunRiseSetUtil;
import com.lifedawn.bestweather.weathers.view.ClockView;
import com.lifedawn.bestweather.weathers.view.DateView;
import com.lifedawn.bestweather.weathers.view.FragmentType;
import com.lifedawn.bestweather.weathers.view.IconTextView;
import com.lifedawn.bestweather.weathers.view.NonScrolledView;
import com.lifedawn.bestweather.weathers.view.TextValueView;
import com.lifedawn.bestweather.weathers.view.SingleWeatherIconView;

import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HourlyForecastComparisonFragment extends BaseForecastComparisonFragment {
	private MultipleJsonDownloader<JsonElement> multipleJsonDownloader;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.rootScrollView.setVisibility(View.GONE);

		binding.toolbar.fragmentTitle.setText(R.string.comparison_hourly_forecast);

		binding.addressName.setText(addressName);
		loadForecasts();
	}

	@Override
	public void setValuesToViews() {
	}

	private void setValuesToViews(HourlyForecastResponse hourlyForecastResponse) {
		final int dateValueRowHeight = (int) getResources().getDimension(R.dimen.dateValueRowHeightInCOMMON);
		final int clockValueRowHeight = (int) getResources().getDimension(R.dimen.clockValueRowHeightInCOMMON);
		final int weatherValueRowHeight = (int) getResources().getDimension(R.dimen.singleWeatherIconValueRowHeightInSC);
		final int defaultValueRowHeight = (int) getResources().getDimension(R.dimen.defaultValueRowHeightInSC);

		List<WeatherSourceType> weatherSourceTypeList = new ArrayList<>();

		List<ForecastObj<FinalHourlyForecast>> kmaFinalHourlyForecasts = null;
		List<ForecastObj<TwelveHoursOfHourlyForecastsResponse.Item>> accuFinalHourlyForecasts = null;
		List<ForecastObj<OneCallResponse.Hourly>> owmFinalHourlyForecasts = null;

		if (hourlyForecastResponse.kmaHourlyForecastList != null) {
			kmaFinalHourlyForecasts = new ArrayList<>();
			for (FinalHourlyForecast finalHourlyForecast : hourlyForecastResponse.kmaHourlyForecastList) {
				kmaFinalHourlyForecasts.add(new ForecastObj<>(finalHourlyForecast.getFcstDateTime(), finalHourlyForecast));
			}

			weatherSourceTypeList.add(WeatherSourceType.KMA);
			binding.kma.setVisibility(View.VISIBLE);
		} else {
			binding.kma.setVisibility(View.GONE);
		}

		if (hourlyForecastResponse.accuHourlyForecastsResponse != null) {
			accuFinalHourlyForecasts = new ArrayList<>();
			for (TwelveHoursOfHourlyForecastsResponse.Item item : hourlyForecastResponse.accuHourlyForecastsResponse.getItems()) {
				accuFinalHourlyForecasts.add(new ForecastObj<>(
						WeatherResponseProcessor.convertDateTimeOfHourlyForecast(Long.parseLong(item.getEpochDateTime()) * 1000L, zoneId),
						item));
			}

			weatherSourceTypeList.add(WeatherSourceType.ACCU_WEATHER);
			binding.accu.setVisibility(View.VISIBLE);
		} else {
			binding.accu.setVisibility(View.GONE);
		}

		if (hourlyForecastResponse.owmOneCallResponse != null) {
			owmFinalHourlyForecasts = new ArrayList<>();
			for (OneCallResponse.Hourly hourly : hourlyForecastResponse.owmOneCallResponse.getHourly()) {
				owmFinalHourlyForecasts.add(new ForecastObj<>(
						WeatherResponseProcessor.convertDateTimeOfHourlyForecast(Long.parseLong(hourly.getDt()) * 1000L, zoneId),
						hourly));
			}

			weatherSourceTypeList.add(WeatherSourceType.OPEN_WEATHER_MAP);
			binding.owm.setVisibility(View.VISIBLE);
		} else {
			binding.owm.setVisibility(View.GONE);
		}

		ZonedDateTime now = ZonedDateTime.now(zoneId).plusDays(2).withMinute(0).withSecond(0).withNano(0);

		ZonedDateTime firstDateTime = ZonedDateTime.of(now.toLocalDateTime(), zoneId);
		now = now.minusDays(5);
		ZonedDateTime lastDateTime = ZonedDateTime.of(now.toLocalDateTime(), zoneId);

		if (kmaFinalHourlyForecasts != null) {
			if (kmaFinalHourlyForecasts.get(0).dateTime.isBefore(firstDateTime)) {
				firstDateTime = ZonedDateTime.of(kmaFinalHourlyForecasts.get(0).dateTime.toLocalDateTime(), zoneId);
			}
			if (kmaFinalHourlyForecasts.get(kmaFinalHourlyForecasts.size() - 1).dateTime.isAfter(lastDateTime)) {
				lastDateTime =
						ZonedDateTime.of(kmaFinalHourlyForecasts.get(kmaFinalHourlyForecasts.size() - 1).dateTime.toLocalDateTime(), zoneId);
			}
		}
		if (accuFinalHourlyForecasts != null) {
			if (accuFinalHourlyForecasts.get(0).dateTime.isBefore(firstDateTime)) {
				firstDateTime = ZonedDateTime.of(accuFinalHourlyForecasts.get(0).dateTime.toLocalDateTime(), zoneId);
			}
			if (accuFinalHourlyForecasts.get(accuFinalHourlyForecasts.size() - 1).dateTime.isAfter(lastDateTime)) {
				lastDateTime =
						ZonedDateTime.of(accuFinalHourlyForecasts.get(accuFinalHourlyForecasts.size() - 1).dateTime.toLocalDateTime(), zoneId);
			}
		}
		if (owmFinalHourlyForecasts != null) {
			if (owmFinalHourlyForecasts.get(0).dateTime.isBefore(firstDateTime)) {
				firstDateTime = ZonedDateTime.of(owmFinalHourlyForecasts.get(0).dateTime.toLocalDateTime(), zoneId);
			}
			if (owmFinalHourlyForecasts.get(owmFinalHourlyForecasts.size() - 1).dateTime.isAfter(lastDateTime)) {
				lastDateTime =
						ZonedDateTime.of(owmFinalHourlyForecasts.get(owmFinalHourlyForecasts.size() - 1).dateTime.toLocalDateTime(), zoneId);
			}
		}

		List<ZonedDateTime> dateTimeList = new ArrayList<>();
		//firstDateTime부터 lastDateTime까지 추가
		now = ZonedDateTime.of(firstDateTime.toLocalDateTime(), zoneId);
		while (!now.isAfter(lastDateTime)) {
			dateTimeList.add(now);
			now = now.plusHours(1);
		}

		final int columnsCount = dateTimeList.size();
		final int columnWidth = (int) getResources().getDimension(R.dimen.valueColumnWidthInSCHourly);
		final int valueRowWidth = columnWidth * columnsCount;

		//날짜, 시각, 날씨, 기온, 강수량, 강수확률
		dateRow = new DateView(getContext(), FragmentType.Comparison, valueRowWidth, dateValueRowHeight, columnWidth);
		ClockView clockRow = new ClockView(getContext(), FragmentType.Comparison, valueRowWidth, clockValueRowHeight, columnWidth);
		SingleWeatherIconView[] weatherIconRows = new SingleWeatherIconView[columnsCount];
		TextValueView[] tempRows = new TextValueView[columnsCount];
		IconTextView[] precipitationVolumeRows = new IconTextView[columnsCount];
		IconTextView[] probabilityOfPrecipitationRows = new IconTextView[columnsCount];

		for (int i = 0; i < weatherSourceTypeList.size(); i++) {
			int specificRowWidth = 0;
			int beginColumnIndex = 0;

			if (weatherSourceTypeList.get(i) == WeatherSourceType.KMA) {
				specificRowWidth = kmaFinalHourlyForecasts.size() * columnWidth;

				for (int idx = 0; idx < dateTimeList.size(); idx++) {
					if (dateTimeList.get(idx).equals(kmaFinalHourlyForecasts.get(0).dateTime)) {
						beginColumnIndex = idx;
						break;
					}
				}
			} else if (weatherSourceTypeList.get(i) == WeatherSourceType.ACCU_WEATHER) {
				specificRowWidth = accuFinalHourlyForecasts.size() * columnWidth;

				for (int idx = 0; idx < dateTimeList.size(); idx++) {
					if (dateTimeList.get(idx).equals(accuFinalHourlyForecasts.get(0).dateTime)) {
						beginColumnIndex = idx;
						break;
					}
				}
			} else if (weatherSourceTypeList.get(i) == WeatherSourceType.OPEN_WEATHER_MAP) {
				specificRowWidth = owmFinalHourlyForecasts.size() * columnWidth;

				for (int idx = 0; idx < dateTimeList.size(); idx++) {
					if (dateTimeList.get(idx).equals(owmFinalHourlyForecasts.get(0).dateTime)) {
						beginColumnIndex = idx;
						break;
					}
				}
			}

			weatherIconRows[i] = new SingleWeatherIconView(getContext(), FragmentType.Comparison, specificRowWidth, weatherValueRowHeight,
					columnWidth);
			weatherIconRows[i].setTag(R.id.begin_column_index, beginColumnIndex);

			tempRows[i] = new TextValueView(getContext(), FragmentType.Comparison, specificRowWidth, defaultValueRowHeight, columnWidth);
			tempRows[i].setTag(R.id.begin_column_index, beginColumnIndex);

			precipitationVolumeRows[i] = new IconTextView(getContext(), FragmentType.Comparison, specificRowWidth,
					columnWidth, R.drawable.precipitationvolume);
			precipitationVolumeRows[i].setTag(R.id.begin_column_index, beginColumnIndex);

			probabilityOfPrecipitationRows[i] = new IconTextView(getContext(), FragmentType.Comparison,
					specificRowWidth, columnWidth, R.drawable.pop);
			probabilityOfPrecipitationRows[i].setTag(R.id.begin_column_index, beginColumnIndex);
		}

		//날짜, 시각
		dateRow.init(dateTimeList);
		clockRow.setClockList(dateTimeList);
		Context context = getContext();

		String tempUnitStr = getString(R.string.degree_symbol);

		//날씨,기온,강수량,강수확률
		//kma, accu weather, owm 순서
		for (int i = 0; i < weatherSourceTypeList.size(); i++) {
			List<SingleWeatherIconView.WeatherIconObj> weatherIconObjList = new ArrayList<>();
			List<String> tempList = new ArrayList<>();
			List<String> probabilityOfPrecipitationList = new ArrayList<>();
			List<String> precipitationVolumeList = new ArrayList<>();

			if (weatherSourceTypeList.get(i) == WeatherSourceType.KMA) {
				//일출, 일몰 시각 계산
				Map<Integer, SunRiseSetUtil.SunRiseSetObj> sunSetRiseDataMap = SunRiseSetUtil.getDailySunRiseSetMap(
						ZonedDateTime.of(kmaFinalHourlyForecasts.get(0).dateTime.toLocalDateTime(), zoneId),
						ZonedDateTime.of(kmaFinalHourlyForecasts.get(kmaFinalHourlyForecasts.size() - 1).dateTime.toLocalDateTime(),
								zoneId), latitude, longitude);

				boolean isNight = false;
				Calendar sunRise = null;
				Calendar sunSet = null;

				for (ForecastObj<FinalHourlyForecast> finalHourlyForecastObj : kmaFinalHourlyForecasts) {
					tempList.add(ValueUnits.convertTemperature(finalHourlyForecastObj.e.getTemp1Hour(), tempUnit).toString() + tempUnitStr);
					probabilityOfPrecipitationList.add(finalHourlyForecastObj.e.getProbabilityOfPrecipitation());
					precipitationVolumeList.add(finalHourlyForecastObj.e.getRainPrecipitation1Hour());

					Date itemDate = new Date(finalHourlyForecastObj.dateTime.toInstant().toEpochMilli());
					sunRise = sunSetRiseDataMap.get(finalHourlyForecastObj.dateTime.getDayOfYear()).getSunrise();
					sunSet = sunSetRiseDataMap.get(finalHourlyForecastObj.dateTime.getDayOfYear()).getSunset();
					isNight = SunRiseSetUtil.isNight(itemDate, sunRise.getTime(), sunSet.getTime());

					weatherIconObjList.add(new SingleWeatherIconView.WeatherIconObj(ContextCompat.getDrawable(context,
							KmaResponseProcessor.getWeatherSkyIconImg(finalHourlyForecastObj.e.getSky(), isNight))));
				}

			} else if (weatherSourceTypeList.get(i) == WeatherSourceType.ACCU_WEATHER) {
				for (ForecastObj<TwelveHoursOfHourlyForecastsResponse.Item> item : accuFinalHourlyForecasts) {
					tempList.add(ValueUnits.convertTemperature(item.e.getTemperature().getValue(), tempUnit).toString() + tempUnitStr);
					probabilityOfPrecipitationList.add(item.e.getPrecipitationProbability());
					precipitationVolumeList.add(item.e.getTotalLiquid().getValue());
					weatherIconObjList.add(new SingleWeatherIconView.WeatherIconObj(
							ContextCompat.getDrawable(context, AccuWeatherResponseProcessor.getWeatherIconImg(item.e.getWeatherIcon()))));
				}

			} else if (weatherSourceTypeList.get(i) == WeatherSourceType.OPEN_WEATHER_MAP) {
				for (ForecastObj<OneCallResponse.Hourly> item : owmFinalHourlyForecasts) {
					tempList.add(ValueUnits.convertTemperature(item.e.getTemp(), tempUnit).toString() + tempUnitStr);
					probabilityOfPrecipitationList.add((String.valueOf((int) (Double.parseDouble(item.e.getPop()) * 100.0))));
					precipitationVolumeList.add(item.e.getRain() == null ? "0.0" : item.e.getRain().getPrecipitation1Hour());
					weatherIconObjList.add(new SingleWeatherIconView.WeatherIconObj(ContextCompat.getDrawable(context,
							OpenWeatherMapResponseProcessor.getWeatherIconImg(item.e.getWeather().get(0).getId(),
									item.e.getWeather().get(0).getIcon().contains("n")))));
				}

			}

			weatherIconRows[i].setWeatherImgs(weatherIconObjList);
			tempRows[i].setValueList(tempList);
			probabilityOfPrecipitationRows[i].setValueList(probabilityOfPrecipitationList);
			precipitationVolumeRows[i].setValueList(precipitationVolumeList);
		}

		LinearLayout.LayoutParams rowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		rowLayoutParams.gravity = Gravity.CENTER_VERTICAL;

		binding.datetime.addView(dateRow, rowLayoutParams);
		binding.datetime.addView(clockRow, rowLayoutParams);
		LinearLayout view = null;
		nonScrolledViews = new NonScrolledView[weatherSourceTypeList.size()];

		LinearLayout.LayoutParams nonScrollRowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		nonScrollRowLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		int nonScrollViewMargin = (int) getResources().getDimension(R.dimen.nonScrollViewTopBottomMargin);
		nonScrollRowLayoutParams.setMargins(0, nonScrollViewMargin, 0, nonScrollViewMargin);

		for (int i = 0; i < weatherSourceTypeList.size(); i++) {
			LinearLayout.LayoutParams specificRowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			specificRowLayoutParams.leftMargin = columnWidth * (Integer) weatherIconRows[i].getTag(R.id.begin_column_index);

			LinearLayout.LayoutParams iconTextRowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			iconTextRowLayoutParams.leftMargin = columnWidth * (Integer) weatherIconRows[i].getTag(R.id.begin_column_index);
			iconTextRowLayoutParams.topMargin = (int) getResources().getDimension(R.dimen.iconValueViewMargin);

			String sourceName;
			switch (weatherSourceTypeList.get(i)) {
				case KMA:
					view = binding.kma;
					sourceName = getString(R.string.kma);
					break;
				case ACCU_WEATHER:
					view = binding.accu;
					sourceName = getString(R.string.accu_weather);
					break;
				default:
					view = binding.owm;
					sourceName = getString(R.string.owm);
					break;
			}
			nonScrolledViews[i] = new NonScrolledView(getContext(), FragmentType.Comparison, valueRowWidth, columnWidth, sourceName);

			view.addView(nonScrolledViews[i], nonScrollRowLayoutParams);
			view.addView(weatherIconRows[i], specificRowLayoutParams);
			view.addView(probabilityOfPrecipitationRows[i], iconTextRowLayoutParams);
			view.addView(precipitationVolumeRows[i], iconTextRowLayoutParams);
			view.addView(tempRows[i], specificRowLayoutParams);
		}
	}

	private void loadForecasts() {
		ArrayMap<WeatherSourceType, RequestWeatherSource> request = new ArrayMap<>();

		RequestAccu requestAccu = new RequestAccu();
		requestAccu.addRequestServiceType(RetrofitClient.ServiceType.ACCU_12_HOURLY);

		RequestOwm requestOwm = new RequestOwm();
		requestOwm.addRequestServiceType(RetrofitClient.ServiceType.OWM_ONE_CALL);
		Set<OneCallParameter.OneCallApis> exclude = new HashSet<>();
		exclude.add(OneCallParameter.OneCallApis.alerts);
		exclude.add(OneCallParameter.OneCallApis.minutely);
		exclude.add(OneCallParameter.OneCallApis.current);
		exclude.add(OneCallParameter.OneCallApis.daily);
		requestOwm.setExcludeApis(exclude);

		request.put(WeatherSourceType.ACCU_WEATHER, requestAccu);
		request.put(WeatherSourceType.OPEN_WEATHER_MAP, requestOwm);

		if (countryCode.equals("KR")) {
			RequestKma requestKma = new RequestKma();
			requestKma.addRequestServiceType(RetrofitClient.ServiceType.VILAGE_FCST).addRequestServiceType(
					RetrofitClient.ServiceType.ULTRA_SRT_FCST);

			request.put(WeatherSourceType.KMA, requestKma);
		}

		AlertDialog dialog = ProgressDialog.show(getActivity(), getString(R.string.msg_refreshing_weather_data), new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (multipleJsonDownloader != null) {
					multipleJsonDownloader.cancel();
				}
				getParentFragmentManager().popBackStack();
			}
		});

		multipleJsonDownloader = new MultipleJsonDownloader<JsonElement>() {
			@Override
			public void onResult() {
				setTable(this, latitude, longitude, dialog);
			}

			@Override
			public void onCanceled() {

			}
		};
		ExecutorService executorService = Executors.newSingleThreadExecutor();
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				MainProcessing.requestNewWeatherData(getContext(), latitude, longitude, request, multipleJsonDownloader);
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		multipleJsonDownloader.cancel();
	}

	private void setTable(MultipleJsonDownloader<JsonElement> multipleJsonDownloader, Double latitude, Double longitude,
	                      AlertDialog dialog) {
		Map<WeatherSourceType, ArrayMap<RetrofitClient.ServiceType, MultipleJsonDownloader.ResponseResult<JsonElement>>> responseMap = multipleJsonDownloader.getResponseMap();
		ArrayMap<RetrofitClient.ServiceType, MultipleJsonDownloader.ResponseResult<JsonElement>> arrayMap;
		HourlyForecastResponse hourlyForecastResponse = new HourlyForecastResponse();

		//kma
		if (responseMap.containsKey(WeatherSourceType.KMA)) {
			arrayMap = responseMap.get(WeatherSourceType.KMA);
			MultipleJsonDownloader.ResponseResult<JsonElement> ultraSrtFcstResponse = arrayMap.get(
					RetrofitClient.ServiceType.ULTRA_SRT_FCST);
			MultipleJsonDownloader.ResponseResult<JsonElement> vilageFcstResponse = arrayMap.get(RetrofitClient.ServiceType.VILAGE_FCST);

			if (ultraSrtFcstResponse.getResponse() != null && vilageFcstResponse.getResponse() != null) {
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
		if (responseMap.containsKey(WeatherSourceType.ACCU_WEATHER)) {
			arrayMap = responseMap.get(WeatherSourceType.ACCU_WEATHER);
			MultipleJsonDownloader.ResponseResult<JsonElement> accuHourlyForecastResponse = arrayMap.get(
					RetrofitClient.ServiceType.ACCU_12_HOURLY);

			if (accuHourlyForecastResponse.getT() == null) {

				hourlyForecastResponse.accuHourlyForecastsResponse = AccuWeatherResponseProcessor.getHourlyForecastObjFromJson(
						arrayMap.get(RetrofitClient.ServiceType.ACCU_12_HOURLY).getResponse().body());
			} else {
				hourlyForecastResponse.accuThrowable = accuHourlyForecastResponse.getT();
			}
		}
		//owm
		if (responseMap.containsKey(WeatherSourceType.OPEN_WEATHER_MAP)) {
			arrayMap = responseMap.get(WeatherSourceType.OPEN_WEATHER_MAP);
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
					binding.rootScrollView.setVisibility(View.VISIBLE);
					dialog.dismiss();
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
