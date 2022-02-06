package com.lifedawn.bestweather.weathers.comparison.base;

import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.google.android.gms.ads.AdListener;
import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherDataSourceType;
import com.lifedawn.bestweather.databinding.BaseLayoutForecastComparisonBinding;
import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.weathers.simplefragment.interfaces.IWeatherValues;
import com.lifedawn.bestweather.weathers.view.DateView;
import com.lifedawn.bestweather.weathers.view.NotScrolledView;

import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.util.List;

public class BaseForecastComparisonFragment extends Fragment {
	protected BaseLayoutForecastComparisonBinding binding;
	protected DateView dateRow;
	protected SharedPreferences sharedPreferences;
	protected ValueUnits tempUnit;
	protected ValueUnits windUnit;
	protected ValueUnits visibilityUnit;
	protected ValueUnits clockUnit;
	protected String tempUnitStr;

	protected Double latitude;
	protected Double longitude;
	protected String addressName;
	protected String countryCode;
	protected WeatherDataSourceType mainWeatherDataSourceType;
	protected ZoneId zoneId;

	protected NotScrolledView[] notScrolledViews;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

		tempUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_temp), ValueUnits.celsius.name()));
		windUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_wind), ValueUnits.mPerSec.name()));
		visibilityUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_visibility), ValueUnits.km.name()));
		clockUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_clock), ValueUnits.clock24.name()));
		tempUnitStr = ValueUnits.convertToStr(getContext(), tempUnit);

		Bundle bundle = getArguments();
		latitude = bundle.getDouble(BundleKey.Latitude.name());
		longitude = bundle.getDouble(BundleKey.Longitude.name());
		addressName = bundle.getString(BundleKey.AddressName.name());
		countryCode = bundle.getString(BundleKey.CountryCode.name());
		zoneId = (ZoneId) bundle.getSerializable(BundleKey.TimeZone.name());
		mainWeatherDataSourceType = (WeatherDataSourceType) bundle.getSerializable(
				BundleKey.WeatherDataSource.name());
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = BaseLayoutForecastComparisonBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) binding.toolbar.getRoot().getLayoutParams();
		layoutParams.topMargin = MyApplication.getStatusBarHeight();
		binding.toolbar.getRoot().setLayoutParams(layoutParams);

		binding.adViewBelowScrollView.loadAd(new AdRequest.Builder().build());
		binding.adViewBelowScrollView.setAdListener(new AdListener() {
			@Override
			public void onAdClosed() {
				super.onAdClosed();
				binding.adViewBelowScrollView.loadAd(new AdRequest.Builder().build());
			}
		});

		binding.toolbar.backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View view) {
				getParentFragmentManager().popBackStackImmediate();
			}
		});

		binding.scrollview.setOnScrollChangeListener(new View.OnScrollChangeListener() {
			@Override
			public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
				if (dateRow != null) {
					dateRow.reDraw(scrollX);
				}

				if (notScrolledViews != null) {
					for (NotScrolledView notScrolledView : notScrolledViews) {
						notScrolledView.reDraw(scrollX);
					}
				}
			}
		});
	}


	protected void createValueUnitsDescription(List<WeatherSourceUnitObj> weatherSourceUnitObjs) {
		StringBuilder stringBuilder = new StringBuilder();

		for (WeatherSourceUnitObj weatherSourceTypeObj : weatherSourceUnitObjs) {
			if (weatherSourceTypeObj.haveRain || weatherSourceTypeObj.haveSnow) {
				String rainUnit = "mm";
				String snowUnit = null;
				String weatherSourceTypeName = null;

				switch (weatherSourceTypeObj.weatherDataSourceType) {
					case ACCU_WEATHER:
						weatherSourceTypeName = getString(R.string.accu_weather);
						snowUnit = "cm";
						break;
					case OWM_ONECALL:
						weatherSourceTypeName = getString(R.string.owm);
						snowUnit = "mm";
						break;
					default:
						weatherSourceTypeName = getString(R.string.kma);
						snowUnit = "cm";
				}

				if (stringBuilder.length() > 0) {
					stringBuilder.append("\n");
				}
				stringBuilder.append(weatherSourceTypeName).append(": ");

				if (weatherSourceTypeObj.haveRain) {
					stringBuilder.append(getString(R.string.rain)).append(" ").append(rainUnit);
				}
				if (weatherSourceTypeObj.haveSnow) {
					if (weatherSourceTypeObj.haveRain) {
						stringBuilder.append(" ");
					}
					stringBuilder.append(getString(R.string.snow)).append(" ").append(snowUnit);
				}
			}
		}

		if (stringBuilder.length() > 0) {
			TextView textView = new TextView(getContext());
			FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			layoutParams.gravity = Gravity.RIGHT;
			textView.setLayoutParams(layoutParams);
			textView.setTextColor(Color.GRAY);
			textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f);
			textView.setText(stringBuilder.toString());
			textView.setIncludeFontPadding(false);

			binding.extraView.removeAllViews();
			binding.extraView.addView(textView);
			binding.extraView.setVisibility(View.VISIBLE);
		} else {
			binding.extraView.setVisibility(View.GONE);

		}
	}

	protected static class WeatherSourceUnitObj {
		final WeatherDataSourceType weatherDataSourceType;
		final boolean haveRain;
		final boolean haveSnow;

		public WeatherSourceUnitObj(WeatherDataSourceType weatherDataSourceType, boolean haveRain, boolean haveSnow) {
			this.weatherDataSourceType = weatherDataSourceType;
			this.haveRain = haveRain;
			this.haveSnow = haveSnow;
		}
	}
}