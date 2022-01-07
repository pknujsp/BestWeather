package com.lifedawn.bestweather.weathers.simplefragment.openweathermap.hourlyforecast;

import android.content.Context;
import android.graphics.Color;
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
import com.lifedawn.bestweather.commons.enums.WeatherValueType;
import com.lifedawn.bestweather.commons.enums.WeatherDataSourceType;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.individual.hourlyforecast.OwmHourlyForecastResponse;
import com.lifedawn.bestweather.retrofit.responses.openweathermap.onecall.OwmOneCallResponse;
import com.lifedawn.bestweather.weathers.WeatherFragment;
import com.lifedawn.bestweather.weathers.comparison.hourlyforecast.HourlyForecastComparisonFragment;
import com.lifedawn.bestweather.weathers.dataprocessing.response.OpenWeatherMapResponseProcessor;
import com.lifedawn.bestweather.weathers.models.HourlyForecastDto;
import com.lifedawn.bestweather.weathers.detailfragment.openweathermap.hourlyforecast.OwmDetailHourlyForecastFragment;
import com.lifedawn.bestweather.weathers.simplefragment.base.BaseSimpleForecastFragment;
import com.lifedawn.bestweather.weathers.view.DateView;
import com.lifedawn.bestweather.weathers.FragmentType;
import com.lifedawn.bestweather.weathers.view.DetailSingleTemperatureView;
import com.lifedawn.bestweather.weathers.view.IconTextView;
import com.lifedawn.bestweather.weathers.view.SingleWeatherIconView;
import com.lifedawn.bestweather.weathers.view.TextsView;

import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.List;

public class OwmSimpleHourlyForecastFragment extends BaseSimpleForecastFragment {
	private OwmOneCallResponse owmOneCallResponse;
	private OwmHourlyForecastResponse owmHourlyForecastResponse;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		needCompare = true;
	}

	public void setOwmHourlyForecastResponse(OwmHourlyForecastResponse owmHourlyForecastResponse) {
		this.owmHourlyForecastResponse = owmHourlyForecastResponse;
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
				detailHourlyForecastFragment.setOneCallResponse(owmOneCallResponse);

				Bundle bundle = new Bundle();
				bundle.putString(BundleKey.AddressName.name(), addressName);
				bundle.putSerializable(BundleKey.TimeZone.name(), zoneId);
				bundle.putSerializable(BundleKey.WeatherDataSource.name(), mainWeatherDataSourceType);

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

	public OwmSimpleHourlyForecastFragment setOneCallResponse(OwmOneCallResponse owmOneCallResponse) {
		this.owmOneCallResponse = owmOneCallResponse;
		return this;
	}

	@Override
	public void setValuesToViews() {
		//owm hourly forecast simple : 날짜, 시각, 날씨, 기온, 강수확률, 강수량, 강설량
		Context context = getContext();

		List<HourlyForecastDto> hourlyForecastDtoList = null;
		if (mainWeatherDataSourceType == WeatherDataSourceType.OWM_ONECALL) {
			hourlyForecastDtoList = OpenWeatherMapResponseProcessor.makeHourlyForecastDtoListOneCall(getContext(),
					owmOneCallResponse, windUnit, tempUnit, visibilityUnit);
		} else if (mainWeatherDataSourceType == WeatherDataSourceType.OWM_INDIVIDUAL) {
			hourlyForecastDtoList = OpenWeatherMapResponseProcessor.makeHourlyForecastDtoListIndividual(getContext(),
					owmHourlyForecastResponse, windUnit, tempUnit, visibilityUnit);
		}

		final int WEATHER_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.singleWeatherIconValueRowHeightInSC);

		final int COLUMN_COUNT = hourlyForecastDtoList.size();
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
		List<Integer> tempList = new ArrayList<>();
		List<String> probabilityOfPrecipitationList = new ArrayList<>();
		List<String> rainVolumeList = new ArrayList<>();
		List<String> snowVolumeList = new ArrayList<>();

		boolean haveSnow = false;
		boolean haveRain = false;

		final String degree = "°";

		for (HourlyForecastDto item : hourlyForecastDtoList) {
			dateList.add(item.getHours());
			hourList.add(String.valueOf(item.getHours().getHour()));
			iconObjList.add(new SingleWeatherIconView.WeatherIconObj(ContextCompat.getDrawable(context, item.getWeatherIcon()), item.getWeatherDescription()));
			tempList.add(Integer.parseInt(item.getTemp().replace(degree, "")));
			probabilityOfPrecipitationList.add(item.getPop());
			rainVolumeList.add(item.getRainVolume().replace("mm", ""));

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
			snowVolumeList.add(item.getSnowVolume().replace("mm", ""));
		}
		weatherIconRow.setWeatherImgs(iconObjList);
		dateRow.init(dateList);

		popRow.setValueList(probabilityOfPrecipitationList);
		rainVolumeRow.setValueList(rainVolumeList);
		snowVolumeRow.setValueList(snowVolumeList);

		TextsView hourRow = new TextsView(context, VIEW_WIDTH, COLUMN_WIDTH, hourList);
		DetailSingleTemperatureView tempRow = new DetailSingleTemperatureView(context, tempList);
		tempRow.setLineColor(Color.WHITE);
		tempRow.setCircleColor(Color.WHITE);

		LinearLayout.LayoutParams rowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

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
			popRow.setValueTextSize(textSizeMap.get(WeatherValueType.pop));
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
			popRow.setTextColor(textColorMap.get(WeatherValueType.pop));
		}
		if (textColorMap.containsKey(WeatherValueType.rainVolume)) {
			rainVolumeRow.setTextColor(textColorMap.get(WeatherValueType.rainVolume));
		}
		if (textColorMap.containsKey(WeatherValueType.snowVolume)) {
			snowVolumeRow.setTextColor(textColorMap.get(WeatherValueType.snowVolume));
		}

		binding.forecastView.addView(dateRow, rowLayoutParams);
		binding.forecastView.addView(hourRow, rowLayoutParams);
		binding.forecastView.addView(weatherIconRow, rowLayoutParams);
		binding.forecastView.addView(popRow, rowLayoutParams);
		binding.forecastView.addView(rainVolumeRow, rowLayoutParams);
		if (haveSnow) {
			binding.forecastView.addView(snowVolumeRow, rowLayoutParams);
		}

		final int tempRowHeight = (int) context.getResources().getDimension(R.dimen.singleTemperatureRowHeightInCOMMON);

		LinearLayout.LayoutParams tempRowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				tempRowHeight);
		binding.forecastView.addView(tempRow, tempRowLayoutParams);

		createValueUnitsDescription(WeatherDataSourceType.OWM_ONECALL, haveRain, haveSnow);
	}

}