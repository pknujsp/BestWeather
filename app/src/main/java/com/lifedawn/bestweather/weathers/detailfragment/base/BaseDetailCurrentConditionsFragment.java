package com.lifedawn.bestweather.weathers.detailfragment.base;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.gridlayout.widget.GridLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.preference.PreferenceManager;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.databinding.BaseLayoutDetailCurrentConditionsBinding;
import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.weathers.simplefragment.interfaces.IWeatherValues;
import com.lifedawn.bestweather.weathers.viewmodels.WeatherFragmentViewModel;

import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;

public class BaseDetailCurrentConditionsFragment extends Fragment implements IWeatherValues {
	protected BaseLayoutDetailCurrentConditionsBinding binding;
	protected LayoutInflater layoutInflater;
	protected ValueUnits tempUnit;
	protected ValueUnits windUnit;
	protected ValueUnits visibilityUnit;
	protected ValueUnits clockUnit;
	protected Double latitude;
	protected Double longitude;
	protected ZoneId zoneId;
	protected WeatherProviderType mainWeatherProviderType;
	protected Bundle bundle;
	protected WeatherFragmentViewModel weatherFragmentViewModel;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		bundle = savedInstanceState != null ? savedInstanceState : getArguments();

		zoneId = (ZoneId) bundle.getSerializable(BundleKey.TimeZone.name());
		latitude = bundle.getDouble(BundleKey.Latitude.name());
		longitude = bundle.getDouble(BundleKey.Longitude.name());
		mainWeatherProviderType = (WeatherProviderType) bundle.getSerializable(
				BundleKey.WeatherProvider.name());

		tempUnit = MyApplication.VALUE_UNIT_OBJ.getTempUnit();
		windUnit = MyApplication.VALUE_UNIT_OBJ.getWindUnit();
		visibilityUnit = MyApplication.VALUE_UNIT_OBJ.getVisibilityUnit();
		clockUnit = MyApplication.VALUE_UNIT_OBJ.getClockUnit();
		weatherFragmentViewModel = new ViewModelProvider(requireParentFragment()).get(WeatherFragmentViewModel.class);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = BaseLayoutDetailCurrentConditionsBinding.inflate(inflater);
		binding.weatherCardViewHeader.forecastName.setText(R.string.current_conditions);
		binding.weatherCardViewHeader.compareForecast.setVisibility(View.GONE);
		binding.weatherCardViewHeader.detailForecast.setVisibility(View.GONE);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		layoutInflater = getLayoutInflater();
	}

	@Override
	public void onDestroy() {
		binding.conditionsGrid.removeAllViews();
		super.onDestroy();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		layoutInflater = null;
		binding = null;
	}

	@Override
	public void setValuesToViews() {

	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putAll(bundle);
	}

	protected final View addGridItem(int labelDescriptionId, String value, @Nullable Integer labelIconId) {
		View gridItem = layoutInflater.inflate(R.layout.view_detail_weather_data_item, null, false);

		((TextView) gridItem.findViewById(R.id.label)).setText(labelDescriptionId);
		((TextView) gridItem.findViewById(R.id.value)).setText(value);
		if (labelIconId == null) {
			gridItem.findViewById(R.id.label_icon).setVisibility(View.GONE);
		} else {
			((ImageView) gridItem.findViewById(R.id.label_icon)).setImageResource(labelIconId);
		}

		((TextView) gridItem.findViewById(R.id.value)).setTextColor(Color.WHITE);

		int cellCount = binding.conditionsGrid.getChildCount();
		int row = cellCount / binding.conditionsGrid.getColumnCount();
		int column = cellCount % binding.conditionsGrid.getColumnCount();

		GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();

		layoutParams.columnSpec = GridLayout.spec(column, GridLayout.FILL, 1);
		layoutParams.rowSpec = GridLayout.spec(row, GridLayout.FILL, 1);

		binding.conditionsGrid.addView(gridItem, layoutParams);


		return gridItem;
	}


	protected final View addGridItem(int labelDescriptionId, String value, @Nullable Integer labelIconId, boolean defaultIconColor) {
		View gridItem = addGridItem(labelDescriptionId, value, labelIconId);
		if (!defaultIconColor)
			((ImageView) gridItem.findViewById(R.id.label_icon)).setImageTintList(ColorStateList.valueOf(Color.WHITE));
		return gridItem;
	}

}