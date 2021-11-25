package com.lifedawn.bestweather.weathers.simplefragment.kma.hourlyforecast;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.weathers.comparison.hourlyforecast.HourlyForecastComparisonFragment;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalHourlyForecast;
import com.lifedawn.bestweather.weathers.dataprocessing.util.SunRiseSetUtil;
import com.lifedawn.bestweather.weathers.detailfragment.kma.hourlyforecast.KmaDetailHourlyForecastFragment;
import com.lifedawn.bestweather.weathers.simplefragment.base.BaseSimpleForecastFragment;
import com.lifedawn.bestweather.weathers.view.ClockView;
import com.lifedawn.bestweather.weathers.view.DateView;
import com.lifedawn.bestweather.weathers.view.FragmentType;
import com.lifedawn.bestweather.weathers.view.IconTextView;
import com.lifedawn.bestweather.weathers.view.TextValueView;
import com.lifedawn.bestweather.weathers.view.SingleWeatherIconView;
import com.luckycatlabs.sunrisesunset.SunriseSunsetCalculator;
import com.luckycatlabs.sunrisesunset.dto.Location;

import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;


public class KmaSimpleHourlyForecastFragment extends BaseSimpleForecastFragment {
	private List<FinalHourlyForecast> finalHourlyForecastList;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		needCompare = true;

	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.weatherCardViewHeader.forecastName.setText(R.string.hourly_forecast);
		setValuesToViews();
		binding.labels.setVisibility(View.GONE);


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
				bundle.putString(BundleKey.AddressName.name(), addressName);
				bundle.putSerializable(BundleKey.TimeZone.name(), zoneId);
				bundle.putDouble(BundleKey.Latitude.name(), latitude);
				bundle.putDouble(BundleKey.Longitude.name(), longitude);

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

		/*
		addLabelView(R.drawable.date, getString(R.string.date), dateRowHeight);
		addLabelView(R.drawable.time, getString(R.string.clock), clockRowHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.weather), weatherRowHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.temperature), defaultTextRowHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.probability_of_precipitation), defaultTextRowHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.precipitation_volume), defaultTextRowHeight);
		 */

		dateRow = new DateView(context, FragmentType.Simple, viewWidth, dateRowHeight, columnWidth);
		ClockView clockRow = new ClockView(context, FragmentType.Simple, viewWidth, clockRowHeight, columnWidth);
		SingleWeatherIconView weatherIconRow = new SingleWeatherIconView(context, FragmentType.Simple, viewWidth, weatherRowHeight,
				columnWidth);
		TextValueView tempRow = new TextValueView(context, FragmentType.Simple, viewWidth, defaultTextRowHeight, columnWidth);
		IconTextView probabilityOfPrecipitationRow = new IconTextView(context, FragmentType.Simple, viewWidth,
				columnWidth, R.drawable.pop);
		IconTextView rainVolumeRow = new IconTextView(context, FragmentType.Simple, viewWidth,
				columnWidth, R.drawable.raindrop);
		IconTextView snowVolumeRow = new IconTextView(context, FragmentType.Simple, viewWidth,
				columnWidth, R.drawable.snowparticle);

		//시각 --------------------------------------------------------------------------
		List<ZonedDateTime> dateTimeList = new ArrayList<>();
		for (FinalHourlyForecast finalHourlyForecast : finalHourlyForecastList) {
			dateTimeList.add(finalHourlyForecast.getFcstDateTime());
		}
		dateRow.init(dateTimeList);
		clockRow.setClockList(dateTimeList);

		//기온, 강수확률, 강수량
		List<SingleWeatherIconView.WeatherIconObj> weatherIconObjList = new ArrayList<>();
		List<String> tempList = new ArrayList<>();
		List<String> probabilityOfPrecipitationList = new ArrayList<>();
		List<String> rainVolumeList = new ArrayList<>();
		List<String> snowVolumeList = new ArrayList<>();

		Map<Integer, SunRiseSetUtil.SunRiseSetObj> sunRiseSetObjMap =
				SunRiseSetUtil.getDailySunRiseSetMap(finalHourlyForecastList.get(0).getFcstDateTime(),
						finalHourlyForecastList.get(finalHourlyForecastList.size() - 1).getFcstDateTime(), latitude, longitude);
		Calendar calendar = Calendar.getInstance(TimeZone.getTimeZone(zoneId.getId()));
		int dayOfYear = 0;

		final String lessThan1mm = getString(R.string.kma_less_than_1mm);
		final String noSnow = getString(R.string.kma_no_snow);
		final String zero = "0.0";
		String tempUnitStr = getString(R.string.degree_symbol);
		String percent = ValueUnits.convertToStr(getContext(), ValueUnits.percent);

		boolean haveSnow = false;

		for (FinalHourlyForecast finalHourlyForecast : finalHourlyForecastList) {
			calendar.setTimeInMillis(finalHourlyForecast.getFcstDateTime().toInstant().toEpochMilli());
			dayOfYear = finalHourlyForecast.getFcstDateTime().getDayOfYear();

			weatherIconObjList.add(new SingleWeatherIconView.WeatherIconObj(
					ContextCompat.getDrawable(context, KmaResponseProcessor.getWeatherSkyIconImg(finalHourlyForecast.getSky(),
							SunRiseSetUtil.isNight(calendar,
									sunRiseSetObjMap.get(dayOfYear).getSunrise(),
									sunRiseSetObjMap.get(dayOfYear).getSunset()
							)))));
			tempList.add(ValueUnits.convertTemperature(finalHourlyForecast.getTemp1Hour(), tempUnit) + tempUnitStr);

			probabilityOfPrecipitationList.add(finalHourlyForecast.getProbabilityOfPrecipitation() == null ? "-" :
					finalHourlyForecast.getProbabilityOfPrecipitation() + percent);
			rainVolumeList.add(finalHourlyForecast.getRainPrecipitation1Hour().equals(lessThan1mm) ? zero :
					finalHourlyForecast.getRainPrecipitation1Hour());
			if (finalHourlyForecast.getSnowPrecipitation1Hour() != null) {
				if (!finalHourlyForecast.getSnowPrecipitation1Hour().equals(noSnow)) {
					if (!haveSnow) {
						haveSnow = true;
					}
				}
				snowVolumeList.add(finalHourlyForecast.getSnowPrecipitation1Hour().equals(noSnow) ? zero :
						finalHourlyForecast.getSnowPrecipitation1Hour());
			} else {
				snowVolumeList.add(zero);
			}
		}

		tempRow.setValueList(tempList);
		probabilityOfPrecipitationRow.setValueList(probabilityOfPrecipitationList);
		rainVolumeRow.setValueList(rainVolumeList);
		snowVolumeRow.setValueList(snowVolumeList);
		weatherIconRow.setWeatherImgs(weatherIconObjList);

		LinearLayout.LayoutParams rowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		LinearLayout.LayoutParams iconTextRowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		iconTextRowLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		iconTextRowLayoutParams.topMargin = (int) getResources().getDimension(R.dimen.iconValueViewMargin);

		binding.forecastView.addView(dateRow, rowLayoutParams);
		binding.forecastView.addView(clockRow, rowLayoutParams);
		binding.forecastView.addView(weatherIconRow, rowLayoutParams);
		binding.forecastView.addView(probabilityOfPrecipitationRow, iconTextRowLayoutParams);
		binding.forecastView.addView(rainVolumeRow, iconTextRowLayoutParams);
		if (haveSnow) {
			binding.forecastView.addView(snowVolumeRow, iconTextRowLayoutParams);
		}
		binding.forecastView.addView(tempRow, rowLayoutParams);

	}

}