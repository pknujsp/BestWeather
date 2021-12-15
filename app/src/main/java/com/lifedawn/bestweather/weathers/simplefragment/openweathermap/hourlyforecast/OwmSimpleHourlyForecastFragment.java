package com.lifedawn.bestweather.weathers.simplefragment.openweathermap.hourlyforecast;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.WeatherDataType;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.onecall.OneCallResponse;
import com.lifedawn.bestweather.weathers.WeatherFragment;
import com.lifedawn.bestweather.weathers.comparison.hourlyforecast.HourlyForecastComparisonFragment;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;
import com.lifedawn.bestweather.weathers.models.HourlyForecastDto;
import com.lifedawn.bestweather.weathers.detailfragment.openweathermap.hourlyforecast.OwmDetailHourlyForecastFragment;
import com.lifedawn.bestweather.weathers.simplefragment.base.BaseSimpleForecastFragment;
import com.lifedawn.bestweather.weathers.view.DateView;
import com.lifedawn.bestweather.weathers.FragmentType;
import com.lifedawn.bestweather.weathers.view.IconTextView;
import com.lifedawn.bestweather.weathers.view.SingleWeatherIconView;
import com.lifedawn.bestweather.weathers.view.TextsView;

import org.jetbrains.annotations.NotNull;

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
				detailHourlyForecastFragment.setOneCallResponse(oneCallResponse);

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

		final int WEATHER_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.singleWeatherIconValueRowHeightInSC);

		List<OneCallResponse.Hourly> items = oneCallResponse.getHourly();

		final int COLUMN_COUNT = items.size();
		final int COLUMN_WIDTH = (int) context.getResources().getDimension(R.dimen.valueColumnWidthInSCHourly);
		final int VIEW_WIDTH = COLUMN_COUNT * COLUMN_WIDTH;

		dateRow = new DateView(context, FragmentType.Simple, VIEW_WIDTH, COLUMN_WIDTH);
		SingleWeatherIconView weatherIconRow = new SingleWeatherIconView(context, FragmentType.Simple, VIEW_WIDTH, WEATHER_ROW_HEIGHT,
				COLUMN_WIDTH);
		IconTextView popRow = new IconTextView(context, FragmentType.Simple, VIEW_WIDTH,
				COLUMN_WIDTH, R.drawable.pop);
		IconTextView rainVolumeRow = new IconTextView(context, FragmentType.Simple, VIEW_WIDTH,
				COLUMN_WIDTH, R.drawable.raindrop);
		IconTextView snowVolumeRow = new IconTextView(context, FragmentType.Simple, VIEW_WIDTH,
				COLUMN_WIDTH, R.drawable.snowparticle);

		//시각, 기온, 강수확률, 강수량
		List<SingleWeatherIconView.WeatherIconObj> iconObjList = new ArrayList<>();
		List<ZonedDateTime> dateList = new ArrayList<>();
		List<String> hourList = new ArrayList<>();
		List<String> tempList = new ArrayList<>();
		List<String> probabilityOfPrecipitationList = new ArrayList<>();
		List<String> rainVolumeList = new ArrayList<>();
		List<String> snowVolumeList = new ArrayList<>();

		boolean haveSnowVolumes = false;

		List<HourlyForecastDto> hourlyForecastDtoList = OpenWeatherMapResponseProcessor.makeHourlyForecastDtoList(getContext(),
				oneCallResponse, windUnit, tempUnit, visibilityUnit);

		for (HourlyForecastDto item : hourlyForecastDtoList) {
			dateList.add(item.getHours());
			hourList.add(String.valueOf(item.getHours().getHour()));
			iconObjList.add(new SingleWeatherIconView.WeatherIconObj(ContextCompat.getDrawable(context, item.getWeatherIcon()), item.getWeatherDescription()));
			tempList.add(item.getTemp());
			probabilityOfPrecipitationList.add(item.getPop());
			rainVolumeList.add(item.getRainVolume().replace("mm", ""));

			if (item.isHasSnow()) {
				if (!haveSnowVolumes) {
					haveSnowVolumes = true;
				}
			}
			snowVolumeList.add(item.getSnowVolume().replace("mm", ""));
		}
		weatherIconRow.setWeatherImgs(iconObjList);
		dateRow.init(dateList);

		popRow.setValueList(probabilityOfPrecipitationList);
		rainVolumeRow.setValueList(rainVolumeList);
		snowVolumeRow.setValueList(snowVolumeList);

		TextsView hourRow = new TextsView(context, VIEW_WIDTH, COLUMN_WIDTH, hourList);
		TextsView tempRow = new TextsView(context, VIEW_WIDTH, COLUMN_WIDTH, tempList);

		LinearLayout.LayoutParams rowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		if (textSizeMap.containsKey(WeatherDataType.date)) {
			dateRow.setTextSize(textSizeMap.get(WeatherDataType.date));
		}
		if (textSizeMap.containsKey(WeatherDataType.time)) {
			hourRow.setValueTextSize(textSizeMap.get(WeatherDataType.time));
		}
		if (textSizeMap.containsKey(WeatherDataType.temp)) {
			tempRow.setValueTextSize(textSizeMap.get(WeatherDataType.temp));
		} else {
			tempRow.setValueTextSize(17);
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
			hourRow.setValueTextColor(textColorMap.get(WeatherDataType.time));
		}
		if (textColorMap.containsKey(WeatherDataType.temp)) {
			tempRow.setValueTextColor(textColorMap.get(WeatherDataType.temp));
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
		binding.forecastView.addView(hourRow, rowLayoutParams);
		binding.forecastView.addView(weatherIconRow, rowLayoutParams);
		binding.forecastView.addView(popRow, rowLayoutParams);
		binding.forecastView.addView(rainVolumeRow, rowLayoutParams);
		if (haveSnowVolumes) {
			binding.forecastView.addView(snowVolumeRow, rowLayoutParams);
		}

		LinearLayout.LayoutParams tempRowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		tempRowLayoutParams.topMargin = (int) getResources().getDimension(R.dimen.tempTopMargin);
		binding.forecastView.addView(tempRow, tempRowLayoutParams);

	}

}