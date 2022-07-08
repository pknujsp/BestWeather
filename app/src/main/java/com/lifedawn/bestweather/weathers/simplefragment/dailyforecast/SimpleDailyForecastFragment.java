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

public class SimpleDailyForecastFragment extends BaseSimpleForecastFragment {
	private List<DailyForecastDto> dailyForecastDtoList;


	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		needCompare = true;
	}


	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		dailyForecastDtoList = (ArrayList<DailyForecastDto>) bundle.getSerializable(WeatherDataType.dailyForecast.name());

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
				arguments.putSerializable(WeatherDataType.dailyForecast.name(), (Serializable) dailyForecastDtoList);
				arguments.putString(BundleKey.AddressName.name(), bundle.getString(BundleKey.AddressName.name()));
				arguments.putSerializable(BundleKey.TimeZone.name(), bundle.getSerializable(BundleKey.TimeZone.name()));
				arguments.putSerializable(BundleKey.WeatherProvider.name(), mainWeatherProviderType);

				DetailDailyForecastFragment detailDailyForecastFragment = new DetailDailyForecastFragment();
				detailDailyForecastFragment.setArguments(arguments);

				String tag = getString(R.string.tag_detail_daily_forecast_fragment);
				FragmentManager fragmentManager = getParentFragment().getParentFragmentManager();

				fragmentManager.beginTransaction().hide(
						fragmentManager.findFragmentByTag(WeatherFragment.class.getName())).add(R.id.fragment_container,
						detailDailyForecastFragment, tag).addToBackStack(tag).commit();
			}
		});

		//setValuesToViews();
	}

	@Override
	public void setValuesToViews() {
		super.setValuesToViews();
		// 날짜 ,낮과 밤의 날씨상태, 강수확률, 강우량, 강설량, 최저/최고 기온
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

		for (DailyForecastDto item : dailyForecastDtoList) {
			rainVolume = 0f;
			snowVolume = 0f;

			dateList.add(item.getDate().format(dateTimeFormatter));
			minTempList.add(Integer.parseInt(item.getMinTemp().replace(tempDegree, "")));
			maxTempList.add(Integer.parseInt(item.getMaxTemp().replace(tempDegree, "")));

			if (item.isSingle()) {
				popList.add(item.getSingleValues().getPop());
				weatherIconObjList.add(new DoubleWeatherIconView.WeatherIconObj(ContextCompat.getDrawable(context,
						item.getSingleValues().getWeatherIcon()), item.getSingleValues().getWeatherDescription()));

				if (item.getSingleValues().getRainVolume() != null) {
					rainVolume = Float.parseFloat(item.getSingleValues().getRainVolume().replace(mm, "").replace(cm, ""));
				}
				if (item.getSingleValues().getSnowVolume() != null) {
					snowVolume = Float.parseFloat(item.getSingleValues().getSnowVolume().replace(mm, "").replace(cm, ""));
				}
			} else {
				popList.add(item.getAmValues().getPop() + "/" + item.getPmValues().getPop());
				weatherIconObjList.add(new DoubleWeatherIconView.WeatherIconObj(ContextCompat.getDrawable(context,
						item.getAmValues().getWeatherIcon()),
						ContextCompat.getDrawable(context, item.getPmValues().getWeatherIcon()),
						item.getAmValues().getWeatherDescription(),
						item.getPmValues().getWeatherDescription()));

				if (item.getAmValues().getRainVolume() != null || item.getPmValues().getRainVolume() != null) {
					rainVolume = Float.parseFloat(item.getAmValues().getRainVolume().replace(mm, "").replace(cm, ""))
							+ Float.parseFloat(item.getPmValues().getRainVolume().replace(mm, "").replace(cm, ""));
				}
				if (item.getAmValues().getSnowVolume() != null || item.getPmValues().getSnowVolume() != null) {
					snowVolume = Float.parseFloat(item.getAmValues().getSnowVolume().replace(mm, "").replace(cm, ""))
							+ Float.parseFloat(item.getPmValues().getSnowVolume().replace(mm, "").replace(cm, ""));
				}
			}

			rainVolumeList.add(String.format(Locale.getDefault(), rainVolume > 0f ? "%.2f" : "%.1f", rainVolume));
			snowVolumeList.add(String.format(Locale.getDefault(), snowVolume > 0f ? "%.2f" : "%.1f", snowVolume));

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

		binding.forecastView.addView(dateRow, dateRowLayoutParams);
		binding.forecastView.addView(weatherIconRow, rowLayoutParams);
		binding.forecastView.addView(probabilityOfPrecipitationRow, rowLayoutParams);
		if (haveRain) {
			binding.forecastView.addView(rainVolumeRow, rowLayoutParams);
		}
		if (haveSnow) {
			binding.forecastView.addView(snowVolumeRow, rowLayoutParams);
		}

		LinearLayout.LayoutParams tempRowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		tempRowLayoutParams.topMargin = (int) getResources().getDimension(R.dimen.tempTopMargin);
		binding.forecastView.addView(tempRow, tempRowLayoutParams);

		createValueUnitsDescription(mainWeatherProviderType, haveRain, haveSnow);

	}

}