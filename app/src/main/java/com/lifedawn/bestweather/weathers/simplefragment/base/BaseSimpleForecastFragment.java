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
import androidx.asynclayoutinflater.view.AsyncLayoutInflater;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.NetworkStatus;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.commons.enums.WeatherValueType;
import com.lifedawn.bestweather.databinding.BaseLayoutSimpleCurrentConditionsBinding;
import com.lifedawn.bestweather.databinding.BaseLayoutSimpleForecastBinding;
import com.lifedawn.bestweather.databinding.LoadingViewAsyncBinding;
import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.weathers.simplefragment.interfaces.IWeatherValues;
import com.lifedawn.bestweather.weathers.view.DateView;
import com.lifedawn.bestweather.weathers.viewmodels.WeatherFragmentViewModel;

import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.Map;

public class BaseSimpleForecastFragment extends Fragment implements IWeatherValues, AsyncLayoutInflater.OnInflateFinishedListener {
	protected BaseLayoutSimpleForecastBinding binding;
	protected LoadingViewAsyncBinding asyncBinding;

	protected DateView dateRow;
	protected String countryCode;
	protected WeatherProviderType mainWeatherProviderType;
	protected boolean needCompare;
	protected Map<WeatherValueType, Integer> textSizeMap = new HashMap<>();
	protected Map<WeatherValueType, Integer> textColorMap = new HashMap<>();
	protected Integer cardBackgroundColor;
	protected NetworkStatus networkStatus;
	protected Bundle bundle;
	protected Double latitude;
	protected Double longitude;
	protected ZoneId zoneId;
	protected WeatherFragmentViewModel weatherFragmentViewModel;

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

		countryCode = MyApplication.getLocaleCountryCode();
		mainWeatherProviderType = (WeatherProviderType) bundle.getSerializable(BundleKey.WeatherProvider.name());

		latitude = bundle.getDouble(BundleKey.Latitude.name(), 0);
		longitude = bundle.getDouble(BundleKey.Longitude.name(), 0);
		zoneId = (ZoneId) bundle.getSerializable(BundleKey.TimeZone.name());

		networkStatus = NetworkStatus.getInstance(requireContext().getApplicationContext());
		weatherFragmentViewModel = new ViewModelProvider(requireParentFragment()).get(WeatherFragmentViewModel.class);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		asyncBinding = LoadingViewAsyncBinding.inflate(inflater, container, false);

		final AsyncLayoutInflater asyncLayoutInflater = new AsyncLayoutInflater(requireContext());
		asyncLayoutInflater.inflate(R.layout.base_layout_simple_forecast, container, this);
		return asyncBinding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
	}

	@Override
	public void onDestroy() {

		super.onDestroy();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		textColorMap.clear();
		textColorMap = null;

		textSizeMap.clear();
		textSizeMap = null;
		binding.forecastView.removeAllViews();
		binding = null;
		asyncBinding = null;
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

			if (weatherProviderType == WeatherProviderType.OWM_ONECALL || weatherProviderType == WeatherProviderType.MET_NORWAY) {
				snowUnit = "mm";
			} else {
				snowUnit = "cm";
			}

			StringBuilder stringBuilder = new StringBuilder();

			if (haveRain) {
				stringBuilder.append(getString(R.string.rain)).append(" - ").append(rainUnit);
			}
			if (haveSnow) {
				if (stringBuilder.length() > 0) {
					stringBuilder.append(", ");
				}
				stringBuilder.append(getString(R.string.snow)).append(" - ").append(snowUnit);
			}

			TextView textView = new TextView(requireContext().getApplicationContext());
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

	protected void createValueUnitsDescription(WeatherProviderType weatherProviderType, boolean haveRain, boolean haveSnow,
	                                           ZonedDateTime firstDateTime_hasNextNHoursPrecipitation,
	                                           String hourAmount) {
		createValueUnitsDescription(weatherProviderType, haveRain, haveSnow);

		if (haveRain || haveSnow) {
			DateTimeFormatter dateTimeFormatter = null;
			String txt = null;

			if (countryCode.equals("KR")) {
				dateTimeFormatter = DateTimeFormatter.ofPattern("d일 E HH시");
				txt = firstDateTime_hasNextNHoursPrecipitation.format(dateTimeFormatter) +
						" 이후로 표시되는 강수량은 직후 " + hourAmount +
						"시간 동안의 강수량입니다";
			} else {
				dateTimeFormatter = DateTimeFormatter.ofPattern("HH EEEE d");
				txt = "The precipitation shown from " +
						firstDateTime_hasNextNHoursPrecipitation.format(dateTimeFormatter) +
						" is the precipitation for the next " + hourAmount + " hours";
			}

			TextView textView = new TextView(requireContext().getApplicationContext());
			FrameLayout.LayoutParams layoutParams = new FrameLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
					ViewGroup.LayoutParams.WRAP_CONTENT);
			layoutParams.gravity = Gravity.RIGHT;
			textView.setLayoutParams(layoutParams);
			textView.setTextColor(Color.GRAY);
			textView.setTextSize(TypedValue.COMPLEX_UNIT_SP, 11f);
			textView.setText(txt);
			textView.setIncludeFontPadding(false);

			binding.extraView.addView(textView);
		}
	}

	@Override
	public void onInflateFinished(@NonNull View view, int resid, @Nullable ViewGroup parent) {
		binding = BaseLayoutSimpleForecastBinding.bind(view);
		binding.getRoot().setVisibility(View.GONE);
		asyncBinding.getRoot().addView(binding.getRoot());

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


	protected final void onFinishedSetData() {
		binding.getRoot().setVisibility(View.VISIBLE);
		asyncBinding.progressCircular.setVisibility(View.GONE);
		asyncBinding.progressCircular.pauseAnimation();
		weatherFragmentViewModel.onResumeWithAsync(this);
	}
}