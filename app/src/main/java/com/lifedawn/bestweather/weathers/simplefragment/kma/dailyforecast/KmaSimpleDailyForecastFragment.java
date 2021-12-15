package com.lifedawn.bestweather.weathers.simplefragment.kma.dailyforecast;

import android.content.Context;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentManager;

import android.util.TypedValue;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.WeatherDataType;
import com.lifedawn.bestweather.weathers.WeatherFragment;
import com.lifedawn.bestweather.weathers.comparison.dailyforecast.DailyForecastComparisonFragment;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalDailyForecast;
import com.lifedawn.bestweather.weathers.models.DailyForecastDto;
import com.lifedawn.bestweather.weathers.detailfragment.kma.dailyforecast.KmaDetailDailyForecastFragment;
import com.lifedawn.bestweather.weathers.simplefragment.base.BaseSimpleForecastFragment;
import com.lifedawn.bestweather.weathers.view.DetailDoubleTemperatureView;
import com.lifedawn.bestweather.weathers.view.DoubleWeatherIconView;
import com.lifedawn.bestweather.weathers.FragmentType;
import com.lifedawn.bestweather.weathers.view.IconTextView;
import com.lifedawn.bestweather.weathers.view.TextsView;

import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;


public class KmaSimpleDailyForecastFragment extends BaseSimpleForecastFragment {
	private List<FinalDailyForecast> finalDailyForecastList;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		needCompare = true;

	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		binding.weatherCardViewHeader.forecastName.setText(R.string.daily_forecast);
		setValuesToViews();

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
				KmaDetailDailyForecastFragment detailDailyForecastFragment = new KmaDetailDailyForecastFragment();
				detailDailyForecastFragment.setFinalDailyForecastList(finalDailyForecastList);

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

	}

	public KmaSimpleDailyForecastFragment setFinalDailyForecastList(List<FinalDailyForecast> finalDailyForecastList) {
		this.finalDailyForecastList = finalDailyForecastList;
		return this;
	}

	@Override
	public void setValuesToViews() {
		super.setValuesToViews();
		// 날짜, 최저/최고 기온 ,낮과 밤의 날씨상태, 강수확률
		Context context = getContext();

		final int WEATHER_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.singleWeatherIconValueRowHeightInSC);
		final int TEMP_ROW_HEIGHT = (int) context.getResources().getDimension(R.dimen.doubleTemperatureRowHeightInSC);

		final int COLUMN_COUNT = finalDailyForecastList.size();
		final int COLUMN_WIDTH = (int) context.getResources().getDimension(R.dimen.valueColumnWidthInSDailyAccuKma);
		final int VIEW_WIDTH = COLUMN_COUNT * COLUMN_WIDTH;

		DoubleWeatherIconView weatherIconRow = new DoubleWeatherIconView(context, FragmentType.Simple, VIEW_WIDTH, WEATHER_ROW_HEIGHT,
				COLUMN_WIDTH);
		IconTextView probabilityOfPrecipitationRow = new IconTextView(context, FragmentType.Simple, VIEW_WIDTH,
				COLUMN_WIDTH, R.drawable.pop);

		//시각 --------------------------------------------------------------------------
		List<String> dateList = new ArrayList<>();
		List<DoubleWeatherIconView.WeatherIconObj> weatherIconObjList = new ArrayList<>();
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("M.d\nE", Locale.getDefault());
		final String degree = "°";
		//기온, 강수확률, 강수량
		List<Integer> minTempList = new ArrayList<>();
		List<Integer> maxTempList = new ArrayList<>();
		List<String> probabilityOfPrecipitationList = new ArrayList<>();

		List<DailyForecastDto> dailyForecastDtoList = KmaResponseProcessor.makeDailyForecastDtoList(getContext(), finalDailyForecastList,
				tempUnit);

		for (DailyForecastDto dailyForecastDto : dailyForecastDtoList) {
			dateList.add(dailyForecastDto.getDate().format(dateTimeFormatter));
			minTempList.add(Integer.parseInt(dailyForecastDto.getMinTemp().replace(degree, "")));
			maxTempList.add(Integer.parseInt(dailyForecastDto.getMaxTemp().replace(degree, "")));

			if (dailyForecastDto.isSingle()) {
				probabilityOfPrecipitationList.add(dailyForecastDto.getSingleValues().getPop());
				weatherIconObjList.add(new DoubleWeatherIconView.WeatherIconObj(ContextCompat.getDrawable(context,
						dailyForecastDto.getSingleValues().getWeatherIcon()), dailyForecastDto.getSingleValues().getWeatherDescription()));
			} else {
				probabilityOfPrecipitationList.add(
						dailyForecastDto.getAmValues().getPop() + "/" + dailyForecastDto.getPmValues().getPop());
				weatherIconObjList.add(new DoubleWeatherIconView.WeatherIconObj(ContextCompat.getDrawable(context,
						dailyForecastDto.getAmValues().getWeatherIcon()),
						ContextCompat.getDrawable(context, dailyForecastDto.getAmValues().getWeatherIcon()),
						dailyForecastDto.getAmValues().getWeatherDescription(),
						dailyForecastDto.getPmValues().getWeatherDescription()));
			}
		}
		weatherIconRow.setIcons(weatherIconObjList);
		probabilityOfPrecipitationRow.setValueList(probabilityOfPrecipitationList);

		TextsView dateRow = new TextsView(context, VIEW_WIDTH, COLUMN_WIDTH, dateList);
		DetailDoubleTemperatureView tempRow = new DetailDoubleTemperatureView(getContext(), FragmentType.Simple, VIEW_WIDTH,
				TEMP_ROW_HEIGHT, COLUMN_WIDTH, minTempList, maxTempList);

		LinearLayout.LayoutParams rowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);

		LinearLayout.LayoutParams dateRowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		dateRowLayoutParams.bottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, getResources().getDisplayMetrics());

		if (textSizeMap.containsKey(WeatherDataType.date)) {
			dateRow.setValueTextSize(textSizeMap.get(WeatherDataType.date));
		}
		if (textSizeMap.containsKey(WeatherDataType.pop)) {
			probabilityOfPrecipitationRow.setValueTextSize(textSizeMap.get(WeatherDataType.pop));
		}
		if (textSizeMap.containsKey(WeatherDataType.temp)) {
			tempRow.setTempTextSize(textSizeMap.get(WeatherDataType.temp));
		}

		if (textColorMap.containsKey(WeatherDataType.date)) {
			dateRow.setValueTextColor(textColorMap.get(WeatherDataType.date));
		}
		if (textColorMap.containsKey(WeatherDataType.pop)) {
			probabilityOfPrecipitationRow.setTextColor(textColorMap.get(WeatherDataType.pop));
		}
		if (textColorMap.containsKey(WeatherDataType.temp)) {
			tempRow.setTextColor(textColorMap.get(WeatherDataType.temp));
		}

		binding.forecastView.addView(dateRow, dateRowLayoutParams);
		binding.forecastView.addView(weatherIconRow, rowLayoutParams);
		binding.forecastView.addView(probabilityOfPrecipitationRow, rowLayoutParams);


		LinearLayout.LayoutParams tempRowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		tempRowLayoutParams.topMargin = (int) getResources().getDimension(R.dimen.iconValueViewMargin);
		binding.forecastView.addView(tempRow, tempRowLayoutParams);


/*
		FragmentContainerView fragmentContainerView = new FragmentContainerView(getContext());
		fragmentContainerView.setClipChildren(false);
		fragmentContainerView.setId(R.id.fragment_container);

		FrameLayout.LayoutParams fragmentContainerLayoutParams = new FrameLayout.LayoutParams(VIEW_WIDTH - COLUMN_WIDTH,
				(int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 100f, getResources().getDisplayMetrics()));

		binding.forecastView.addView(fragmentContainerView, fragmentContainerLayoutParams);

		CubicLineChartFragment cubicLineChartFragment = new CubicLineChartFragment();
		Bundle bundle = new Bundle();
		bundle.putIntegerArrayList("minTempList", (ArrayList<Integer>) minTempList);
		bundle.putIntegerArrayList("maxTempList", (ArrayList<Integer>) maxTempList);

		cubicLineChartFragment.setArguments(bundle);
		getChildFragmentManager().beginTransaction().add(R.id.fragment_container, cubicLineChartFragment).commit();

 */
	}
}