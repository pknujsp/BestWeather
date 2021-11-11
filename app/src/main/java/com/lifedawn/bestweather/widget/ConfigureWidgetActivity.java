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
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.ArrayMap;
import android.util.TypedValue;
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
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.databinding.ActivityConfigureWidgetBinding;
import com.lifedawn.bestweather.findaddress.FindAddressFragment;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.room.repository.FavoriteAddressRepository;

import org.jetbrains.annotations.NotNull;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
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
	private ArrayMap<Integer, TextView> textViewMap = new ArrayMap<>();
	private ArrayMap<Integer, Float> textSizeMap = new ArrayMap<>();

	private OnBackPressedCallback onBackPressedCallback = new OnBackPressedCallback(true) {
		@Override
		public void handleOnBackPressed() {
			if (!getSupportFragmentManager().popBackStackImmediate()) {
				finish();
			}
		}
	};

	private FragmentManager.FragmentLifecycleCallbacks fragmentLifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
		@Override
		public void onFragmentCreated(@NonNull @NotNull FragmentManager fm, @NonNull @NotNull Fragment f, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
			super.onFragmentCreated(fm, f, savedInstanceState);
			if (f instanceof FindAddressFragment) {
				binding.scrollView.setVisibility(View.GONE);
				binding.fragmentContainer.setVisibility(View.VISIBLE);
			}
		}

		@Override
		public void onFragmentDestroyed(@NonNull @NotNull FragmentManager fm, @NonNull @NotNull Fragment f) {
			super.onFragmentDestroyed(fm, f);
			if (f instanceof FindAddressFragment) {
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
		loadTextViewsAndTextSize();
		binding.previewWidgetContainer.addView(previewWidgetView);

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
				Class<?> className = null;

				switch (layoutId) {
					case R.layout.current_first:
						className = CurrentFirst.class;
						break;
					case R.layout.current_daily:
						className = CurrentDaily.class;
						break;
					case R.layout.current_hourly:
						className = CurrentHourly.class;
						break;
					case R.layout.current_hourly_daily:
						className = CurrentHourlyDaily.class;
						break;
				}


				Intent resultIntent = new Intent();
				resultIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
				setResult(RESULT_OK, resultIntent);

				ArrayList<TextSizeObj> textSizeObjList = new ArrayList<>();
				for (int i = 0; i < textSizeMap.size(); i++) {
					textSizeObjList.add(new TextSizeObj(textSizeMap.keyAt(i), textSizeMap.valueAt(i)));
				}

				LocationType locationType = binding.currentLocationRadio.isChecked() ? LocationType.CurrentLocation :
						LocationType.SelectedAddress;
				WeatherSourceType weatherSourceType = null;
				if (binding.accuWeatherRadio.isChecked()) {
					weatherSourceType = WeatherSourceType.ACCU_WEATHER;
				} else if (binding.owmRadio.isChecked()) {
					weatherSourceType = WeatherSourceType.OPEN_WEATHER_MAP;
				}

				CustomAttributeObj customAttributeObj = new CustomAttributeObj(appWidgetId, textSizeObjList,
						previewWidgetView.getBackground().getAlpha(), locationType, weatherSourceType,
						binding.kmaTopPrioritySwitch.isChecked(), autoRefreshInterval, binding.displayDatetimeSwitch.isChecked(),
						binding.displayLocalDatetimeSwitch.isChecked(), newSelectedAddressDto);

				Intent initIntent = new Intent(getApplicationContext(), className);
				initIntent.setAction(getString(R.string.ACTION_INIT));

				Bundle newBundle = new Bundle();
				newBundle.putSerializable(getString(R.string.bundle_key_widgetname), className);
				newBundle.putInt(getString(R.string.bundle_key_widget_layout_id), layoutId);
				newBundle.putSerializable(getString(R.string.bundle_key_widget_custom_attributes), customAttributeObj);
				newBundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
				initIntent.putExtras(newBundle);
				PendingIntent initPendingIntent = PendingIntent.getBroadcast(getApplicationContext(), 0, initIntent, 0);

				try {
					initPendingIntent.send();
				} catch (PendingIntent.CanceledException e) {
					e.printStackTrace();
				}

				finish();
			}
		});

	}

	@SuppressLint("NonConstantResourceId")
	private void loadTextViewsAndTextSize() {
		switch (layoutId) {
			case R.layout.current_first:
				createTextViewMap(R.id.address, R.id.refresh, R.id.current_temperature, R.id.current_realfeel_temperature, R.id.current_airquality, R.id.current_precipitation);
				createTextViewSizeMap(R.id.address, R.id.refresh, R.id.current_temperature, R.id.current_realfeel_temperature, R.id.current_airquality, R.id.current_precipitation);
				break;

			case R.layout.current_daily:
				createTextViewMap(R.id.address, R.id.refresh, R.id.current_temperature, R.id.current_realfeel_temperature, R.id.current_airquality, R.id.current_precipitation,
						R.id.daily_date1, R.id.daily_date2, R.id.daily_date3, R.id.daily_date4, R.id.daily_left_weather_icon1, R.id.daily_left_weather_icon2,
						R.id.daily_left_weather_icon3, R.id.daily_left_weather_icon4, R.id.daily_right_weather_icon1, R.id.daily_right_weather_icon2,
						R.id.daily_right_weather_icon3, R.id.daily_right_weather_icon4, R.id.daily_temperature1, R.id.daily_temperature2, R.id.daily_temperature3,
						R.id.daily_temperature4);
				createTextViewSizeMap(R.id.address, R.id.refresh, R.id.current_temperature, R.id.current_realfeel_temperature, R.id.current_airquality, R.id.current_precipitation,
						R.id.daily_date1, R.id.daily_date2, R.id.daily_date3, R.id.daily_date4, R.id.daily_left_weather_icon1, R.id.daily_left_weather_icon2,
						R.id.daily_left_weather_icon3, R.id.daily_left_weather_icon4, R.id.daily_right_weather_icon1, R.id.daily_right_weather_icon2,
						R.id.daily_right_weather_icon3, R.id.daily_right_weather_icon4, R.id.daily_temperature1, R.id.daily_temperature2, R.id.daily_temperature3,
						R.id.daily_temperature4);
				break;

			case R.layout.current_hourly:
				createTextViewMap(R.id.address, R.id.refresh, R.id.current_temperature, R.id.current_realfeel_temperature, R.id.current_airquality, R.id.current_precipitation,
						R.id.hourly_clock1, R.id.hourly_clock2, R.id.hourly_clock3, R.id.hourly_clock4, R.id.hourly_clock5, R.id.hourly_clock6, R.id.hourly_clock7, R.id.hourly_clock8,
						R.id.hourly_clock9,
						R.id.hourly_clock10, R.id.hourly_temperature1, R.id.hourly_temperature2, R.id.hourly_temperature3, R.id.hourly_temperature4, R.id.hourly_temperature5,
						R.id.hourly_temperature6, R.id.hourly_temperature7, R.id.hourly_temperature8, R.id.hourly_temperature9, R.id.hourly_temperature10);
				createTextViewSizeMap(R.id.address, R.id.refresh, R.id.current_temperature, R.id.current_realfeel_temperature, R.id.current_airquality, R.id.current_precipitation,
						R.id.hourly_clock1, R.id.hourly_clock2, R.id.hourly_clock3, R.id.hourly_clock4, R.id.hourly_clock5, R.id.hourly_clock6, R.id.hourly_clock7, R.id.hourly_clock8,
						R.id.hourly_clock9,
						R.id.hourly_clock10, R.id.hourly_temperature1, R.id.hourly_temperature2, R.id.hourly_temperature3, R.id.hourly_temperature4, R.id.hourly_temperature5,
						R.id.hourly_temperature6, R.id.hourly_temperature7, R.id.hourly_temperature8, R.id.hourly_temperature9, R.id.hourly_temperature10);
				break;

			case R.layout.current_hourly_daily:
				createTextViewMap(R.id.address, R.id.refresh, R.id.current_temperature, R.id.current_realfeel_temperature, R.id.current_airquality, R.id.current_precipitation,
						R.id.hourly_clock1, R.id.hourly_clock2, R.id.hourly_clock3, R.id.hourly_clock4, R.id.hourly_clock5, R.id.hourly_clock6, R.id.hourly_clock7, R.id.hourly_clock8,
						R.id.hourly_clock9,
						R.id.hourly_clock10, R.id.hourly_temperature1, R.id.hourly_temperature2, R.id.hourly_temperature3, R.id.hourly_temperature4, R.id.hourly_temperature5,
						R.id.hourly_temperature6, R.id.hourly_temperature7, R.id.hourly_temperature8, R.id.hourly_temperature9,
						R.id.hourly_temperature10, R.id.daily_date1, R.id.daily_date2, R.id.daily_date3, R.id.daily_date4, R.id.daily_left_weather_icon1, R.id.daily_left_weather_icon2,
						R.id.daily_left_weather_icon3, R.id.daily_left_weather_icon4, R.id.daily_right_weather_icon1, R.id.daily_right_weather_icon2,
						R.id.daily_right_weather_icon3, R.id.daily_right_weather_icon4, R.id.daily_temperature1, R.id.daily_temperature2, R.id.daily_temperature3,
						R.id.daily_temperature4);
				createTextViewSizeMap(R.id.address, R.id.refresh, R.id.current_temperature, R.id.current_realfeel_temperature, R.id.current_airquality, R.id.current_precipitation,
						R.id.hourly_clock1, R.id.hourly_clock2, R.id.hourly_clock3, R.id.hourly_clock4, R.id.hourly_clock5, R.id.hourly_clock6, R.id.hourly_clock7, R.id.hourly_clock8,
						R.id.hourly_clock9,
						R.id.hourly_clock10, R.id.hourly_temperature1, R.id.hourly_temperature2, R.id.hourly_temperature3, R.id.hourly_temperature4, R.id.hourly_temperature5,
						R.id.hourly_temperature6, R.id.hourly_temperature7, R.id.hourly_temperature8, R.id.hourly_temperature9,
						R.id.hourly_temperature10, R.id.daily_date1, R.id.daily_date2, R.id.daily_date3, R.id.daily_date4, R.id.daily_left_weather_icon1, R.id.daily_left_weather_icon2,
						R.id.daily_left_weather_icon3, R.id.daily_left_weather_icon4, R.id.daily_right_weather_icon1, R.id.daily_right_weather_icon2,
						R.id.daily_right_weather_icon3, R.id.daily_right_weather_icon4, R.id.daily_temperature1, R.id.daily_temperature2, R.id.daily_temperature3,
						R.id.daily_temperature4);
				break;

		}
	}

	private void createTextViewMap(int... layoutId) {
		for (int id : layoutId) {
			textViewMap.put(id, (TextView) previewWidgetView.findViewById(id));
		}
	}

	private void createTextViewSizeMap(int... layoutId) {
		for (int id : layoutId) {
			textSizeMap.put(id, textViewMap.get(id).getTextSize());
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
		binding.displayDatetimeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				binding.displayLocalDatetimeSwitch.setVisibility(isChecked ? View.VISIBLE : View.GONE);
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
						openFindAddressFragment();
					}
				}
			}
		});

		binding.changeAddressBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				openFindAddressFragment();
			}
		});
	}

	private void openFindAddressFragment() {
		FragmentManager fragmentManager = getSupportFragmentManager();
		fragmentManager.setFragmentResult(getString(R.string.key_from_widget_config_main_to_find_address), new Bundle());

		FindAddressFragment findAddressFragment = new FindAddressFragment();
		getSupportFragmentManager().beginTransaction().add(binding.fragmentContainer.getId(), findAddressFragment, getString(R.string.tag_find_address_fragment))
				.addToBackStack(getString(R.string.tag_find_address_fragment)).commit();

		fragmentManager.setFragmentResultListener(getString(R.string.key_back_from_find_address_to_widget_config_main), ConfigureWidgetActivity.this,
				new FragmentResultListener() {
					@Override
					public void onFragmentResult(@NonNull @NotNull String requestKey, @NonNull @NotNull Bundle result) {
						fragmentManager.clearFragmentResult(getString(R.string.key_from_widget_config_main_to_find_address));
						fragmentManager.clearFragmentResultListener(requestKey);

						final boolean selectedAddress = result.getBoolean(getString(R.string.bundle_key_selected_address_dto));
						if (selectedAddress) {
							int id = result.getInt(getString(R.string.bundle_key_new_favorite_address_dto_id));
							FavoriteAddressRepository favoriteAddressRepository =
									new FavoriteAddressRepository(getApplicationContext());

							favoriteAddressRepository.get(id, new DbQueryCallback<FavoriteAddressDto>() {
								@Override
								public void onResultSuccessful(FavoriteAddressDto result) {
									runOnUiThread(new Runnable() {
										@Override
										public void run() {
											newSelectedAddressDto = result;
											String text =
													getString(R.string.current_location) + ", " + newSelectedAddressDto.getAddress();
											binding.selectedLocationRadio.setText(text);
											binding.changeAddressBtn.setVisibility(View.VISIBLE);
										}
									});
								}

								@Override
								public void onResultNoData() {

								}
							});
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
		int viewId = 0;

		for (int i = 0; i < textViewMap.size(); i++) {
			viewId = textViewMap.keyAt(i);
			originalSize = textSizeMap.get(viewId);
			textViewMap.valueAt(i).setTextSize(TypedValue.COMPLEX_UNIT_PX, value < 0f ? originalSize - extraSize : originalSize + extraSize);
		}

	}

	private void setBackgroundAlpha(int alpha) {
		previewWidgetView.getBackground().setAlpha(255 - alpha);
	}

	public static class TextSizeObj implements Serializable {
		final int viewId;
		float textSize;

		public TextSizeObj(int viewId, float textSize) {
			this.viewId = viewId;
			this.textSize = textSize;
		}
	}

	public static class CustomAttributeObj implements Serializable {
		final int appWidgetId;
		final List<TextSizeObj> textSizeObjList;
		final int backgroundAlpha;
		final LocationType locationType;
		final WeatherSourceType weatherSourceType;
		final boolean topPriorityKma;
		final Long updateInterval;
		final boolean displayDateTime;
		final boolean displayLocalDateTime;
		final FavoriteAddressDto selectedAddressDto;

		public CustomAttributeObj(int appWidgetId, List<TextSizeObj> textSizeObjList, int backgroundAlpha, LocationType locationType,
		                          WeatherSourceType weatherSourceType, boolean topPriorityKma, Long updateInterval,
		                          boolean displayDateTime, boolean displayLocalDateTime, FavoriteAddressDto selectedAddressDto) {
			this.appWidgetId = appWidgetId;
			this.textSizeObjList = textSizeObjList;
			this.backgroundAlpha = backgroundAlpha;
			this.locationType = locationType;
			this.weatherSourceType = weatherSourceType;
			this.topPriorityKma = topPriorityKma;
			this.updateInterval = updateInterval;
			this.displayDateTime = displayDateTime;
			this.displayLocalDateTime = displayLocalDateTime;
			this.selectedAddressDto = selectedAddressDto;
		}
	}
}