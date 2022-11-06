package com.lifedawn.bestweather.weathers.simplefragment.dailyforecast;

import android.content.Context;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.WeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherValueType;
import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.weathers.FragmentType;
import com.lifedawn.bestweather.weathers.WeatherFragment;
import com.lifedawn.bestweather.weathers.comparison.dailyforecast.DailyForecastComparisonFragment;
import com.lifedawn.bestweather.weathers.detailfragment.dailyforecast.DetailDailyForecastFragment;
import com.lifedawn.bestweather.weathers.models.DailyForecastDto;
import com.lifedawn.bestweather.weathers.simplefragment.base.BaseSimpleForecastFragment;
import com.lifedawn.bestweather.weathers.view.DetailDoubleTemperatureView;
import com.lifedawn.bestweather.weathers.view.DoubleWeatherIconView;
import com.lifedawn.bestweather.weathers.view.IconTextView;
import com.lifedawn.bestweather.weathers.view.TextsView;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.Objects;

public class SimpleDailyForecastFragment extends BaseSimpleForecastFragment {
	private static List<DailyForecastDto> dailyForecastDtoList;

	public SimpleDailyForecastFragment setDailyForecastDtoList(List<DailyForecastDto> dailyForecastDtoList) {
		this.dailyForecastDtoList = dailyForecastDtoList;
		return this;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		needCompare = true;
	}

	@Override
	public void onInflateFinished(@NonNull View view, int resid, @Nullable ViewGroup parent) {
		super.onInflateFinished(view, resid, parent);

		binding.weatherCardViewHeader.forecastName.setText(R.string.daily_forecast);
		binding.weatherCardViewHeader.compareForecast.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				if (availableNetwork()) {
					DailyForecastComparisonFragment comparisonFragment = new DailyForecastComparisonFragment();
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
			public void onClick(View view) {
				Bundle arguments = new Bundle();
				arguments.putString(BundleKey.AddressName.name(), bundle.getString(BundleKey.AddressName.name()));
				arguments.putSerializable(BundleKey.TimeZone.name(), bundle.getSerializable(BundleKey.TimeZone.name()));
				arguments.putSerializable(BundleKey.WeatherProvider.name(), mainWeatherProviderType);

				DetailDailyForecastFragment detailDailyForecastFragment = new DetailDailyForecastFragment();
				DetailDailyForecastFragment.setDailyForecastDtoList(dailyForecastDtoList);
				detailDailyForecastFragment.setArguments(arguments);

				String tag = getString(R.string.tag_detail_daily_forecast_fragment);
				FragmentManager fragmentManager = getParentFragment().getParentFragmentManager();

				fragmentManager.beginTransaction().hide(
						fragmentManager.findFragmentByTag(WeatherFragment.class.getName())).add(R.id.fragment_container,
						detailDailyForecastFragment, tag).addToBackStack(tag).commit();
			}
		});

		setValuesToViews();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void setValuesToViews() {
		super.setValuesToViews();
		// 날짜 ,낮과 밤의 날씨상태, 강수확률, 강우량, 강설량, 최저/최고 기온

		MyApplication.getExecutorService().submit(() -> {
			Context context = getContext();

			final int WEATHER_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.singleWeatherIconValueRowHeightInSC);
			final int TEMP_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.doubleTemperatureRowHeightInSC);

			final int COLUMN_COUNT = dailyForecastDtoList.size();
			final int COLUMN_WIDTH = (int) context.getResources().getDimension(R.dimen.valueColumnWidthInSDailyOwm);
			final int VIEW_WIDTH = COLUMN_COUNT * COLUMN_WIDTH;

			DoubleWeatherIconView weatherIconRow = new DoubleWeatherIconView(context, FragmentType.Simple, VIEW_WIDTH, WEATHER_ROW_HEIGHT,
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
			List<DoubleWeatherIconView.WeatherIconObj> weatherIconObjList = new ArrayList<>();
			//기온, 강수확률, 강수량
			List<Integer> minTempList = new ArrayList<>();
			List<Integer> maxTempList = new ArrayList<>();
			List<String> popList = new ArrayList<>();
			List<String> rainVolumeList = new ArrayList<>();
			List<String> snowVolumeList = new ArrayList<>();

			final String tempDegree = MyApplication.VALUE_UNIT_OBJ.getTempUnitText();
			final String mm = "mm";
			final String cm = "cm";

			boolean haveSnow = false;
			boolean haveRain = false;

			float rainVolume = 0f;
			float snowVolume = 0f;

			final String zeroPrecipitationFormat = "%.1f";

			for (DailyForecastDto item : dailyForecastDtoList) {
				if (!item.isAvailable_toMakeMinMaxTemp())
					break;

				rainVolume = 0f;
				snowVolume = 0f;

				dateList.add(item.getDate().format(dateTimeFormatter));
				minTempList.add(Integer.parseInt(item.getMinTemp().replace(tempDegree, "")));
				maxTempList.add(Integer.parseInt(item.getMaxTemp().replace(tempDegree, "")));

				if (item.getValuesList().size() == 1) {
					popList.add(item.getValuesList().get(0).getPop());
					weatherIconObjList.add(new DoubleWeatherIconView.WeatherIconObj(ContextCompat.getDrawable(context,
							item.getValuesList().get(0).getWeatherIcon()), item.getValuesList().get(0).getWeatherDescription()));

					if (item.getValuesList().get(0).getRainVolume() != null) {
						rainVolume = Float.parseFloat(item.getValuesList().get(0).getRainVolume().replace(mm, "").replace(cm, ""));
					}
					if (item.getValuesList().get(0).getSnowVolume() != null) {
						snowVolume = Float.parseFloat(item.getValuesList().get(0).getSnowVolume().replace(mm, "").replace(cm, ""));
					}
				} else if (item.getValuesList().size() == 2) {
					popList.add(item.getValuesList().get(0).getPop() + "/" + item.getValuesList().get(1).getPop());
					weatherIconObjList.add(new DoubleWeatherIconView.WeatherIconObj(ContextCompat.getDrawable(context,
							item.getValuesList().get(0).getWeatherIcon()),
							ContextCompat.getDrawable(context, item.getValuesList().get(1).getWeatherIcon()),
							item.getValuesList().get(0).getWeatherDescription(),
							item.getValuesList().get(1).getWeatherDescription()));

					if (item.getValuesList().get(0).getRainVolume() != null || item.getValuesList().get(1).getRainVolume() != null) {
						rainVolume = Float.parseFloat(item.getValuesList().get(0).getRainVolume().replace(mm, "").replace(cm, ""))
								+ Float.parseFloat(item.getValuesList().get(1).getRainVolume().replace(mm, "").replace(cm, ""));
					}
					if (item.getValuesList().get(0).getSnowVolume() != null || item.getValuesList().get(1).getSnowVolume() != null) {
						snowVolume = Float.parseFloat(item.getValuesList().get(0).getSnowVolume().replace(mm, "").replace(cm, ""))
								+ Float.parseFloat(item.getValuesList().get(1).getSnowVolume().replace(mm, "").replace(cm, ""));
					}
				} else if (item.getValuesList().size() == 4) {
					weatherIconObjList.add(new DoubleWeatherIconView.WeatherIconObj(ContextCompat.getDrawable(context,
							item.getValuesList().get(1).getWeatherIcon()),
							ContextCompat.getDrawable(context, item.getValuesList().get(2).getWeatherIcon()),
							item.getValuesList().get(1).getWeatherDescription(),
							item.getValuesList().get(2).getWeatherDescription()));

					if (item.getValuesList().get(0).isHasPrecipitationVolume() || item.getValuesList().get(1).isHasPrecipitationVolume() ||
							item.getValuesList().get(2).isHasPrecipitationVolume() || item.getValuesList().get(3).isHasPrecipitationVolume()) {
						rainVolume = Float.parseFloat(item.getValuesList().get(0).getPrecipitationVolume().replace(mm, "").replace(cm, ""))
								+ Float.parseFloat(item.getValuesList().get(1).getPrecipitationVolume().replace(mm, "").replace(cm, ""))
								+ Float.parseFloat(item.getValuesList().get(2).getPrecipitationVolume().replace(mm, "").replace(cm, ""))
								+ Float.parseFloat(item.getValuesList().get(3).getPrecipitationVolume().replace(mm, "").replace(cm, ""));
					}

				}

				rainVolumeList.add(String.format(Locale.getDefault(), zeroPrecipitationFormat, rainVolume));
				snowVolumeList.add(String.format(Locale.getDefault(), zeroPrecipitationFormat, snowVolume));

				if (!haveRain) {
					if (rainVolume > 0f) {
						haveRain = true;
					}
				}
				if (!haveSnow) {
					if (snowVolume > 0f) {
						haveSnow = true;
					}
				}
			}

			weatherIconRow.setIcons(weatherIconObjList);
			probabilityOfPrecipitationRow.setValueList(popList);
			rainVolumeRow.setValueList(rainVolumeList);
			snowVolumeRow.setValueList(snowVolumeList);

			TextsView dateRow = new TextsView(context, VIEW_WIDTH, COLUMN_WIDTH, dateList);
			DetailDoubleTemperatureView tempRow = new DetailDoubleTemperatureView(getContext(), FragmentType.Simple, VIEW_WIDTH,
					TEMP_ROW_HEIGHT, COLUMN_WIDTH, minTempList, maxTempList);

			LinearLayout.LayoutParams rowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);

			LinearLayout.LayoutParams dateRowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			dateRowLayoutParams.gravity = Gravity.CENTER_VERTICAL;
			dateRowLayoutParams.bottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, getResources().getDisplayMetrics());

			if (textSizeMap.containsKey(WeatherValueType.date)) {
				dateRow.setValueTextSize(textSizeMap.get(WeatherValueType.date));
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
			if (textSizeMap.containsKey(WeatherValueType.temp)) {
				tempRow.setTempTextSize(textSizeMap.get(WeatherValueType.temp));
			}

			if (textColorMap.containsKey(WeatherValueType.date)) {
				dateRow.setValueTextColor(textColorMap.get(WeatherValueType.date));
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
			if (textColorMap.containsKey(WeatherValueType.temp)) {
				tempRow.setTextColor(textColorMap.get(WeatherValueType.temp));
			}

			boolean finalHaveRain = haveRain;
			boolean finalHaveSnow = haveSnow;
			Objects.requireNonNull(requireActivity()).runOnUiThread(() -> {
				binding.forecastView.addView(dateRow, dateRowLayoutParams);
				binding.forecastView.addView(weatherIconRow, rowLayoutParams);
				binding.forecastView.addView(probabilityOfPrecipitationRow, rowLayoutParams);
				if (finalHaveRain) {
					binding.forecastView.addView(rainVolumeRow, rowLayoutParams);
				}
				if (finalHaveSnow) {
					binding.forecastView.addView(snowVolumeRow, rowLayoutParams);
				}

				LinearLayout.LayoutParams tempRowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);
				tempRowLayoutParams.topMargin = (int) getResources().getDimension(R.dimen.tempTopMargin);
				binding.forecastView.addView(tempRow, tempRowLayoutParams);

				createValueUnitsDescription(mainWeatherProviderType, finalHaveRain, finalHaveSnow);

				onFinishedSetData();
			});
		});


	}

}