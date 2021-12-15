package com.lifedawn.bestweather.weathers.comparison.dailyforecast;

import android.annotation.SuppressLint;
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
import com.lifedawn.bestweather.retrofit.responses.accuweather.fivedaysofdailyforecasts.FiveDaysOfDailyForecastsResponse;
import com.lifedawn.bestweather.retrofit.responses.kma.json.midlandfcstresponse.MidLandFcstResponse;
import com.lifedawn.bestweather.retrofit.responses.kma.json.midtaresponse.MidTaResponse;
import com.lifedawn.bestweather.retrofit.responses.kma.json.vilagefcstcommons.VilageFcstResponse;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.onecall.OneCallResponse;
import com.lifedawn.bestweather.retrofit.util.MultipleJsonDownloader;
import com.lifedawn.bestweather.weathers.comparison.base.BaseForecastComparisonFragment;
import com.lifedawn.bestweather.weathers.dataprocessing.request.MainProcessing;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AccuWeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalDailyForecast;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalHourlyForecast;
import com.lifedawn.bestweather.weathers.models.DailyForecastDto;
import com.lifedawn.bestweather.weathers.view.DoubleWeatherIconView;
import com.lifedawn.bestweather.weathers.FragmentType;
import com.lifedawn.bestweather.weathers.view.IconTextView;
import com.lifedawn.bestweather.weathers.view.NotScrolledView;
import com.lifedawn.bestweather.weathers.view.TextsView;

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

	@SuppressLint("DefaultLocale")
	private void setValuesToViews(DailyForecastResponse dailyForecastResponse) {
		final int weatherValueRowHeight = (int) getResources().getDimension(R.dimen.singleWeatherIconValueRowHeightInSC);

		List<WeatherSourceType> weatherSourceTypeList = new ArrayList<>();

		List<ForecastObj<DailyForecastDto>> kmaFinalDailyForecasts = null;
		List<ForecastObj<DailyForecastDto>> accuFinalDailyForecasts = null;
		List<ForecastObj<DailyForecastDto>> owmFinalDailyForecasts = null;

		if (dailyForecastResponse.kmaDailyForecastList != null) {
			List<DailyForecastDto> dailyForecastDtoList = KmaResponseProcessor.makeDailyForecastDtoList(getContext(),
					dailyForecastResponse.kmaDailyForecastList, tempUnit);

			kmaFinalDailyForecasts = new ArrayList<>();
			for (DailyForecastDto finalDailyForecast : dailyForecastDtoList) {
				kmaFinalDailyForecasts.add(new ForecastObj<>(finalDailyForecast.getDate(), finalDailyForecast));
			}
			weatherSourceTypeList.add(WeatherSourceType.KMA);
			binding.kma.setVisibility(View.VISIBLE);
		} else {
			binding.kma.setVisibility(View.GONE);
		}

		if (dailyForecastResponse.accuDailyForecastsResponse != null) {
			List<DailyForecastDto> dailyForecastDtoList = AccuWeatherResponseProcessor.makeDailyForecastDtoList(getContext(),
					dailyForecastResponse.accuDailyForecastsResponse.getDailyForecasts(), windUnit, tempUnit);
			accuFinalDailyForecasts = new ArrayList<>();

			for (DailyForecastDto item : dailyForecastDtoList) {
				accuFinalDailyForecasts.add(new ForecastObj<>(
						item.getDate(), item));
			}

			weatherSourceTypeList.add(WeatherSourceType.ACCU_WEATHER);
			binding.accu.setVisibility(View.VISIBLE);
		} else {
			binding.accu.setVisibility(View.GONE);
		}

		if (dailyForecastResponse.owmOneCallResponse != null) {
			List<DailyForecastDto> dailyForecastDtoList =
					OpenWeatherMapResponseProcessor.makeDailyForecastDtoList(getContext(), dailyForecastResponse.owmOneCallResponse,
							windUnit, tempUnit);
			owmFinalDailyForecasts = new ArrayList<>();
			for (DailyForecastDto daily : dailyForecastDtoList) {
				owmFinalDailyForecasts.add(new ForecastObj<>(daily.getDate(), daily));
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
		TextsView dateRow = new TextsView(getContext(), valueRowWidth, columnWidth, null);
		List<DoubleWeatherIconView> weatherIconRows = new ArrayList<>();
		List<IconTextView> rainVolumeRows = new ArrayList<>();
		List<IconTextView> snowVolumeRows = new ArrayList<>();
		List<IconTextView> probabilityOfPrecipitationRows = new ArrayList<>();
		List<TextsView> tempRows = new ArrayList<>();

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

			tempRows.add(new TextsView(getContext(), specificRowWidth, columnWidth, null));
			tempRows.get(i).setTag(R.id.begin_column_index, beginColumnIndex);

			rainVolumeRows.add(
					new IconTextView(getContext(), FragmentType.Comparison, specificRowWidth, columnWidth, R.drawable.raindrop));
			rainVolumeRows.get(i).setTag(R.id.begin_column_index, beginColumnIndex);

			snowVolumeRows.add(
					new IconTextView(getContext(), FragmentType.Comparison, specificRowWidth, columnWidth, R.drawable.snowparticle));
			snowVolumeRows.get(i).setTag(R.id.begin_column_index, beginColumnIndex);

			probabilityOfPrecipitationRows.add(
					new IconTextView(getContext(), FragmentType.Comparison, specificRowWidth, columnWidth, R.drawable.pop));
			probabilityOfPrecipitationRows.get(i).setTag(R.id.begin_column_index, beginColumnIndex);
		}

		//날짜
		List<String> dateList = new ArrayList<>();
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("M.d\nE");

		for (ZonedDateTime date : dateTimeList) {
			dateList.add(date.format(dateTimeFormatter));
		}
		dateRow.setValueList(dateList);
		dateRow.setValueTextColor(Color.BLACK);

		//날씨,기온,강수량,강수확률
		//kma, accu weather, owm 순서
		String temp = null;
		String pop = null;
		Context context = getContext();

		final String tempUnitStr = "°";
		final String percent = "%";
		final String cm = "cm";
		final String mm = "mm";

		for (int i = 0; i < weatherSourceTypeList.size(); i++) {
			List<DoubleWeatherIconView.WeatherIconObj> weatherIconObjList = new ArrayList<>();
			List<String> tempList = new ArrayList<>();
			List<String> probabilityOfPrecipitationList = new ArrayList<>();
			List<String> rainVolumeList = new ArrayList<>();
			List<String> snowVolumeList = new ArrayList<>();
			boolean haveSnow = false;

			if (weatherSourceTypeList.get(i) == WeatherSourceType.KMA) {
				for (ForecastObj<DailyForecastDto> item : kmaFinalDailyForecasts) {
					temp = item.e.getMinTemp() + " / " + item.e.getMaxTemp();
					tempList.add(temp);

					if (item.e.isSingle()) {
						pop = item.e.getSingleValues().getPop();
						weatherIconObjList.add(new DoubleWeatherIconView.WeatherIconObj(ContextCompat.getDrawable(getContext(), item.e.getSingleValues().getWeatherIcon()),
								item.e.getSingleValues().getWeatherDescription()));
					} else {
						pop = item.e.getAmValues().getPop() + " / " + item.e.getPmValues().getPop();
						weatherIconObjList.add(new DoubleWeatherIconView.WeatherIconObj(
								ContextCompat.getDrawable(context, item.e.getAmValues().getWeatherIcon()),
								ContextCompat.getDrawable(context, item.e.getPmValues().getWeatherIcon()),
								item.e.getAmValues().getWeatherDescription(),
								item.e.getPmValues().getWeatherDescription()));
					}
					probabilityOfPrecipitationList.add(pop);

				}

			} else if (weatherSourceTypeList.get(i) == WeatherSourceType.ACCU_WEATHER) {
				for (ForecastObj<DailyForecastDto> item : accuFinalDailyForecasts) {
					temp = item.e.getMinTemp() + " / " + item.e.getMaxTemp();
					tempList.add(temp);

					pop = item.e.getAmValues().getPop() + " / " + item.e.getPmValues().getPop();
					probabilityOfPrecipitationList.add(pop);

					rainVolumeList.add(
							String.format("%.2f", Float.parseFloat(item.e.getAmValues().getRainVolume().replace(mm, ""))
									+ Float.parseFloat(item.e.getPmValues().getRainVolume().replace(mm, ""))));
					snowVolumeList.add(
							String.format("%.2f", Float.parseFloat(item.e.getAmValues().getSnowVolume().replace(cm, ""))
									+ Float.parseFloat(item.e.getPmValues().getSnowVolume().replace(cm, ""))));

					if (!haveSnow) {
						if (item.e.getAmValues().isHasSnowVolume() ||
								item.e.getPmValues().isHasSnowVolume()) {
							haveSnow = true;
						}
					}
					weatherIconObjList.add(new DoubleWeatherIconView.WeatherIconObj(
							ContextCompat.getDrawable(context, item.e.getAmValues().getWeatherIcon()),
							ContextCompat.getDrawable(context, item.e.getPmValues().getWeatherIcon()),
							item.e.getAmValues().getWeatherDescription(),
							item.e.getPmValues().getWeatherDescription()));
				}

			} else if (weatherSourceTypeList.get(i) == WeatherSourceType.OPEN_WEATHER_MAP) {
				for (ForecastObj<DailyForecastDto> item : owmFinalDailyForecasts) {
					temp = item.e.getMinTemp() + " / " + item.e.getMaxTemp();
					tempList.add(temp);

					pop = item.e.getSingleValues().getPop();
					probabilityOfPrecipitationList.add(pop);

					if (item.e.getSingleValues().isHasSnowVolume()) {
						if (!haveSnow) {
							haveSnow = true;
						}
					}
					snowVolumeList.add(item.e.getSingleValues().getSnowVolume().replace(mm, ""));

					probabilityOfPrecipitationList.add(item.e.getSingleValues().getPop());
					rainVolumeList.add(item.e.getSingleValues().getRainVolume().replace(mm, ""));

					weatherIconObjList.add(new DoubleWeatherIconView.WeatherIconObj(ContextCompat.getDrawable(getContext(), item.e.getSingleValues().getWeatherIcon()),
							item.e.getSingleValues().getWeatherDescription()));
				}

			}

			weatherIconRows.get(i).setIcons(weatherIconObjList);
			tempRows.get(i).setValueList(tempList);
			probabilityOfPrecipitationRows.get(i).setValueList(probabilityOfPrecipitationList);
			rainVolumeRows.get(i).setValueList(rainVolumeList);

			if (haveSnow) {
				snowVolumeRows.get(i).setValueList(snowVolumeList);
			}
		}

		LinearLayout.LayoutParams rowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		binding.datetime.addView(dateRow, rowLayoutParams);
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
			specificRowLayoutParams.leftMargin = columnWidth * (Integer) weatherIconRows.get(i).getTag(R.id.begin_column_index);

			LinearLayout.LayoutParams tempRowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			tempRowLayoutParams.leftMargin = columnWidth * (Integer) weatherIconRows.get(i).getTag(R.id.begin_column_index);
			tempRowLayoutParams.topMargin = tempRowTopMargin;

			LinearLayout.LayoutParams iconTextRowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			iconTextRowLayoutParams.leftMargin = columnWidth * (Integer) weatherIconRows.get(i).getTag(R.id.begin_column_index);

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
			view.addView(weatherIconRows.get(i), specificRowLayoutParams);
			view.addView(probabilityOfPrecipitationRows.get(i), iconTextRowLayoutParams);
			if (weatherSourceTypeList.get(i) != WeatherSourceType.KMA) {
				view.addView(rainVolumeRows.get(i), iconTextRowLayoutParams);
				if (snowVolumeRows.get(i).getValueList() != null) {
					view.addView(snowVolumeRows.get(i), iconTextRowLayoutParams);
				}
			}
			tempRows.get(i).setValueTextSize(17);
			tempRows.get(i).setValueTextColor(Color.BLACK);
			view.addView(tempRows.get(i), tempRowLayoutParams);
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
					RetrofitClient.ServiceType.MID_TA_FCST)
					.addRequestServiceType(RetrofitClient.ServiceType.ULTRA_SRT_FCST)
					.addRequestServiceType(RetrofitClient.ServiceType.VILAGE_FCST);

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
			MultipleJsonDownloader.ResponseResult vilageFcstResponse = arrayMap.get(RetrofitClient.ServiceType.VILAGE_FCST);
			MultipleJsonDownloader.ResponseResult ultraSrtFcstResponse = arrayMap.get(RetrofitClient.ServiceType.ULTRA_SRT_FCST);

			if (midLandFcstResponse.isSuccessful() && midTaFcstResponse.isSuccessful() &&
					vilageFcstResponse.isSuccessful() && ultraSrtFcstResponse.isSuccessful()) {
				MidLandFcstResponse midLandFcstRoot =
						(MidLandFcstResponse) midLandFcstResponse.getResponse().body();
				MidTaResponse midTaRoot = (MidTaResponse) midTaFcstResponse.getResponse().body();
				VilageFcstResponse vilageFcstRoot = (VilageFcstResponse) vilageFcstResponse.getResponse().body();
				VilageFcstResponse ultraSrtFcstRoot = (VilageFcstResponse) ultraSrtFcstResponse.getResponse().body();

				List<FinalHourlyForecast> finalHourlyForecasts = KmaResponseProcessor.getFinalHourlyForecastList(ultraSrtFcstRoot,
						vilageFcstRoot);
				dailyForecastResponse.kmaDailyForecastList = KmaResponseProcessor.getFinalDailyForecastList(midLandFcstRoot, midTaRoot,
						Long.parseLong(multipleJsonDownloader.get("tmFc")));
				KmaResponseProcessor.getDailyForecastList(dailyForecastResponse.kmaDailyForecastList, finalHourlyForecasts);
			} else {
				if (midLandFcstResponse.getT() != null) {
					dailyForecastResponse.kmaThrowable = midLandFcstResponse.getT();
				} else if (midTaFcstResponse.getT() != null) {
					dailyForecastResponse.kmaThrowable = midTaFcstResponse.getT();
				} else if (vilageFcstResponse.getT() != null) {
					dailyForecastResponse.kmaThrowable = vilageFcstResponse.getT();
				} else if (ultraSrtFcstResponse.getT() != null) {
					dailyForecastResponse.kmaThrowable = ultraSrtFcstResponse.getT();
				}
			}
		}

		//accu
		if (responseMap.containsKey(WeatherSourceType.ACCU_WEATHER)) {
			arrayMap = responseMap.get(WeatherSourceType.ACCU_WEATHER);
			MultipleJsonDownloader.ResponseResult accuDailyForecastResponse = arrayMap.get(
					RetrofitClient.ServiceType.ACCU_5_DAYS_OF_DAILY);

			if (accuDailyForecastResponse.isSuccessful()) {
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

			if (responseResult.isSuccessful()) {
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
