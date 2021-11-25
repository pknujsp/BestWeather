package com.lifedawn.bestweather.weathers.simplefragment.openweathermap.dailyforecast;

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
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.onecall.OneCallResponse;
import com.lifedawn.bestweather.weathers.comparison.dailyforecast.DailyForecastComparisonFragment;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.detailfragment.openweathermap.dailyforecast.OwmDetailDailyForecastFragment;
import com.lifedawn.bestweather.weathers.simplefragment.base.BaseSimpleForecastFragment;
import com.lifedawn.bestweather.weathers.view.DetailDoubleTemperatureView;
import com.lifedawn.bestweather.weathers.view.FragmentType;
import com.lifedawn.bestweather.weathers.view.IconTextView;
import com.lifedawn.bestweather.weathers.view.TextValueView;
import com.lifedawn.bestweather.weathers.view.SingleWeatherIconView;

import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class OwmSimpleDailyForecastFragment extends BaseSimpleForecastFragment {
	private OneCallResponse oneCallResponse;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		needCompare = true;

	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.labels.setVisibility(View.GONE);
		binding.weatherCardViewHeader.forecastName.setText(R.string.daily_forecast);
		binding.weatherCardViewHeader.compareForecast.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				DailyForecastComparisonFragment comparisonFragment = new DailyForecastComparisonFragment();
				comparisonFragment.setArguments(getArguments());

				String tag = getString(R.string.tag_comparison_fragment);
				FragmentManager fragmentManager = getParentFragment().getParentFragment().getParentFragmentManager();
				fragmentManager.beginTransaction().hide(
						fragmentManager.findFragmentByTag(getString(R.string.tag_weather_main_fragment))).add(R.id.fragment_container,
						comparisonFragment, tag).addToBackStack(tag).commit();
			}
		});

		binding.weatherCardViewHeader.detailForecast.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				OwmDetailDailyForecastFragment detailDailyForecastFragment = new OwmDetailDailyForecastFragment();
				detailDailyForecastFragment.setDailyList(oneCallResponse.getDaily());

				Bundle bundle = new Bundle();
				bundle.putString(BundleKey.AddressName.name(), addressName);
				bundle.putSerializable(BundleKey.TimeZone.name(), zoneId);

				detailDailyForecastFragment.setArguments(bundle);

				String tag = getString(R.string.tag_detail_daily_forecast_fragment);
				FragmentManager fragmentManager = getParentFragment().getParentFragment().getParentFragmentManager();
				fragmentManager.beginTransaction().hide(
						fragmentManager.findFragmentByTag(getString(R.string.tag_weather_main_fragment))).add(R.id.fragment_container,
						detailDailyForecastFragment, tag).addToBackStack(tag).commit();
			}
		});

		setValuesToViews();
	}

	public OwmSimpleDailyForecastFragment setOneCallResponse(OneCallResponse oneCallResponse) {
		this.oneCallResponse = oneCallResponse;
		return this;
	}

	@Override
	public void setValuesToViews() {
		super.setValuesToViews();
		// 날짜 ,낮과 밤의 날씨상태, 강수확률, 강우량, 강설량, 최저/최고 기온
		Context context = getContext();

		final int WEATHER_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.singleWeatherIconValueRowHeightInSC);
		final int TEMP_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.doubleTemperatureRowHeightInSC);

		List<OneCallResponse.Daily> items = oneCallResponse.getDaily();

		final int COLUMN_COUNT = items.size();
		final int COLUMN_WIDTH = (int) context.getResources().getDimension(R.dimen.valueColumnWidthInSCDaily);
		final int VIEW_WIDTH = COLUMN_COUNT * COLUMN_WIDTH;

		/*
		addLabelView(R.drawable.date, getString(R.string.date), DATE_ROW_HEIGHT);
		addLabelView(R.drawable.day_clear, getString(R.string.weather), WEATHER_ROW_HEIGHT);
		addLabelView(R.drawable.pop, getString(R.string.probability_of_precipitation), DEFAULT_TEXT_ROW_HEIGHT);
		addLabelView(R.drawable.rainvolume, getString(R.string.rain_volume), DEFAULT_TEXT_ROW_HEIGHT);
		ImageView snowVolumeLabel = addLabelView(R.drawable.snowvolume, getString(R.string.snow_volume), DEFAULT_TEXT_ROW_HEIGHT);
		addLabelView(R.drawable.temperature, getString(R.string.temperature), TEMP_ROW_HEIGHT);

		 */

		TextValueView dateRow = new TextValueView(context, FragmentType.Simple, VIEW_WIDTH, (int) getResources().getDimension(R.dimen.multipleDateTextRowHeightInCOMMON),
				COLUMN_WIDTH);
		SingleWeatherIconView weatherIconRow = new SingleWeatherIconView(context, FragmentType.Simple, VIEW_WIDTH, WEATHER_ROW_HEIGHT,
				COLUMN_WIDTH);
		IconTextView probabilityOfPrecipitationRow = new IconTextView(context, FragmentType.Simple, VIEW_WIDTH,
				COLUMN_WIDTH, R.drawable.pop);
		IconTextView rainVolumeRow = new IconTextView(context, FragmentType.Simple, VIEW_WIDTH,
				COLUMN_WIDTH, R.drawable.raindrop);
		IconTextView snowVolumeRow = new IconTextView(context, FragmentType.Simple, VIEW_WIDTH,
				COLUMN_WIDTH, R.drawable.snowparticle);

		//시각 --------------------------------------------------------------------------
		List<String> dateList = new ArrayList<>();
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("M.d\nE");
		//날씨 아이콘
		List<SingleWeatherIconView.WeatherIconObj> weatherIconObjList = new ArrayList<>();
		//기온, 강수확률, 강수량
		List<Integer> minTempList = new ArrayList<>();
		List<Integer> maxTempList = new ArrayList<>();
		List<String> probabilityOfPrecipitationList = new ArrayList<>();
		List<String> rainVolumeList = new ArrayList<>();
		List<String> snowVolumeList = new ArrayList<>();

		String rainVolume = null;
		String snowVolume = null;
		String percent = "%";

		boolean haveSnowVolumes = false;

		for (OneCallResponse.Daily item : items) {
			dateList.add((WeatherResponseProcessor.convertDateTimeOfDailyForecast(Long.parseLong(item.getDt()) * 1000L, zoneId).format(
					dateTimeFormatter)));
			minTempList.add(ValueUnits.convertTemperature(item.getTemp().getMin(), tempUnit));
			maxTempList.add(ValueUnits.convertTemperature(item.getTemp().getMax(), tempUnit));

			rainVolume = item.getRain() == null ? "0.0" : item.getRain();
			if (item.getSnow() != null) {
				if (!haveSnowVolumes) {
					haveSnowVolumes = true;
				}
			}
			snowVolume = item.getSnow() == null ? "0.0" : item.getSnow();
			snowVolumeList.add(snowVolume);

			probabilityOfPrecipitationList.add((int) (Double.parseDouble(item.getPop()) * 100.0) + percent);
			rainVolumeList.add(rainVolume);

			weatherIconObjList.add(new SingleWeatherIconView.WeatherIconObj(ContextCompat.getDrawable(context,
					OpenWeatherMapResponseProcessor.getWeatherIconImg(item.getWeather().get(0).getId(), false))));
		}

		dateRow.setValueList(dateList);
		weatherIconRow.setWeatherImgs(weatherIconObjList);
		probabilityOfPrecipitationRow.setValueList(probabilityOfPrecipitationList);
		rainVolumeRow.setValueList(rainVolumeList);
		snowVolumeRow.setValueList(snowVolumeList);

		DetailDoubleTemperatureView tempRow = new DetailDoubleTemperatureView(getContext(), FragmentType.Simple, VIEW_WIDTH,
				TEMP_ROW_HEIGHT, COLUMN_WIDTH, minTempList, maxTempList);

		LinearLayout.LayoutParams rowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		rowLayoutParams.gravity = Gravity.CENTER_VERTICAL;

		LinearLayout.LayoutParams iconTextRowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		iconTextRowLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		int margin = (int) getResources().getDimension(R.dimen.iconValueViewMargin);
		iconTextRowLayoutParams.topMargin = margin;

		LinearLayout.LayoutParams dateRowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		dateRowLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		dateRowLayoutParams.bottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, getResources().getDisplayMetrics());

		binding.forecastView.addView(dateRow, dateRowLayoutParams);
		binding.forecastView.addView(weatherIconRow, rowLayoutParams);
		binding.forecastView.addView(probabilityOfPrecipitationRow, iconTextRowLayoutParams);
		binding.forecastView.addView(rainVolumeRow, iconTextRowLayoutParams);
		if (haveSnowVolumes) {
			binding.forecastView.addView(snowVolumeRow, iconTextRowLayoutParams);
		}

		LinearLayout.LayoutParams tempRowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		tempRowLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		tempRowLayoutParams.topMargin = margin;
		binding.forecastView.addView(tempRow, tempRowLayoutParams);
	}

}