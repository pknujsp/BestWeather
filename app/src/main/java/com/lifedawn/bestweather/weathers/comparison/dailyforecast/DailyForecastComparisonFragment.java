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
import androidx.core.content.ContextCompat;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.ForecastObj;
import com.lifedawn.bestweather.commons.classes.MainThreadWorker;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestKma;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestMet;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestOwmOneCall;
import com.lifedawn.bestweather.commons.classes.requestweathersource.RequestWeatherSource;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.commons.views.ProgressDialog;
import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.retrofit.client.RetrofitClient;
import com.lifedawn.bestweather.retrofit.parameters.openweathermap.onecall.OneCallParameter;
import com.lifedawn.bestweather.retrofit.responses.accuweather.dailyforecasts.AccuDailyForecastsResponse;
import com.lifedawn.bestweather.retrofit.responses.kma.html.KmaDailyForecast;
import com.lifedawn.bestweather.retrofit.responses.kma.json.midlandfcstresponse.MidLandFcstResponse;
import com.lifedawn.bestweather.retrofit.responses.kma.json.midtaresponse.MidTaResponse;
import com.lifedawn.bestweather.retrofit.responses.kma.json.vilagefcstcommons.VilageFcstResponse;
import com.lifedawn.bestweather.retrofit.responses.metnorway.locationforecast.LocationForecastResponse;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.individual.dailyforecast.OwmDailyForecastResponse;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.onecall.OwmOneCallResponse;
import com.lifedawn.bestweather.retrofit.util.WeatherRestApiDownloader;
import com.lifedawn.bestweather.weathers.comparison.base.BaseForecastComparisonFragment;
import com.lifedawn.bestweather.weathers.dataprocessing.request.MainProcessing;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AccuWeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.MetNorwayResponseProcessor;
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
import java.util.Locale;
import java.util.Map;
import java.util.Set;

public class DailyForecastComparisonFragment extends BaseForecastComparisonFragment {
	private WeatherRestApiDownloader weatherRestApiDownloader;

	@Override
	public void onAttach(@NonNull Context context) {
		super.onAttach(context);
		ProgressDialog.show(requireActivity(), getString(R.string.msg_refreshing_weather_data), new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (weatherRestApiDownloader != null) {
					weatherRestApiDownloader.cancel();
				}
				getParentFragmentManager().popBackStack();
			}
		});
	}

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

	@Override
	public void onStart() {
		super.onStart();
	}

	@SuppressLint("DefaultLocale")
	private void setValues(DailyForecastResponse dailyForecastResponse) {
		final int weatherValueRowHeight = (int) getResources().getDimension(R.dimen.singleWeatherIconValueRowHeightInSC);

		List<WeatherProviderType> weatherProviderTypeList = new ArrayList<>();

		List<ForecastObj<DailyForecastDto>> kmaFinalDailyForecasts = null;
		List<ForecastObj<DailyForecastDto>> accuFinalDailyForecasts = null;
		List<ForecastObj<DailyForecastDto>> owmFinalDailyForecasts = null;
		List<ForecastObj<DailyForecastDto>> metNorwayFinalDailyForecasts = null;

		if (dailyForecastResponse.kmaDailyForecastList != null) {
			List<DailyForecastDto> dailyForecastDtoList = dailyForecastResponse.kmaDailyForecastList;

			kmaFinalDailyForecasts = new ArrayList<>();
			for (DailyForecastDto finalDailyForecast : dailyForecastDtoList) {
				kmaFinalDailyForecasts.add(new ForecastObj<>(finalDailyForecast.getDate(), finalDailyForecast));
			}
			weatherProviderTypeList.add(WeatherProviderType.KMA_WEB);
			binding.kma.setVisibility(View.VISIBLE);
		} else {
			binding.kma.setVisibility(View.GONE);
		}

		if (dailyForecastResponse.accuDailyForecastList != null) {
			List<DailyForecastDto> dailyForecastDtoList = dailyForecastResponse.accuDailyForecastList;
			accuFinalDailyForecasts = new ArrayList<>();

			for (DailyForecastDto item : dailyForecastDtoList) {
				accuFinalDailyForecasts.add(new ForecastObj<>(
						item.getDate(), item));
			}

			weatherProviderTypeList.add(WeatherProviderType.ACCU_WEATHER);
			binding.accu.setVisibility(View.VISIBLE);
		} else {
			binding.accu.setVisibility(View.GONE);
		}

		if (dailyForecastResponse.owmDailyForecastList != null) {
			List<DailyForecastDto> dailyForecastDtoList = dailyForecastResponse.owmDailyForecastList;
			owmFinalDailyForecasts = new ArrayList<>();
			for (DailyForecastDto daily : dailyForecastDtoList) {
				owmFinalDailyForecasts.add(new ForecastObj<>(daily.getDate(), daily));
			}
			weatherProviderTypeList.add(WeatherProviderType.OWM_ONECALL);
			binding.owm.setVisibility(View.VISIBLE);
		} else {
			binding.owm.setVisibility(View.GONE);
		}

		int idx_MetNorway_unavailableToMakeMinMaxTemp = 0;

		if (dailyForecastResponse.metNorwayDailyForecastList != null) {
			List<DailyForecastDto> dailyForecastDtoList = dailyForecastResponse.metNorwayDailyForecastList;
			metNorwayFinalDailyForecasts = new ArrayList<>();
			for (DailyForecastDto daily : dailyForecastDtoList) {
				if (!daily.isAvailable_toMakeMinMaxTemp())
					break;
				metNorwayFinalDailyForecasts.add(new ForecastObj<>(daily.getDate(), daily));
				idx_MetNorway_unavailableToMakeMinMaxTemp++;
			}
			weatherProviderTypeList.add(WeatherProviderType.MET_NORWAY);
			binding.metNorway.setVisibility(View.VISIBLE);
		} else {
			binding.metNorway.setVisibility(View.GONE);
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
		if (metNorwayFinalDailyForecasts != null) {
			if (metNorwayFinalDailyForecasts.get(0).dateTime.isBefore(firstDateTime)) {
				firstDateTime = ZonedDateTime.of(metNorwayFinalDailyForecasts.get(0).dateTime.toLocalDateTime(), zoneId);
			}
			if (metNorwayFinalDailyForecasts.get(idx_MetNorway_unavailableToMakeMinMaxTemp - 1).dateTime.isAfter(lastDateTime)) {
				lastDateTime = ZonedDateTime.of(metNorwayFinalDailyForecasts.get(idx_MetNorway_unavailableToMakeMinMaxTemp - 1).dateTime.toLocalDateTime(),
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

		for (int i = 0; i < weatherProviderTypeList.size(); i++) {
			int specificRowWidth = 0;
			int beginColumnIndex = 0;

			if (weatherProviderTypeList.get(i) == WeatherProviderType.KMA_WEB) {
				specificRowWidth = kmaFinalDailyForecasts.size() * columnWidth;

				for (int idx = 0; idx < dateTimeList.size(); idx++) {
					if (dateTimeList.get(idx).equals(kmaFinalDailyForecasts.get(0).dateTime)) {
						beginColumnIndex = idx;
						break;
					}
				}
			} else if (weatherProviderTypeList.get(i) == WeatherProviderType.ACCU_WEATHER) {
				specificRowWidth = accuFinalDailyForecasts.size() * columnWidth;

				for (int idx = 0; idx < dateTimeList.size(); idx++) {
					if (dateTimeList.get(idx).equals(accuFinalDailyForecasts.get(0).dateTime)) {
						beginColumnIndex = idx;
						break;
					}
				}
			} else if (weatherProviderTypeList.get(i) == WeatherProviderType.OWM_ONECALL) {
				specificRowWidth = owmFinalDailyForecasts.size() * columnWidth;

				for (int idx = 0; idx < dateTimeList.size(); idx++) {
					if (dateTimeList.get(idx).equals(owmFinalDailyForecasts.get(0).dateTime)) {
						beginColumnIndex = idx;
						break;
					}
				}
			} else if (weatherProviderTypeList.get(i) == WeatherProviderType.MET_NORWAY) {
				specificRowWidth = (idx_MetNorway_unavailableToMakeMinMaxTemp) * columnWidth;

				for (int idx = 0; idx < dateTimeList.size(); idx++) {
					if (dateTimeList.get(idx).equals(metNorwayFinalDailyForecasts.get(0).dateTime)) {
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

		final String cm = "cm";
		final String mm = "mm";
		final String degree = "°";

		List<WeatherSourceUnitObj> weatherSourceUnitObjList = new ArrayList<>();

		for (int i = 0; i < weatherProviderTypeList.size(); i++) {
			List<DoubleWeatherIconView.WeatherIconObj> weatherIconObjList = new ArrayList<>();
			List<String> tempList = new ArrayList<>();
			List<String> probabilityOfPrecipitationList = new ArrayList<>();
			List<String> rainVolumeList = new ArrayList<>();
			List<String> snowVolumeList = new ArrayList<>();
			boolean haveSnow = false;
			boolean haveRain = false;

			if (weatherProviderTypeList.get(i) == WeatherProviderType.KMA_WEB) {
				for (ForecastObj<DailyForecastDto> item : kmaFinalDailyForecasts) {
					temp = item.e.getMinTemp().replace(tempUnitText, degree) + " / " + item.e.getMaxTemp().replace(tempUnitText, degree);
					tempList.add(temp);

					if (item.e.getValuesList().size() == 1) {
						pop = item.e.getValuesList().get(0).getPop();
						weatherIconObjList.add(new DoubleWeatherIconView.WeatherIconObj(ContextCompat.getDrawable(getContext(), item.e.getValuesList().get(0).getWeatherIcon()),
								item.e.getValuesList().get(0).getWeatherDescription()));
					} else {
						pop = item.e.getValuesList().get(0).getPop() + " / " + item.e.getValuesList().get(1).getPop();
						weatherIconObjList.add(new DoubleWeatherIconView.WeatherIconObj(
								ContextCompat.getDrawable(context, item.e.getValuesList().get(0).getWeatherIcon()),
								ContextCompat.getDrawable(context, item.e.getValuesList().get(1).getWeatherIcon()),
								item.e.getValuesList().get(0).getWeatherDescription(),
								item.e.getValuesList().get(1).getWeatherDescription()));
					}
					probabilityOfPrecipitationList.add(pop);

				}

			} else if (weatherProviderTypeList.get(i) == WeatherProviderType.ACCU_WEATHER) {
				for (ForecastObj<DailyForecastDto> item : accuFinalDailyForecasts) {
					temp = item.e.getMinTemp().replace(tempUnitText, degree) + " / " + item.e.getMaxTemp().replace(tempUnitText, degree);
					tempList.add(temp);

					pop = item.e.getValuesList().get(0).getPop() + " / " + item.e.getValuesList().get(1).getPop();
					probabilityOfPrecipitationList.add(pop);

					rainVolumeList.add(
							String.format("%.2f", Float.parseFloat(item.e.getValuesList().get(0).getRainVolume().replace(mm, ""))
									+ Float.parseFloat(item.e.getValuesList().get(1).getRainVolume().replace(mm, ""))));
					snowVolumeList.add(
							String.format("%.2f", Float.parseFloat(item.e.getValuesList().get(0).getSnowVolume().replace(cm, ""))
									+ Float.parseFloat(item.e.getValuesList().get(1).getSnowVolume().replace(cm, ""))));

					if (!haveSnow) {
						if (item.e.getValuesList().get(0).isHasSnowVolume() ||
								item.e.getValuesList().get(1).isHasSnowVolume()) {
							haveSnow = true;
						}
					}

					if (!haveRain) {
						if (item.e.getValuesList().get(0).isHasRainVolume() ||
								item.e.getValuesList().get(1).isHasRainVolume()) {
							haveRain = true;
						}
					}
					weatherIconObjList.add(new DoubleWeatherIconView.WeatherIconObj(
							ContextCompat.getDrawable(context, item.e.getValuesList().get(0).getWeatherIcon()),
							ContextCompat.getDrawable(context, item.e.getValuesList().get(1).getWeatherIcon()),
							item.e.getValuesList().get(0).getWeatherDescription(),
							item.e.getValuesList().get(1).getWeatherDescription()));
				}

			} else if (weatherProviderTypeList.get(i) == WeatherProviderType.OWM_ONECALL) {
				for (ForecastObj<DailyForecastDto> item : owmFinalDailyForecasts) {
					temp = item.e.getMinTemp().replace(tempUnitText, degree) + " / " + item.e.getMaxTemp().replace(tempUnitText, degree);
					tempList.add(temp);

					pop = item.e.getValuesList().get(0).getPop();
					probabilityOfPrecipitationList.add(pop);

					if (item.e.getValuesList().get(0).isHasSnowVolume()) {
						if (!haveSnow) {
							haveSnow = true;
						}
					}
					if (item.e.getValuesList().get(0).isHasRainVolume()) {
						if (!haveRain) {
							haveRain = true;
						}
					}
					snowVolumeList.add(item.e.getValuesList().get(0).getSnowVolume().replace(mm, ""));
					rainVolumeList.add(item.e.getValuesList().get(0).getRainVolume().replace(mm, ""));

					weatherIconObjList.add(new DoubleWeatherIconView.WeatherIconObj(ContextCompat.getDrawable(getContext(), item.e.getValuesList().get(0).getWeatherIcon()),
							item.e.getValuesList().get(0).getWeatherDescription()));
				}
			} else if (weatherProviderTypeList.get(i) == WeatherProviderType.MET_NORWAY) {
				int idx = 0;
				float precipitationVolume = 0f;

				for (ForecastObj<DailyForecastDto> item : metNorwayFinalDailyForecasts) {
					temp = item.e.getMinTemp().replace(tempUnitText, degree) + " / " + item.e.getMaxTemp().replace(tempUnitText, degree);
					tempList.add(temp);

					probabilityOfPrecipitationList.add("-");


					if (item.e.getValuesList().get(0).isHasPrecipitationVolume() || item.e.getValuesList().get(1).isHasPrecipitationVolume() ||
							item.e.getValuesList().get(2).isHasPrecipitationVolume() || item.e.getValuesList().get(3).isHasPrecipitationVolume()) {
						if (!haveRain) {
							haveRain = true;
						}
					}

					precipitationVolume =
							Float.parseFloat(item.e.getValuesList().get(0).getPrecipitationVolume().replace(mm, ""))
									+ Float.parseFloat(item.e.getValuesList().get(1).getPrecipitationVolume().replace(mm, ""))
									+ Float.parseFloat(item.e.getValuesList().get(2).getPrecipitationVolume().replace(mm, ""))
									+ Float.parseFloat(item.e.getValuesList().get(3).getPrecipitationVolume().replace(mm, ""));

					rainVolumeList.add(String.format(Locale.getDefault(), "%.1f", precipitationVolume));

					weatherIconObjList.add(new DoubleWeatherIconView.WeatherIconObj(
							ContextCompat.getDrawable(context, item.e.getValuesList().get(1).getWeatherIcon()),
							ContextCompat.getDrawable(context, item.e.getValuesList().get(2).getWeatherIcon()),
							item.e.getValuesList().get(1).getWeatherDescription(),
							item.e.getValuesList().get(2).getWeatherDescription()));

					if (++idx == idx_MetNorway_unavailableToMakeMinMaxTemp)
						break;
				}
			}
			weatherSourceUnitObjList.add(new WeatherSourceUnitObj(weatherProviderTypeList.get(i), haveRain, haveSnow));

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
		notScrolledViews = new NotScrolledView[weatherProviderTypeList.size()];

		LinearLayout.LayoutParams nonScrollRowLayoutParams = new LinearLayout.LayoutParams(valueRowWidth,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		nonScrollRowLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		int nonScrollViewMargin = (int) getResources().getDimension(R.dimen.nonScrollViewTopBottomMargin);
		nonScrollRowLayoutParams.setMargins(nonScrollViewMargin, nonScrollViewMargin, 0, nonScrollViewMargin);

		final int tempRowTopMargin = (int) getResources().getDimension(R.dimen.tempTopMargin);

		for (int i = 0; i < weatherProviderTypeList.size(); i++) {
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
			switch (weatherProviderTypeList.get(i)) {
				case KMA_WEB:
					view = binding.kma;
					sourceName = getString(R.string.kma);
					logoId = R.drawable.kmaicon;
					break;
				case ACCU_WEATHER:
					view = binding.accu;
					sourceName = getString(R.string.accu_weather);
					logoId = R.drawable.accuicon;
					break;
				case OWM_ONECALL:
					view = binding.owm;
					sourceName = getString(R.string.owm);
					logoId = R.drawable.owmicon;
					break;
				default:
					view = binding.metNorway;
					sourceName = getString(R.string.met);
					logoId = R.drawable.metlogo;
					break;
			}
			notScrolledViews[i] = new NotScrolledView(getContext());
			notScrolledViews[i].setImg(logoId);
			notScrolledViews[i].setText(sourceName);

			view.addView(notScrolledViews[i], nonScrollRowLayoutParams);
			view.addView(weatherIconRows.get(i), specificRowLayoutParams);
			view.addView(probabilityOfPrecipitationRows.get(i), iconTextRowLayoutParams);

			if (weatherProviderTypeList.get(i) != WeatherProviderType.KMA_WEB) {
				view.addView(rainVolumeRows.get(i), iconTextRowLayoutParams);

				if (snowVolumeRows.get(i).getValueList() != null) {
					view.addView(snowVolumeRows.get(i), iconTextRowLayoutParams);
				}
			}
			tempRows.get(i).setValueTextSize(17);
			tempRows.get(i).setValueTextColor(Color.BLACK);
			view.addView(tempRows.get(i), tempRowLayoutParams);
		}

		createValueUnitsDescription(weatherSourceUnitObjList);
	}

	private void loadForecasts() {
		ArrayMap<WeatherProviderType, RequestWeatherSource> request = new ArrayMap<>();

		//RequestAccu requestAccu = new RequestAccu();
		//requestAccu.addRequestServiceType(RetrofitClient.ServiceType.ACCU_DAILY_FORECAST);

		/*
		RequestOwmIndividual requestOwmIndividual = new RequestOwmIndividual();
		requestOwmIndividual.addRequestServiceType(RetrofitClient.ServiceType.OWM_DAILY_FORECAST);

		 */

		RequestMet requestMet = new RequestMet();
		requestMet.addRequestServiceType(RetrofitClient.ServiceType.MET_NORWAY_LOCATION_FORECAST);

		request.put(WeatherProviderType.MET_NORWAY, requestMet);

		RequestOwmOneCall requestOwmOneCall = new RequestOwmOneCall();
		requestOwmOneCall.addRequestServiceType(RetrofitClient.ServiceType.OWM_ONE_CALL);
		Set<OneCallParameter.OneCallApis> exclude = new HashSet<>();
		exclude.add(OneCallParameter.OneCallApis.alerts);
		exclude.add(OneCallParameter.OneCallApis.minutely);
		exclude.add(OneCallParameter.OneCallApis.current);
		exclude.add(OneCallParameter.OneCallApis.hourly);
		requestOwmOneCall.setExcludeApis(exclude);

		//request.put(WeatherDataSourceType.ACCU_WEATHER, requestAccu);
		request.put(WeatherProviderType.OWM_ONECALL, requestOwmOneCall);
		//request.put(WeatherDataSourceType.OWM_INDIVIDUAL, requestOwmIndividual);

		if (countryCode.equals("KR")) {
			RequestKma requestKma = new RequestKma();
			requestKma.addRequestServiceType(RetrofitClient.ServiceType.KMA_WEB_FORECASTS);
			request.put(WeatherProviderType.KMA_WEB, requestKma);
		}

		weatherRestApiDownloader = new WeatherRestApiDownloader() {
			@Override
			public void onResult() {
				setTable(this);
			}

			@Override
			public void onCanceled() {

			}
		};
		weatherRestApiDownloader.setZoneId(zoneId);
		MyApplication.getExecutorService().execute(new Runnable() {
			@Override
			public void run() {
				MainProcessing.requestNewWeatherData(getContext(), latitude, longitude, request, weatherRestApiDownloader);
			}
		});
	}

	@Override
	public void onDestroy() {
		super.onDestroy();

		if (weatherRestApiDownloader != null) {
			weatherRestApiDownloader.cancel();
		}
	}

	private void setTable(WeatherRestApiDownloader weatherRestApiDownloader) {
		Map<WeatherProviderType, ArrayMap<RetrofitClient.ServiceType, WeatherRestApiDownloader.ResponseResult>> responseMap = weatherRestApiDownloader.getResponseMap();
		ArrayMap<RetrofitClient.ServiceType, WeatherRestApiDownloader.ResponseResult> arrayMap;
		DailyForecastResponse dailyForecastResponse = new DailyForecastResponse();

		//kma api
		if (responseMap.containsKey(WeatherProviderType.KMA_API)) {
			arrayMap = responseMap.get(WeatherProviderType.KMA_API);
			WeatherRestApiDownloader.ResponseResult midLandFcstResponse = arrayMap.get(RetrofitClient.ServiceType.KMA_MID_LAND_FCST);
			WeatherRestApiDownloader.ResponseResult midTaFcstResponse = arrayMap.get(RetrofitClient.ServiceType.KMA_MID_TA_FCST);
			WeatherRestApiDownloader.ResponseResult vilageFcstResponse = arrayMap.get(RetrofitClient.ServiceType.KMA_VILAGE_FCST);
			WeatherRestApiDownloader.ResponseResult ultraSrtFcstResponse = arrayMap.get(RetrofitClient.ServiceType.KMA_ULTRA_SRT_FCST);

			if (midLandFcstResponse.isSuccessful() && midTaFcstResponse.isSuccessful() &&
					vilageFcstResponse.isSuccessful() && ultraSrtFcstResponse.isSuccessful()) {
				MidLandFcstResponse midLandFcstRoot =
						(MidLandFcstResponse) midLandFcstResponse.getResponseObj();
				MidTaResponse midTaRoot = (MidTaResponse) midTaFcstResponse.getResponseObj();
				VilageFcstResponse vilageFcstRoot = (VilageFcstResponse) vilageFcstResponse.getResponseObj();
				VilageFcstResponse ultraSrtFcstRoot = (VilageFcstResponse) ultraSrtFcstResponse.getResponseObj();

				List<FinalHourlyForecast> finalHourlyForecasts = KmaResponseProcessor.getFinalHourlyForecastListByXML(ultraSrtFcstRoot,
						vilageFcstRoot);
				List<FinalDailyForecast> finalDailyForecasts = KmaResponseProcessor.getFinalDailyForecastListByXML(midLandFcstRoot, midTaRoot,
						Long.parseLong(weatherRestApiDownloader.get("tmFc")));
				KmaResponseProcessor.getDailyForecastListByXML(finalDailyForecasts, finalHourlyForecasts);

				dailyForecastResponse.kmaDailyForecastList = KmaResponseProcessor.makeDailyForecastDtoListOfXML(finalDailyForecasts
				);
			} else {
				if (midLandFcstResponse.getT() != null) {
					dailyForecastResponse.kmaThrowable = midLandFcstResponse.getT();
				} else if (midTaFcstResponse.getT() != null) {
					dailyForecastResponse.kmaThrowable = midTaFcstResponse.getT();
				} else if (vilageFcstResponse.getT() != null) {
					dailyForecastResponse.kmaThrowable = vilageFcstResponse.getT();
				} else {
					dailyForecastResponse.kmaThrowable = ultraSrtFcstResponse.getT();
				}
			}
		}

		//kma web
		if (responseMap.containsKey(WeatherProviderType.KMA_WEB)) {
			arrayMap = responseMap.get(WeatherProviderType.KMA_WEB);
			WeatherRestApiDownloader.ResponseResult forecastsResponseResult = arrayMap.get(RetrofitClient.ServiceType.KMA_WEB_FORECASTS);

			if (forecastsResponseResult.isSuccessful()) {
				Object[] objects = (Object[]) forecastsResponseResult.getResponseObj();
				List<KmaDailyForecast> kmaDailyForecasts = (List<KmaDailyForecast>) objects[1];

				dailyForecastResponse.kmaDailyForecastList = KmaResponseProcessor.makeDailyForecastDtoListOfWEB(
						kmaDailyForecasts);
			} else {
				dailyForecastResponse.kmaThrowable = forecastsResponseResult.getT();
			}
		}


		//accu
		if (responseMap.containsKey(WeatherProviderType.ACCU_WEATHER)) {
			arrayMap = responseMap.get(WeatherProviderType.ACCU_WEATHER);
			WeatherRestApiDownloader.ResponseResult accuDailyForecastResponse = arrayMap.get(
					RetrofitClient.ServiceType.ACCU_DAILY_FORECAST);

			if (accuDailyForecastResponse.isSuccessful()) {
				AccuDailyForecastsResponse dailyForecastsResponse =
						(AccuDailyForecastsResponse) arrayMap.get(RetrofitClient.ServiceType.ACCU_DAILY_FORECAST).getResponseObj();
				dailyForecastResponse.accuDailyForecastList = AccuWeatherResponseProcessor.makeDailyForecastDtoList(getContext(),
						dailyForecastsResponse.getDailyForecasts());
				;
			} else {
				dailyForecastResponse.accuThrowable = accuDailyForecastResponse.getT();
			}
		}

		//owm onecall
		if (responseMap.containsKey(WeatherProviderType.OWM_ONECALL)) {
			arrayMap = responseMap.get(WeatherProviderType.OWM_ONECALL);
			WeatherRestApiDownloader.ResponseResult responseResult = arrayMap.get(RetrofitClient.ServiceType.OWM_ONE_CALL);

			if (responseResult.isSuccessful()) {
				dailyForecastResponse.owmDailyForecastList =
						OpenWeatherMapResponseProcessor.makeDailyForecastDtoListOneCall(getContext(),
								(OwmOneCallResponse) responseResult.getResponseObj(), zoneId);
			} else {
				dailyForecastResponse.owmThrowable = responseResult.getT();
			}
		}

		//owm individual
		if (responseMap.containsKey(WeatherProviderType.OWM_INDIVIDUAL)) {
			arrayMap = responseMap.get(WeatherProviderType.OWM_INDIVIDUAL);
			WeatherRestApiDownloader.ResponseResult responseResult = arrayMap.get(RetrofitClient.ServiceType.OWM_DAILY_FORECAST);

			if (responseResult.isSuccessful()) {
				dailyForecastResponse.owmDailyForecastList =
						OpenWeatherMapResponseProcessor.makeDailyForecastDtoListIndividual(getContext(),
								(OwmDailyForecastResponse) responseResult.getResponseObj(), zoneId);
			} else {
				dailyForecastResponse.owmThrowable = responseResult.getT();
			}
		}

		//met norway
		if (responseMap.containsKey(WeatherProviderType.MET_NORWAY)) {
			arrayMap = responseMap.get(WeatherProviderType.MET_NORWAY);
			WeatherRestApiDownloader.ResponseResult responseResult = arrayMap.get(RetrofitClient.ServiceType.MET_NORWAY_LOCATION_FORECAST);

			if (responseResult.isSuccessful()) {
				dailyForecastResponse.metNorwayDailyForecastList =
						MetNorwayResponseProcessor.makeDailyForecastDtoList(getContext(),
								(LocationForecastResponse) responseResult.getResponseObj(), zoneId);
			} else {
				dailyForecastResponse.metNorwayThrowable = responseResult.getT();
			}
		}

		if (getActivity() != null) {
			MainThreadWorker.runOnUiThread(new Runnable() {
				@Override
				public void run() {
					setValues(dailyForecastResponse);
					binding.rootScrollView.setVisibility(View.VISIBLE);
					ProgressDialog.clearDialogs();
				}
			});

		}
	}

	private static class DailyForecastResponse {
		List<DailyForecastDto> kmaDailyForecastList;
		List<DailyForecastDto> accuDailyForecastList;
		List<DailyForecastDto> owmDailyForecastList;
		List<DailyForecastDto> metNorwayDailyForecastList;

		Throwable kmaThrowable;
		Throwable accuThrowable;
		Throwable owmThrowable;
		Throwable metNorwayThrowable;
	}

}
