package com.lifedawn.bestweather.weathers.comparison.dailyforecast;

import android.os.Bundle;
import android.util.ArrayMap;
import android.util.ArraySet;
import android.util.Log;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import com.google.gson.JsonElement;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.ForecastObj;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.responses.accuweather.fivedaysofdailyforecasts.FiveDaysOfDailyForecastsResponse;
import com.lifedawn.bestweather.retrofit.responses.kma.midlandfcstresponse.MidLandFcstRoot;
import com.lifedawn.bestweather.retrofit.responses.kma.midtaresponse.MidTaRoot;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.onecall.OneCallResponse;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.weathers.comparison.base.BaseForecastComparisonFragment;
import com.lifedawn.bestweather.weathers.dataprocessing.request.MainProcessing;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AccuWeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalDailyForecast;
import com.lifedawn.bestweather.weathers.view.TextValueView;
import com.lifedawn.bestweather.weathers.view.SingleWeatherIconView;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class DailyForecastComparisonFragment extends BaseForecastComparisonFragment {

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.toolbar.fragmentTitle.setText(R.string.comparison_daily_forecast);

		binding.addressName.setText(addressName);

		binding.dateLabelIcon.setVisibility(View.GONE);
		loadForecasts();
	}

	private void setValuesToViews(DailyForecastResponse dailyForecastResponse) {
		List<MainProcessing.WeatherSourceType> weatherSourceTypeList = new ArrayList<>();

		List<ForecastObj<FinalDailyForecast>> kmaFinalDailyForecasts = null;
		List<ForecastObj<FiveDaysOfDailyForecastsResponse.DailyForecasts>> accuFinalDailyForecasts = null;
		List<ForecastObj<OneCallResponse.Daily>> owmFinalDailyForecasts = null;

		if (dailyForecastResponse.kmaDailyForecastList != null) {
			kmaFinalDailyForecasts = new ArrayList<>();
			for (FinalDailyForecast finalDailyForecast : dailyForecastResponse.kmaDailyForecastList) {
				kmaFinalDailyForecasts.add(new ForecastObj<>(finalDailyForecast.getDate(), finalDailyForecast));
			}

			weatherSourceTypeList.add(MainProcessing.WeatherSourceType.KMA);
			binding.kmaLabelLayout.getRoot().setVisibility(View.VISIBLE);
		} else if (dailyForecastResponse.kmaThrowable != null) {
			binding.kmaLabelLayout.getRoot().setVisibility(View.GONE);
		}

		if (dailyForecastResponse.accuDailyForecastsResponse != null) {
			accuFinalDailyForecasts = new ArrayList<>();
			for (FiveDaysOfDailyForecastsResponse.DailyForecasts item : dailyForecastResponse.accuDailyForecastsResponse.getDailyForecasts()) {
				accuFinalDailyForecasts.add(new ForecastObj<>(
						WeatherResponseProcessor.convertDateTimeOfDailyForecast(Long.parseLong(item.getEpochDate()) * 1000L, timeZone), item));
			}
			weatherSourceTypeList.add(MainProcessing.WeatherSourceType.ACCU_WEATHER);
			binding.accuLabelLayout.getRoot().setVisibility(View.VISIBLE);
		} else if (dailyForecastResponse.accuThrowable != null) {
			binding.accuLabelLayout.getRoot().setVisibility(View.GONE);
		}

		if (dailyForecastResponse.owmOneCallResponse != null) {
			owmFinalDailyForecasts = new ArrayList<>();
			for (OneCallResponse.Daily daily : dailyForecastResponse.owmOneCallResponse.getDaily()) {
				owmFinalDailyForecasts.add(
						new ForecastObj<>(WeatherResponseProcessor.convertDateTimeOfDailyForecast(Long.parseLong(daily.getDt()) * 1000L, timeZone),
								daily));
			}
			weatherSourceTypeList.add(MainProcessing.WeatherSourceType.OPEN_WEATHER_MAP);
			binding.owmLabelLayout.getRoot().setVisibility(View.VISIBLE);
		} else if (dailyForecastResponse.owmThrowable != null) {
			binding.owmLabelLayout.getRoot().setVisibility(View.GONE);
		}

		binding.forecastView.removeAllViews();

		Calendar calendar = Calendar.getInstance(timeZone);
		calendar.add(Calendar.DATE, 10);
		calendar.set(Calendar.HOUR_OF_DAY, 0);
		calendar.set(Calendar.MINUTE, 0);
		calendar.set(Calendar.SECOND, 0);
		calendar.set(Calendar.MILLISECOND, 0);

		Date firstDateTime = calendar.getTime();
		calendar.add(Calendar.DATE, -20);
		Date lastDateTime = calendar.getTime();

		if (kmaFinalDailyForecasts != null) {
			if (kmaFinalDailyForecasts.get(0).dateTime.before(firstDateTime)) {
				firstDateTime.setTime(kmaFinalDailyForecasts.get(0).dateTime.getTime());
			}
			if (kmaFinalDailyForecasts.get(kmaFinalDailyForecasts.size() - 1).dateTime.after(lastDateTime)) {
				lastDateTime.setTime(kmaFinalDailyForecasts.get(kmaFinalDailyForecasts.size() - 1).dateTime.getTime());
			}
		}
		if (accuFinalDailyForecasts != null) {
			if (accuFinalDailyForecasts.get(0).dateTime.before(firstDateTime)) {
				firstDateTime.setTime(accuFinalDailyForecasts.get(0).dateTime.getTime());
			}
			if (accuFinalDailyForecasts.get(accuFinalDailyForecasts.size() - 1).dateTime.after(lastDateTime)) {
				lastDateTime.setTime(accuFinalDailyForecasts.get(accuFinalDailyForecasts.size() - 1).dateTime.getTime());
			}
		}
		if (owmFinalDailyForecasts != null) {
			if (owmFinalDailyForecasts.get(0).dateTime.before(firstDateTime)) {
				firstDateTime.setTime(owmFinalDailyForecasts.get(0).dateTime.getTime());
			}
			if (owmFinalDailyForecasts.get(owmFinalDailyForecasts.size() - 1).dateTime.after(lastDateTime)) {
				lastDateTime.setTime(owmFinalDailyForecasts.get(owmFinalDailyForecasts.size() - 1).dateTime.getTime());
			}
		}

		List<Date> dateTimeList = new ArrayList<>();
		//firstDateTime부터 lastDateTime까지 추가
		calendar.setTime(firstDateTime);
		while (!calendar.getTime().after(lastDateTime)) {
			dateTimeList.add(calendar.getTime());
			calendar.add(Calendar.DATE, 1);
		}

		final int columnsCount = dateTimeList.size();
		final int columnWidth = (int) getResources().getDimension(R.dimen.valueColumnWidthInSCDaily);
		final int valueRowWidth = columnWidth * columnsCount;
		final int lastRowMargin = (int) getResources().getDimension(R.dimen.lastRowInForecastTableMargin);

		final int dateValueRowHeight = (int) getResources().getDimension(R.dimen.dateValueRowHeightInCOMMON);
		final int weatherValueRowHeight = (int) getResources().getDimension(R.dimen.singleWeatherIconValueRowHeightInSC);
		final int defaultValueRowHeight = (int) getResources().getDimension(R.dimen.defaultValueRowHeightInSC);

		//날짜, 날씨, 기온, 강수량, 강수확률
		TextValueView dateRow = new TextValueView(getContext(), valueRowWidth, dateValueRowHeight, columnWidth);
		List<SingleWeatherIconView> weatherIconRows = new ArrayList<>();
		List<TextValueView> tempRows = new ArrayList<>();
		List<TextValueView> precipitationVolumeRows = new ArrayList<>();
		List<TextValueView> probabilityOfPrecipitationRows = new ArrayList<>();

		for (int i = 0; i < weatherSourceTypeList.size(); i++) {
			int specificRowWidth = 0;
			int beginColumnIndex = 0;

			if (weatherSourceTypeList.get(i) == MainProcessing.WeatherSourceType.KMA) {
				specificRowWidth = kmaFinalDailyForecasts.size() * columnWidth;

				for (int idx = 0; idx < dateTimeList.size(); idx++) {
					if (dateTimeList.get(idx).equals(kmaFinalDailyForecasts.get(0).dateTime)) {
						beginColumnIndex = idx;
						break;
					}
				}
			} else if (weatherSourceTypeList.get(i) == MainProcessing.WeatherSourceType.ACCU_WEATHER) {
				specificRowWidth = accuFinalDailyForecasts.size() * columnWidth;

				for (int idx = 0; idx < dateTimeList.size(); idx++) {
					if (dateTimeList.get(idx).equals(accuFinalDailyForecasts.get(0).dateTime)) {
						beginColumnIndex = idx;
						break;
					}
				}
			} else if (weatherSourceTypeList.get(i) == MainProcessing.WeatherSourceType.OPEN_WEATHER_MAP) {
				specificRowWidth = owmFinalDailyForecasts.size() * columnWidth;

				for (int idx = 0; idx < dateTimeList.size(); idx++) {
					if (dateTimeList.get(idx).equals(owmFinalDailyForecasts.get(0).dateTime)) {
						beginColumnIndex = idx;
						break;
					}
				}
			}
			weatherIconRows.add(new SingleWeatherIconView(getContext(), specificRowWidth, weatherValueRowHeight, columnWidth));
			weatherIconRows.get(i).setTag(R.id.begin_column_index, beginColumnIndex);

			tempRows.add(new TextValueView(getContext(), specificRowWidth, defaultValueRowHeight, columnWidth));
			tempRows.get(i).setTag(R.id.begin_column_index, beginColumnIndex);

			precipitationVolumeRows.add(new TextValueView(getContext(), specificRowWidth, defaultValueRowHeight, columnWidth));
			precipitationVolumeRows.get(i).setTag(R.id.begin_column_index, beginColumnIndex);

			probabilityOfPrecipitationRows.add(new TextValueView(getContext(), specificRowWidth, defaultValueRowHeight, columnWidth));
			probabilityOfPrecipitationRows.get(i).setTag(R.id.begin_column_index, beginColumnIndex);
		}

		//날짜
		List<String> dateList = new ArrayList<>();
		SimpleDateFormat MdE = new SimpleDateFormat("M.d E", Locale.getDefault());

		for (Date date : dateTimeList) {
			dateList.add(MdE.format(date));
		}
		dateRow.setValueList(dateList);

		//날씨,기온,강수량,강수확률
		//kma, accu weather, owm 순서
		String temp = null;
		String pop = null;

		for (int i = 0; i < weatherSourceTypeList.size(); i++) {
			List<String> tempList = new ArrayList<>();
			List<String> probabilityOfPrecipitationList = new ArrayList<>();
			List<String> precipitationVolumeList = new ArrayList<>();

			if (weatherSourceTypeList.get(i) == MainProcessing.WeatherSourceType.KMA) {
				int index = 0;
				for (ForecastObj<FinalDailyForecast> item : kmaFinalDailyForecasts) {
					temp = ValueUnits.convertTemperature(item.e.getMinTemp(), tempUnit).toString() + " / " + ValueUnits.convertTemperature(
							item.e.getMaxTemp(), tempUnit).toString();
					tempList.add(temp);

					if (index++ > 4) {
						pop = item.e.getProbabilityOfPrecipitation();
					} else {
						pop = item.e.getAmProbabilityOfPrecipitation() + " / " + item.e.getPmProbabilityOfPrecipitation();
					}
					probabilityOfPrecipitationList.add(pop);
				}

			} else if (weatherSourceTypeList.get(i) == MainProcessing.WeatherSourceType.ACCU_WEATHER) {
				for (ForecastObj<FiveDaysOfDailyForecastsResponse.DailyForecasts> item : accuFinalDailyForecasts) {
					temp = ValueUnits.convertTemperature(item.e.getTemperature().getMinimum().getValue(),
							tempUnit).toString() + " " + "/ " + ValueUnits.convertTemperature(
							item.e.getTemperature().getMaximum().getValue(), tempUnit).toString();
					tempList.add(temp);

					pop = item.e.getDay().getPrecipitationProbability() == null ? "0" : ((int) (Double.parseDouble(
							item.e.getDay().getTotalLiquid().getValue()) * 100.0)) + " / " + item.e.getNight().getPrecipitationProbability() == null ? "0" : (String.valueOf(
							(int) (Double.parseDouble(item.e.getNight().getTotalLiquid().getValue()) * 100.0)));
					probabilityOfPrecipitationList.add(pop);
					precipitationVolumeList.add(
							item.e.getDay().getTotalLiquid().getValue() + " / " + item.e.getNight().getTotalLiquid().getValue());
				}

			} else if (weatherSourceTypeList.get(i) == MainProcessing.WeatherSourceType.OPEN_WEATHER_MAP) {
				for (ForecastObj<OneCallResponse.Daily> item : owmFinalDailyForecasts) {
					temp = ValueUnits.convertTemperature(item.e.getTemp().getMin(),
							tempUnit).toString() + " / " + ValueUnits.convertTemperature(item.e.getTemp().getMax(), tempUnit).toString();
					tempList.add(temp);

					pop = item.e.getPop();
					probabilityOfPrecipitationList.add((String.valueOf((int) (Double.parseDouble(pop) * 100.0))));
					precipitationVolumeList.add(item.e.getRain() == null ? "-" : item.e.getRain());
				}

			}

			tempRows.get(i).setValueList(tempList);
			probabilityOfPrecipitationRows.get(i).setValueList(probabilityOfPrecipitationList);
			precipitationVolumeRows.get(i).setValueList(precipitationVolumeList);
		}

		LinearLayout.LayoutParams rowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		rowLayoutParams.gravity = Gravity.CENTER_VERTICAL;

		binding.forecastView.addView(dateRow, rowLayoutParams);
		for (int i = 0; i < weatherSourceTypeList.size(); i++) {
			LinearLayout.LayoutParams specificRowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			specificRowLayoutParams.leftMargin = columnWidth * (Integer) weatherIconRows.get(i).getTag(R.id.begin_column_index);

			binding.forecastView.addView(weatherIconRows.get(i), specificRowLayoutParams);
			binding.forecastView.addView(tempRows.get(i), specificRowLayoutParams);
			binding.forecastView.addView(precipitationVolumeRows.get(i), specificRowLayoutParams);

			LinearLayout.LayoutParams lastRowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			lastRowLayoutParams.leftMargin = columnWidth * (Integer) weatherIconRows.get(i).getTag(R.id.begin_column_index);
			lastRowLayoutParams.bottomMargin = lastRowMargin;
			binding.forecastView.addView(probabilityOfPrecipitationRows.get(i), lastRowLayoutParams);
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
		Log.e(RetrofitClient.LOG_TAG, "일별 비교 날씨 정보 요청, " + requestWeatherSourceTypeSet.toString());

		MainProcessing.downloadDailyForecasts(getContext(), latitude, longitude,
				requestWeatherSourceTypeSet, new MultipleJsonDownloader<JsonElement>() {
					@Override
					public void onResult() {
						setTable(this, latitude, longitude);
					}
				});

	}

	private void setTable(MultipleJsonDownloader<JsonElement> multipleJsonDownloader, Double latitude, Double longitude) {
		Map<MainProcessing.WeatherSourceType, ArrayMap<RetrofitClient.ServiceType, MultipleJsonDownloader.ResponseResult<JsonElement>>> responseMap = multipleJsonDownloader.getResponseMap();
		ArrayMap<RetrofitClient.ServiceType, MultipleJsonDownloader.ResponseResult<JsonElement>> arrayMap;
		DailyForecastResponse dailyForecastResponse = new DailyForecastResponse();

		//kma
		if (responseMap.containsKey(MainProcessing.WeatherSourceType.KMA)) {
			arrayMap = responseMap.get(MainProcessing.WeatherSourceType.KMA);
			MultipleJsonDownloader.ResponseResult<JsonElement> midLandFcstResponse = arrayMap.get(RetrofitClient.ServiceType.MID_LAND_FCST);
			MultipleJsonDownloader.ResponseResult<JsonElement> midTaFcstResponse = arrayMap.get(RetrofitClient.ServiceType.MID_TA_FCST);

			if (midLandFcstResponse.getT() == null && midTaFcstResponse.getT() == null) {
				MidLandFcstRoot midLandFcstRoot = KmaResponseProcessor.getMidLandObjFromJson(
						midLandFcstResponse.getResponse().body().toString());
				MidTaRoot midTaRoot = KmaResponseProcessor.getMidTaObjFromJson(midTaFcstResponse.getResponse().body().toString());

				String successfulCode = "00";
				if (midLandFcstRoot.getResponse().getHeader().getResultCode().equals(
						successfulCode) && midTaRoot.getResponse().getHeader().getResultCode().equals(successfulCode)) {
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
		if (responseMap.containsKey(MainProcessing.WeatherSourceType.ACCU_WEATHER)) {
			arrayMap = responseMap.get(MainProcessing.WeatherSourceType.ACCU_WEATHER);
			MultipleJsonDownloader.ResponseResult<JsonElement> geoCodingResponse = arrayMap.get(
					RetrofitClient.ServiceType.ACCU_GEOPOSITION_SEARCH);
			MultipleJsonDownloader.ResponseResult<JsonElement> accuDailyForecastResponse = arrayMap.get(
					RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY);

			if (geoCodingResponse.getT() == null && accuDailyForecastResponse.getT() == null) {

				dailyForecastResponse.accuDailyForecastsResponse = AccuWeatherResponseProcessor.getDailyForecastObjFromJson(
						arrayMap.get(RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY).getResponse().body().toString());
			} else {
				dailyForecastResponse.accuThrowable = geoCodingResponse.getT() != null ? geoCodingResponse.getT() : accuDailyForecastResponse.getT();
			}
		}
		//owm
		if (responseMap.containsKey(MainProcessing.WeatherSourceType.OPEN_WEATHER_MAP)) {
			arrayMap = responseMap.get(MainProcessing.WeatherSourceType.OPEN_WEATHER_MAP);
			MultipleJsonDownloader.ResponseResult<JsonElement> responseResult = arrayMap.get(RetrofitClient.ServiceType.OWM_ONE_CALL);

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
					binding.customProgressView.onSuccessfulProcessingData();
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
