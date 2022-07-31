package com.lifedawn.bestweather.weathers.detailfragment.base;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.WeatherDataType;
import com.lifedawn.bestweather.weathers.detailfragment.base.dialogfragments.DetailHourlyForecastDialogFragment;
import com.lifedawn.bestweather.weathers.models.HourlyForecastDto;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.List;

public class BaseDetailHourlyForecastFragment extends BaseDetailForecastFragment {
	protected static List<HourlyForecastDto> hourlyForecastDtoList;

	public static void setHourlyForecastDtoList(List<HourlyForecastDto> hourlyForecastDtoList) {
		BaseDetailHourlyForecastFragment.hourlyForecastDtoList = hourlyForecastDtoList;
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		binding.toolbar.extraBtn.setText(R.string.show_data_type);
		binding.toolbar.extraBtn.setVisibility(View.VISIBLE);
		binding.toolbar.extraBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final CharSequence[] items = getResources().getTextArray(R.array.DetailHourlyForecastShowDataType);
				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
				final int selectedItem = sharedPreferences.getInt(getString(R.string.pref_key_detail_hourly_forecast_show_data), 0);

				new MaterialAlertDialogBuilder(getActivity())
						.setSingleChoiceItems(items, selectedItem, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								sharedPreferences.edit().putInt(getString(R.string.pref_key_detail_hourly_forecast_show_data), which).commit();

								if (selectedItem != which) {
									setDataViewsByList();
								}
								dialog.dismiss();
							}
						}).setTitle(R.string.show_data_type).create().show();

			}
		});

	}

	@Override
	public void onClickedItem(Integer position) {
		if (clickableItem) {
			clickableItem = false;

			Bundle bundle = new Bundle();
			bundle.putInt("FirstSelectedPosition", position);

			DetailHourlyForecastDialogFragment detailHourlyForecastDialogFragment =
					new DetailHourlyForecastDialogFragment();
			DetailHourlyForecastDialogFragment.setHourlyForecastDtoList(hourlyForecastDtoList);
			detailHourlyForecastDialogFragment.setArguments(bundle);

			detailHourlyForecastDialogFragment.show(getChildFragmentManager(),
					DetailHourlyForecastDialogFragment.class.getName());
		}
	}

	@Override
	protected void onFragmentStarted(Fragment fragment) {
		if (fragment instanceof DetailHourlyForecastDialogFragment) {
			super.onFragmentStarted(fragment);
		}
	}

	@Override
	protected void setDataViewsByList() {

	}

	@Override
	protected void setDataViewsByTable() {

	}
}
