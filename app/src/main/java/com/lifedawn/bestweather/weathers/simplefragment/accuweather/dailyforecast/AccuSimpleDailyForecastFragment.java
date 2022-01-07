package com.lifedawn.bestweather.weathers.simplefragment.accuweather.dailyforecast;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.WeatherDataSourceType;
import com.lifedawn.bestweather.commons.enums.WeatherValueType;
import com.lifedawn.bestweather.retrofit.responses.accuweather.dailyforecasts.AccuDailyForecastsResponse;
import com.lifedawn.bestweather.weathers.WeatherFragment;
import com.lifedawn.bestweather.weathers.comparison.dailyforecast.DailyForecastComparisonFragment;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AccuWeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.detailfragment.accuweather.dailyforecast.AccuDetailDailyForecastFragment;
import com.lifedawn.bestweather.weathers.models.DailyForecastDto;
import com.lifedawn.bestweather.weathers.simplefragment.base.BaseSimpleForecastFragment;
import com.lifedawn.bestweather.weathers.view.DetailDoubleTemperatureView;
import com.lifedawn.bestweather.weathers.view.DoubleWeatherIconView;
import com.lifedawn.bestweather.weathers.FragmentType;
import com.lifedawn.bestweather.weathers.view.IconTextView;
import com.lifedawn.bestweather.weathers.view.TextsView;

import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AccuSimpleDailyForecastFragment extends BaseSimpleForecastFragment {
	private AccuDailyForecastsResponse accuDailyForecastsResponse;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		needCompare = true;

	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.weatherCardViewHeader.forecastName.setText(R.string.daily_forecast);
		binding.weatherCardViewHeader.compareForecast.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				DailyForecastComparisonFragment comparisonFragment = new DailyForecastComparisonFragment();
				comparisonFragment.setArguments(getArguments());

				String tag = getString(R.string.tag_comparison_fragment);
				FragmentManager fragmentManager = getParentFragment().getParentFragmentManager();

				fragmentManager.beginTransaction().hide(
						fragmentManager.findFragmentByTag(WeatherFragment.class.getName())).add(R.id.fragment_container,
						comparisonFragment, tag).addToBackStack(tag).commit();
			}
		});

		binding.weatherCardViewHeader.detailForecast.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				AccuDetailDailyForecastFragment detailDailyForecastFragment = new AccuDetailDailyForecastFragment();
				detailDailyForecastFragment.setDailyForecastsList(accuDailyForecastsResponse.getDailyForecasts());

				Bundle bundle = new Bundle();
				bundle.putString(BundleKey.AddressName.name(), addressName);
				bundle.putSerializable(BundleKey.TimeZone.name(), zoneId);
				detailDailyForecastFragment.setArguments(bundle);

				String tag = getString(R.string.tag_detail_daily_forecast_fragment);
				FragmentManager fragmentManager = getParentFragment().getParentFragmentManager();
				fragmentManager.beginTransaction().hide(
						fragmentManager.findFragmentByTag(WeatherFragment.class.getName())).add(R.id.fragment_container,
						detailDailyForecastFragment, tag).addToBackStack(tag).commit();
			}
		});
		setValuesToViews();
	}

	public AccuSimpleDailyForecastFragment setFiveDaysOfDailyForecastsResponse(
			AccuDailyForecastsResponse accuDailyForecastsResponse) {
		this.accuDailyForecastsResponse = accuDailyForecastsResponse;
		return this;
	}

	@Override
	public void setValuesToViews() {
		// 날짜, 최저/최고 기온 ,낮과 밤의 날씨상태, 강수확률, 강수량
		Context context = getContext();

		final int WEATHER_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.singleWeatherIconValueRowHeightInSC);
		final int TEMP_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.doubleTemperatureRowHeightInSC);

		List<AccuDailyForecastsResponse.DailyForecasts> items = accuDailyForecastsResponse.getDailyForecasts();

		final int columnCount = items.size();
		final int columnWidth = (int) context.getResources().getDimension(R.dimen.valueColumnWidthInSDailyAccuKma);
		final int viewWidth = columnCount * columnWidth;


		DoubleWeatherIconView weatherIconRow = new DoubleWeatherIconView(context, FragmentType.Simple, viewWidth, WEATHER_ROW_HEIGHT,
				columnWidth);
		IconTextView popRow = new IconTextView(context, FragmentType.Simple, viewWidth,
				columnWidth, R.drawable.pop);
		IconTextView rainVolumeRow = new IconTextView(context, FragmentType.Simple, viewWidth,
				columnWidth, R.drawable.raindrop);
		IconTextView snowVolumeRow = new IconTextView(context, FragmentType.Simple, viewWidth,
				columnWidth, R.drawable.snowparticle);

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("M.d\nE", Locale.getDefault());

		//시각, 기온, 강수확률, 강수량--------------------------------
		List<DoubleWeatherIconView.WeatherIconObj> weatherIconObjList = new ArrayList<>();
		List<String> dateList = new ArrayList<>();
		List<Integer> minTempList = new ArrayList<>();
		List<Integer> maxTempList = new ArrayList<>();
		List<String> popList = new ArrayList<>();
		List<String> rainVolumeList = new ArrayList<>();
		List<String> snowVolumeList = new ArrayList<>();

		final String mm = "mm";
		final String cm = "cm";
		final String degree = "°";
		boolean haveSnow = false;
		boolean haveRain = false;

		Float rainVolume = 0f;
		Float snowVolume = 0f;

		List<DailyForecastDto> dailyForecastDtoList = AccuWeatherResponseProcessor.makeDailyForecastDtoList(getContext(), items, windUnit,
				tempUnit);

		int minTemp;
		int maxTemp;

		for (DailyForecastDto dailyForecasts : dailyForecastDtoList) {
			rainVolume = 0f;
			snowVolume = 0f;

			dateList.add(dailyForecasts.getDate().format(dateTimeFormatter));
			weatherIconObjList.add(new DoubleWeatherIconView.WeatherIconObj(
					ContextCompat.getDrawable(context, dailyForecasts.getAmValues().getWeatherIcon()),
					ContextCompat.getDrawable(context, dailyForecasts.getAmValues().getWeatherIcon()),
					dailyForecasts.getAmValues().getWeatherDescription(),
					dailyForecasts.getPmValues().getWeatherDescription()));

			minTemp = Integer.parseInt(dailyForecasts.getMinTemp().replace(degree, ""));
			maxTemp = Integer.parseInt(dailyForecasts.getMaxTemp().replace(degree, ""));

			minTempList.add(minTemp);
			maxTempList.add(maxTemp);

			popList.add(
					dailyForecasts.getAmValues().getPop() + " / " + dailyForecasts.getPmValues().getPop());

			rainVolume = Float.parseFloat(dailyForecasts.getAmValues().getRainVolume().replace(mm, ""))
					+ Float.parseFloat(dailyForecasts.getPmValues().getRainVolume().replace(mm, ""));
			snowVolume = Float.parseFloat(dailyForecasts.getAmValues().getSnowVolume().replace(cm, ""))
					+ Float.parseFloat(dailyForecasts.getPmValues().getSnowVolume().replace(cm, ""));

			rainVolumeList.add(
					String.format("%.2f", rainVolume));
			snowVolumeList.add(
					String.format("%.2f", snowVolume));

			if (!haveSnow) {
				if (dailyForecasts.getAmValues().isHasSnowVolume() ||
						dailyForecasts.getPmValues().isHasSnowVolume()) {
					haveSnow = true;
				}
			}

			if (!haveRain) {
				if (rainVolume > 0.0) {
					haveRain = true;
				}
			}
		}

		weatherIconRow.setIcons(weatherIconObjList);
		popRow.setValueList(popList);
		rainVolumeRow.setValueList(rainVolumeList);
		snowVolumeRow.setValueList(snowVolumeList);
		DetailDoubleTemperatureView tempRow = new DetailDoubleTemperatureView(getContext(), FragmentType.Simple, viewWidth, TEMP_ROW_HEIGHT,
				columnWidth, minTempList, maxTempList);

		TextsView dateRow = new TextsView(context, viewWidth, columnWidth, dateList);


		LinearLayout.LayoutParams rowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		LinearLayout.LayoutParams dateRowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		dateRowLayoutParams.bottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, getResources().getDisplayMetrics());

		if (textSizeMap.containsKey(WeatherValueType.date)) {
			dateRow.setValueTextSize(textSizeMap.get(WeatherValueType.date));
		}
		if (textSizeMap.containsKey(WeatherValueType.pop)) {
			popRow.setValueTextSize(textSizeMap.get(WeatherValueType.pop));
		}
		if (textSizeMap.containsKey(WeatherValueType.rainVolume)) {
			rainVolumeRow.setValueTextSize(textSizeMap.get(WeatherValueType.rainVolume));
		}
		if (textSizeMap.containsKey(WeatherValueType.snowVolume)) {
			snowVolumeRow.setValueTextSize(textSizeMap.get(WeatherValueType.snowVolume));
		}
		if (textSizeMap.containsKey(WeatherValueType.temp)) {
			tempRow.setTempTextSize(textSizeMap.get(WeatherValueType.temp));
		}

		if (textColorMap.containsKey(WeatherValueType.date)) {
			dateRow.setValueTextColor(textColorMap.get(WeatherValueType.date));
		}
		if (textColorMap.containsKey(WeatherValueType.pop)) {
			popRow.setTextColor(textColorMap.get(WeatherValueType.pop));
		}
		if (textColorMap.containsKey(WeatherValueType.rainVolume)) {
			rainVolumeRow.setTextColor(textColorMap.get(WeatherValueType.rainVolume));
		}
		if (textColorMap.containsKey(WeatherValueType.snowVolume)) {
			snowVolumeRow.setTextColor(textColorMap.get(WeatherValueType.snowVolume));
		}
		if (textColorMap.containsKey(WeatherValueType.temp)) {
			tempRow.setTextColor(textColorMap.get(WeatherValueType.temp));
		}


		binding.forecastView.addView(dateRow, dateRowLayoutParams);
		binding.forecastView.addView(weatherIconRow, rowLayoutParams);
		binding.forecastView.addView(popRow, rowLayoutParams);
		binding.forecastView.addView(rainVolumeRow, rowLayoutParams);
		if (haveSnow) {
			binding.forecastView.addView(snowVolumeRow, rowLayoutParams);
		}

		LinearLayout.LayoutParams tempRowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		tempRowLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		tempRowLayoutParams.topMargin = (int) getResources().getDimension(R.dimen.iconValueViewMargin);
		binding.forecastView.addView(tempRow, tempRowLayoutParams);

		createValueUnitsDescription(WeatherDataSourceType.ACCU_WEATHER, haveRain, haveSnow);


	}
}