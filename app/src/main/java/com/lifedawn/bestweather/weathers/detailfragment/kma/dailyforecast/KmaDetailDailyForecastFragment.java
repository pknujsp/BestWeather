package com.lifedawn.bestweather.weathers.detailfragment.kma.dailyforecast;

import android.content.Context;
import android.os.Bundle;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.interfaces.OnClickedListViewItemListener;
import com.lifedawn.bestweather.weathers.dataprocessing.response.KmaResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.finaldata.kma.FinalDailyForecast;
import com.lifedawn.bestweather.weathers.detailfragment.base.BaseDetailForecastFragment;
import com.lifedawn.bestweather.weathers.view.DetailDoubleTemperatureView;
import com.lifedawn.bestweather.weathers.view.DoubleWeatherIconView;
import com.lifedawn.bestweather.weathers.view.FragmentType;
import com.lifedawn.bestweather.weathers.view.TextValueView;
import com.lifedawn.bestweather.weathers.view.SingleWeatherIconView;

import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class KmaDetailDailyForecastFragment extends BaseDetailForecastFragment {
	private List<FinalDailyForecast> finalDailyForecastList;

	@Override
	public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.toolbar.fragmentTitle.setText(R.string.detail_daily_forecast);
	}

	public KmaDetailDailyForecastFragment setFinalDailyForecastList(List<FinalDailyForecast> finalDailyForecastList) {
		this.finalDailyForecastList = finalDailyForecastList;
		return this;
	}

	@Override
	protected void setDataViewsByList() {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				String tempDegree = getString(R.string.degree_symbol);
				String percent = ValueUnits.convertToStr(getContext(), ValueUnits.percent);
				DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(getString(R.string.date_pattern));
				List<DailyForecastListItemObj> dailyForecastListItemObjs = new ArrayList<>();

				int index = 0;
				for (FinalDailyForecast finalDailyForecast : finalDailyForecastList) {
					DailyForecastListItemObj item = new DailyForecastListItemObj();
					item.setDateTime(finalDailyForecast.getDate().format(dateTimeFormatter))
							.setMinTemp(ValueUnits.convertTemperature(finalDailyForecast.getMinTemp(), tempUnit) + tempDegree)
							.setMaxTemp(ValueUnits.convertTemperature(finalDailyForecast.getMaxTemp(), tempUnit) + tempDegree);

					if (index++ > 4) {
						item.setPop(finalDailyForecast.getProbabilityOfPrecipitation() + percent)
								.setSingle(true)
								.setLeftWeatherIconId(KmaResponseProcessor.getWeatherMidIconImg(finalDailyForecast.getSky(), false));
					} else {
						item.setPop(finalDailyForecast.getAmProbabilityOfPrecipitation() + percent + "/" + finalDailyForecast.getPmProbabilityOfPrecipitation() + percent)
								.setSingle(false)
								.setLeftWeatherIconId(KmaResponseProcessor.getWeatherMidIconImg(finalDailyForecast.getAmSky(), false))
								.setRightWeatherIconId(KmaResponseProcessor.getWeatherMidIconImg(finalDailyForecast.getPmSky(), true));
					}
					dailyForecastListItemObjs.add(item);
				}

				if (getActivity() != null) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							DailyForecastListAdapter adapter = new DailyForecastListAdapter(getContext(), new OnClickedListViewItemListener<Integer>() {
								@Override
								public void onClickedItem(Integer position) {

								}
							});
							adapter.setDailyForecastListItemObjs(dailyForecastListItemObjs);
							binding.listview.setLayoutManager(new LinearLayoutManager(getContext(), RecyclerView.VERTICAL, false));
							binding.listview.addItemDecoration(new DividerItemDecoration(getContext(), DividerItemDecoration.VERTICAL));
							binding.listview.setAdapter(adapter);
						}
					});
				}
			}
		});
	}

	@Override
	protected void setDataViewsByTable() {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				//kma daily forecast detail :
				//날짜, 오전/오후 날씨상태, 강수확률, 최저/최고기온
				binding.forecastView.removeAllViews();
				binding.labels.removeAllViews();

				Context context = getContext();

				final int dateRowHeight = (int) getResources().getDimension(R.dimen.dateValueRowHeightInCOMMON);
				final int weatherRowHeight = (int) getResources().getDimension(R.dimen.singleWeatherIconValueRowHeightInD);
				final int tempRowHeight = (int) getResources().getDimension(R.dimen.doubleTemperatureRowHeightInD);
				final int defaultTextRowHeight = (int) getResources().getDimension(R.dimen.defaultValueRowHeightInD);

				final int columnsCount = finalDailyForecastList.size();
				final int columnWidth = (int) getResources().getDimension(R.dimen.valueColumnWidthInDDaily);
				final int viewWidth = columnsCount * columnWidth;


				TextValueView dateRow = new TextValueView(context, FragmentType.Detail, viewWidth, dateRowHeight, columnWidth);
				DoubleWeatherIconView weatherIconRow = new DoubleWeatherIconView(context, FragmentType.Detail, viewWidth, weatherRowHeight,
						columnWidth);
				TextValueView probabilityOfPrecipitationRow = new TextValueView(context, FragmentType.Detail, viewWidth, defaultTextRowHeight,
						columnWidth);

				//날짜, 시각 --------------------------------------------------------------------------
				List<String> dateList = new ArrayList<>();
				DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern(getString(R.string.date_pattern));
				for (FinalDailyForecast finalDailyForecast : finalDailyForecastList) {
					dateList.add(finalDailyForecast.getDate().format(dateTimeFormatter));
				}
				dateRow.setValueList(dateList);
				//오전/오후 날씨상태, 강수확률, 최저/최고기온
				List<Integer> minTempList = new ArrayList<>();
				List<Integer> maxTempList = new ArrayList<>();
				List<String> probabilityOfPrecipitationList = new ArrayList<>();
				List<DoubleWeatherIconView.WeatherIconObj> weatherIconObjList = new ArrayList<>();

				int index = 0;
				String pop = null;

				for (FinalDailyForecast finalDailyForecast : finalDailyForecastList) {
					minTempList.add(ValueUnits.convertTemperature(finalDailyForecast.getMinTemp(), tempUnit));
					maxTempList.add(ValueUnits.convertTemperature(finalDailyForecast.getMaxTemp(), tempUnit));

					if (index++ > 4) {
						pop = finalDailyForecast.getProbabilityOfPrecipitation();
						weatherIconObjList.add(new DoubleWeatherIconView.WeatherIconObj(
								ContextCompat.getDrawable(context, KmaResponseProcessor.getWeatherMidIconImg(finalDailyForecast.getSky(), false))));
					} else {
						pop = finalDailyForecast.getAmProbabilityOfPrecipitation() + " / " + finalDailyForecast.getPmProbabilityOfPrecipitation();
						weatherIconObjList.add(new DoubleWeatherIconView.WeatherIconObj(
								ContextCompat.getDrawable(context, KmaResponseProcessor.getWeatherMidIconImg(finalDailyForecast.getAmSky(), false)),
								ContextCompat.getDrawable(context,
										KmaResponseProcessor.getWeatherMidIconImg(finalDailyForecast.getPmSky(), true))));
					}
					probabilityOfPrecipitationList.add(pop);
				}

				weatherIconRow.setIcons(weatherIconObjList);
				DetailDoubleTemperatureView tempRow = new DetailDoubleTemperatureView(context, FragmentType.Detail, viewWidth, tempRowHeight,
						columnWidth, minTempList, maxTempList);
				probabilityOfPrecipitationRow.setValueList(probabilityOfPrecipitationList);


				if (getActivity() != null) {
					getActivity().runOnUiThread(new Runnable() {
						@Override
						public void run() {
							addLabelView(R.drawable.date, getString(R.string.date), dateRowHeight);
							addLabelView(R.drawable.day_clear, getString(R.string.weather), weatherRowHeight);
							addLabelView(R.drawable.pop, getString(R.string.probability_of_precipitation), defaultTextRowHeight);
							addLabelView(R.drawable.temperature, getString(R.string.temperature), tempRowHeight);

							LinearLayout.LayoutParams rowLayoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
									ViewGroup.LayoutParams.WRAP_CONTENT);
							rowLayoutParams.gravity = Gravity.CENTER;

							binding.forecastView.addView(dateRow, rowLayoutParams);
							binding.forecastView.addView(weatherIconRow, rowLayoutParams);
							binding.forecastView.addView(probabilityOfPrecipitationRow, rowLayoutParams);
							binding.forecastView.addView(tempRow, rowLayoutParams);
						}
					});
				}
			}
		});
	}

}
