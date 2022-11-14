package com.lifedawn.bestweather.weathers.comparison.base;

import android.annotation.SuppressLint;
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
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.databinding.BaseLayoutForecastComparisonBinding;
import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.weathers.view.DateView;
import com.lifedawn.bestweather.weathers.view.ICleaner;
import com.lifedawn.bestweather.weathers.view.NotScrolledView;

import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class BaseForecastComparisonFragment extends Fragment {
	protected BaseLayoutForecastComparisonBinding binding;
	protected DateView dateRow;
	protected String tempUnitText;
	protected Bundle bundle;
	protected Double latitude;
	protected Double longitude;
	protected String addressName;
	protected String countryCode;
	protected ZoneId zoneId;

	protected NotScrolledView[] notScrolledViews;

	protected List<ICleaner> customViewList = new ArrayList<>();

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		bundle = savedInstanceState != null ? savedInstanceState : getArguments();

		latitude = bundle.getDouble(BundleKey.Latitude.name());
		longitude = bundle.getDouble(BundleKey.Longitude.name());
		addressName = bundle.getString(BundleKey.AddressName.name());
		countryCode = bundle.getString(BundleKey.CountryCode.name());
		zoneId = (ZoneId) bundle.getSerializable(BundleKey.TimeZone.name());
		tempUnitText = MyApplication.VALUE_UNIT_OBJ.getTempUnitText();
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		binding = BaseLayoutForecastComparisonBinding.inflate(inflater, container, false);

		LinearLayout.LayoutParams layoutParams = (LinearLayout.LayoutParams) binding.toolbar.getRoot().getLayoutParams();
		layoutParams.topMargin = MyApplication.getStatusBarHeight();
		binding.toolbar.getRoot().setLayoutParams(layoutParams);

		return binding.getRoot();
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putAll(bundle);
	}

	@SuppressLint("MissingPermission")
	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

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

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		for (ICleaner iCleaner : customViewList) {
			if (iCleaner != null)
				iCleaner.clear();
		}
		customViewList.clear();

		binding.metNorway.removeAllViews();
		binding.accu.removeAllViews();
		binding.kma.removeAllViews();
		binding.owm.removeAllViews();
		binding = null;
	}


	protected void createValueUnitsDescription(List<WeatherSourceUnitObj> weatherSourceUnitObjs) {
		StringBuilder stringBuilder = new StringBuilder();

		for (WeatherSourceUnitObj weatherSourceTypeObj : weatherSourceUnitObjs) {
			if (weatherSourceTypeObj.haveRain || weatherSourceTypeObj.haveSnow) {
				String rainUnit = "mm";
				String snowUnit = null;
				String weatherSourceTypeName = null;

				switch (weatherSourceTypeObj.weatherProviderType) {
					case ACCU_WEATHER:
						weatherSourceTypeName = getString(R.string.accu_weather);
						snowUnit = "cm";
						break;
					case OWM_ONECALL:
						weatherSourceTypeName = getString(R.string.owm);
						snowUnit = "mm";
						break;
					case MET_NORWAY:
						weatherSourceTypeName = getString(R.string.met);
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
		final WeatherProviderType weatherProviderType;
		final boolean haveRain;
		final boolean haveSnow;

		public WeatherSourceUnitObj(WeatherProviderType weatherProviderType, boolean haveRain, boolean haveSnow) {
			this.weatherProviderType = weatherProviderType;
			this.haveRain = haveRain;
			this.haveSnow = haveSnow;
		}
	}
}