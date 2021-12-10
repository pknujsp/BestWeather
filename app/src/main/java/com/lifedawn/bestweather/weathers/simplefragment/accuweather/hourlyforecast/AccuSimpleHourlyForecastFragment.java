package com.lifedawn.bestweather.weathers.simplefragment.accuweather.hourlyforecast;

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
import com.lifedawn.bestweather.commons.enums.WeatherDataType;
import com.lifedawn.bestweather.retrofit.responses.accuweather.twelvehoursofhourlyforecasts.TwelveHoursOfHourlyForecastsResponse;
import com.lifedawn.bestweather.weathers.WeatherFragment;
import com.lifedawn.bestweather.weathers.comparison.hourlyforecast.HourlyForecastComparisonFragment;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AccuWeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.detailfragment.accuweather.hourlyforecast.AccuDetailHourlyForecastFragment;
import com.lifedawn.bestweather.weathers.detailfragment.dto.HourlyForecastDto;
import com.lifedawn.bestweather.weathers.simplefragment.base.BaseSimpleForecastFragment;
import com.lifedawn.bestweather.weathers.view.ClockView;
import com.lifedawn.bestweather.weathers.view.DateView;
import com.lifedawn.bestweather.weathers.FragmentType;
import com.lifedawn.bestweather.weathers.view.IconTextView;
import com.lifedawn.bestweather.weathers.view.TextValueView;
import com.lifedawn.bestweather.weathers.view.SingleWeatherIconView;

import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;


public class AccuSimpleHourlyForecastFragment extends BaseSimpleForecastFragment {
	private TwelveHoursOfHourlyForecastsResponse twelveHoursOfHourlyForecastsResponse;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		needCompare = true;

	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

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
				AccuDetailHourlyForecastFragment detailHourlyForecastFragment = new AccuDetailHourlyForecastFragment();
				detailHourlyForecastFragment.setHourlyItemList(twelveHoursOfHourlyForecastsResponse.getItems());

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

	public AccuSimpleHourlyForecastFragment setTwelveHoursOfHourlyForecastsResponse(
			TwelveHoursOfHourlyForecastsResponse twelveHoursOfHourlyForecastsResponse) {
		this.twelveHoursOfHourlyForecastsResponse = twelveHoursOfHourlyForecastsResponse;
		return this;
	}

	@Override
	public void setValuesToViews() {
		//accu hourly forecast simple : 날짜, 시각, 날씨, 기온, 강수확률, 강수량
		Context context = getContext();

		final int weatherRowHeight = (int) context.getResources().getDimension(R.dimen.singleWeatherIconValueRowHeightInSC);
		final int defaultTextRowHeight = (int) context.getResources().getDimension(R.dimen.defaultValueRowHeightInSC);

		List<TwelveHoursOfHourlyForecastsResponse.Item> items = twelveHoursOfHourlyForecastsResponse.getItems();

		final int columnCount = items.size();
		final int columnWidth = (int) context.getResources().getDimension(R.dimen.valueColumnWidthInSCHourly);
		final int viewWidth = columnCount * columnWidth;

		dateRow = new DateView(context, FragmentType.Simple, viewWidth, columnWidth);
		ClockView clockRow = new ClockView(context, FragmentType.Simple, viewWidth, columnWidth);
		SingleWeatherIconView weatherIconRow = new SingleWeatherIconView(context, FragmentType.Simple, viewWidth, weatherRowHeight,
				columnWidth);
		TextValueView tempRow = new TextValueView(context, FragmentType.Simple, viewWidth, defaultTextRowHeight, columnWidth);
		IconTextView popRow = new IconTextView(context, FragmentType.Simple, viewWidth,
				columnWidth, R.drawable.pop);
		IconTextView rainVolumeRow = new IconTextView(context, FragmentType.Simple, viewWidth,
				columnWidth, R.drawable.raindrop);
		IconTextView snowVolumeRow = new IconTextView(context, FragmentType.Simple, viewWidth,
				columnWidth, R.drawable.snowparticle);

		//시각, 기온, 강수확률, 강수량-----
		List<SingleWeatherIconView.WeatherIconObj> weatherIconObjList = new ArrayList<>();
		List<ZonedDateTime> dateTimeList = new ArrayList<>();
		List<String> tempList = new ArrayList<>();
		List<String> popList = new ArrayList<>();
		List<String> rainVolumeList = new ArrayList<>();
		List<String> snowVolumeList = new ArrayList<>();

		boolean haveSnow = false;

		final String mm = "mm";
		final String cm = "cm";

		List<HourlyForecastDto> hourlyForecastDtoList = AccuWeatherResponseProcessor.makeHourlyForecastDtoList(getContext(), items,
				windUnit, tempUnit, visibilityUnit, zoneId);

		for (HourlyForecastDto item : hourlyForecastDtoList) {
			dateTimeList.add(item.getHours());
			weatherIconObjList.add(new SingleWeatherIconView.WeatherIconObj(
					ContextCompat.getDrawable(context, item.getWeatherIcon())));
			tempList.add(item.getTemp());
			popList.add(item.getPop());
			rainVolumeList.add(item.getRainVolume().replace(mm, ""));

			if (item.isHasSnow()) {
				if (!haveSnow) {
					haveSnow = true;
				}
			}
			snowVolumeList.add(item.getSnowVolume().replace(cm, ""));
		}

		weatherIconRow.setWeatherImgs(weatherIconObjList);
		dateRow.init(dateTimeList);
		clockRow.setClockList(dateTimeList);
		tempRow.setValueList(tempList);

		popRow.setValueList(popList);
		rainVolumeRow.setValueList(rainVolumeList);
		snowVolumeRow.setValueList(snowVolumeList);

		LinearLayout.LayoutParams rowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		rowLayoutParams.gravity = Gravity.CENTER_VERTICAL;

		LinearLayout.LayoutParams iconTextRowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		iconTextRowLayoutParams.gravity = Gravity.CENTER_VERTICAL;
		iconTextRowLayoutParams.topMargin = (int) getResources().getDimension(R.dimen.iconValueViewMargin);

		if (textSizeMap.containsKey(WeatherDataType.date)) {
			dateRow.setTextSize(textSizeMap.get(WeatherDataType.date));
		}
		if (textSizeMap.containsKey(WeatherDataType.time)) {
			clockRow.setTextSize(textSizeMap.get(WeatherDataType.time));
		}
		if (textSizeMap.containsKey(WeatherDataType.temp)) {
			tempRow.setTextSize(textSizeMap.get(WeatherDataType.temp));
		} else {
			tempRow.setTextSize(17);
		}
		if (textSizeMap.containsKey(WeatherDataType.pop)) {
			popRow.setValueTextSize(textSizeMap.get(WeatherDataType.pop));
		}
		if (textSizeMap.containsKey(WeatherDataType.rainVolume)) {
			rainVolumeRow.setValueTextSize(textSizeMap.get(WeatherDataType.rainVolume));
		}
		if (textSizeMap.containsKey(WeatherDataType.snowVolume)) {
			snowVolumeRow.setValueTextSize(textSizeMap.get(WeatherDataType.snowVolume));
		}


		if (textColorMap.containsKey(WeatherDataType.date)) {
			dateRow.setTextColor(textColorMap.get(WeatherDataType.date));
		}
		if (textColorMap.containsKey(WeatherDataType.time)) {
			clockRow.setTextColor(textColorMap.get(WeatherDataType.time));
		}
		if (textColorMap.containsKey(WeatherDataType.temp)) {
			tempRow.setTextColor(textColorMap.get(WeatherDataType.temp));
		}
		if (textColorMap.containsKey(WeatherDataType.pop)) {
			popRow.setTextColor(textColorMap.get(WeatherDataType.pop));
		}
		if (textColorMap.containsKey(WeatherDataType.rainVolume)) {
			rainVolumeRow.setTextColor(textColorMap.get(WeatherDataType.rainVolume));
		}
		if (textColorMap.containsKey(WeatherDataType.snowVolume)) {
			snowVolumeRow.setTextColor(textColorMap.get(WeatherDataType.snowVolume));
		}

		binding.forecastView.addView(dateRow, rowLayoutParams);
		binding.forecastView.addView(clockRow, rowLayoutParams);
		binding.forecastView.addView(weatherIconRow, rowLayoutParams);
		binding.forecastView.addView(popRow, iconTextRowLayoutParams);
		binding.forecastView.addView(rainVolumeRow, iconTextRowLayoutParams);
		if (haveSnow) {
			binding.forecastView.addView(snowVolumeRow, iconTextRowLayoutParams);
		}
		LinearLayout.LayoutParams tempRowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		tempRowLayoutParams.topMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4, getResources().getDisplayMetrics());
		binding.forecastView.addView(tempRow, tempRowLayoutParams);

	}

}