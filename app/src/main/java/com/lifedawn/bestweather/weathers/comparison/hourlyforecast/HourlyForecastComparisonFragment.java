package com.lifedawn.bestweather.weathers.comparison.hourlyforecast;

import android.content.Context;
import android.graphics.Color;
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

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.ForecastObj;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestAccu;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestKma;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestOwm;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestWeatherSource;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.commons.views.ProgressDialog;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.openweathermap.OneCallParameter;
import com.lifedawn.bestweather.retrofit.responses.accuweather.twelvehoursofhourlyforecasts.TwelveHoursOfHourlyForecastsResponse;
import com.lifedawn.bestweather.retrofit.responses.kma.json.vilagefcstcommons.VilageFcstResponse;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.onecall.OneCallResponse;
import com.lifedawn.bestweather.retrofit.util.MultipleRestApiDownloader;
import com.lifedawn.bestweather.weathers.comparison.base.BaseForecastComparisonFragment;
import com.lifedawn.bestweather.weathers.dataprocessing.request.MainProcessing;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AccuWeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalHourlyForecast;
import com.lifedawn.bestweather.weathers.models.HourlyForecastDto;
import com.lifedawn.bestweather.weathers.view.DateView;
import com.lifedawn.bestweather.weathers.FragmentType;
import com.lifedawn.bestweather.weathers.view.IconTextView;
import com.lifedawn.bestweather.weathers.view.NotScrolledView;
import com.lifedawn.bestweather.weathers.view.SingleWeatherIconView;
import com.lifedawn.bestweather.weathers.view.TextsView;

import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class HourlyForecastComparisonFragment extends BaseForecastComparisonFragment {
	private MultipleRestApiDownloader multipleRestApiDownloader;


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
		final int weatherValueRowHeight = (int) getResources().getDimension(R.dimen.singleWeatherIconValueRowHeightInSC);
		final int defaultValueRowHeight = (int) getResources().getDimension(R.dimen.defaultValueRowHeightInSC);

		final List<WeatherSourceType> weatherSourceTypeList = new ArrayList<>();

		List<ForecastObj<HourlyForecastDto>> kmaFinalHourlyForecasts = null;
		List<ForecastObj<HourlyForecastDto>> accuFinalHourlyForecasts = null;
		List<ForecastObj<HourlyForecastDto>> owmFinalHourlyForecasts = null;

		if (hourlyForecastResponse.kmaSuccessful) {
			List<HourlyForecastDto> hourlyForecastDtoList = KmaResponseProcessor.makeHourlyForecastDtoList(getContext(),
					hourlyForecastResponse.kmaHourlyForecastList, latitude, longitude, windUnit, tempUnit);

			kmaFinalHourlyForecasts = new ArrayList<>();
			for (HourlyForecastDto finalHourlyForecast : hourlyForecastDtoList) {
				kmaFinalHourlyForecasts.add(new ForecastObj<>(finalHourlyForecast.getHours(), finalHourlyForecast));
			}

			weatherSourceTypeList.add(WeatherSourceType.KMA);
			binding.kma.setVisibility(View.VISIBLE);
		} else {
			binding.kma.setVisibility(View.GONE);
		}

		if (hourlyForecastResponse.accuSuccessful) {
			List<HourlyForecastDto> hourlyForecastDtoList = AccuWeatherResponseProcessor.makeHourlyForecastDtoList(getContext(),
					hourlyForecastResponse.accuHourlyForecastsResponse.getItems(), windUnit, tempUnit, visibilityUnit);
			accuFinalHourlyForecasts = new ArrayList<>();
			for (HourlyForecastDto finalHourlyForecast : hourlyForecastDtoList) {
				accuFinalHourlyForecasts.add(new ForecastObj<>(finalHourlyForecast.getHours(), finalHourlyForecast));
			}

			weatherSourceTypeList.add(WeatherSourceType.ACCU_WEATHER);
			binding.accu.setVisibility(View.VISIBLE);
		} else {
			binding.accu.setVisibility(View.GONE);
		}

		if (hourlyForecastResponse.owmSuccessful) {
			List<HourlyForecastDto> hourlyForecastDtoList = OpenWeatherMapResponseProcessor.makeHourlyForecastDtoList(getContext(),
					hourlyForecastResponse.owmOneCallResponse, windUnit, tempUnit, visibilityUnit);
			owmFinalHourlyForecasts = new ArrayList<>();

			for (HourlyForecastDto finalHourlyForecast : hourlyForecastDtoList) {
				owmFinalHourlyForecasts.add(new ForecastObj<>(finalHourlyForecast.getHours(), finalHourlyForecast));
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
		List<String> hourList = new ArrayList<>();
		//firstDateTime부터 lastDateTime까지 추가
		now = ZonedDateTime.of(firstDateTime.toLocalDateTime(), zoneId);
		while (!now.isAfter(lastDateTime)) {
			dateTimeList.add(now);
			hourList.add(String.valueOf(now.getHour()));
			now = now.plusHours(1);
		}

		final int columnsCount = dateTimeList.size();
		final int columnWidth = (int) getResources().getDimension(R.dimen.valueColumnWidthInSCHourly);
		final int valueRowWidth = columnWidth * columnsCount;

		//날짜, 시각, 날씨, 기온, 강수량, 강수확률
		dateRow = new DateView(getContext(), FragmentType.Comparison, valueRowWidth, columnWidth);
		TextsView clockRow = new TextsView(getContext(), valueRowWidth, columnWidth, hourList);
		SingleWeatherIconView[] weatherIconRows = new SingleWeatherIconView[columnsCount];
		TextsView[] tempRows = new TextsView[columnsCount];
		IconTextView[] rainVolumeRows = new IconTextView[columnsCount];
		IconTextView[] probabilityOfPrecipitationRows = new IconTextView[columnsCount];
		IconTextView[] snowVolumeRows = new IconTextView[columnsCount];

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

			tempRows[i] = new TextsView(getContext(), specificRowWidth, columnWidth, null);
			tempRows[i].setTag(R.id.begin_column_index, beginColumnIndex);

			rainVolumeRows[i] = new IconTextView(getContext(), FragmentType.Comparison, specificRowWidth,
					columnWidth, R.drawable.raindrop);
			rainVolumeRows[i].setTag(R.id.begin_column_index, beginColumnIndex);

			probabilityOfPrecipitationRows[i] = new IconTextView(getContext(), FragmentType.Comparison,
					specificRowWidth, columnWidth, R.drawable.pop);
			probabilityOfPrecipitationRows[i].setTag(R.id.begin_column_index, beginColumnIndex);

			snowVolumeRows[i] = new IconTextView(getContext(), FragmentType.Comparison, specificRowWidth,
					columnWidth, R.drawable.snowparticle);
			snowVolumeRows[i].setTag(R.id.begin_column_index, beginColumnIndex);
		}

		//날짜, 시각
		dateRow.init(dateTimeList);
		Context context = getContext();

		final String cm = "cm";
		final String mm = "mm";

		//날씨,기온,강수량,강수확률
		//kma, accu weather, owm 순서

		for (int i = 0; i < weatherSourceTypeList.size(); i++) {
			List<SingleWeatherIconView.WeatherIconObj> weatherIconObjList = new ArrayList<>();
			List<String> tempList = new ArrayList<>();
			List<String> popList = new ArrayList<>();
			List<String> rainVolumeList = new ArrayList<>();
			List<String> snowVolumeList = new ArrayList<>();
			boolean haveSnow = false;

			if (weatherSourceTypeList.get(i) == WeatherSourceType.KMA) {

				for (ForecastObj<HourlyForecastDto> item : kmaFinalHourlyForecasts) {
					weatherIconObjList.add(new SingleWeatherIconView.WeatherIconObj(
							ContextCompat.getDrawable(context, item.e.getWeatherIcon()), item.e.getWeatherDescription()));
					tempList.add(item.e.getTemp());

					popList.add(item.e.getPop());
					rainVolumeList.add(item.e.getRainVolume().replace(mm, ""));
					if (item.e.isHasSnow()) {
						if (!haveSnow) {
							haveSnow = true;
						}
					}
					snowVolumeList.add(item.e.getSnowVolume().replace(cm, ""));
				}

			} else if (weatherSourceTypeList.get(i) == WeatherSourceType.ACCU_WEATHER) {
				for (ForecastObj<HourlyForecastDto> item : accuFinalHourlyForecasts) {
					dateTimeList.add(item.e.getHours());
					weatherIconObjList.add(new SingleWeatherIconView.WeatherIconObj(
							ContextCompat.getDrawable(context, item.e.getWeatherIcon()), item.e.getWeatherDescription()));
					tempList.add(item.e.getTemp());
					popList.add(item.e.getPop());
					rainVolumeList.add(item.e.getRainVolume().replace(mm, ""));

					if (item.e.isHasSnow()) {
						if (!haveSnow) {
							haveSnow = true;
						}
					}
					snowVolumeList.add(item.e.getSnowVolume().replace(cm, ""));
				}

			} else if (weatherSourceTypeList.get(i) == WeatherSourceType.OPEN_WEATHER_MAP) {
				for (ForecastObj<HourlyForecastDto> item : owmFinalHourlyForecasts) {
					dateTimeList.add(item.e.getHours());
					weatherIconObjList.add(new SingleWeatherIconView.WeatherIconObj(ContextCompat.getDrawable(context, item.e.getWeatherIcon()),
							item.e.getWeatherDescription()));
					tempList.add(item.e.getTemp());
					popList.add(item.e.getPop());
					rainVolumeList.add(item.e.getRainVolume().replace("mm", ""));

					if (item.e.isHasSnow()) {
						if (!haveSnow) {
							haveSnow = true;
						}
					}
					snowVolumeList.add(item.e.getSnowVolume().replace("mm", ""));
				}

			}

			weatherIconRows[i].setWeatherImgs(weatherIconObjList);
			tempRows[i].setValueList(tempList);
			probabilityOfPrecipitationRows[i].setValueList(popList);
			rainVolumeRows[i].setValueList(rainVolumeList);
			if (haveSnow) {
				snowVolumeRows[i].setValueList(snowVolumeList);
			}
		}

		LinearLayout.LayoutParams rowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		clockRow.setValueTextColor(Color.BLACK);
		binding.datetime.addView(dateRow, rowLayoutParams);
		binding.datetime.addView(clockRow, rowLayoutParams);
		LinearLayout view = null;
		notScrolledViews = new NotScrolledView[weatherSourceTypeList.size()];

		LinearLayout.LayoutParams nonScrollRowLayoutParams = new LinearLayout.LayoutParams(valueRowWidth,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		nonScrollRowLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		int nonScrollViewMargin = (int) getResources().getDimension(R.dimen.nonScrollViewTopBottomMargin);
		nonScrollRowLayoutParams.setMargins(0, nonScrollViewMargin, 0, nonScrollViewMargin);

		final int tempRowTopMargin = (int) getResources().getDimension(R.dimen.tempTopMargin);

		for (int i = 0; i < weatherSourceTypeList.size(); i++) {
			LinearLayout.LayoutParams specificRowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			specificRowLayoutParams.leftMargin = columnWidth * (Integer) weatherIconRows[i].getTag(R.id.begin_column_index);

			LinearLayout.LayoutParams tempRowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			tempRowLayoutParams.leftMargin = columnWidth * (Integer) weatherIconRows[i].getTag(R.id.begin_column_index);
			tempRowLayoutParams.topMargin = tempRowTopMargin;

			LinearLayout.LayoutParams iconTextRowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			iconTextRowLayoutParams.leftMargin = columnWidth * (Integer) weatherIconRows[i].getTag(R.id.begin_column_index);

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

			view.addView(notScrolledViews[i], nonScrollRowLayoutParams);
			view.addView(weatherIconRows[i], specificRowLayoutParams);
			view.addView(probabilityOfPrecipitationRows[i], iconTextRowLayoutParams);
			view.addView(rainVolumeRows[i], iconTextRowLayoutParams);
			if (snowVolumeRows[i].getValueList() != null) {
				view.addView(snowVolumeRows[i], iconTextRowLayoutParams);
			}
			tempRows[i].setValueTextSize(17);
			tempRows[i].setValueTextColor(Color.BLACK);
			view.addView(tempRows[i], tempRowLayoutParams);
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
				if (multipleRestApiDownloader != null) {
					multipleRestApiDownloader.cancel();
				}
				getParentFragmentManager().popBackStack();
			}
		});

		multipleRestApiDownloader = new MultipleRestApiDownloader() {
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
				MainProcessing.requestNewWeatherData(getContext(), latitude, longitude, request, multipleRestApiDownloader);
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		multipleRestApiDownloader.cancel();
	}

	private void setTable(MultipleRestApiDownloader multipleRestApiDownloader, Double latitude, Double longitude,
	                      AlertDialog dialog) {
		Map<WeatherSourceType, ArrayMap<RetrofitClient.ServiceType, MultipleRestApiDownloader.ResponseResult>> responseMap = multipleRestApiDownloader.getResponseMap();
		ArrayMap<RetrofitClient.ServiceType, MultipleRestApiDownloader.ResponseResult> arrayMap;
		HourlyForecastResponse hourlyForecastResponse = new HourlyForecastResponse();

		//kma
		if (responseMap.containsKey(WeatherSourceType.KMA)) {
			arrayMap = responseMap.get(WeatherSourceType.KMA);
			MultipleRestApiDownloader.ResponseResult ultraSrtFcstResponse = Objects.requireNonNull(arrayMap).get(
					RetrofitClient.ServiceType.ULTRA_SRT_FCST);
			MultipleRestApiDownloader.ResponseResult vilageFcstResponse = arrayMap.get(RetrofitClient.ServiceType.VILAGE_FCST);

			if (ultraSrtFcstResponse.isSuccessful() && vilageFcstResponse.isSuccessful()) {

				VilageFcstResponse ultraSrtFcstRoot =
						(VilageFcstResponse) ultraSrtFcstResponse.getResponseObj();
				VilageFcstResponse vilageFcstRoot =
						(VilageFcstResponse) vilageFcstResponse.getResponseObj();

				hourlyForecastResponse.kmaSuccessful = true;
				hourlyForecastResponse.kmaHourlyForecastList = KmaResponseProcessor.getFinalHourlyForecastList(ultraSrtFcstRoot,
						vilageFcstRoot);
			} else {
				if (!ultraSrtFcstResponse.isSuccessful()) {
					hourlyForecastResponse.kmaThrowable = ultraSrtFcstResponse.getT();
				} else if (!vilageFcstResponse.isSuccessful()) {
					hourlyForecastResponse.kmaThrowable = vilageFcstResponse.getT();
				}
			}
		}

		//accu
		if (responseMap.containsKey(WeatherSourceType.ACCU_WEATHER)) {
			arrayMap = responseMap.get(WeatherSourceType.ACCU_WEATHER);
			MultipleRestApiDownloader.ResponseResult accuHourlyForecastResponse = arrayMap.get(
					RetrofitClient.ServiceType.ACCU_12_HOURLY);

			if (accuHourlyForecastResponse.isSuccessful()) {
				hourlyForecastResponse.accuSuccessful = true;
				hourlyForecastResponse.accuHourlyForecastsResponse =
						(TwelveHoursOfHourlyForecastsResponse) accuHourlyForecastResponse.getResponseObj();
			} else {
				hourlyForecastResponse.accuThrowable = accuHourlyForecastResponse.getT();
			}
		}
		//owm
		if (responseMap.containsKey(WeatherSourceType.OPEN_WEATHER_MAP)) {
			arrayMap = responseMap.get(WeatherSourceType.OPEN_WEATHER_MAP);
			MultipleRestApiDownloader.ResponseResult responseResult = arrayMap.get(RetrofitClient.ServiceType.OWM_ONE_CALL);

			if (responseResult.isSuccessful()) {
				hourlyForecastResponse.owmSuccessful = true;
				hourlyForecastResponse.owmOneCallResponse =
						(OneCallResponse) responseResult.getResponseObj();
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

		boolean kmaSuccessful;
		boolean accuSuccessful;
		boolean owmSuccessful;

		Throwable kmaThrowable;
		Throwable accuThrowable;
		Throwable owmThrowable;
	}

}
