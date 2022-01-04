package com.lifedawn.bestweather.weathers.simplefragment.base;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.Typeface;
import android.net.ConnectivityManager;
import android.net.Network;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.preference.PreferenceManager;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.NetworkStatus;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.databinding.BaseLayoutSimpleForecastBinding;
import com.lifedawn.bestweather.theme.AppTheme;
import com.lifedawn.bestweather.weathers.simplefragment.interfaces.IWeatherValues;
import com.lifedawn.bestweather.weathers.view.DateView;

import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.TimeZone;

public class BaseSimpleForecastFragment extends Fragment implements IWeatherValues {
	protected BaseLayoutSimpleForecastBinding binding;
	protected DateView dateRow;
	protected SharedPreferences sharedPreferences;
	protected ValueUnits tempUnit;
	protected ValueUnits windUnit;
	protected ValueUnits visibilityUnit;
	protected ValueUnits clockUnit;
	protected Double latitude;
	protected Double longitude;
	protected String addressName;
	protected String countryCode;
	protected WeatherSourceType mainWeatherSourceType;
	protected ZoneId zoneId;
	protected NetworkStatus networkStatus;
	protected boolean needCompare;
	protected Map<WeatherDataType, Integer> textSizeMap = new HashMap<>();
	protected Map<WeatherDataType, Integer> textColorMap = new HashMap<>();
	protected Integer cardBackgroundColor;

	protected int headerVisibility = View.VISIBLE;

	public void setHeaderVisibility(int headerVisibility) {
		this.headerVisibility = headerVisibility;
	}

	public void setTextSizeMap(Map<WeatherDataType, Integer> textSizeMap) {
		this.textSizeMap = textSizeMap;
	}

	public void setTextColorMap(Map<WeatherDataType, Integer> textColorMap) {
		this.textColorMap = textColorMap;
	}

	public void setCardBackgroundColor(Integer cardBackgroundColor) {
		this.cardBackgroundColor = cardBackgroundColor;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

		tempUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_temp), ValueUnits.celsius.name()));
		windUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_wind), ValueUnits.mPerSec.name()));
		visibilityUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_visibility), ValueUnits.km.name()));
		clockUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_clock), ValueUnits.clock24.name()));


		Bundle bundle = getArguments();
		latitude = bundle.getDouble(BundleKey.Latitude.name());
		longitude = bundle.getDouble(BundleKey.Longitude.name());
		addressName = bundle.getString(BundleKey.AddressName.name());
		countryCode = bundle.getString(BundleKey.CountryCode.name());
		mainWeatherSourceType = (WeatherSourceType) bundle.getSerializable(BundleKey.WeatherDataSource.name());
		zoneId = (ZoneId) bundle.getSerializable(BundleKey.TimeZone.name());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = BaseLayoutSimpleForecastBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		if (cardBackgroundColor != null) {
			binding.card.setBackgroundColor(cardBackgroundColor);
		}
		binding.weatherCardViewHeader.getRoot().setVisibility(headerVisibility);
		networkStatus = NetworkStatus.getInstance(getContext());
		networkStatus.observe(getViewLifecycleOwner(), new Observer<Boolean>() {
			@Override
			public void onChanged(Boolean available) {
				if (available) {
					if (needCompare) {
						binding.weatherCardViewHeader.compareForecast.setClickable(true);
					}
				} else {
					if (needCompare) {
						binding.weatherCardViewHeader.compareForecast.setClickable(false);
					}
				}
			}
		});

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
	public void setValuesToViews() {

	}

	protected void createValueUnitsDescription(WeatherSourceType weatherSourceType, boolean haveRain, boolean haveSnow) {
		binding.extraView.removeAllViews();

		if (haveRain || haveSnow) {
			String rainUnit = "mm";
			String snowUnit = null;

			if (weatherSourceType == WeatherSourceType.OPEN_WEATHER_MAP) {
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
		}else{
			binding.extraView.setVisibility(View.GONE);
		}
	}
}