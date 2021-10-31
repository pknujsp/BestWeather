package com.lifedawn.bestweather.weathers.simplefragment.accuweather.dailyforecast;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.retrofit.responses.accuweather.fivedaysofdailyforecasts.FiveDaysOfDailyForecastsResponse;
import com.lifedawn.bestweather.weathers.comparison.dailyforecast.DailyForecastComparisonFragment;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.detailfragment.accuweather.dailyforecast.AccuDetailDailyForecastFragment;
import com.lifedawn.bestweather.weathers.simplefragment.base.BaseSimpleForecastFragment;
import com.lifedawn.bestweather.weathers.view.DetailDoubleTemperatureView;
import com.lifedawn.bestweather.weathers.view.FragmentType;
import com.lifedawn.bestweather.weathers.view.TextValueView;
import com.lifedawn.bestweather.weathers.view.SingleWeatherIconView;

import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AccuSimpleDailyForecastFragment extends BaseSimpleForecastFragment {
	private FiveDaysOfDailyForecastsResponse fiveDaysOfDailyForecastsResponse;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
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
				FragmentManager fragmentManager = getParentFragment().getParentFragment().getParentFragmentManager();
				fragmentManager.beginTransaction().hide(
						fragmentManager.findFragmentByTag(getString(R.string.tag_weather_main_fragment))).add(R.id.fragment_container,
						comparisonFragment, tag).addToBackStack(tag).commit();
			}
		});

		binding.weatherCardViewHeader.detailForecast.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				AccuDetailDailyForecastFragment detailDailyForecastFragment = new AccuDetailDailyForecastFragment();
				detailDailyForecastFragment.setDailyForecastsList(fiveDaysOfDailyForecastsResponse.getDailyForecasts());

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
		setValuesToViews();
	}

	public AccuSimpleDailyForecastFragment setFiveDaysOfDailyForecastsResponse(
			FiveDaysOfDailyForecastsResponse fiveDaysOfDailyForecastsResponse) {
		this.fiveDaysOfDailyForecastsResponse = fiveDaysOfDailyForecastsResponse;
		return this;
	}

	@Override
	public void setValuesToViews() {
		super.setValuesToViews();
		// 날짜, 최저/최고 기온 ,낮과 밤의 날씨상태, 강수확률, 강수량
		Context context = getContext();

		final int DATE_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.dateValueRowHeightInCOMMON);
		final int WEATHER_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.singleWeatherIconValueRowHeightInSC);
		final int DEFAULT_TEXT_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.defaultValueRowHeightInSC);
		final int TEMP_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.doubleTemperatureRowHeightInSC);

		List<FiveDaysOfDailyForecastsResponse.DailyForecasts> items = fiveDaysOfDailyForecastsResponse.getDailyForecasts();

		final int columnCount = items.size();
		final int columnWidth = (int) context.getResources().getDimension(R.dimen.valueColumnWidthInSCDaily);
		final int viewWidth = columnCount * columnWidth;

		addLabelView(R.drawable.temp_icon, getString(R.string.date), DATE_ROW_HEIGHT);
		addLabelView(R.drawable.temp_icon, getString(R.string.weather), WEATHER_ROW_HEIGHT);
		addLabelView(R.drawable.temp_icon, getString(R.string.temperature), TEMP_ROW_HEIGHT);
		addLabelView(R.drawable.temp_icon, getString(R.string.probability_of_precipitation), DEFAULT_TEXT_ROW_HEIGHT);
		addLabelView(R.drawable.temp_icon, getString(R.string.precipitation_volume), DEFAULT_TEXT_ROW_HEIGHT);

		TextValueView dateRow = new TextValueView(context, FragmentType.Simple, viewWidth, DATE_ROW_HEIGHT, columnWidth);
		SingleWeatherIconView weatherIconRow = new SingleWeatherIconView(context, FragmentType.Simple, viewWidth, WEATHER_ROW_HEIGHT, columnWidth);
		TextValueView probabilityOfPrecipitationRow = new TextValueView(context, FragmentType.Simple, viewWidth, DEFAULT_TEXT_ROW_HEIGHT, columnWidth);
		TextValueView precipitationVolumeRow = new TextValueView(context, FragmentType.Simple, viewWidth, DEFAULT_TEXT_ROW_HEIGHT, columnWidth);

		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("M.d E", Locale.getDefault());

		//시각, 기온, 강수확률, 강수량--------------------------------
		List<String> dateList = new ArrayList<>();
		List<Integer> minTempList = new ArrayList<>();
		List<Integer> maxTempList = new ArrayList<>();
		List<String> probabilityOfPrecipitationList = new ArrayList<>();
		List<String> precipitationVolumeList = new ArrayList<>();

		for (FiveDaysOfDailyForecastsResponse.DailyForecasts dailyForecasts : items) {
			dateList.add(
					WeatherResponseProcessor.convertDateTimeOfDailyForecast(Long.parseLong(dailyForecasts.getEpochDate()) * 1000L,
							timeZone).format(dateTimeFormatter));
			minTempList.add((int) Double.parseDouble(dailyForecasts.getTemperature().getMinimum().getValue()));
			maxTempList.add((int) Double.parseDouble(dailyForecasts.getTemperature().getMaximum().getValue()));

			probabilityOfPrecipitationList.add(
					dailyForecasts.getDay().getPrecipitationProbability() + " / " + dailyForecasts.getNight().getPrecipitationProbability());
			precipitationVolumeList.add(
					dailyForecasts.getDay().getTotalLiquid().getValue() + " / " + dailyForecasts.getNight().getTotalLiquid().getValue());
		}

		dateRow.setValueList(dateList);
		probabilityOfPrecipitationRow.setValueList(probabilityOfPrecipitationList);
		precipitationVolumeRow.setValueList(precipitationVolumeList);
		DetailDoubleTemperatureView tempRow = new DetailDoubleTemperatureView(getContext(), FragmentType.Simple, viewWidth, TEMP_ROW_HEIGHT,
				columnWidth, minTempList, maxTempList);

		LinearLayout.LayoutParams rowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		rowLayoutParams.gravity = Gravity.CENTER_VERTICAL;

		binding.forecastView.addView(dateRow, rowLayoutParams);
		binding.forecastView.addView(weatherIconRow, rowLayoutParams);
		binding.forecastView.addView(tempRow, rowLayoutParams);
		binding.forecastView.addView(probabilityOfPrecipitationRow, rowLayoutParams);
		binding.forecastView.addView(precipitationVolumeRow, rowLayoutParams);
	}
}