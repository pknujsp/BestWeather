package com.lifedawn.bestweather.widget;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.Preference;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.slider.Slider;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.commons.enums.WidgetNotiConstants;
import com.lifedawn.bestweather.commons.interfaces.OnResultFragmentListener;
import com.lifedawn.bestweather.databinding.ActivityConfigureWidgetBinding;
import com.lifedawn.bestweather.favorites.FavoritesFragment;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;

import org.jetbrains.annotations.NotNull;

import java.util.Locale;

public class ConfigureWidgetActivity extends AppCompatActivity implements WidgetCreator.WidgetUpdateCallback {
	private ActivityConfigureWidgetBinding binding;
	private int appWidgetId;
	private int layoutId;
	private boolean isKr;

	private FavoriteAddressDto newSelectedAddressDto;

	private WidgetCreator widgetCreator;
	private AppWidgetManager appWidgetManager;
	private boolean selectedFavoriteLocation;

	private final OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
		@Override
		public void handleOnBackPressed() {
			if (!getSupportFragmentManager().popBackStackImmediate()) {
				setResult(RESULT_CANCELED);
				finish();
			}
		}
	};

	private final FragmentManager.FragmentLifecycleCallbacks fragmentLifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
		@Override
		public void onFragmentCreated(@NonNull @NotNull FragmentManager fm, @NonNull @NotNull Fragment f, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
			super.onFragmentCreated(fm, f, savedInstanceState);
			if (f instanceof FavoritesFragment) {
				binding.widgetSettingsContainer.setVisibility(View.GONE);
				binding.fragmentContainer.setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void onFragmentDestroyed(@NonNull @NotNull FragmentManager fm, @NonNull @NotNull Fragment f) {
			super.onFragmentDestroyed(fm, f);
			if (f instanceof FavoritesFragment) {
				binding.widgetSettingsContainer.setVisibility(View.VISIBLE);
				binding.fragmentContainer.setVisibility(View.GONE);
			}
		}
	};


	private boolean isReadStoragePermissionGranted() {
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
			if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
					== PackageManager.PERMISSION_GRANTED) {
				return true;
			} else {
				ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);
				return false;
			}
		} else {
			return true;
		}
	}

	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		setBackgroundImg();
	}

	private void setBackgroundImg() {
		if (isReadStoragePermissionGranted()) {
			WallpaperManager wallpaperManager = WallpaperManager.getInstance(this);
			Drawable wallpaperDrawable = wallpaperManager.getDrawable();
			Glide.with(this).load(wallpaperDrawable).into(binding.wallpaper);
		}
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = DataBindingUtil.setContentView(this, R.layout.activity_configure_widget);

		getSupportFragmentManager().registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false);
		getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);
		setBackgroundImg();

		Bundle bundle = getIntent().getExtras();

		if (bundle != null) {
			appWidgetId = bundle.getInt(
					AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
		layoutId = appWidgetManager.getAppWidgetInfo(appWidgetId).initialLayout;

		widgetCreator = new WidgetCreator(getApplicationContext(), this, appWidgetId);
		widgetCreator.loadDefaultPreferences();
		updatePreview();

		//위치, 날씨제공사, 대한민국 최우선, 자동 업데이트 간격, 날짜와 시각표시,
		//현지 시각으로 표시, 글자크기, 배경 투명도
		initLocation();
		initWeatherDataSource();
		initAutoRefreshInterval();
		initDisplayDateTime();
		initTextSize();
		initBackground();

		binding.currentLocationRadio.setChecked(true);

		binding.check.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Class<?> widgetProviderClass = null;
				if (layoutId == R.layout.widget_current) {
					widgetProviderClass = WidgetProviderCurrent.class;
				} else if (layoutId == R.layout.widget_current_hourly) {
					widgetProviderClass = WidgetProviderCurrentHourly.class;
				} else if (layoutId == R.layout.widget_current_daily) {
					widgetProviderClass = WidgetProviderCurrentDaily.class;
				} else if (layoutId == R.layout.widget_current_hourly_daily) {
					widgetProviderClass = WidgetProviderCurrentHourlyDaily.class;
				}
				if (binding.selectedLocationRadio.isChecked()) {
					SharedPreferences.Editor editor = getSharedPreferences(WidgetCreator.getSharedPreferenceName(appWidgetId),
							MODE_PRIVATE).edit();

					editor.putString(WidgetNotiConstants.Commons.DataKeys.ADDRESS_NAME.name(), newSelectedAddressDto.getAddress())
							.putString(WidgetNotiConstants.Commons.DataKeys.LATITUDE.name(), newSelectedAddressDto.getLatitude())
							.putString(WidgetNotiConstants.Commons.DataKeys.LONGITUDE.name(), newSelectedAddressDto.getLongitude())
							.putString(WidgetNotiConstants.Commons.DataKeys.COUNTRY_CODE.name(), newSelectedAddressDto.getCountryCode()).commit();
				}
				widgetCreator.saveFinalWidgetPreferences();

				Intent resultValue = new Intent();
				resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
				setResult(RESULT_OK, resultValue);

				Intent intent = new Intent(getApplicationContext(), widgetProviderClass);
				intent.setAction(getString(R.string.com_lifedawn_bestweather_action_INIT));
				Bundle initBundle = new Bundle();

				initBundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
				intent.putExtras(initBundle);

				PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), appWidgetId, intent, 0);
				try {
					pendingIntent.send();
				} catch (PendingIntent.CanceledException e) {
					e.printStackTrace();
				}
				finish();
			}
		});
	}

	@Override
	protected void onDestroy() {
		SharedPreferences sharedPreferences = getSharedPreferences(WidgetCreator.getSharedPreferenceName(appWidgetId), MODE_PRIVATE);

		if (sharedPreferences.getAll().isEmpty()) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				deleteSharedPreferences(WidgetCreator.getSharedPreferenceName(appWidgetId));
			} else {
				getSharedPreferences(WidgetCreator.getSharedPreferenceName(appWidgetId), Context.MODE_PRIVATE).edit().clear().apply();
			}
		}
		super.onDestroy();
	}

	private void initBackground() {
		binding.backgroundTransparencySlider.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
			@Override
			public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
				setBackgroundAlpha(progress);
			}

			@Override
			public void onStartTrackingTouch(SeekBar seekBar) {
				setBackgroundAlpha(seekBar.getProgress());
			}

			@Override
			public void onStopTrackingTouch(SeekBar seekBar) {
				setBackgroundAlpha(seekBar.getProgress());
			}
		});
	}

	private void initTextSize() {
		binding.textSizeSlider.addOnSliderTouchListener(new Slider.OnSliderTouchListener() {
			@Override
			public void onStartTrackingTouch(@NonNull @NotNull Slider slider) {
				setTextSizeInWidget(slider.getValue());
			}

			@Override
			public void onStopTrackingTouch(@NonNull @NotNull Slider slider) {
				setTextSizeInWidget(slider.getValue());
			}
		});
	}

	private void initDisplayDateTime() {
		binding.displayDatetimeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				binding.displayLocalDatetimeSwitch.setVisibility(isChecked ? View.VISIBLE : View.GONE);
				widgetCreator.setDisplayClock(isChecked);
				updatePreview();
			}
		});
		binding.displayLocalDatetimeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				widgetCreator.setDisplayLocalClock(isChecked);
				updatePreview();
			}
		});
	}

	private void initAutoRefreshInterval() {
		final String[] intervalsDescription = getResources().getStringArray(R.array.AutoRefreshIntervals);
		final String[] intervalsStr = getResources().getStringArray(R.array.AutoRefreshIntervalsLong);
		final long[] intervalsLong = new long[intervalsStr.length];

		for (int i = 0; i < intervalsStr.length; i++) {
			intervalsLong[i] = Long.parseLong(intervalsStr[i]);
		}

		SpinnerAdapter spinnerAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, intervalsDescription);
		binding.autoRefreshIntervalSpinner.setAdapter(spinnerAdapter);

		binding.autoRefreshIntervalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				long autoRefreshInterval = intervalsLong[position];
				widgetCreator.setUpdateInterval(autoRefreshInterval);
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

	}

	private void initWeatherDataSource() {
		Locale locale = null;
		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
			locale = getResources().getConfiguration().getLocales().get(0);
		} else {
			locale = getResources().getConfiguration().locale;
		}
		String country = locale.getCountry();
		isKr = country.equals("KR");

		if (isKr) {
			binding.kmaTopPrioritySwitch.setVisibility(View.VISIBLE);
		} else {
			binding.kmaTopPrioritySwitch.setVisibility(View.GONE);
		}

		//기본 날씨 제공사 확인
		if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean(getString(R.string.pref_key_accu_weather), true)) {
			binding.accuWeatherRadio.setChecked(true);
		} else {
			binding.owmRadio.setChecked(true);
		}

		binding.weatherDataSourceRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				widgetCreator.setWeatherSourceType(checkedId == 0 ? WeatherSourceType.ACCU_WEATHER
						: WeatherSourceType.OPEN_WEATHER_MAP);
			}
		});

		binding.kmaTopPrioritySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				widgetCreator.setKmaTopPriority(isChecked);
			}
		});

	}

	private void initLocation() {
		binding.locationRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				if (checkedId == binding.currentLocationRadio.getId() && binding.currentLocationRadio.isChecked()) {
					binding.changeAddressBtn.setVisibility(View.GONE);
					binding.selectedAddressName.setVisibility(View.GONE);

					widgetCreator.setLocationType(LocationType.CurrentLocation);
				} else if (checkedId == binding.selectedLocationRadio.getId() && binding.selectedLocationRadio.isChecked()) {
					binding.changeAddressBtn.setVisibility(View.VISIBLE);
					binding.selectedAddressName.setVisibility(View.VISIBLE);

					if (!selectedFavoriteLocation) {
						openFavoritesFragment();
					}
				}
			}
		});

		binding.changeAddressBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openFavoritesFragment();
			}
		});
	}

	private void openFavoritesFragment() {
		FavoritesFragment favoritesFragment = new FavoritesFragment();
		Bundle bundle = new Bundle();
		bundle.putString(BundleKey.RequestFragment.name(), ConfigureWidgetActivity.class.getName());
		favoritesFragment.setArguments(bundle);

		favoritesFragment.setOnResultFragmentListener(new OnResultFragmentListener() {
			@Override
			public void onResultFragment(Bundle result) {
				if (result.getSerializable(BundleKey.SelectedAddressDto.name()) == null) {
					if (!selectedFavoriteLocation) {
						Toast.makeText(getApplicationContext(), R.string.not_selected_address, Toast.LENGTH_SHORT).show();
						binding.currentLocationRadio.setChecked(true);
					}
				} else {
					selectedFavoriteLocation = true;
					newSelectedAddressDto = (FavoriteAddressDto) result.getSerializable(BundleKey.SelectedAddressDto.name());
					binding.selectedAddressName.setText(newSelectedAddressDto.getAddress());
				}
			}
		});

		String tag = FavoritesFragment.class.getName();

		getSupportFragmentManager().beginTransaction().add(binding.fragmentContainer.getId(), favoritesFragment, tag)
				.addToBackStack(tag).commit();
	}


	private void setTextSizeInWidget(float value) {
		int absSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, Math.abs(value), getResources().getDisplayMetrics());
		final int extraSize = value > 0 ? absSize : absSize * -1;

		widgetCreator.setAddressInHeaderTextSize(getResources().getDimensionPixelSize(R.dimen.addressTextSizeInHeader) + extraSize);
		widgetCreator.setRefreshInHeaderTextSize(getResources().getDimensionPixelSize(R.dimen.refreshTextSizeInHeader) + extraSize);

		widgetCreator.setTempInCurrentTextSize(getResources().getDimensionPixelSize(R.dimen.tempTextSizeInCurrent) + extraSize);
		widgetCreator.setRealFeelTempInCurrentTextSize(getResources().getDimensionPixelSize(R.dimen.realFeelTempTextSizeInCurrent) + extraSize);
		widgetCreator.setAirQualityInCurrentTextSize(getResources().getDimensionPixelSize(R.dimen.airQualityTextSizeInCurrent) + extraSize);
		widgetCreator.setPrecipitationInCurrentTextSize(getResources().getDimensionPixelSize(R.dimen.precipitationTextSizeInCurrent) + extraSize);

		widgetCreator.setDateInClockTextSize(getResources().getDimensionPixelSize(R.dimen.dateTextSizeInClock) + extraSize);
		widgetCreator.setTimeInClockTextSize(getResources().getDimensionPixelSize(R.dimen.timeTextSizeInClock) + extraSize);

		updatePreview();
	}

	private void setBackgroundAlpha(int alpha) {
		widgetCreator.setBackgroundAlpha(100 - alpha);
		updatePreview();
	}

	@Override
	public void updatePreview() {
		RemoteViews removeViews = widgetCreator.createRemoteViews(true);
		View previewWidgetView = removeViews.apply(getApplicationContext(), binding.previewLayout);

		binding.previewLayout.removeAllViews();
		binding.previewLayout.setMinimumHeight(appWidgetManager.getAppWidgetInfo(appWidgetId).minHeight);
		binding.previewLayout.addView(previewWidgetView);
	}

}