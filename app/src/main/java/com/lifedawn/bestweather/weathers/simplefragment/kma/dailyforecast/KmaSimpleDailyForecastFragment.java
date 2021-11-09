package com.lifedawn.bestweather.weathers.simplefragment.kma.dailyforecast;

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
import com.lifedawn.bestweather.weathers.comparison.dailyforecast.DailyForecastComparisonFragment;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalDailyForecast;
import com.lifedawn.bestweather.weathers.detailfragment.kma.dailyforecast.KmaDetailDailyForecastFragment;
import com.lifedawn.bestweather.weathers.simplefragment.base.BaseSimpleForecastFragment;
import com.lifedawn.bestweather.weathers.view.DetailDoubleTemperatureView;
import com.lifedawn.bestweather.weathers.view.DoubleWeatherIconView;
import com.lifedawn.bestweather.weathers.view.FragmentType;
import com.lifedawn.bestweather.weathers.view.IconTextView;
import com.lifedawn.bestweather.weathers.view.TextValueView;

import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class KmaSimpleDailyForecastFragment extends BaseSimpleForecastFragment {
	private List<FinalDailyForecast> finalDailyForecastList;

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
		setValuesToViews();

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
				KmaDetailDailyForecastFragment detailDailyForecastFragment = new KmaDetailDailyForecastFragment();
				detailDailyForecastFragment.setFinalDailyForecastList(finalDailyForecastList);

				Bundle bundle = new Bundle();
				bundle.putString(getString(R.string.bundle_key_address_name), addressName);
				bundle.putSerializable(getString(R.string.bundle_key_timezone), timeZone);

				detailDailyForecastFragment.setArguments(bundle);

				String tag = getString(R.string.tag_detail_daily_forecast_fragment);
				FragmentManager fragmentManager = getParentFragment().getParentFragment().getParentFragmentManager();
				fragmentManager.beginTransaction().hide(
						fragmentManager.findFragmentByTag(getString(R.string.tag_weather_main_fragment))).add(R.id.fragment_container,
						detailDailyForecastFragment, tag).addToBackStack(tag).commit();
			}
		});

	}

	public KmaSimpleDailyForecastFragment setFinalDailyForecastList(List<FinalDailyForecast> finalDailyForecastList) {
		this.finalDailyForecastList = finalDailyForecastList;
		return this;
	}

	@Override
	public void setValuesToViews() {
		super.setValuesToViews();
		// 날짜, 최저/최고 기온 ,낮과 밤의 날씨상태, 강수확률
		Context context = getContext();

		final int DATE_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.dateValueRowHeightInCOMMON);
		final int WEATHER_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.singleWeatherIconValueRowHeightInSC);
		final int DEFAULT_TEXT_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.defaultValueRowHeightInSC);
		final int TEMP_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.doubleTemperatureRowHeightInSC);

		final int COLUMN_COUNT = finalDailyForecastList.size();
		final int COLUMN_WIDTH = (int) context.getResources().getDimension(R.dimen.valueColumnWidthInSCDaily);
		final int VIEW_WIDTH = COLUMN_COUNT * COLUMN_WIDTH;

		/*
		addLabelView(R.drawable.date, getString(R.string.date), DATE_ROW_HEIGHT);
		addLabelView(R.drawable.day_clear, getString(R.string.weather), WEATHER_ROW_HEIGHT);
		addLabelView(R.drawable.pop, getString(R.string.probability_of_precipitation), DEFAULT_TEXT_ROW_HEIGHT);
		addLabelView(R.drawable.temperature, getString(R.string.temperature), TEMP_ROW_HEIGHT);

		 */

		TextValueView dateRow = new TextValueView(context, FragmentType.Simple, VIEW_WIDTH, (int) getResources().getDimension(R.dimen.multipleDateTextRowHeightInCOMMON), COLUMN_WIDTH);
		DoubleWeatherIconView weatherIconRow = new DoubleWeatherIconView(context, FragmentType.Simple, VIEW_WIDTH, WEATHER_ROW_HEIGHT,
				COLUMN_WIDTH);
		IconTextView probabilityOfPrecipitationRow = new IconTextView(context, FragmentType.Simple, VIEW_WIDTH,
				COLUMN_WIDTH, R.drawable.pop);

		//시각 --------------------------------------------------------------------------
		List<String> dateList = new ArrayList<>();
		List<DoubleWeatherIconView.WeatherIconObj> weatherIconObjList = new ArrayList<>();
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("M.d\nE", Locale.getDefault());

		for (FinalDailyForecast forecast : finalDailyForecastList) {
			dateList.add(forecast.getDate().format(dateTimeFormatter));
		}
		dateRow.setValueList(dateList);

		//기온, 강수확률, 강수량
		List<Integer> minTempList = new ArrayList<>();
		List<Integer> maxTempList = new ArrayList<>();
		List<String> probabilityOfPrecipitationList = new ArrayList<>();

		int index = 0;
		for (; index < 5; index++) {
			minTempList.add(Integer.parseInt(finalDailyForecastList.get(index).getMinTemp()));
			maxTempList.add(Integer.parseInt(finalDailyForecastList.get(index).getMaxTemp()));

			probabilityOfPrecipitationList.add(
					finalDailyForecastList.get(index).getAmProbabilityOfPrecipitation() + " / " + finalDailyForecastList.get(
							index).getPmProbabilityOfPrecipitation());
			weatherIconObjList.add(new DoubleWeatherIconView.WeatherIconObj(ContextCompat.getDrawable(context,
					KmaResponseProcessor.getWeatherMidIconImg(finalDailyForecastList.get(index).getAmSky(), false)),
					ContextCompat.getDrawable(context,
							KmaResponseProcessor.getWeatherMidIconImg(finalDailyForecastList.get(index).getPmSky(), true))));
		}
		for (; index < finalDailyForecastList.size(); index++) {
			minTempList.add(Integer.parseInt(finalDailyForecastList.get(index).getMinTemp()));
			maxTempList.add(Integer.parseInt(finalDailyForecastList.get(index).getMaxTemp()));

			probabilityOfPrecipitationList.add(finalDailyForecastList.get(index).getProbabilityOfPrecipitation());
			weatherIconObjList.add(new DoubleWeatherIconView.WeatherIconObj(ContextCompat.getDrawable(context,
					KmaResponseProcessor.getWeatherMidIconImg(finalDailyForecastList.get(index).getSky(), false))));
		}

		weatherIconRow.setIcons(weatherIconObjList);
		probabilityOfPrecipitationRow.setValueList(probabilityOfPrecipitationList);
		DetailDoubleTemperatureView tempRow = new DetailDoubleTemperatureView(getContext(), FragmentType.Simple, VIEW_WIDTH,
				TEMP_ROW_HEIGHT, COLUMN_WIDTH, minTempList, maxTempList);

		LinearLayout.LayoutParams rowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		rowLayoutParams.gravity = Gravity.CENTER_VERTICAL;

		LinearLayout.LayoutParams iconTextRowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		iconTextRowLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		iconTextRowLayoutParams.topMargin = (int) getResources().getDimension(R.dimen.iconValueViewMargin);

		LinearLayout.LayoutParams dateRowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		dateRowLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		dateRowLayoutParams.bottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP,4f, getResources().getDisplayMetrics());


		binding.forecastView.addView(dateRow, dateRowLayoutParams);
		binding.forecastView.addView(weatherIconRow, rowLayoutParams);
		binding.forecastView.addView(probabilityOfPrecipitationRow, iconTextRowLayoutParams);

		LinearLayout.LayoutParams tempRowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		tempRowLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		tempRowLayoutParams.topMargin = (int) getResources().getDimension(R.dimen.iconValueViewMargin);
		binding.forecastView.addView(tempRow, tempRowLayoutParams);

	}
}