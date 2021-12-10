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
import com.lifedawn.bestweather.commons.enums.WeatherDataType;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.onecall.OneCallResponse;
import com.lifedawn.bestweather.weathers.WeatherFragment;
import com.lifedawn.bestweather.weathers.comparison.dailyforecast.DailyForecastComparisonFragment;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.detailfragment.dto.DailyForecastDto;
import com.lifedawn.bestweather.weathers.detailfragment.openweathermap.dailyforecast.OwmDetailDailyForecastFragment;
import com.lifedawn.bestweather.weathers.simplefragment.base.BaseSimpleForecastFragment;
import com.lifedawn.bestweather.weathers.view.DetailDoubleTemperatureView;
import com.lifedawn.bestweather.weathers.FragmentType;
import com.lifedawn.bestweather.weathers.view.IconTextView;
import com.lifedawn.bestweather.weathers.view.TextValueView;
import com.lifedawn.bestweather.weathers.view.SingleWeatherIconView;

import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

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
		binding.weatherCardViewHeader.forecastName.setText(R.string.daily_forecast);
		binding.weatherCardViewHeader.compareForecast.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				DailyForecastComparisonFragment comparisonFragment = new DailyForecastComparisonFragment();
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
			public void onClick(View view) {
				OwmDetailDailyForecastFragment detailDailyForecastFragment = new OwmDetailDailyForecastFragment();
				detailDailyForecastFragment.setDailyList(oneCallResponse.getDaily());

				Bundle bundle = new Bundle();
				bundle.putString(BundleKey.AddressName.name(), addressName);
				bundle.putSerializable(BundleKey.TimeZone.name(), zoneId);

				detailDailyForecastFragment.setArguments(bundle);

				String tag = getString(R.string.tag_detail_daily_forecast_fragment);
				FragmentManager fragmentManager = getParentFragment().getParentFragmentManager();

				fragmentManager.beginTransaction().hide(
						fragmentManager.findFragmentByTag(WeatherFragment.class.getName())).add(R.id.fragment_container,
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
		final int COLUMN_WIDTH = (int) context.getResources().getDimension(R.dimen.valueColumnWidthInSDailyOwm);
		final int VIEW_WIDTH = COLUMN_COUNT * COLUMN_WIDTH;

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

		final String degree = "º";
		final String mm = "mm";
		boolean haveSnowVolumes = false;

		List<DailyForecastDto> dailyForecastDtoList = OpenWeatherMapResponseProcessor.makeDailyForecastDtoList(getContext(), items,
				windUnit, tempUnit, zoneId);

		for (DailyForecastDto item : dailyForecastDtoList) {
			dateList.add(item.getDate().format(dateTimeFormatter));
			minTempList.add(Integer.parseInt(item.getMinTemp().replace(degree, "")));
			maxTempList.add(Integer.parseInt(item.getMaxTemp().replace(degree, "")));

			if (item.getSingleValues().isHasSnowVolume()) {
				if (!haveSnowVolumes) {
					haveSnowVolumes = true;
				}
			}
			snowVolumeList.add(item.getSingleValues().getSnowVolume().replace(mm, ""));

			probabilityOfPrecipitationList.add(item.getSingleValues().getPop());
			rainVolumeList.add(item.getSingleValues().getRainVolume().replace(mm, ""));

			weatherIconObjList.add(new SingleWeatherIconView.WeatherIconObj(ContextCompat.getDrawable(context,
					item.getSingleValues().getWeatherIcon())));
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

		if (textSizeMap.containsKey(WeatherDataType.date)) {
			dateRow.setTextSize(textSizeMap.get(WeatherDataType.date));
		}
		if (textSizeMap.containsKey(WeatherDataType.pop)) {
			probabilityOfPrecipitationRow.setValueTextSize(textSizeMap.get(WeatherDataType.pop));
		}
		if (textSizeMap.containsKey(WeatherDataType.rainVolume)) {
			rainVolumeRow.setValueTextSize(textSizeMap.get(WeatherDataType.rainVolume));
		}
		if (textSizeMap.containsKey(WeatherDataType.snowVolume)) {
			snowVolumeRow.setValueTextSize(textSizeMap.get(WeatherDataType.snowVolume));
		}
		if (textSizeMap.containsKey(WeatherDataType.temp)) {
			tempRow.setTempTextSize(textSizeMap.get(WeatherDataType.temp));
		}

		if (textColorMap.containsKey(WeatherDataType.date)) {
			dateRow.setTextColor(textColorMap.get(WeatherDataType.date));
		}
		if (textColorMap.containsKey(WeatherDataType.pop)) {
			probabilityOfPrecipitationRow.setTextColor(textColorMap.get(WeatherDataType.pop));
		}
		if (textColorMap.containsKey(WeatherDataType.rainVolume)) {
			rainVolumeRow.setTextColor(textColorMap.get(WeatherDataType.rainVolume));
		}
		if (textColorMap.containsKey(WeatherDataType.snowVolume)) {
			snowVolumeRow.setTextColor(textColorMap.get(WeatherDataType.snowVolume));
		}
		if (textColorMap.containsKey(WeatherDataType.temp)) {
			tempRow.setTextColor(textColorMap.get(WeatherDataType.temp));
		}

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