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
import androidx.fragment.app.FragmentResultListener;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.RadioGroup;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.SpinnerAdapter;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.material.slider.Slider;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.Gps;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.databinding.ActivityConfigureWidgetBinding;
import com.lifedawn.bestweather.favorites.FavoritesFragment;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;

import org.jetbrains.annotations.NotNull;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;

public class ConfigureWidgetActivity extends AppCompatActivity {
	private ActivityConfigureWidgetBinding binding;
	private Integer appWidgetId;
	private Integer layoutId;
	private ViewGroup previewWidgetView;

	private FavoriteAddressDto newSelectedAddressDto;
	private Gps gps;
	private SharedPreferences sharedPreferences;
	private boolean isKr;
	private long autoRefreshInterval;
	private ArrayMap<TextView, Float> textOriginalSizeMap = new ArrayMap<>();

	private OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
		@Override
		public void handleOnBackPressed() {
			if (!getSupportFragmentManager().popBackStackImmediate()) {
				setResult(RESULT_CANCELED);
				finish();
			}
		}
	};

	private FragmentManager.FragmentLifecycleCallbacks fragmentLifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
		@Override
		public void onFragmentCreated(@NonNull @NotNull FragmentManager fm, @NonNull @NotNull Fragment f, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
			super.onFragmentCreated(fm, f, savedInstanceState);
			if (f instanceof FavoritesFragment) {
				binding.scrollView.setVisibility(View.GONE);
				binding.fragmentContainer.setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void onFragmentDestroyed(@NonNull @NotNull FragmentManager fm, @NonNull @NotNull Fragment f) {
			super.onFragmentDestroyed(fm, f);
			if (f instanceof FavoritesFragment) {
				binding.scrollView.setVisibility(View.VISIBLE);
				binding.fragmentContainer.setVisibility(View.GONE);
			}
		}
	};

	@SuppressLint("NonConstantResourceId")
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = DataBindingUtil.setContentView(this, R.layout.activity_configure_widget);
		AppWidgetManager appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());

		getSupportFragmentManager().registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false);
		getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		gps = new Gps(requestOnGpsLauncher, requestLocationPermissionLauncher, moveToAppDetailSettingsLauncher);

		Bundle bundle = getIntent().getExtras();

		if (bundle != null) {
			appWidgetId = bundle.getInt(
					AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		layoutId = appWidgetManager.getAppWidgetInfo(appWidgetId).initialLayout;
		previewWidgetView = (ViewGroup) getLayoutInflater().inflate(layoutId, null);
		initPreviewWidget();

		new Thread(new Runnable() {
			@Override
			public void run() {
				loadTextViewsAndTextSize();

				runOnUiThread(new Runnable() {
					@Override
					public void run() {
						binding.previewWidgetContainer.addView(previewWidgetView);
						//위치, 날씨제공사, 대한민국 최우선, 자동 업데이트 간격, 날짜와 시각표시,
						//현지 시각으로 표시, 글자크기, 배경 투명도
						initLocation();
						initWeatherDataSource();
						initAutoRefreshInterval();
						initDisplayDateTime();
						initTextSize();
						initBackground();
					}
				});
			}
		}).start();

		binding.check.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				if (!binding.currentLocationRadio.isChecked() && !binding.selectedLocationRadio.isChecked()) {
					Toast.makeText(ConfigureWidgetActivity.this, R.string.msg_empty_locations, Toast.LENGTH_SHORT).show();
					return;
				}
				Context context = getApplicationContext();
				Class<?> className = null;
				switch (layoutId) {
					case R.layout.widget_current:
						className = WidgetCurrent.class;
						break;
					case R.layout.widget_current_hourly:
						className = WidgetCurrentHourly.class;
						break;
					case R.layout.widget_current_daily:
						className = WidgetCurrentDaily.class;
						break;
					case R.layout.widget_current_hourly_daily:
						className = WidgetCurrentHourlyDaily.class;
						break;
				}

				final LocationType locationType = binding.currentLocationRadio.isChecked() ? LocationType.CurrentLocation :
						LocationType.SelectedAddress;
				final WeatherSourceType weatherSourceType = binding.accuWeatherRadio.isChecked() ? WeatherSourceType.ACCU_WEATHER : WeatherSourceType.OPEN_WEATHER_MAP;

				SharedPreferences widgetAttributes =
						getSharedPreferences(WidgetAttributes.WIDGET_ATTRIBUTES_ID.name() + appWidgetId,
								MODE_PRIVATE);
				SharedPreferences.Editor editor = widgetAttributes.edit();
				//appwidget id
				editor.putInt(WidgetAttributes.APP_WIDGET_ID.name(), appWidgetId);
				//view text size

				//background alpha
				editor.putInt(WidgetAttributes.BACKGROUND_ALPHA.name(), previewWidgetView.getBackground().getAlpha());
				//location type
				editor.putString(WidgetAttributes.LOCATION_TYPE.name(), locationType.name());
				//weatherSourceType
				editor.putString(WidgetAttributes.WEATHER_SOURCE_TYPE.name(), weatherSourceType.name());
				//top priority kma
				editor.putBoolean(WidgetAttributes.TOP_PRIORITY_KMA.name(), binding.kmaTopPrioritySwitch.isChecked());
				//refresh interval
				editor.putLong(WidgetAttributes.UPDATE_INTERVAL.name(), autoRefreshInterval);
				//display datetime
				editor.putBoolean(WidgetAttributes.DISPLAY_DATETIME.name(), binding.displayDatetimeSwitch.isChecked());
				//display local datetime
				editor.putBoolean(WidgetAttributes.DISPLAY_LOCAL_DATETIME.name(), binding.displayLocalDatetimeSwitch.isChecked());
				//selected address dto id
				editor.putInt(WidgetAttributes.SELECTED_ADDRESS_DTO_ID.name(), locationType == LocationType.SelectedAddress ?
						newSelectedAddressDto.getId() : 0);

				//address name if select
				if (binding.selectedLocationRadio.isChecked()) {
					editor.putString(RootAppWidget.WidgetDataKeys.ADDRESS_NAME.name(), newSelectedAddressDto.getAddress())
							.putString(RootAppWidget.WidgetDataKeys.LATITUDE.name(), newSelectedAddressDto.getLatitude())
							.putString(RootAppWidget.WidgetDataKeys.LONGITUDE.name(), newSelectedAddressDto.getLongitude());
				}
				editor.commit();

				RemoteViews remoteViews = RootAppWidget.createRemoteViews(getApplicationContext(), appWidgetId, layoutId);
				appWidgetManager.updateAppWidget(appWidgetId, remoteViews);

				Intent resultIntent = new Intent();
				resultIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
				setResult(RESULT_OK, resultIntent);

				Intent initIntent = new Intent(context, className);
				initIntent.setAction(getString(R.string.ACTION_INIT));

				Bundle initBundle = new Bundle();
				initBundle.putSerializable(context.getString(R.string.bundle_key_widget_class_name), className);
				initBundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
				initBundle.putInt(context.getString(R.string.bundle_key_widget_layout_id), layoutId);
				initBundle.putParcelable(WidgetAttributes.REMOTE_VIEWS.name(), remoteViews);
				initIntent.putExtras(initBundle);

				PendingIntent initPendingIntent = PendingIntent.getBroadcast(context, appWidgetId, initIntent, 0);
				try {
					initPendingIntent.send();
				} catch (PendingIntent.CanceledException e) {
					e.printStackTrace();
				}

				finish();
			}
		});

	}

	private void initPreviewWidget() {
		if (previewWidgetView.findViewById(R.id.hourly_forecast_rows) != null) {
			final int size = 10;
			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M.d H");
			String hours = null;
			LayoutInflater layoutInflater = getLayoutInflater();

			ViewGroup row1 = ((ViewGroup) previewWidgetView.findViewById(R.id.hourly_forecast_row));

			for (int i = 0; i < size; i++) {
				View view = layoutInflater.inflate(R.layout.view_hourly_forecast_item_in_widget, null);
				if (now.getHour() == 0) {
					hours = now.format(formatter);
				} else {
					hours = String.valueOf(now.getHour());
				}
				now = now.plusHours(1);

				((TextView) view.findViewById(R.id.hourly_clock)).setText(hours);
				
				row1.addView(view);
			}
		}
		if (previewWidgetView.findViewById(R.id.daily_forecast_row) != null) {
			final int size = 4;
			LocalDateTime now = LocalDateTime.now();
			DateTimeFormatter formatter = DateTimeFormatter.ofPattern("M.d E");
			String date = null;
			LayoutInflater layoutInflater = getLayoutInflater();

			ViewGroup row = ((ViewGroup) previewWidgetView.findViewById(R.id.daily_forecast_row));

			for (int i = 0; i < size; i++) {
				View view = layoutInflater.inflate(R.layout.view_daily_forecast_item_in_widget, null);
				date = now.format(formatter);
				now = now.plusDays(1);

				((TextView) view.findViewById(R.id.daily_date)).setText(date);
				row.addView(view);
			}
		}
	}

	private void loadTextViewsAndTextSize() {
		TextView textView = ((TextView) previewWidgetView.findViewById(R.id.address));
		textOriginalSizeMap.put(textView, textView.getTextSize());
		textView = ((TextView) previewWidgetView.findViewById(R.id.refresh));
		textOriginalSizeMap.put(textView, textView.getTextSize());

		textView = ((TextView) previewWidgetView.findViewById(R.id.date));
		textOriginalSizeMap.put(textView, textView.getTextSize());
		textView = ((TextView) previewWidgetView.findViewById(R.id.time));
		textOriginalSizeMap.put(textView, textView.getTextSize());

		textView = ((TextView) previewWidgetView.findViewById(R.id.current_temperature));
		textOriginalSizeMap.put(textView, textView.getTextSize());
		textView = ((TextView) previewWidgetView.findViewById(R.id.current_realfeel_temperature));
		textOriginalSizeMap.put(textView, textView.getTextSize());
		textView = ((TextView) previewWidgetView.findViewById(R.id.current_airquality));
		textOriginalSizeMap.put(textView, textView.getTextSize());
		textView = ((TextView) previewWidgetView.findViewById(R.id.current_precipitation));
		textOriginalSizeMap.put(textView, textView.getTextSize());

		int rowCount = 0;
		if (previewWidgetView.findViewById(R.id.hourly_forecast_rows) != null) {
			ViewGroup layout = ((ViewGroup) previewWidgetView.findViewById(R.id.hourly_forecast_rows));
			rowCount = layout.getChildCount();

			for (int rowIdx = 0; rowIdx < rowCount; rowIdx++) {
				ViewGroup row = (ViewGroup) layout.getChildAt(rowIdx);
				int cellCount = row.getChildCount();

				for (int cellIdx = 0; cellIdx < cellCount; cellIdx++) {
					View cell = row.getChildAt(cellIdx);

					textView = ((TextView) cell.findViewById(R.id.hourly_clock));
					textOriginalSizeMap.put(textView, textView.getTextSize());
					textView = ((TextView) cell.findViewById(R.id.hourly_temperature));
					textOriginalSizeMap.put(textView, textView.getTextSize());
				}
			}
		}
		if (previewWidgetView.findViewById(R.id.daily_forecast_row) != null) {
			ViewGroup row = ((ViewGroup) previewWidgetView.findViewById(R.id.daily_forecast_row));
			int cellCount = row.getChildCount();

			for (int cellIdx = 0; cellIdx < cellCount; cellIdx++) {
				View cell = row.getChildAt(cellIdx);

				textView = ((TextView) cell.findViewById(R.id.daily_date));
				textOriginalSizeMap.put(textView, textView.getTextSize());
				textView = ((TextView) cell.findViewById(R.id.daily_temperature));
				textOriginalSizeMap.put(textView, textView.getTextSize());
			}
		}

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
		DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(getString(R.string.date_pattern));
		DateTimeFormatter timeFormatter =
				DateTimeFormatter.ofPattern(ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_clock),
						ValueUnits.clock12.name())) == ValueUnits.clock12 ? getString(R.string.clock_12_pattern) :
						getString(R.string.clock_24_pattern));
		LocalDateTime now = LocalDateTime.now();
		((TextView) previewWidgetView.findViewById(R.id.date)).setText(now.format(dateFormatter));
		((TextView) previewWidgetView.findViewById(R.id.time)).setText(now.format(timeFormatter));

		binding.displayDatetimeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				binding.displayLocalDatetimeSwitch.setVisibility(isChecked ? View.VISIBLE : View.GONE);
				previewWidgetView.findViewById(R.id.watch).setVisibility(isChecked ? View.VISIBLE : View.GONE);
			}
		});

		if (previewWidgetView.findViewById(R.id.watch).getVisibility() == View.VISIBLE) {
			binding.displayDatetimeSwitch.setChecked(true);
		}

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
				autoRefreshInterval = intervalsLong[position];
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
		if (sharedPreferences.getBoolean(getString(R.string.pref_key_accu_weather), true)) {
			binding.accuWeatherRadio.setChecked(true);
		} else {
			binding.owmRadio.setChecked(true);
		}

		binding.weatherDataSourceRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {

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
					gps.checkPermissionAndGpsEnabled(ConfigureWidgetActivity.this, locationCallback);
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
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.setFragmentResult(getString(R.string.key_from_widget_config_main_to_favorites), new Bundle());

		FavoritesFragment favoritesFragment = new FavoritesFragment();
		getSupportFragmentManager().beginTransaction().add(binding.fragmentContainer.getId(), favoritesFragment, getString(R.string.tag_favorites_fragment))
				.addToBackStack(getString(R.string.tag_favorites_fragment)).commit();

		fragmentManager.setFragmentResultListener(getString(R.string.key_back_from_favorites_to_widget_config_main),
				ConfigureWidgetActivity.this,
				new FragmentResultListener() {
					@Override
					public void onFragmentResult(@NonNull @NotNull String requestKey, @NonNull @NotNull Bundle result) {
						fragmentManager.clearFragmentResult(getString(R.string.key_from_widget_config_main_to_favorites));
						fragmentManager.clearFragmentResultListener(requestKey);

						if (result.getSerializable(getString(R.string.bundle_key_selected_address_dto)) == null) {
							Toast.makeText(getApplicationContext(), R.string.not_selected_address, Toast.LENGTH_SHORT).show();
							binding.selectedLocationRadio.setText(R.string.click_again_to_select_address);
						} else {
							newSelectedAddressDto = (FavoriteAddressDto) result.getSerializable(getString(R.string.bundle_key_selected_address_dto));
							String text = getString(R.string.location) + ", " + newSelectedAddressDto.getAddress();
							binding.selectedLocationRadio.setText(text);
							binding.changeAddressBtn.setVisibility(View.VISIBLE);
						}
					}

				});
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
		float originalSize = 0;
		final float extraSize = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, Math.abs(value), getResources().getDisplayMetrics());

		for (int i = 0; i < textOriginalSizeMap.size(); i++) {
			originalSize = textOriginalSizeMap.valueAt(i);
			textOriginalSizeMap.keyAt(i).setTextSize(TypedValue.COMPLEX_UNIT_PX, value < 0f ? originalSize - extraSize :
					originalSize + extraSize);
		}

	}

	private void setBackgroundAlpha(int alpha) {
		previewWidgetView.getBackground().setAlpha(255 - alpha);
	}

	public enum WidgetAttributes {
		WIDGET_ATTRIBUTES_ID, APP_WIDGET_ID, BACKGROUND_ALPHA, LOCATION_TYPE, WEATHER_SOURCE_TYPE, TOP_PRIORITY_KMA,
		UPDATE_INTERVAL, DISPLAY_DATETIME, DISPLAY_LOCAL_DATETIME, SELECTED_ADDRESS_DTO_ID, WIDGET_CLASS, REMOTE_VIEWS;
	}

	public static class WidgetTextViews {
		public enum Header {
			ADDRESS_TEXT, REFRESH_TEXT;
		}

		public enum Watch {
			DATE_TEXT, TIME_TEXT;
		}

		public enum Current {
			TEMP_TEXT, REAL_FEEL_TEMP_TEXT, AIR_QUALITY_TEXT, PRECIPITATION_TEXT;
		}

		public enum Hourly {
			CLOCK_TEXT, TEMP_TEXT;
		}

		public enum Daily {
			DATE_TEXT, TEMP_TEXT;
		}
	}
}