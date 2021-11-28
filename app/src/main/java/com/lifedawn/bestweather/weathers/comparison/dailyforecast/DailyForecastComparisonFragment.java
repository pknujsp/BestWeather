package com.lifedawn.bestweather.weathers.comparison.dailyforecast;

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
import com.lifedawn.bestweather.retrofit.responses.accuweather.fivedaysofdailyforecasts.FiveDaysOfDailyForecastsResponse;
import com.lifedawn.bestweather.retrofit.responses.kma.json.midlandfcstresponse.MidLandFcstResponse;
import com.lifedawn.bestweather.retrofit.responses.kma.json.midlandfcstresponse.MidLandFcstRoot;
import com.lifedawn.bestweather.retrofit.responses.kma.json.midtaresponse.MidTaResponse;
import com.lifedawn.bestweather.retrofit.responses.kma.json.midtaresponse.MidTaRoot;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.onecall.OneCallResponse;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.weathers.comparison.base.BaseForecastComparisonFragment;
import com.lifedawn.bestweather.weathers.dataprocessing.request.MainProcessing;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AccuWeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalDailyForecast;
import com.lifedawn.bestweather.weathers.view.DoubleWeatherIconView;
import com.lifedawn.bestweather.weathers.view.FragmentType;
import com.lifedawn.bestweather.weathers.view.IconTextView;
import com.lifedawn.bestweather.weathers.view.NonScrolledView;
import com.lifedawn.bestweather.weathers.view.NotScrolledView;
import com.lifedawn.bestweather.weathers.view.TextValueView;

import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DailyForecastComparisonFragment extends BaseForecastComparisonFragment {
	private MultipleJsonDownloader multipleJsonDownloader;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.rootScrollView.setVisibility(View.GONE);
		binding.toolbar.fragmentTitle.setText(R.string.comparison_daily_forecast);
		binding.addressName.setText(addressName);

		loadForecasts();
	}

	private void setValuesToViews(DailyForecastResponse dailyForecastResponse) {
		final int dateValueRowHeight = (int) getResources().getDimension(R.dimen.dateValueRowHeightInCOMMON);
		final int weatherValueRowHeight = (int) getResources().getDimension(R.dimen.singleWeatherIconValueRowHeightInSC);
		final int defaultValueRowHeight = (int) getResources().getDimension(R.dimen.defaultValueRowHeightInSC);

		List<WeatherSourceType> weatherSourceTypeList = new ArrayList<>();

		List<ForecastObj<FinalDailyForecast>> kmaFinalDailyForecasts = null;
		List<ForecastObj<FiveDaysOfDailyForecastsResponse.DailyForecasts>> accuFinalDailyForecasts = null;
		List<ForecastObj<OneCallResponse.Daily>> owmFinalDailyForecasts = null;

		if (dailyForecastResponse.kmaDailyForecastList != null) {
			kmaFinalDailyForecasts = new ArrayList<>();
			for (FinalDailyForecast finalDailyForecast : dailyForecastResponse.kmaDailyForecastList) {
				kmaFinalDailyForecasts.add(new ForecastObj<>(finalDailyForecast.getDate(), finalDailyForecast));
			}
			weatherSourceTypeList.add(WeatherSourceType.KMA);
			binding.kma.setVisibility(View.VISIBLE);
		} else {
			binding.kma.setVisibility(View.GONE);
		}

		if (dailyForecastResponse.accuDailyForecastsResponse != null) {
			accuFinalDailyForecasts = new ArrayList<>();
			for (FiveDaysOfDailyForecastsResponse.DailyForecasts item : dailyForecastResponse.accuDailyForecastsResponse.getDailyForecasts()) {
				accuFinalDailyForecasts.add(new ForecastObj<>(
						WeatherResponseProcessor.convertDateTimeOfDailyForecast(Long.parseLong(item.getEpochDate()) * 1000L, zoneId),
						item));
			}

			weatherSourceTypeList.add(WeatherSourceType.ACCU_WEATHER);
			binding.accu.setVisibility(View.VISIBLE);
		} else {
			binding.accu.setVisibility(View.GONE);
		}

		if (dailyForecastResponse.owmOneCallResponse != null) {
			owmFinalDailyForecasts = new ArrayList<>();
			for (OneCallResponse.Daily daily : dailyForecastResponse.owmOneCallResponse.getDaily()) {
				owmFinalDailyForecasts.add(new ForecastObj<>(
						WeatherResponseProcessor.convertDateTimeOfDailyForecast(Long.parseLong(daily.getDt()) * 1000L, zoneId), daily));
			}
			weatherSourceTypeList.add(WeatherSourceType.OPEN_WEATHER_MAP);
			binding.owm.setVisibility(View.VISIBLE);
		} else {
			binding.owm.setVisibility(View.GONE);
		}

		ZonedDateTime now = ZonedDateTime.now(zoneId).plusDays(10).withHour(0).withMinute(0).withSecond(0).withNano(0);
		ZonedDateTime firstDateTime = ZonedDateTime.of(now.toLocalDateTime(), zoneId);
		now = now.minusDays(20);
		ZonedDateTime lastDateTime = ZonedDateTime.of(now.toLocalDateTime(), zoneId);

		if (kmaFinalDailyForecasts != null) {
			if (kmaFinalDailyForecasts.get(0).dateTime.isBefore(firstDateTime)) {
				firstDateTime = ZonedDateTime.of(kmaFinalDailyForecasts.get(0).dateTime.toLocalDateTime(), zoneId);
			}
			if (kmaFinalDailyForecasts.get(kmaFinalDailyForecasts.size() - 1).dateTime.isAfter(lastDateTime)) {
				lastDateTime = ZonedDateTime.of(kmaFinalDailyForecasts.get(kmaFinalDailyForecasts.size() - 1).dateTime.toLocalDateTime(), zoneId);
			}
		}
		if (accuFinalDailyForecasts != null) {
			if (accuFinalDailyForecasts.get(0).dateTime.isBefore(firstDateTime)) {
				firstDateTime = ZonedDateTime.of(accuFinalDailyForecasts.get(0).dateTime.toLocalDateTime(), zoneId);
			}
			if (accuFinalDailyForecasts.get(accuFinalDailyForecasts.size() - 1).dateTime.isAfter(lastDateTime)) {
				lastDateTime =
						ZonedDateTime.of(accuFinalDailyForecasts.get(accuFinalDailyForecasts.size() - 1).dateTime.toLocalDateTime(), zoneId);
			}
		}
		if (owmFinalDailyForecasts != null) {
			if (owmFinalDailyForecasts.get(0).dateTime.isBefore(firstDateTime)) {
				firstDateTime = ZonedDateTime.of(owmFinalDailyForecasts.get(0).dateTime.toLocalDateTime(), zoneId);
			}
			if (owmFinalDailyForecasts.get(owmFinalDailyForecasts.size() - 1).dateTime.isAfter(lastDateTime)) {
				lastDateTime = ZonedDateTime.of(owmFinalDailyForecasts.get(owmFinalDailyForecasts.size() - 1).dateTime.toLocalDateTime(),
						zoneId);
			}
		}

		List<ZonedDateTime> dateTimeList = new ArrayList<>();
		//firstDateTime부터 lastDateTime까지 추가
		now = ZonedDateTime.of(firstDateTime.toLocalDateTime(), zoneId);
		while (!now.isAfter(lastDateTime)) {
			dateTimeList.add(now);
			now = now.plusDays(1);
		}

		final int columnsCount = dateTimeList.size();
		final int columnWidth = (int) getResources().getDimension(R.dimen.valueColumnWidthInSCDaily);
		final int valueRowWidth = columnWidth * columnsCount;

		//날짜, 날씨, 기온, 강수량, 강수확률
		TextValueView dateRow = new TextValueView(getContext(), FragmentType.Comparison, valueRowWidth, dateValueRowHeight, columnWidth);
		List<DoubleWeatherIconView> weatherIconRows = new ArrayList<>();
		List<IconTextView> precipitationVolumeRows = new ArrayList<>();
		List<IconTextView> probabilityOfPrecipitationRows = new ArrayList<>();
		List<TextValueView> tempRows = new ArrayList<>();

		for (int i = 0; i < weatherSourceTypeList.size(); i++) {
			int specificRowWidth = 0;
			int beginColumnIndex = 0;

			if (weatherSourceTypeList.get(i) == WeatherSourceType.KMA) {
				specificRowWidth = kmaFinalDailyForecasts.size() * columnWidth;

				for (int idx = 0; idx < dateTimeList.size(); idx++) {
					if (dateTimeList.get(idx).equals(kmaFinalDailyForecasts.get(0).dateTime)) {
						beginColumnIndex = idx;
						break;
					}
				}
			} else if (weatherSourceTypeList.get(i) == WeatherSourceType.ACCU_WEATHER) {
				specificRowWidth = accuFinalDailyForecasts.size() * columnWidth;

				for (int idx = 0; idx < dateTimeList.size(); idx++) {
					if (dateTimeList.get(idx).equals(accuFinalDailyForecasts.get(0).dateTime)) {
						beginColumnIndex = idx;
						break;
					}
				}
			} else if (weatherSourceTypeList.get(i) == WeatherSourceType.OPEN_WEATHER_MAP) {
				specificRowWidth = owmFinalDailyForecasts.size() * columnWidth;

				for (int idx = 0; idx < dateTimeList.size(); idx++) {
					if (dateTimeList.get(idx).equals(owmFinalDailyForecasts.get(0).dateTime)) {
						beginColumnIndex = idx;
						break;
					}
				}
			}
			weatherIconRows.add(
					new DoubleWeatherIconView(getContext(), FragmentType.Comparison, specificRowWidth, weatherValueRowHeight, columnWidth));
			weatherIconRows.get(i).setTag(R.id.begin_column_index, beginColumnIndex);

			tempRows.add(new TextValueView(getContext(), FragmentType.Comparison, specificRowWidth, defaultValueRowHeight, columnWidth));
			tempRows.get(i).setTag(R.id.begin_column_index, beginColumnIndex);

			precipitationVolumeRows.add(
					new IconTextView(getContext(), FragmentType.Comparison, specificRowWidth, columnWidth, R.drawable.precipitationvolume));
			precipitationVolumeRows.get(i).setTag(R.id.begin_column_index, beginColumnIndex);

			probabilityOfPrecipitationRows.add(
					new IconTextView(getContext(), FragmentType.Comparison, specificRowWidth, columnWidth, R.drawable.pop));
			probabilityOfPrecipitationRows.get(i).setTag(R.id.begin_column_index, beginColumnIndex);
		}

		//날짜
		List<String> dateList = new ArrayList<>();
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(getString(R.string.date_pattern));

		for (ZonedDateTime date : dateTimeList) {
			dateList.add(date.format(dateTimeFormatter));
		}
		dateRow.setValueList(dateList);

		//날씨,기온,강수량,강수확률
		//kma, accu weather, owm 순서
		String temp = null;
		String pop = null;
		Context context = getContext();

		String tempUnitStr = getString(R.string.degree_symbol);

		for (int i = 0; i < weatherSourceTypeList.size(); i++) {
			List<DoubleWeatherIconView.WeatherIconObj> weatherIconObjList = new ArrayList<>();
			List<String> tempList = new ArrayList<>();
			List<String> probabilityOfPrecipitationList = new ArrayList<>();
			List<String> precipitationVolumeList = new ArrayList<>();

			if (weatherSourceTypeList.get(i) == WeatherSourceType.KMA) {
				int index = 0;
				for (ForecastObj<FinalDailyForecast> item : kmaFinalDailyForecasts) {
					temp = ValueUnits.convertTemperature(item.e.getMinTemp(), tempUnit).toString() + tempUnitStr + " / " + ValueUnits.convertTemperature(
							item.e.getMaxTemp(), tempUnit).toString() + tempUnitStr;
					tempList.add(temp);

					if (index++ > 4) {
						pop = item.e.getProbabilityOfPrecipitation();
						weatherIconObjList.add(new DoubleWeatherIconView.WeatherIconObj(
								ContextCompat.getDrawable(context, KmaResponseProcessor.getWeatherMidIconImg(item.e.getSky(), false))));
					} else {
						pop = item.e.getAmProbabilityOfPrecipitation() + " / " + item.e.getPmProbabilityOfPrecipitation();
						weatherIconObjList.add(new DoubleWeatherIconView.WeatherIconObj(
								ContextCompat.getDrawable(context, KmaResponseProcessor.getWeatherMidIconImg(item.e.getAmSky(), false)),
								ContextCompat.getDrawable(context, KmaResponseProcessor.getWeatherMidIconImg(item.e.getPmSky(), false))));
					}
					probabilityOfPrecipitationList.add(pop);

				}


			} else if (weatherSourceTypeList.get(i) == WeatherSourceType.ACCU_WEATHER) {
				for (ForecastObj<FiveDaysOfDailyForecastsResponse.DailyForecasts> item : accuFinalDailyForecasts) {
					temp = ValueUnits.convertTemperature(item.e.getTemperature().getMinimum().getValue(),
							tempUnit).toString() + tempUnitStr + " " + "/ " + ValueUnits.convertTemperature(
							item.e.getTemperature().getMaximum().getValue(), tempUnit).toString() + tempUnitStr;
					tempList.add(temp);

					pop = (int) Double.parseDouble(item.e.getDay().getPrecipitationProbability()) + " / " +
							(int) Double.parseDouble(item.e.getNight().getPrecipitationProbability());
					probabilityOfPrecipitationList.add(pop);
					precipitationVolumeList.add(
							item.e.getDay().getTotalLiquid().getValue() + " / " + item.e.getNight().getTotalLiquid().getValue());

					weatherIconObjList.add(new DoubleWeatherIconView.WeatherIconObj(
							ContextCompat.getDrawable(context, AccuWeatherResponseProcessor.getWeatherIconImg(item.e.getDay().getIcon())),
							ContextCompat.getDrawable(context,
									AccuWeatherResponseProcessor.getWeatherIconImg(item.e.getNight().getIcon()))));
				}

			} else if (weatherSourceTypeList.get(i) == WeatherSourceType.OPEN_WEATHER_MAP) {
				for (ForecastObj<OneCallResponse.Daily> item : owmFinalDailyForecasts) {
					temp = ValueUnits.convertTemperature(item.e.getTemp().getMin(),
							tempUnit).toString() + tempUnitStr + " / " + ValueUnits.convertTemperature(item.e.getTemp().getMax(), tempUnit).toString() + tempUnitStr;
					tempList.add(temp);

					pop = item.e.getPop();
					probabilityOfPrecipitationList.add((String.valueOf((int) (Double.parseDouble(pop) * 100.0))));
					precipitationVolumeList.add(item.e.getRain() == null ? "0.0" : item.e.getRain());
					weatherIconObjList.add(new DoubleWeatherIconView.WeatherIconObj(ContextCompat.getDrawable(context,
							OpenWeatherMapResponseProcessor.getWeatherIconImg(item.e.getWeather().get(0).getId(), false))));
				}

			}

			weatherIconRows.get(i).setIcons(weatherIconObjList);
			tempRows.get(i).setValueList(tempList);
			probabilityOfPrecipitationRows.get(i).setValueList(probabilityOfPrecipitationList);
			precipitationVolumeRows.get(i).setValueList(precipitationVolumeList);
		}

		LinearLayout.LayoutParams rowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		rowLayoutParams.gravity = Gravity.CENTER_VERTICAL;

		binding.datetime.addView(dateRow, rowLayoutParams);
		LinearLayout view = null;
		notScrolledViews = new NotScrolledView[weatherSourceTypeList.size()];

		LinearLayout.LayoutParams nonScrollRowLayoutParams = new LinearLayout.LayoutParams(valueRowWidth,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		nonScrollRowLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		int nonScrollViewMargin = (int) getResources().getDimension(R.dimen.nonScrollViewTopBottomMargin);
		nonScrollRowLayoutParams.setMargins(0, nonScrollViewMargin, 0, nonScrollViewMargin);

		for (int i = 0; i < weatherSourceTypeList.size(); i++) {
			LinearLayout.LayoutParams specificRowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			specificRowLayoutParams.leftMargin = columnWidth * (Integer) weatherIconRows.get(i).getTag(R.id.begin_column_index);

			LinearLayout.LayoutParams iconTextRowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			iconTextRowLayoutParams.leftMargin = columnWidth * (Integer) weatherIconRows.get(i).getTag(R.id.begin_column_index);
			iconTextRowLayoutParams.topMargin = (int) getResources().getDimension(R.dimen.iconValueViewMargin);

			String sourceName;
			int logoId;
			switch (weatherSourceTypeList.get(i)) {
				case KMA:
					view = binding.kma;
					sourceName = getString(R.string.kma);
					logoId = R.drawable.kmaicon;
					break;
				case ACCU_WEATHER:
					view = binding.accu;
					sourceName = getString(R.string.accu_weather);
					logoId = R.drawable.accuicon;
					break;
				default:
					view = binding.owm;
					sourceName = getString(R.string.owm);
					logoId = R.drawable.owmicon;
					break;
			}
			notScrolledViews[i] = new NotScrolledView(getContext());
			notScrolledViews[i].setImg(logoId);
			notScrolledViews[i].setText(sourceName);

			//view.addView(nonScrolledViews[i], nonScrollRowLayoutParams);
			view.addView(notScrolledViews[i], nonScrollRowLayoutParams);
			view.addView(weatherIconRows.get(i), specificRowLayoutParams);
			view.addView(probabilityOfPrecipitationRows.get(i), iconTextRowLayoutParams);
			if (weatherSourceTypeList.get(i) != WeatherSourceType.KMA) {
				view.addView(precipitationVolumeRows.get(i), iconTextRowLayoutParams);
			}
			view.addView(tempRows.get(i), specificRowLayoutParams);
		}
	}

	private void loadForecasts() {
		ArrayMap<WeatherSourceType, RequestWeatherSource> request = new ArrayMap<>();

		RequestAccu requestAccu = new RequestAccu();
		requestAccu.addRequestServiceType(RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY);

		RequestOwm requestOwm = new RequestOwm();
		requestOwm.addRequestServiceType(RetrofitClient.ServiceType.OWM_ONE_CALL);
		Set<OneCallParameter.OneCallApis> exclude = new HashSet<>();
		exclude.add(OneCallParameter.OneCallApis.alerts);
		exclude.add(OneCallParameter.OneCallApis.minutely);
		exclude.add(OneCallParameter.OneCallApis.current);
		exclude.add(OneCallParameter.OneCallApis.hourly);
		requestOwm.setExcludeApis(exclude);

		request.put(WeatherSourceType.ACCU_WEATHER, requestAccu);
		request.put(WeatherSourceType.OPEN_WEATHER_MAP, requestOwm);

		if (countryCode.equals("KR")) {
			RequestKma requestKma = new RequestKma();
			requestKma.addRequestServiceType(RetrofitClient.ServiceType.MID_LAND_FCST).addRequestServiceType(
					RetrofitClient.ServiceType.MID_TA_FCST);

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

		multipleJsonDownloader = new MultipleJsonDownloader() {
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

	private void setTable(MultipleJsonDownloader multipleJsonDownloader, Double latitude, Double longitude,
	                      AlertDialog dialog) {
		Map<WeatherSourceType, ArrayMap<RetrofitClient.ServiceType, MultipleJsonDownloader.ResponseResult>> responseMap = multipleJsonDownloader.getResponseMap();
		ArrayMap<RetrofitClient.ServiceType, MultipleJsonDownloader.ResponseResult> arrayMap;
		DailyForecastResponse dailyForecastResponse = new DailyForecastResponse();

		//kma
		if (responseMap.containsKey(WeatherSourceType.KMA)) {
			arrayMap = responseMap.get(WeatherSourceType.KMA);
			MultipleJsonDownloader.ResponseResult midLandFcstResponse = arrayMap.get(RetrofitClient.ServiceType.MID_LAND_FCST);
			MultipleJsonDownloader.ResponseResult midTaFcstResponse = arrayMap.get(RetrofitClient.ServiceType.MID_TA_FCST);

			if (midLandFcstResponse.getT() == null && midTaFcstResponse.getT() == null) {
				MidLandFcstResponse midLandFcstRoot =
						(MidLandFcstResponse) midLandFcstResponse.getResponse().body();
				MidTaResponse midTaRoot = (MidTaResponse) midTaFcstResponse.getResponse().body();

				String successfulCode = "00";
				if (midLandFcstRoot.getKmaHeader().getResultCode().equals(
						successfulCode) && midTaRoot.getKmaHeader().getResultCode().equals(successfulCode)) {
					dailyForecastResponse.kmaDailyForecastList = KmaResponseProcessor.getFinalDailyForecastList(midLandFcstRoot, midTaRoot,
							Long.parseLong(multipleJsonDownloader.get("tmFc")));
				}
			} else {
				if (midLandFcstResponse.getT() != null) {
					dailyForecastResponse.kmaThrowable = midLandFcstResponse.getT();
				} else if (midTaFcstResponse.getT() != null) {
					dailyForecastResponse.kmaThrowable = midTaFcstResponse.getT();
				}
			}
		}

		//accu
		if (responseMap.containsKey(WeatherSourceType.ACCU_WEATHER)) {
			arrayMap = responseMap.get(WeatherSourceType.ACCU_WEATHER);
			MultipleJsonDownloader.ResponseResult accuDailyForecastResponse = arrayMap.get(
					RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY);

			if (accuDailyForecastResponse.getT() == null) {
				dailyForecastResponse.accuDailyForecastsResponse = AccuWeatherResponseProcessor.getDailyForecastObjFromJson(
						arrayMap.get(RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY).getResponse().body().toString());
			} else {
				dailyForecastResponse.accuThrowable = accuDailyForecastResponse.getT();
			}
		}
		//owm
		if (responseMap.containsKey(WeatherSourceType.OPEN_WEATHER_MAP)) {
			arrayMap = responseMap.get(WeatherSourceType.OPEN_WEATHER_MAP);
			MultipleJsonDownloader.ResponseResult responseResult = arrayMap.get(RetrofitClient.ServiceType.OWM_ONE_CALL);

			if (responseResult.getT() == null) {
				dailyForecastResponse.owmOneCallResponse = OpenWeatherMapResponseProcessor.getOneCallObjFromJson(
						responseResult.getResponse().body().toString());
			} else {
				dailyForecastResponse.owmThrowable = responseResult.getT();
			}
		}

		if (getActivity() != null) {
			getActivity().runOnUiThread(new Runnable() {
				@Override
				public void run() {
					setValuesToViews(dailyForecastResponse);
					binding.rootScrollView.setVisibility(View.VISIBLE);
					dialog.dismiss();
				}
			});

		}
	}

	static class DailyForecastResponse {
		List<FinalDailyForecast> kmaDailyForecastList;
		FiveDaysOfDailyForecastsResponse accuDailyForecastsResponse;
		OneCallResponse owmOneCallResponse;

		Throwable kmaThrowable;
		Throwable accuThrowable;
		Throwable owmThrowable;
	}

}
