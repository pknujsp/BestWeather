package com.lifedawn.bestweather.weathers.comparison.hourlyforecast;

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
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalHourlyForecast;
import com.lifedawn.bestweather.weathers.view.ClockView;
import com.lifedawn.bestweather.weathers.view.DateView;
import com.lifedawn.bestweather.weathers.view.TextValueView;
import com.lifedawn.bestweather.weathers.view.SingleWeatherIconView;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class HourlyForecastComparisonFragment extends BaseForecastComparisonFragment {
	
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.toolbar.fragmentTitle.setText(R.string.comparison_hourly_forecast);
		
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
			binding.kmaLabelLayout.getRoot().setVisibility(View.VISIBLE);
		} else if (hourlyForecastResponse.kmaThrowable != null) {
			binding.kmaLabelLayout.getRoot().setVisibility(View.GONE);
		}
		
		if (hourlyForecastResponse.accuHourlyForecastsResponse != null) {
			accuFinalHourlyForecasts = new ArrayList<>();
			for (TwelveHoursOfHourlyForecastsResponse.Item item : hourlyForecastResponse.accuHourlyForecastsResponse.getItems()) {
				accuFinalHourlyForecasts.add(new ForecastObj<>(new Date(Long.parseLong(item.getEpochDateTime()) * 1000L), item));
			}
			weatherSourceTypeList.add(MainProcessing.WeatherSourceType.ACCU_WEATHER);
			binding.accuLabelLayout.getRoot().setVisibility(View.VISIBLE);
		} else if (hourlyForecastResponse.accuThrowable != null) {
			binding.accuLabelLayout.getRoot().setVisibility(View.GONE);
		}
		
		if (hourlyForecastResponse.owmOneCallResponse != null) {
			owmFinalHourlyForecasts = new ArrayList<>();
			for (OneCallResponse.Hourly hourly : hourlyForecastResponse.owmOneCallResponse.getHourly()) {
				owmFinalHourlyForecasts.add(new ForecastObj<>(new Date(Long.parseLong(hourly.getDt()) * 1000L), hourly));
			}
			weatherSourceTypeList.add(MainProcessing.WeatherSourceType.OPEN_WEATHER_MAP);
			binding.owmLabelLayout.getRoot().setVisibility(View.VISIBLE);
		} else if (hourlyForecastResponse.owmThrowable != null) {
			binding.owmLabelLayout.getRoot().setVisibility(View.GONE);
		}
		
		binding.forecastView.removeAllViews();
		
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
				firstDateTime.setTime(kmaFinalHourlyForecasts.get(0).dateTime.getTime());
			}
			if (kmaFinalHourlyForecasts.get(kmaFinalHourlyForecasts.size() - 1).dateTime.after(lastDateTime)) {
				lastDateTime.setTime(kmaFinalHourlyForecasts.get(kmaFinalHourlyForecasts.size() - 1).dateTime.getTime());
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
		final int columnWidth = (int) getResources().getDimension(R.dimen.valueColumnWidthInSCHourly);
		final int valueRowWidth = columnWidth * columnsCount;
		final int lastRowMargin = (int) getResources().getDimension(R.dimen.lastRowInForecastTableMargin);
		
		final int dateValueRowHeight = (int) getResources().getDimension(R.dimen.dateValueRowHeightInCOMMON);
		final int clockValueRowHeight = (int) getResources().getDimension(R.dimen.clockValueRowHeightInCOMMON);
		final int weatherValueRowHeight = (int) getResources().getDimension(R.dimen.singleWeatherIconValueRowHeightInSC);
		final int defaultValueRowHeight = (int) getResources().getDimension(R.dimen.defaultValueRowHeightInSC);
		
		//날짜, 시각, 날씨, 기온, 강수량, 강수확률
		dateRow = new DateView(getContext(), valueRowWidth, dateValueRowHeight, columnWidth);
		ClockView clockRow = new ClockView(getContext(), valueRowWidth, clockValueRowHeight, columnWidth);
		SingleWeatherIconView[] weatherIconRows = new SingleWeatherIconView[columnsCount];
		TextValueView[] tempRows = new TextValueView[columnsCount];
		TextValueView[] precipitationVolumeRows = new TextValueView[columnsCount];
		TextValueView[] probabilityOfPrecipitationRows = new TextValueView[columnsCount];
		
		for (int i = 0; i < weatherSourceTypeList.size(); i++) {
			int specificRowWidth = 0;
			int beginColumnIndex = 0;
			
			if (weatherSourceTypeList.get(i) == MainProcessing.WeatherSourceType.KMA) {
				specificRowWidth = kmaFinalHourlyForecasts.size() * columnWidth;
				
				for (int idx = 0; idx < dateTimeList.size(); idx++) {
					if (dateTimeList.get(idx).equals(kmaFinalHourlyForecasts.get(0).dateTime)) {
						beginColumnIndex = idx;
						break;
					}
				}
			} else if (weatherSourceTypeList.get(i) == MainProcessing.WeatherSourceType.ACCU_WEATHER) {
				specificRowWidth = accuFinalHourlyForecasts.size() * columnWidth;
				
				for (int idx = 0; idx < dateTimeList.size(); idx++) {
					if (dateTimeList.get(idx).equals(accuFinalHourlyForecasts.get(0).dateTime)) {
						beginColumnIndex = idx;
						break;
					}
				}
			} else if (weatherSourceTypeList.get(i) == MainProcessing.WeatherSourceType.OPEN_WEATHER_MAP) {
				specificRowWidth = owmFinalHourlyForecasts.size() * columnWidth;
				
				for (int idx = 0; idx < dateTimeList.size(); idx++) {
					if (dateTimeList.get(idx).equals(owmFinalHourlyForecasts.get(0).dateTime)) {
						beginColumnIndex = idx;
						break;
					}
				}
			}
			
			weatherIconRows[i] = new SingleWeatherIconView(getContext(), specificRowWidth, weatherValueRowHeight, columnWidth);
			weatherIconRows[i].setTag(R.id.begin_column_index, beginColumnIndex);
			
			tempRows[i] = new TextValueView(getContext(), specificRowWidth, defaultValueRowHeight, columnWidth);
			tempRows[i].setTag(R.id.begin_column_index, beginColumnIndex);
			
			precipitationVolumeRows[i] = new TextValueView(getContext(), specificRowWidth, defaultValueRowHeight, columnWidth);
			precipitationVolumeRows[i].setTag(R.id.begin_column_index, beginColumnIndex);
			
			probabilityOfPrecipitationRows[i] = new TextValueView(getContext(), specificRowWidth, defaultValueRowHeight, columnWidth);
			probabilityOfPrecipitationRows[i].setTag(R.id.begin_column_index, beginColumnIndex);
		}
		
		//날짜, 시각
		dateRow.init(dateTimeList);
		clockRow.setClockList(dateTimeList);
		
		//날씨,기온,강수량,강수확률
		//kma, accu weather, owm 순서
		for (int i = 0; i < weatherSourceTypeList.size(); i++) {
			List<String> tempList = new ArrayList<>();
			List<String> probabilityOfPrecipitationList = new ArrayList<>();
			List<String> precipitationVolumeList = new ArrayList<>();
			
			if (weatherSourceTypeList.get(i) == MainProcessing.WeatherSourceType.KMA) {
				for (ForecastObj<FinalHourlyForecast> finalHourlyForecastObj : kmaFinalHourlyForecasts) {
					tempList.add(ValueUnits.convertTemperature(finalHourlyForecastObj.e.getTemp1Hour(), tempUnit).toString());
					probabilityOfPrecipitationList.add(finalHourlyForecastObj.e.getProbabilityOfPrecipitation());
					precipitationVolumeList.add(finalHourlyForecastObj.e.getRainPrecipitation1Hour());
				}
				
			} else if (weatherSourceTypeList.get(i) == MainProcessing.WeatherSourceType.ACCU_WEATHER) {
				for (ForecastObj<TwelveHoursOfHourlyForecastsResponse.Item> item : accuFinalHourlyForecasts) {
					tempList.add(ValueUnits.convertTemperature(item.e.getTemperature().getValue(), tempUnit).toString());
					probabilityOfPrecipitationList.add(
							item.e.getPrecipitationProbability() == null ? "-" : item.e.getPrecipitationProbability());
					precipitationVolumeList.add(item.e.getTotalLiquid().getValue());
				}
				
			} else if (weatherSourceTypeList.get(i) == MainProcessing.WeatherSourceType.OPEN_WEATHER_MAP) {
				for (ForecastObj<OneCallResponse.Hourly> item : owmFinalHourlyForecasts) {
					tempList.add(ValueUnits.convertTemperature(item.e.getTemp(), tempUnit).toString());
					probabilityOfPrecipitationList.add((String.valueOf((int) (Double.parseDouble(item.e.getPop()) * 100.0))));
					precipitationVolumeList.add(item.e.getRain() == null ? "-" : item.e.getRain().getPrecipitation1Hour());
				}
				
			}
			
			tempRows[i].setValueList(tempList);
			probabilityOfPrecipitationRows[i].setValueList(probabilityOfPrecipitationList);
			precipitationVolumeRows[i].setValueList(precipitationVolumeList);
		}
		
		LinearLayout.LayoutParams rowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		rowLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		
		binding.forecastView.addView(dateRow, rowLayoutParams);
		binding.forecastView.addView(clockRow, rowLayoutParams);
		for (int i = 0; i < weatherSourceTypeList.size(); i++) {
			LinearLayout.LayoutParams specificRowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			specificRowLayoutParams.leftMargin = columnWidth * (Integer) weatherIconRows[i].getTag(R.id.begin_column_index);
			
			binding.forecastView.addView(weatherIconRows[i], specificRowLayoutParams);
			binding.forecastView.addView(tempRows[i], specificRowLayoutParams);
			binding.forecastView.addView(precipitationVolumeRows[i], specificRowLayoutParams);
			
			LinearLayout.LayoutParams lastRowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			lastRowLayoutParams.leftMargin = columnWidth * (Integer) weatherIconRows[i].getTag(R.id.begin_column_index);
			lastRowLayoutParams.bottomMargin = lastRowMargin;
			binding.forecastView.addView(probabilityOfPrecipitationRows[i], lastRowLayoutParams);
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
