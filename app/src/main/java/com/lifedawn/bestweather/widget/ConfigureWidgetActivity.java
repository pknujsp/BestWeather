package com.lifedawn.bestweather.widget;

import androidx.activity.OnBackPressedCallback;
import androidx.activity.result.ActivityResult;
import androidx.activity.result.ActivityResultCallback;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
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
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
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
import com.lifedawn.bestweather.commons.classes.Gps;
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
	private static String tag = "ConfigureWidgetActivity";
	private ActivityConfigureWidgetBinding binding;
	private Integer appWidgetId;
	private Integer layoutId;
	private boolean isKr;

	private FavoriteAddressDto newSelectedAddressDto;
	private Gps gps;

	private WidgetCreator widgetCreator;

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

		gps = new Gps(getApplicationContext(), requestOnGpsLauncher, requestLocationPermissionLauncher, moveToAppDetailSettingsLauncher);
		Bundle bundle = getIntent().getExtras();

		if (bundle != null) {
			appWidgetId = bundle.getInt(
					AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		Log.e("configure", "appwidgetId : " + appWidgetId);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
		layoutId = appWidgetManager.getAppWidgetInfo(appWidgetId).initialLayout;

		widgetCreator = new WidgetCreator(getApplicationContext(), this);

		SharedPreferences widgetPreferences =
				getSharedPreferences(WidgetCreator.getSharedPreferenceName(appWidgetId), MODE_PRIVATE);
		widgetCreator.setIntialValues(appWidgetId, widgetPreferences);
		widgetPreferences =
				getSharedPreferences(WidgetCreator.getSharedPreferenceName(appWidgetId), MODE_PRIVATE);

		//위치, 날씨제공사, 대한민국 최우선, 자동 업데이트 간격, 날짜와 시각표시,
		//현지 시각으로 표시, 글자크기, 배경 투명도
		initLocation();
		initWeatherDataSource();
		initAutoRefreshInterval();
		initDisplayDateTime();
		initTextSize();
		initBackground();

		binding.check.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!binding.currentLocationRadio.isChecked() && !binding.selectedLocationRadio.isChecked()) {
					Toast.makeText(ConfigureWidgetActivity.this, R.string.msg_empty_locations, Toast.LENGTH_SHORT).show();
					return;
				}

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

				Intent intent = new Intent(getApplicationContext(), widgetProviderClass);
				intent.setAction(getString(R.string.com_lifedawn_bestweather_action_INIT));
				Bundle initBundle = new Bundle();

				initBundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
				intent.putExtras(initBundle);

				PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), appWidgetId, intent, 0);
				widgetCreator.removeWidgetUpdateCallback();
				try {
					pendingIntent.send();
				} catch (PendingIntent.CanceledException e) {
					e.printStackTrace();
				}
				finish();
			}
		});

		widgetPreferences.registerOnSharedPreferenceChangeListener(widgetCreator);
		widgetCreator.onSharedPreferenceChanged(widgetPreferences, null);
	}

	@Override
	protected void onDestroy() {
		widgetCreator.removeWidgetUpdateCallback();
		SharedPreferences sharedPreferences = getSharedPreferences(WidgetCreator.getSharedPreferenceName(appWidgetId), MODE_PRIVATE);
		sharedPreferences.unregisterOnSharedPreferenceChangeListener(widgetCreator);
		widgetCreator = null;

		if (sharedPreferences.getAll().isEmpty()) {
			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
				deleteSharedPreferences(WidgetCreator.getSharedPreferenceName(appWidgetId));
			} else {
				getSharedPreferences(WidgetCreator.getSharedPreferenceName(appWidgetId), Context.MODE_PRIVATE).edit().clear().apply();
			}
		}
		super.onDestroy();
	}

	Preference.OnPreferenceChangeListener onPreferenceChangeListener = new Preference.OnPreferenceChangeListener() {
		@Override
		public boolean onPreferenceChange(Preference preference, Object newValue) {
			SharedPreferences.Editor editor = getSharedPreferences(WidgetCreator.getSharedPreferenceName(appWidgetId), Context.MODE_PRIVATE).edit();
			String key = preference.getKey();

			if (key.equals(WidgetNotiConstants.WidgetAttributes.BACKGROUND_ALPHA.name()))
				editor.putInt(key, (int) newValue);
			else if (key.equals(WidgetNotiConstants.Commons.Attributes.UPDATE_INTERVAL.name()))
				editor.putLong(key, (long) newValue);
			else if (key.equals(WidgetNotiConstants.Commons.Attributes.LOCATION_TYPE.name()))
				editor.putString(key, (String) newValue);
			else if (key.equals(WidgetNotiConstants.WidgetAttributes.DISPLAY_CLOCK.name()))
				editor.putBoolean(key, (boolean) newValue);
			else if (key.equals(WidgetNotiConstants.WidgetAttributes.DISPLAY_LOCAL_CLOCK.name()))
				editor.putBoolean(key, (boolean) newValue);
			else if (key.equals(WidgetNotiConstants.Commons.Attributes.TOP_PRIORITY_KMA.name()))
				editor.putBoolean(key, (boolean) newValue);
			else if (key.equals(WidgetNotiConstants.Commons.Attributes.SELECTED_ADDRESS_DTO_ID.name()))
				editor.putInt(key, (int) newValue);
			else if (key.equals(WidgetNotiConstants.Commons.Attributes.WEATHER_SOURCE_TYPE.name()))
				editor.putString(key, (String) newValue);

			editor.commit();
			return true;
		}
	};


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
				Preference preference = new Preference(getApplicationContext());
				preference.setKey(WidgetNotiConstants.WidgetAttributes.DISPLAY_CLOCK.name());
				onPreferenceChangeListener.onPreferenceChange(preference, isChecked);
			}
		});
		binding.displayLocalDatetimeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Preference preference = new Preference(getApplicationContext());
				preference.setKey(WidgetNotiConstants.WidgetAttributes.DISPLAY_LOCAL_CLOCK.name());
				onPreferenceChangeListener.onPreferenceChange(preference, isChecked);
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
				Preference preference = new Preference(getApplicationContext());
				preference.setKey(WidgetNotiConstants.Commons.Attributes.UPDATE_INTERVAL.name());
				onPreferenceChangeListener.onPreferenceChange(preference, autoRefreshInterval);
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
				Preference preference = new Preference(getApplicationContext());
				preference.setKey(WidgetNotiConstants.Commons.Attributes.WEATHER_SOURCE_TYPE.name());
				onPreferenceChangeListener.onPreferenceChange(preference, checkedId == 0 ? WeatherSourceType.ACCU_WEATHER.name()
						: WeatherSourceType.OPEN_WEATHER_MAP.name());
			}
		});
		binding.kmaTopPrioritySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				Preference preference = new Preference(getApplicationContext());
				preference.setKey(WidgetNotiConstants.Commons.Attributes.TOP_PRIORITY_KMA.name());
				onPreferenceChangeListener.onPreferenceChange(preference, isChecked);
			}
		});

	}

	private void initLocation() {
		binding.currentLocationRadio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					//현재 위치
					//위치 권한, gps on 확인
					if (gps.checkPermissionAndGpsEnabled(ConfigureWidgetActivity.this, locationCallback)) {
						Preference preference = new Preference(getApplicationContext());
						preference.setKey(WidgetNotiConstants.Commons.Attributes.LOCATION_TYPE.name());
						onPreferenceChangeListener.onPreferenceChange(preference, LocationType.CurrentLocation.name());
					}
				}
			}
		});

		binding.selectedLocationRadio.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				if (isChecked) {
					if (newSelectedAddressDto == null) {
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
					Toast.makeText(getApplicationContext(), R.string.not_selected_address, Toast.LENGTH_SHORT).show();
					binding.selectedLocationRadio.setText(R.string.click_again_to_select_address);
					binding.selectedLocationRadio.setChecked(false);
					binding.currentLocationRadio.setChecked(false);
				} else {
					newSelectedAddressDto = (FavoriteAddressDto) result.getSerializable(BundleKey.SelectedAddressDto.name());
					String text = getString(R.string.location) + ", " + newSelectedAddressDto.getAddress();
					binding.selectedLocationRadio.setText(text);
					binding.changeAddressBtn.setVisibility(View.VISIBLE);

					Preference preference = new Preference(getApplicationContext());
					preference.setKey(WidgetNotiConstants.Commons.Attributes.LOCATION_TYPE.name());
					onPreferenceChangeListener.onPreferenceChange(preference, LocationType.SelectedAddress.name());

					SharedPreferences.Editor editor = getSharedPreferences(WidgetCreator.getSharedPreferenceName(appWidgetId),
							MODE_PRIVATE).edit();

					editor.putString(WidgetNotiConstants.Commons.DataKeys.ADDRESS_NAME.name(), newSelectedAddressDto.getAddress())
							.putString(WidgetNotiConstants.Commons.DataKeys.LATITUDE.name(), newSelectedAddressDto.getLatitude())
							.putString(WidgetNotiConstants.Commons.DataKeys.LONGITUDE.name(), newSelectedAddressDto.getLongitude())
							.putString(WidgetNotiConstants.Commons.DataKeys.COUNTRY_CODE.name(), newSelectedAddressDto.getCountryCode()).apply();
				}
			}
		});

		String tag = FavoritesFragment.class.getName();

		getSupportFragmentManager().beginTransaction().add(binding.fragmentContainer.getId(), favoritesFragment, tag)
				.addToBackStack(tag).commit();
	}

	private final ActivityResultLauncher<Intent> requestOnGpsLauncher = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
				@Override
				public void onActivityResult(ActivityResult result) {
					//gps 사용확인 화면에서 나온뒤 현재 위치 다시 파악
					LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
					boolean isGpsEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);

					if (isGpsEnabled) {
					} else {
						locationCallback.onFailed(Gps.LocationCallback.Fail.DISABLED_GPS);
					}
				}
			});

	private final ActivityResultLauncher<Intent> moveToAppDetailSettingsLauncher = registerForActivityResult(
			new ActivityResultContracts.StartActivityForResult(), new ActivityResultCallback<ActivityResult>() {
				@Override
				public void onActivityResult(ActivityResult result) {
					if (ContextCompat.checkSelfPermission(getApplicationContext(),
							Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
						PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean(
								getString(R.string.pref_key_never_ask_again_permission_for_access_fine_location), false).apply();

					} else {
						locationCallback.onFailed(Gps.LocationCallback.Fail.REJECT_PERMISSION);
					}

				}
			});

	private final ActivityResultLauncher<String> requestLocationPermissionLauncher = registerForActivityResult(
			new ActivityResultContracts.RequestPermission(), new ActivityResultCallback<Boolean>() {
				@Override
				public void onActivityResult(Boolean isGranted) {
					//gps사용 권한
					//허가남 : 현재 위치 다시 파악
					//거부됨 : 작업 취소
					//계속 거부 체크됨 : 작업 취소
					if (isGranted) {
						PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean(
								getString(R.string.pref_key_never_ask_again_permission_for_access_fine_location), false).apply();
					} else {
						if (!ActivityCompat.shouldShowRequestPermissionRationale(ConfigureWidgetActivity.this,
								Manifest.permission.ACCESS_FINE_LOCATION)) {
							PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).edit().putBoolean(
									getString(R.string.pref_key_never_ask_again_permission_for_access_fine_location), true).apply();
						}
						locationCallback.onFailed(Gps.LocationCallback.Fail.REJECT_PERMISSION);
					}
				}
			});

	private final Gps.LocationCallback locationCallback = new Gps.LocationCallback() {
		@Override
		public void onSuccessful(Location location) {
		}

		@Override
		public void onFailed(Fail fail) {
			if (fail == Fail.DISABLED_GPS) {
				Toast.makeText(ConfigureWidgetActivity.this, R.string.request_to_make_gps_on, Toast.LENGTH_SHORT).show();
			} else if (fail == Fail.REJECT_PERMISSION) {
				Toast.makeText(ConfigureWidgetActivity.this, R.string.message_needs_location_permission, Toast.LENGTH_SHORT).show();
			}

			binding.currentLocationRadio.setChecked(false);
			binding.selectedLocationRadio.setChecked(false);
		}
	};


	private void setTextSizeInWidget(float value) {
		int absSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, Math.abs(value), getResources().getDisplayMetrics());
		final int extraSize = value > 0 ? absSize : absSize * -1;
		SharedPreferences.Editor editor = getSharedPreferences(WidgetCreator.getSharedPreferenceName(appWidgetId), Context.MODE_PRIVATE).edit();

		editor.putInt(WidgetNotiConstants.WidgetTextViews.Header.ADDRESS_TEXT_IN_HEADER.name(),
				getResources().getDimensionPixelSize(R.dimen.addressTextSizeInHeader) + extraSize);
		editor.putInt(WidgetNotiConstants.WidgetTextViews.Header.REFRESH_TEXT_IN_HEADER.name(), getResources().getDimensionPixelSize(R.dimen.refreshTextSizeInHeader) + extraSize);

		editor.putInt(WidgetNotiConstants.WidgetTextViews.Current.TEMP_TEXT_IN_CURRENT.name(), getResources().getDimensionPixelSize(R.dimen.tempTextSizeInCurrent) + extraSize);
		editor.putInt(WidgetNotiConstants.WidgetTextViews.Current.REAL_FEEL_TEMP_TEXT_IN_CURRENT.name(), getResources().getDimensionPixelSize(R.dimen.realFeelTempTextSizeInCurrent) + extraSize);
		editor.putInt(WidgetNotiConstants.WidgetTextViews.Current.AIR_QUALITY_TEXT_IN_CURRENT.name(), getResources().getDimensionPixelSize(R.dimen.airQualityTextSizeInCurrent) + extraSize);
		editor.putInt(WidgetNotiConstants.WidgetTextViews.Current.PRECIPITATION_TEXT_IN_CURRENT.name(), getResources().getDimensionPixelSize(R.dimen.precipitationTextSizeInCurrent) + extraSize);

		editor.putInt(WidgetNotiConstants.WidgetTextViews.Clock.DATE_TEXT_IN_CLOCK.name(),
				getResources().getDimensionPixelSize(R.dimen.dateTextSizeInClock) + extraSize);
		editor.putInt(WidgetNotiConstants.WidgetTextViews.Clock.TIME_TEXT_IN_CLOCK.name(),
				getResources().getDimensionPixelSize(R.dimen.timeTextSizeInClock) + extraSize);

		editor.apply();
	}

	private void setBackgroundAlpha(int alpha) {
		Preference preference = new Preference(getApplicationContext());
		preference.setKey(WidgetNotiConstants.WidgetAttributes.BACKGROUND_ALPHA.name());
		onPreferenceChangeListener.onPreferenceChange(preference, 100 - alpha);
		Log.e(tag, "background alpha : " + (100 - alpha));
	}

	@Override
	public void updateWidget() {
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
		binding.previewLayout.removeAllViews();

		RemoteViews removeViews = widgetCreator.createRemoteViews(true);
		View previewWidgetView = removeViews.apply(getApplicationContext(), binding.previewLayout);
		binding.previewLayout.setMinimumHeight(appWidgetManager.getAppWidgetInfo(appWidgetId).minHeight);
		binding.previewLayout.addView(previewWidgetView);

		appWidgetManager.updateAppWidget(appWidgetId, removeViews);

		Intent resultValue = new Intent();
		resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
		setResult(RESULT_OK, resultValue);
	}
}