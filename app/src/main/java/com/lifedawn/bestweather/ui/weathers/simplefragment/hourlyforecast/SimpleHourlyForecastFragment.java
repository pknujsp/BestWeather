package com.lifedawn.bestweather.ui.weathers.simplefragment.hourlyforecast;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Color;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.constants.BundleKey;
import com.lifedawn.bestweather.commons.constants.WeatherValueType;
import com.lifedawn.bestweather.data.MyApplication;
import com.lifedawn.bestweather.ui.weathers.FragmentType;
import com.lifedawn.bestweather.ui.weathers.WeatherFragment;
import com.lifedawn.bestweather.ui.weathers.comparison.hourlyforecast.HourlyForecastComparisonFragment;

import com.lifedawn.bestweather.ui.weathers.detailfragment.hourlyforecast.DetailHourlyForecastFragment;

import com.lifedawn.bestweather.data.local.weather.models.HourlyForecastDto;
import com.lifedawn.bestweather.ui.weathers.simplefragment.base.BaseSimpleForecastFragment;

import com.lifedawn.bestweather.ui.weathers.view.DateView;
import com.lifedawn.bestweather.ui.weathers.view.DetailSingleTemperatureView;
import com.lifedawn.bestweather.ui.weathers.view.IconTextView;
import com.lifedawn.bestweather.ui.weathers.view.SingleWeatherIconView;
import com.lifedawn.bestweather.ui.weathers.view.TextsView;

import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class SimpleHourlyForecastFragment extends BaseSimpleForecastFragment {
	private static List<HourlyForecastDto> hourlyForecastDtoList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		needCompare = true;
	}

	@Override
	public void onInflateFinished(@NonNull View view, int resid, @Nullable ViewGroup parent) {
		super.onInflateFinished(view, resid, parent);
		binding.weatherCardViewHeader.forecastName.setText(R.string.hourly_forecast);
		binding.weatherCardViewHeader.compareForecast.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (availableNetwork()) {
					HourlyForecastComparisonFragment comparisonFragment = new HourlyForecastComparisonFragment();
					comparisonFragment.setArguments(bundle);

					String tag = getString(R.string.tag_comparison_fragment);
					FragmentManager fragmentManager = getParentFragment().getParentFragmentManager();

					fragmentManager.beginTransaction().hide(
							fragmentManager.findFragmentByTag(WeatherFragment.class.getName())).add(R.id.fragment_container,
							comparisonFragment, tag).addToBackStack(tag).commit();
				}
			}
		});

		binding.weatherCardViewHeader.detailForecast.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DetailHourlyForecastFragment detailHourlyForecastFragment = new DetailHourlyForecastFragment();

				Bundle arguments = new Bundle();
				arguments.putString(BundleKey.AddressName.name(), bundle.getString(BundleKey.AddressName.name()));
				arguments.putSerializable(BundleKey.TimeZone.name(), bundle.getSerializable(BundleKey.TimeZone.name()));
				arguments.putDouble(BundleKey.Latitude.name(), bundle.getDouble(BundleKey.Latitude.name()));
				arguments.putDouble(BundleKey.Longitude.name(), bundle.getDouble(BundleKey.Longitude.name()));
				arguments.putSerializable(BundleKey.WeatherProvider.name(), mainWeatherProviderType);

				List<HourlyForecastDto> copiedList = new ArrayList<>(hourlyForecastDtoList);
				Collections.copy(copiedList, hourlyForecastDtoList);

				DetailHourlyForecastFragment.setHourlyForecastDtoList(copiedList);
				detailHourlyForecastFragment.setArguments(arguments);

				String tag = getString(R.string.tag_detail_hourly_forecast_fragment);
				FragmentManager fragmentManager = getParentFragment().getParentFragmentManager();

				fragmentManager.beginTransaction().hide(
						fragmentManager.findFragmentByTag(WeatherFragment.class.getName())).add(R.id.fragment_container,
						detailHourlyForecastFragment, tag).addToBackStack(tag).commitAllowingStateLoss();
			}
		});

		setValuesToViews();

	}

	public void setHourlyForecastDtoList(List<HourlyForecastDto> hourlyForecastDtoList) {
		SimpleHourlyForecastFragment.hourlyForecastDtoList = hourlyForecastDtoList;
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}


	@SuppressLint("ClickableViewAccessibility")
	@Override
	public void setValuesToViews() {
		MyApplication.getExecutorService().submit(() -> {
			Context context = requireContext().getApplicationContext();

			final int weatherRowHeight = (int) context.getResources().getDimension(R.dimen.singleWeatherIconValueRowHeightInSC);
			final int columnCount = hourlyForecastDtoList.size();
			final int columnWidth = (int) context.getResources().getDimension(R.dimen.valueColumnWidthInSCHourly);
			final int viewWidth = columnCount * columnWidth;

			dateRow = new DateView(context, FragmentType.Simple, viewWidth, columnWidth);
			SingleWeatherIconView weatherIconRow = new SingleWeatherIconView(context, FragmentType.Simple, viewWidth, weatherRowHeight,
					columnWidth);
			IconTextView probabilityOfPrecipitationRow = new IconTextView(context, FragmentType.Simple, viewWidth,
					columnWidth, R.drawable.pop);
			IconTextView rainVolumeRow = new IconTextView(context, FragmentType.Simple, viewWidth,
					columnWidth, R.drawable.raindrop);
			IconTextView precipitationVolumeRow = new IconTextView(context, FragmentType.Simple, viewWidth,
					columnWidth, R.drawable.raindrop);
			IconTextView snowVolumeRow = new IconTextView(context, FragmentType.Simple, viewWidth,
					columnWidth, R.drawable.snowparticle);

			customViewList.add(weatherIconRow);
			customViewList.add(probabilityOfPrecipitationRow);
			customViewList.add(rainVolumeRow);
			customViewList.add(precipitationVolumeRow);
			customViewList.add(snowVolumeRow);

			List<SingleWeatherIconView.WeatherIconObj> weatherIconObjList = new ArrayList<>();
			List<String> hourList = new ArrayList<>();
			List<Integer> tempList = new ArrayList<>();
			List<String> popList = new ArrayList<>();
			List<String> rainVolumeList = new ArrayList<>();
			List<String> snowVolumeList = new ArrayList<>();
			List<String> precipitationVolumeList = new ArrayList<>();
			List<ZonedDateTime> dateTimeList = new ArrayList<>();
			List<Boolean> precipitationVisibleList = new ArrayList<>();

			final String mm = "mm";
			final String cm = "cm";
			final String degree = MyApplication.VALUE_UNIT_OBJ.getTempUnitText();

			boolean haveSnow = false;
			boolean haveRain = false;
			boolean havePrecipitation = false;
			boolean hasNextNHoursPrecipitation = false;
			ZonedDateTime firstDateTime_hasNextNHours = null;

			for (HourlyForecastDto item : hourlyForecastDtoList) {
				dateTimeList.add(item.getHours());
				hourList.add(String.valueOf(item.getHours().getHour()));
				weatherIconObjList.add(new SingleWeatherIconView.WeatherIconObj(
						ContextCompat.getDrawable(context, item.getWeatherIcon()), item.getWeatherDescription()));
				tempList.add(Integer.parseInt(item.getTemp().replace(degree, "")));

				if (item.getPop() != null)
					popList.add(item.getPop());
				if (item.getRainVolume() != null)
					rainVolumeList.add(item.getRainVolume().replace(mm, "").replace(cm, ""));
				if (item.getSnowVolume() != null)
					snowVolumeList.add(item.getSnowVolume().replace(mm, "").replace(cm, ""));

				if (item.getPrecipitationVolume() != null) {
					precipitationVolumeList.add(item.getPrecipitationVolume().replace(mm, "").replace(cm, ""));

					if (item.isHasNext6HoursPrecipitation() && firstDateTime_hasNextNHours == null) {
						firstDateTime_hasNextNHours = item.getHours();
						hasNextNHoursPrecipitation = true;
					}
				}
				if (item.isHasSnow()) {
					if (!haveSnow) {
						haveSnow = true;
					}
				}
				if (item.isHasRain()) {
					if (!haveRain) {
						haveRain = true;
					}
				}
				if (item.isHasNext6HoursPrecipitation() || item.isHasPrecipitation()) {
					if (!havePrecipitation) {
						havePrecipitation = true;
					}
				}
				precipitationVisibleList.add(item.isHasSnow() || item.isHasRain() || item.isHasPrecipitation());
			}

			dateRow.init(dateTimeList);
			probabilityOfPrecipitationRow.setValueList(popList);
			rainVolumeRow.setValueList(rainVolumeList).setVisibleList(precipitationVisibleList);
			precipitationVolumeRow.setVisibleList(precipitationVisibleList);
			precipitationVolumeRow.setValueList(precipitationVolumeList);
			snowVolumeRow.setValueList(snowVolumeList).setVisibleList(precipitationVisibleList);
			weatherIconRow.setWeatherImgs(weatherIconObjList);

			TextsView hourRow = new TextsView(context, viewWidth, columnWidth, hourList);
			DetailSingleTemperatureView tempRow = new DetailSingleTemperatureView(context, tempList);

			customViewList.add(hourRow);
			customViewList.add(tempRow);

			tempRow.setLineColor(Color.WHITE);
			tempRow.setCircleColor(Color.WHITE);

			if (textSizeMap.containsKey(WeatherValueType.date)) {
				dateRow.setTextSize(textSizeMap.get(WeatherValueType.date));
			}
			if (textSizeMap.containsKey(WeatherValueType.time)) {
				hourRow.setValueTextSize(textSizeMap.get(WeatherValueType.time));
			}
			if (textSizeMap.containsKey(WeatherValueType.temp)) {
				tempRow.setTempTextSizeSp(textSizeMap.get(WeatherValueType.temp));
			} else {
				tempRow.setTempTextSizeSp(16);
			}
			if (textSizeMap.containsKey(WeatherValueType.pop)) {
				probabilityOfPrecipitationRow.setValueTextSize(textSizeMap.get(WeatherValueType.pop));
			}
			if (textSizeMap.containsKey(WeatherValueType.rainVolume)) {
				rainVolumeRow.setValueTextSize(textSizeMap.get(WeatherValueType.rainVolume));
			}
			if (textSizeMap.containsKey(WeatherValueType.snowVolume)) {
				snowVolumeRow.setValueTextSize(textSizeMap.get(WeatherValueType.snowVolume));
			}

			if (textColorMap.containsKey(WeatherValueType.date)) {
				dateRow.setTextColor(textColorMap.get(WeatherValueType.date));
			}
			if (textColorMap.containsKey(WeatherValueType.time)) {
				hourRow.setValueTextColor(textColorMap.get(WeatherValueType.time));
			}
			if (textColorMap.containsKey(WeatherValueType.temp)) {
				tempRow.setTextColor(textColorMap.get(WeatherValueType.temp));
			} else {
				tempRow.setTextColor(Color.WHITE);
			}
			if (textColorMap.containsKey(WeatherValueType.pop)) {
				probabilityOfPrecipitationRow.setTextColor(textColorMap.get(WeatherValueType.pop));
			}
			if (textColorMap.containsKey(WeatherValueType.rainVolume)) {
				rainVolumeRow.setTextColor(textColorMap.get(WeatherValueType.rainVolume));
			}
			if (textColorMap.containsKey(WeatherValueType.snowVolume)) {
				snowVolumeRow.setTextColor(textColorMap.get(WeatherValueType.snowVolume));
			}

			LinearLayout.LayoutParams rowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);


			boolean finalHaveRain = haveRain;
			boolean finalHaveSnow = haveSnow;
			boolean finalHavePrecipitation = havePrecipitation;
			boolean finalHasNextNHoursPrecipitation = hasNextNHoursPrecipitation;
			ZonedDateTime finalFirstDateTime_hasNextNHours = firstDateTime_hasNextNHours;

			try {

				requireActivity().runOnUiThread(() -> {
					binding.forecastView.addView(dateRow, rowLayoutParams);
					binding.forecastView.addView(hourRow, rowLayoutParams);
					binding.forecastView.addView(weatherIconRow, rowLayoutParams);
					binding.forecastView.addView(probabilityOfPrecipitationRow, rowLayoutParams);

					if (finalHaveRain) {
						binding.forecastView.addView(rainVolumeRow, rowLayoutParams);
					}
					if (finalHaveSnow) {
						binding.forecastView.addView(snowVolumeRow, rowLayoutParams);
					}
					if (finalHavePrecipitation) {
						binding.forecastView.addView(precipitationVolumeRow, rowLayoutParams);
					}

					LinearLayout.LayoutParams tempRowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
							(int) context.getResources().getDimension(R.dimen.singleTemperatureRowHeightInCOMMON));
					binding.forecastView.addView(tempRow, tempRowLayoutParams);

					if (finalHasNextNHoursPrecipitation) {
						createValueUnitsDescription(mainWeatherProviderType, true, finalHaveSnow, finalFirstDateTime_hasNextNHours, "6");
					} else {
						createValueUnitsDescription(mainWeatherProviderType, finalHaveRain, finalHaveSnow);
					}

					onFinishedSetData();
				});
			} catch (Exception e) {

			}
		});


	}


}