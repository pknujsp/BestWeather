package com.lifedawn.bestweather.weathers.simplefragment.aqicn;

import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.databinding.FragmentAirQualitySimpleBinding;
import com.lifedawn.bestweather.retrofit.responses.aqicn.GeolocalizedFeedResponse;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.detailfragment.aqicn.DetailAirQualityFragment;
import com.lifedawn.bestweather.weathers.simplefragment.interfaces.IWeatherValues;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Locale;


public class SimpleAirQualityFragment extends Fragment implements IWeatherValues {
	private FragmentAirQualitySimpleBinding binding;
	private GeolocalizedFeedResponse geolocalizedFeedResponse;
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}
	
	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = FragmentAirQualitySimpleBinding.inflate(inflater);
		return binding.getRoot();
	}
	
	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		
		binding.weatherCardViewHeader.forecastName.setText(R.string.air_quality);
		binding.weatherCardViewHeader.compareForecast.setVisibility(View.GONE);
		binding.weatherCardViewHeader.detailForecast.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				DetailAirQualityFragment detailAirQualityFragment = new DetailAirQualityFragment();
				detailAirQualityFragment.setResponse(geolocalizedFeedResponse);
				
				String tag = getString(R.string.tag_detail_air_quality_fragment);
				FragmentManager fragmentManager = getParentFragment().getParentFragment().getParentFragmentManager();
				fragmentManager.beginTransaction().hide(
						fragmentManager.findFragmentByTag(getString(R.string.tag_weather_main_fragment))).add(R.id.fragment_container,
						detailAirQualityFragment, tag).addToBackStack(tag).commit();
			}
		});
		
		setValuesToViews();
	}
	
	public SimpleAirQualityFragment setGeolocalizedFeedResponse(GeolocalizedFeedResponse geolocalizedFeedResponse) {
		this.geolocalizedFeedResponse = geolocalizedFeedResponse;
		return this;
	}
	
	@Override
	public void setValuesToViews() {
		Integer pm10Val = null;
		Integer pm25Val = null;
		Integer coVal = null;
		Integer no2Val = null;
		Integer o3Val = null;
		Integer so2Val = null;
		
		if (geolocalizedFeedResponse.getData().getIaqi().getPm10() == null) {
			binding.pm10.setText(R.string.not_data);
		} else {
			pm10Val = (int) Double.parseDouble(geolocalizedFeedResponse.getData().getIaqi().getPm10().getValue());
			binding.pm10.setTextColor(AqicnResponseProcessor.getGradeColorId(pm10Val.intValue()));
			binding.pm10.setText(AqicnResponseProcessor.getGradeDescription(pm10Val.intValue()));
		}
		if (geolocalizedFeedResponse.getData().getIaqi().getPm25() == null) {
			binding.pm25.setText(R.string.not_data);
		} else {
			pm25Val = (int) Double.parseDouble(geolocalizedFeedResponse.getData().getIaqi().getPm25().getValue());
			binding.pm25.setTextColor(AqicnResponseProcessor.getGradeColorId(pm25Val.intValue()));
			binding.pm25.setText(AqicnResponseProcessor.getGradeDescription(pm25Val.intValue()));
		}
		if (geolocalizedFeedResponse.getData().getIaqi().getCo() == null) {
			binding.co.setText(R.string.not_data);
		} else {
			coVal = (int) Double.parseDouble(geolocalizedFeedResponse.getData().getIaqi().getCo().getValue());
			binding.co.setTextColor(AqicnResponseProcessor.getGradeColorId(coVal.intValue()));
			binding.co.setText(AqicnResponseProcessor.getGradeDescription(coVal.intValue()));
		}
		if (geolocalizedFeedResponse.getData().getIaqi().getNo2() == null) {
			binding.no2.setText(R.string.not_data);
		} else {
			no2Val = (int) Double.parseDouble(geolocalizedFeedResponse.getData().getIaqi().getNo2().getValue());
			binding.no2.setTextColor(AqicnResponseProcessor.getGradeColorId(no2Val.intValue()));
			binding.no2.setText(AqicnResponseProcessor.getGradeDescription(no2Val.intValue()));
		}
		if (geolocalizedFeedResponse.getData().getIaqi().getO3() == null) {
			binding.o3.setText(R.string.not_data);
		} else {
			o3Val = (int) Double.parseDouble(geolocalizedFeedResponse.getData().getIaqi().getO3().getValue());
			binding.o3.setTextColor(AqicnResponseProcessor.getGradeColorId(o3Val.intValue()));
			binding.o3.setText(AqicnResponseProcessor.getGradeDescription(o3Val.intValue()));
		}
		if (geolocalizedFeedResponse.getData().getIaqi().getSo2() == null) {
			binding.so2.setText(R.string.not_data);
		} else {
			so2Val = (int) Double.parseDouble(geolocalizedFeedResponse.getData().getIaqi().getSo2().getValue());
			binding.so2.setTextColor(AqicnResponseProcessor.getGradeColorId(so2Val.intValue()));
			binding.so2.setText(AqicnResponseProcessor.getGradeDescription(so2Val.intValue()));
		}
		
		
		SimpleDateFormat dateFormat = new SimpleDateFormat("M.d E", Locale.getDefault());
		LayoutInflater layoutInflater = LayoutInflater.from(getContext());
		
		String notData = getString(R.string.not_data);
		View labelView = layoutInflater.inflate(R.layout.air_quality_simple_forecast_item, null);
		((TextView) labelView.findViewById(R.id.date)).setText(null);
		((TextView) labelView.findViewById(R.id.pm10)).setText(getString(R.string.pm10_str));
		((TextView) labelView.findViewById(R.id.pm25)).setText(getString(R.string.pm25_str));
		((TextView) labelView.findViewById(R.id.o3)).setText(getString(R.string.o3_str));
		
		List<AirQualityForecastObj> forecastObjList = AqicnResponseProcessor.getAirQualityForecastObjList(geolocalizedFeedResponse);
		for (AirQualityForecastObj forecastObj : forecastObjList) {
			View forecastItemView = layoutInflater.inflate(R.layout.air_quality_simple_forecast_item, null);
			((TextView) forecastItemView.findViewById(R.id.date)).setText(dateFormat.format(forecastObj.date));
			((TextView) forecastItemView.findViewById(R.id.pm10)).setText(forecastObj.pm10Str == null ? notData : forecastObj.pm10Str);
			((TextView) forecastItemView.findViewById(R.id.pm25)).setText(forecastObj.pm25Str == null ? notData : forecastObj.pm25Str);
			((TextView) forecastItemView.findViewById(R.id.o3)).setText(forecastObj.o3Str == null ? notData : forecastObj.o3Str);
			
			binding.forecast.addView(forecastItemView);
		}
	}
	
	
}