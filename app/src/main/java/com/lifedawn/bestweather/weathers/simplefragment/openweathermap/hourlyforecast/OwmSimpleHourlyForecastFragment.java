package com.lifedawn.bestweather.weathers.simplefragment.openweathermap.hourlyforecast;

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
import com.lifedawn.bestweather.weathers.WeatherFragment;
import com.lifedawn.bestweather.weathers.comparison.hourlyforecast.HourlyForecastComparisonFragment;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.detailfragment.openweathermap.hourlyforecast.OwmDetailHourlyForecastFragment;
import com.lifedawn.bestweather.weathers.simplefragment.base.BaseSimpleForecastFragment;
import com.lifedawn.bestweather.weathers.view.ClockView;
import com.lifedawn.bestweather.weathers.view.DateView;
import com.lifedawn.bestweather.weathers.view.FragmentType;
import com.lifedawn.bestweather.weathers.view.IconTextView;
import com.lifedawn.bestweather.weathers.view.TextValueView;
import com.lifedawn.bestweather.weathers.view.SingleWeatherIconView;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class OwmSimpleHourlyForecastFragment extends BaseSimpleForecastFragment {
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
		binding.weatherCardViewHeader.forecastName.setText(R.string.hourly_forecast);
		binding.weatherCardViewHeader.compareForecast.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				HourlyForecastComparisonFragment comparisonFragment = new HourlyForecastComparisonFragment();
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
			public void onClick(View v) {
				OwmDetailHourlyForecastFragment detailHourlyForecastFragment = new OwmDetailHourlyForecastFragment();
				detailHourlyForecastFragment.setHourlyList(oneCallResponse.getHourly());

				Bundle bundle = new Bundle();
				bundle.putString(BundleKey.AddressName.name(), addressName);
				bundle.putSerializable(BundleKey.TimeZone.name(), zoneId);

				detailHourlyForecastFragment.setArguments(bundle);

				String tag = getString(R.string.tag_detail_hourly_forecast_fragment);
				FragmentManager fragmentManager = getParentFragment().getParentFragmentManager();

				fragmentManager.beginTransaction().hide(
						fragmentManager.findFragmentByTag(WeatherFragment.class.getName())).add(R.id.fragment_container,
						detailHourlyForecastFragment, tag).addToBackStack(tag).commit();
			}
		});

		setValuesToViews();
	}

	public OwmSimpleHourlyForecastFragment setOneCallResponse(OneCallResponse oneCallResponse) {
		this.oneCallResponse = oneCallResponse;
		return this;
	}

	@Override
	public void setValuesToViews() {
		//owm hourly forecast simple : 날짜, 시각, 날씨, 기온, 강수확률, 강수량, 강설량
		Context context = getContext();

		final int DATE_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.dateValueRowHeightInCOMMON);
		final int CLOCK_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.clockValueRowHeightInCOMMON);
		final int WEATHER_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.singleWeatherIconValueRowHeightInSC);
		final int DEFAULT_TEXT_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.defaultValueRowHeightInSC);

		List<OneCallResponse.Hourly> items = oneCallResponse.getHourly();

		final int COLUMN_COUNT = items.size();
		final int COLUMN_WIDTH = (int) context.getResources().getDimension(R.dimen.valueColumnWidthInSCHourly);
		final int VIEW_WIDTH = COLUMN_COUNT * COLUMN_WIDTH;

		/*
		addLabelView(R.drawable.date, getString(R.string.date), DATE_ROW_HEIGHT);
		addLabelView(R.drawable.time, getString(R.string.clock), CLOCK_ROW_HEIGHT);
		addLabelView(R.drawable.day_clear, getString(R.string.weather), WEATHER_ROW_HEIGHT);
		addLabelView(R.drawable.temperature, getString(R.string.temperature), DEFAULT_TEXT_ROW_HEIGHT);
		addLabelView(R.drawable.pop, getString(R.string.probability_of_precipitation), DEFAULT_TEXT_ROW_HEIGHT);
		addLabelView(R.drawable.rainvolume, getString(R.string.rain_volume), DEFAULT_TEXT_ROW_HEIGHT);
		ImageView snowVolumeLabel = addLabelView(R.drawable.snowvolume, getString(R.string.snow_volume), DEFAULT_TEXT_ROW_HEIGHT);

		 */

		dateRow = new DateView(context, FragmentType.Simple, VIEW_WIDTH, DATE_ROW_HEIGHT, COLUMN_WIDTH);
		ClockView clockRow = new ClockView(context, FragmentType.Simple, VIEW_WIDTH, CLOCK_ROW_HEIGHT, COLUMN_WIDTH);
		SingleWeatherIconView weatherIconRow = new SingleWeatherIconView(context, FragmentType.Simple, VIEW_WIDTH, WEATHER_ROW_HEIGHT,
				COLUMN_WIDTH);
		TextValueView tempRow = new TextValueView(context, FragmentType.Simple, VIEW_WIDTH, DEFAULT_TEXT_ROW_HEIGHT, COLUMN_WIDTH);
		IconTextView probabilityOfPrecipitationRow = new IconTextView(context, FragmentType.Simple, VIEW_WIDTH,
				COLUMN_WIDTH, R.drawable.pop);
		IconTextView rainVolumeRow = new IconTextView(context, FragmentType.Simple, VIEW_WIDTH,
				COLUMN_WIDTH, R.drawable.raindrop);
		IconTextView snowVolumeRow = new IconTextView(context, FragmentType.Simple, VIEW_WIDTH,
				COLUMN_WIDTH, R.drawable.snowparticle);

		//시각, 기온, 강수확률, 강수량
		List<SingleWeatherIconView.WeatherIconObj> iconObjList = new ArrayList<>();
		List<ZonedDateTime> dateTimeList = new ArrayList<>();
		List<String> tempList = new ArrayList<>();
		List<String> probabilityOfPrecipitationList = new ArrayList<>();
		List<String> rainVolumeList = new ArrayList<>();
		List<String> snowVolumeList = new ArrayList<>();

		String percent = ValueUnits.convertToStr(getContext(), ValueUnits.percent);
		String degree = getString(R.string.degree_symbol);

		boolean haveSnowVolumes = false;


		for (OneCallResponse.Hourly item : items) {
			dateTimeList.add(WeatherResponseProcessor.convertDateTimeOfHourlyForecast(Long.parseLong(item.getDt()) * 1000L, zoneId));
			iconObjList.add(new SingleWeatherIconView.WeatherIconObj(ContextCompat.getDrawable(context,
					OpenWeatherMapResponseProcessor.getWeatherIconImg(item.getWeather().get(0).getId(),
							item.getWeather().get(0).getIcon().contains("n")))));
			tempList.add(ValueUnits.convertTemperature(item.getTemp(), tempUnit) + degree);
			probabilityOfPrecipitationList.add((int) (Double.parseDouble(item.getPop()) * 100.0) + percent);
			rainVolumeList.add(item.getRain() != null ? item.getRain().getPrecipitation1Hour() : "0.0");

			if (item.getSnow() != null) {
				if (!haveSnowVolumes) {
					haveSnowVolumes = true;
				}
			}
			snowVolumeList.add(item.getSnow() != null ? item.getSnow().getPrecipitation1Hour() : "0.0");
		}
		weatherIconRow.setWeatherImgs(iconObjList);
		dateRow.init(dateTimeList);
		clockRow.setClockList(dateTimeList);
		tempRow.setValueList(tempList);
		tempRow.setTextSize(16);

		probabilityOfPrecipitationRow.setValueList(probabilityOfPrecipitationList);
		rainVolumeRow.setValueList(rainVolumeList);
		snowVolumeRow.setValueList(snowVolumeList);

		LinearLayout.LayoutParams rowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		rowLayoutParams.gravity = Gravity.CENTER_VERTICAL;

		LinearLayout.LayoutParams iconTextRowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		iconTextRowLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		int margin = (int) getResources().getDimension(R.dimen.iconValueViewMargin);
		iconTextRowLayoutParams.topMargin = margin;

		binding.forecastView.addView(dateRow, rowLayoutParams);
		binding.forecastView.addView(clockRow, rowLayoutParams);
		binding.forecastView.addView(weatherIconRow, rowLayoutParams);
		binding.forecastView.addView(probabilityOfPrecipitationRow, iconTextRowLayoutParams);
		binding.forecastView.addView(rainVolumeRow, iconTextRowLayoutParams);
		if (haveSnowVolumes) {
			binding.forecastView.addView(snowVolumeRow, iconTextRowLayoutParams);
		}

		LinearLayout.LayoutParams tempRowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		tempRowLayoutParams.topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
		binding.forecastView.addView(tempRow, tempRowLayoutParams);

	}

}