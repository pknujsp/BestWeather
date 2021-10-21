package com.lifedawn.bestweather.weathers.detailfragment.aqicn;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.databinding.FragmentAirQualityDetailBinding;
import com.lifedawn.bestweather.retrofit.responses.aqicn.GeolocalizedFeedResponse;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.simplefragment.aqicn.AirQualityForecastObj;
import com.lifedawn.bestweather.weathers.simplefragment.interfaces.IWeatherValues;
import com.lifedawn.bestweather.weathers.view.AirQualityBarView;
import com.lifedawn.bestweather.weathers.view.TextValueView;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class DetailAirQualityFragment extends Fragment implements IWeatherValues {
	private GeolocalizedFeedResponse response;
	private FragmentAirQualityDetailBinding binding;

	public void setResponse(GeolocalizedFeedResponse response) {
		this.response = response;
	}

	@Override
	public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Nullable
	@org.jetbrains.annotations.Nullable
	@Override
	public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		binding = FragmentAirQualityDetailBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.toolbar.backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getParentFragmentManager().popBackStackImmediate();
			}
		});

		binding.toolbar.fragmentTitle.setText(R.string.detail_air_quality);
		setValuesToViews();
	}

	@Override
	public void setValuesToViews() {
		List<AirQualityForecastObj> airQualityForecastObjList = AqicnResponseProcessor.getAirQualityForecastObjList(response);

		final int columnWidth = (int) getResources().getDimension(R.dimen.columnWidthInAirQualityBarView);
		final int viewHeight = (int) getResources().getDimension(R.dimen.viewHeightOfAirQualityBarView);
		final int columnsCount = airQualityForecastObjList.size();
		final int viewWidth = columnWidth * columnsCount;

		List<AirQualityBarView.AirQualityObj> pm10AirQualityObjList = new ArrayList<>();
		List<AirQualityBarView.AirQualityObj> pm25AirQualityObjList = new ArrayList<>();
		List<AirQualityBarView.AirQualityObj> o3AirQualityObjList = new ArrayList<>();

		List<String> dateList = new ArrayList<>();

		pm10AirQualityObjList.add(new AirQualityBarView.AirQualityObj(response.getData().getIaqi().getPm10().getValue()));
		pm25AirQualityObjList.add(new AirQualityBarView.AirQualityObj(response.getData().getIaqi().getPm25().getValue()));
		o3AirQualityObjList.add(new AirQualityBarView.AirQualityObj(response.getData().getIaqi().getO3().getValue()));
		dateList.add(getString(R.string.current));

		SimpleDateFormat dateFormat = new SimpleDateFormat("M.d E", Locale.getDefault());

		for (AirQualityForecastObj airQualityForecastObj : airQualityForecastObjList) {
			dateList.add(dateFormat.format(airQualityForecastObj.date));
			pm10AirQualityObjList.add(new AirQualityBarView.AirQualityObj(airQualityForecastObj.pm10));
			pm25AirQualityObjList.add(new AirQualityBarView.AirQualityObj(airQualityForecastObj.pm25));
			o3AirQualityObjList.add(new AirQualityBarView.AirQualityObj(airQualityForecastObj.o3));
		}

		TextValueView dateRow = new TextValueView(getContext(), viewWidth, viewHeight, columnWidth);
		AirQualityBarView pm10BarView = new AirQualityBarView(getContext(), viewWidth, viewHeight, columnWidth, pm10AirQualityObjList);
		AirQualityBarView pm25BarView = new AirQualityBarView(getContext(), viewWidth, viewHeight, columnWidth, pm25AirQualityObjList);
		AirQualityBarView o3BarView = new AirQualityBarView(getContext(), viewWidth, viewHeight, columnWidth, o3AirQualityObjList);

		dateRow.setValueList(dateList);

		binding.forecastView.addView(dateRow);
		binding.forecastView.addView(pm10BarView);
		binding.forecastView.addView(pm25BarView);
		binding.forecastView.addView(o3BarView);
	}
}
