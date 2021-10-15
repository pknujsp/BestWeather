package com.lifedawn.bestweather.weathers.detailfragment.base;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.bumptech.glide.Glide;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.databinding.BaseLayoutDetailCurrentConditionsBinding;
import com.lifedawn.bestweather.databinding.BaseLayoutSimpleCurrentConditionsBinding;
import com.lifedawn.bestweather.retrofit.responses.aqicn.GeolocalizedFeedResponse;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.detailfragment.dto.GridItemDto;
import com.lifedawn.bestweather.weathers.simplefragment.base.BaseSimpleCurrentConditionsFragment;
import com.lifedawn.bestweather.weathers.simplefragment.interfaces.IWeatherValues;

import org.jetbrains.annotations.NotNull;

import java.util.Objects;

public class BaseDetailCurrentConditionsFragment extends Fragment implements IWeatherValues {
	protected BaseLayoutDetailCurrentConditionsBinding binding;
	protected LayoutInflater layoutInflater;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = BaseLayoutDetailCurrentConditionsBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.weatherCardViewHeader.forecastName.setText(R.string.current_conditions);
		binding.weatherCardViewHeader.compareForecast.setVisibility(View.GONE);
		layoutInflater = getLayoutInflater();
	}

	@Override
	public void setValuesToViews() {

	}

	protected final View addGridItem(int labelStrId, String value, int imgId) {
		View gridItem = layoutInflater.inflate(R.layout.current_conditions_detail_item, null);

		((ImageView) gridItem.findViewById(R.id.label_icon)).setImageResource(imgId);
		((TextView) gridItem.findViewById(R.id.label)).setText(labelStrId);
		((TextView) gridItem.findViewById(R.id.value)).setText(value);
		if (imgId == 0) {
			gridItem.findViewById(R.id.value_img).setVisibility(View.GONE);
		} else {
			((ImageView) gridItem.findViewById(R.id.value_img)).setImageResource(imgId);
		}
		binding.conditionsGrid.addView(gridItem);
		return gridItem;
	}

}