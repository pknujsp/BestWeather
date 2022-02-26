package com.lifedawn.bestweather.weathers.simplefragment.base;

import android.graphics.Color;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.NetworkStatus;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.commons.enums.WeatherValueType;
import com.lifedawn.bestweather.databinding.BaseLayoutSimpleForecastBinding;
import com.lifedawn.bestweather.weathers.simplefragment.interfaces.IWeatherValues;
import com.lifedawn.bestweather.weathers.view.DateView;

import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.Map;

public class BaseSimpleForecastFragment extends Fragment implements IWeatherValues {
	protected BaseLayoutSimpleForecastBinding binding;
	protected DateView dateRow;
	protected String countryCode;
	protected WeatherProviderType mainWeatherProviderType;
	protected boolean needCompare;
	protected Map<WeatherValueType, Integer> textSizeMap = new HashMap<>();
	protected Map<WeatherValueType, Integer> textColorMap = new HashMap<>();
	protected Integer cardBackgroundColor;
	protected NetworkStatus networkStatus;
	protected Bundle bundle;

	protected int headerVisibility = View.VISIBLE;


	public void setTextSizeMap(Map<WeatherValueType, Integer> textSizeMap) {
		this.textSizeMap = textSizeMap;
	}

	public void setTextColorMap(Map<WeatherValueType, Integer> textColorMap) {
		this.textColorMap = textColorMap;
	}

	public void setCardBackgroundColor(Integer cardBackgroundColor) {
		this.cardBackgroundColor = cardBackgroundColor;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		bundle = savedInstanceState != null ? savedInstanceState : getArguments();

		countryCode = bundle.getString(BundleKey.CountryCode.name());
		mainWeatherProviderType = (WeatherProviderType) bundle.getSerializable(BundleKey.WeatherProvider.name());

		networkStatus = NetworkStatus.getInstance(getContext());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = BaseLayoutSimpleForecastBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if (!countryCode.equals("KR")) {
			binding.weatherCardViewHeader.compareForecast.setVisibility(View.GONE);
		} else {
			binding.weatherCardViewHeader.compareForecast.setVisibility(View.VISIBLE);
		}

		if (cardBackgroundColor != null) {
			binding.card.setBackgroundColor(cardBackgroundColor);
		}
		binding.weatherCardViewHeader.getRoot().setVisibility(headerVisibility);

		binding.scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
			@Override
			public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
				if (dateRow != null) {
					dateRow.reDraw(scrollX);
				}
			}
		});

	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putAll(bundle);
	}

	@Override
	public void setValuesToViews() {

	}

	protected boolean availableNetwork() {
		if (networkStatus.networkAvailable()) {
			return true;
		} else {
			Toast.makeText(getContext(), R.string.disconnected_network, Toast.LENGTH_SHORT).show();
			return false;
		}
	}

	protected void createValueUnitsDescription(WeatherProviderType weatherProviderType, boolean haveRain, boolean haveSnow) {
		binding.extraView.removeAllViews();

		if (haveRain || haveSnow) {
			String rainUnit = "mm";
			String snowUnit = null;

			if (weatherProviderType == WeatherProviderType.OWM_ONECALL) {
				snowUnit = "mm";
			} else {
				snowUnit = "cm";
			}

			StringBuilder stringBuilder = new StringBuilder();

			if (haveRain) {
				stringBuilder.append(getString(R.string.rain)).append(" : ").append(rainUnit);
			}
			if (haveSnow) {
				if (stringBuilder.length() > 0) {
					stringBuilder.append(", ");
				}
				stringBuilder.append(getString(R.string.snow)).append(" : ").append(snowUnit);
			}

			TextView textView = new TextView(getContext());
			FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			layoutParams.gravity = Gravity.RIGHT;
			textView.setLayoutParams(layoutParams);
			textView.setTextColor(Color.GRAY);
			textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
			textView.setText(stringBuilder.toString());
			textView.setIncludeFontPadding(false);

			binding.extraView.addView(textView);
			binding.extraView.setVisibility(View.VISIBLE);
		} else {
			binding.extraView.setVisibility(View.GONE);
		}
	}
}