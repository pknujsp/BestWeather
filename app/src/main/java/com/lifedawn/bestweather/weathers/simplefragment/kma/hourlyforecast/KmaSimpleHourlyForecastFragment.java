package com.lifedawn.bestweather.weathers.simplefragment.kma.hourlyforecast;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.FragmentManager;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.weathers.comparison.hourlyforecast.HourlyForecastComparisonFragment;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalHourlyForecast;
import com.lifedawn.bestweather.weathers.detailfragment.kma.hourlyforecast.KmaDetailHourlyForecastFragment;
import com.lifedawn.bestweather.weathers.simplefragment.base.BaseSimpleForecastFragment;
import com.lifedawn.bestweather.weathers.view.ClockView;
import com.lifedawn.bestweather.weathers.view.DateView;
import com.lifedawn.bestweather.weathers.view.TextValueView;
import com.lifedawn.bestweather.weathers.view.SingleWeatherIconView;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;


public class KmaSimpleHourlyForecastFragment extends BaseSimpleForecastFragment {
	private List<FinalHourlyForecast> finalHourlyForecastList;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.weatherCardViewHeader.forecastName.setText(R.string.hourly_forecast);
		setValuesToViews();

		binding.weatherCardViewHeader.compareForecast.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				HourlyForecastComparisonFragment comparisonFragment = new HourlyForecastComparisonFragment();
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
			public void onClick(View v) {
				KmaDetailHourlyForecastFragment detailHourlyForecastFragment = new KmaDetailHourlyForecastFragment();
				detailHourlyForecastFragment.setFinalHourlyForecastList(finalHourlyForecastList);

				Bundle bundle = new Bundle();
				bundle.putString(getString(R.string.bundle_key_address_name), addressName);
				bundle.putSerializable(getString(R.string.bundle_key_timezone), timeZone);

				detailHourlyForecastFragment.setArguments(bundle);

				String tag = getString(R.string.tag_detail_hourly_forecast_fragment);
				FragmentManager fragmentManager = getParentFragment().getParentFragment().getParentFragmentManager();
				fragmentManager.beginTransaction().hide(
						fragmentManager.findFragmentByTag(getString(R.string.tag_weather_main_fragment))).add(R.id.fragment_container,
						detailHourlyForecastFragment, tag).addToBackStack(tag).commit();
			}
		});
	}

	public KmaSimpleHourlyForecastFragment setFinalHourlyForecastList(List<FinalHourlyForecast> finalHourlyForecastList) {
		this.finalHourlyForecastList = finalHourlyForecastList;
		return this;
	}

	@Override
	public void setValuesToViews() {
		//kma hourly forecast simple : 날짜, 시각, 날씨, 기온, 강수확률, 강수량
		Context context = getContext();

		final int dateRowHeight = (int) context.getResources().getDimension(R.dimen.dateValueRowHeightInCOMMON);
		final int clockRowHeight = (int) context.getResources().getDimension(R.dimen.clockValueRowHeightInCOMMON);
		final int weatherRowHeight = (int) context.getResources().getDimension(R.dimen.singleWeatherIconValueRowHeightInSC);
		final int defaultTextRowHeight = (int) context.getResources().getDimension(R.dimen.defaultValueRowHeightInSC);

		final int columnCount = finalHourlyForecastList.size();
		final int columnWidth = (int) context.getResources().getDimension(R.dimen.valueColumnWidthInSCHourly);
		final int viewWidth = columnCount * columnWidth;

		addLabelView(R.drawable.date, getString(R.string.date), dateRowHeight);
		addLabelView(R.drawable.time, getString(R.string.clock), clockRowHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.weather), weatherRowHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.temperature), defaultTextRowHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.probability_of_precipitation), defaultTextRowHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.precipitation_volume), defaultTextRowHeight);

		dateRow = new DateView(context, viewWidth, dateRowHeight, columnWidth);
		ClockView clockRow = new ClockView(context, viewWidth, clockRowHeight, columnWidth);
		SingleWeatherIconView weatherIconRow = new SingleWeatherIconView(context, viewWidth, weatherRowHeight, columnWidth);
		TextValueView tempRow = new TextValueView(context, viewWidth, defaultTextRowHeight, columnWidth);
		TextValueView probabilityOfPrecipitationRow = new TextValueView(context, viewWidth, defaultTextRowHeight, columnWidth);
		TextValueView precipitationVolumeRow = new TextValueView(context, viewWidth, defaultTextRowHeight, columnWidth);

		//시각 --------------------------------------------------------------------------
		List<LocalDateTime> dateTimeList = new ArrayList<>();
		for (FinalHourlyForecast finalHourlyForecast : finalHourlyForecastList) {
			dateTimeList.add(finalHourlyForecast.getFcstDateTime());
		}
		dateRow.init(dateTimeList);
		clockRow.setClockList(dateTimeList);
		//날씨 아이콘

		//기온, 강수확률, 강수량
		List<String> tempList = new ArrayList<>();
		List<String> probabilityOfPrecipitationList = new ArrayList<>();
		List<String> precipitationVolumeList = new ArrayList<>();

		for (FinalHourlyForecast finalHourlyForecast : finalHourlyForecastList) {
			tempList.add(ValueUnits.convertTemperature(finalHourlyForecast.getTemp1Hour(), tempUnit).toString());

			probabilityOfPrecipitationList.add(finalHourlyForecast.getProbabilityOfPrecipitation());
			precipitationVolumeList.add(finalHourlyForecast.getRainPrecipitation1Hour());
		}

		tempRow.setValueList(tempList);
		probabilityOfPrecipitationRow.setValueList(probabilityOfPrecipitationList);
		precipitationVolumeRow.setValueList(precipitationVolumeList);

		LinearLayout.LayoutParams rowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		binding.forecastView.addView(dateRow, rowLayoutParams);
		binding.forecastView.addView(clockRow, rowLayoutParams);
		binding.forecastView.addView(weatherIconRow, rowLayoutParams);
		binding.forecastView.addView(tempRow, rowLayoutParams);
		binding.forecastView.addView(probabilityOfPrecipitationRow, rowLayoutParams);
		binding.forecastView.addView(precipitationVolumeRow, rowLayoutParams);
	}

}