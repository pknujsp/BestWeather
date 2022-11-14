package com.lifedawn.bestweather.widget;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.PendingIntent;
import android.app.WallpaperManager;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProviderInfo;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.Window;
import android.view.WindowManager;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.CompoundButton;
import android.widget.FrameLayout;
import android.widget.RadioGroup;
import android.widget.RemoteViews;
import android.widget.SeekBar;
import android.widget.SpinnerAdapter;
import android.widget.Toast;

import com.bumptech.glide.Glide;
import com.google.android.material.slider.Slider;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.MainThreadWorker;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.IntentRequestCodes;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.databinding.ActivityConfigureWidgetBinding;
import com.lifedawn.bestweather.findaddress.map.MapFragment;
import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.room.dto.WidgetDto;
import com.lifedawn.bestweather.widget.creator.AbstractWidgetCreator;
import com.lifedawn.bestweather.widget.creator.EighthWidgetCreator;
import com.lifedawn.bestweather.widget.creator.EleventhWidgetCreator;
import com.lifedawn.bestweather.widget.creator.FirstWidgetCreator;
import com.lifedawn.bestweather.widget.creator.NinthWidgetCreator;
import com.lifedawn.bestweather.widget.creator.SecondWidgetCreator;
import com.lifedawn.bestweather.widget.creator.SeventhWidgetCreator;
import com.lifedawn.bestweather.widget.creator.SixthWidgetCreator;
import com.lifedawn.bestweather.widget.creator.TenthWidgetCreator;
import com.lifedawn.bestweather.widget.creator.ThirdWidgetCreator;
import com.lifedawn.bestweather.widget.creator.FourthWidgetCreator;
import com.lifedawn.bestweather.widget.creator.FifthWidgetCreator;
import com.lifedawn.bestweather.widget.widgetprovider.EighthWidgetProvider;
import com.lifedawn.bestweather.widget.widgetprovider.EleventhWidgetProvider;
import com.lifedawn.bestweather.widget.widgetprovider.FirstWidgetProvider;
import com.lifedawn.bestweather.widget.widgetprovider.NinthWidgetProvider;
import com.lifedawn.bestweather.widget.widgetprovider.SecondWidgetProvider;
import com.lifedawn.bestweather.widget.widgetprovider.SeventhWidgetProvider;
import com.lifedawn.bestweather.widget.widgetprovider.SixthWidgetProvider;
import com.lifedawn.bestweather.widget.widgetprovider.TenthWidgetProvider;
import com.lifedawn.bestweather.widget.widgetprovider.ThirdWidgetProvider;
import com.lifedawn.bestweather.widget.widgetprovider.FourthWidgetProvider;
import com.lifedawn.bestweather.widget.widgetprovider.FifthWidgetProvider;

import org.jetbrains.annotations.NotNull;

import java.util.List;

public class ConfigureWidgetActivity extends AppCompatActivity implements AbstractWidgetCreator.WidgetUpdateCallback {
	private ActivityConfigureWidgetBinding binding;
	private Integer appWidgetId;
	private Integer layoutId;
	private WidgetDto widgetDto;

	private Long[] widgetRefreshIntervalValues;
	private int originalWidgetRefreshIntervalValueIndex;
	private int widgetRefreshIntervalValueIndex;

	private int widgetHeight;
	private int widgetWidth;

	private FavoriteAddressDto newSelectedAddressDto;

	private AbstractWidgetCreator widgetCreator;
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
		public void onFragmentAttached(@NonNull FragmentManager fm, @NonNull Fragment f, @NonNull Context context) {
			super.onFragmentAttached(fm, f, context);

			if (f instanceof MapFragment) {
				binding.widgetSettingsContainer.setVisibility(View.GONE);
				binding.fragmentContainer.setVisibility(View.VISIBLE);
			}
		}


		@Override
		public void onFragmentDestroyed(@NonNull @NotNull FragmentManager fm, @NonNull @NotNull Fragment f) {
			super.onFragmentDestroyed(fm, f);

			if (f instanceof MapFragment) {
				binding.widgetSettingsContainer.setVisibility(View.VISIBLE);
				binding.fragmentContainer.setVisibility(View.GONE);
			}
		}
	};


	@Override
	public void onRequestPermissionsResult(int requestCode, String[] permissions, int[] grantResults) {
		super.onRequestPermissionsResult(requestCode, permissions, grantResults);
		boolean denied = false;
		for (int result : grantResults) {
			if (result == PackageManager.PERMISSION_DENIED) {
				denied = true;
				break;
			}
		}
		if (denied) {
			binding.loadingAnimation.setVisibility(View.GONE);
		} else {
			setBackgroundImg();
		}
	}

	private void setBackgroundImg() {
		if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE)
				== PackageManager.PERMISSION_GRANTED) {
			MyApplication.getExecutorService().submit(() -> {
				WallpaperManager wallpaperManager = WallpaperManager.getInstance(getApplicationContext());
				Drawable wallpaperDrawable = wallpaperManager.getDrawable();

				if (!isFinishing()) {
					MainThreadWorker.runOnUiThread(() -> {
						Glide.with(ConfigureWidgetActivity.this).load(wallpaperDrawable).into(binding.wallpaper);
						binding.loadingAnimation.setVisibility(View.GONE);
					});
				}
			});
		} else {
			ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, 3);

		}

	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		binding = ActivityConfigureWidgetBinding.inflate(getLayoutInflater());
		setContentView(binding.getRoot());

		binding.displayDatetimeSwitch.setVisibility(View.GONE);
		binding.displayLocalDatetimeSwitch.setVisibility(View.GONE);

		getSupportFragmentManager().registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false);
		getOnBackPressedDispatcher().addCallback(this, onBackPressedCallback);

		Bundle bundle = getIntent().getExtras();

		if (bundle != null) {
			appWidgetId = bundle.getInt(
					AppWidgetManager.EXTRA_APPWIDGET_ID,
					AppWidgetManager.INVALID_APPWIDGET_ID);
		}

		appWidgetManager = AppWidgetManager.getInstance(this);
		final AppWidgetProviderInfo appWidgetProviderInfo = appWidgetManager.getAppWidgetInfo(appWidgetId);
		layoutId = appWidgetProviderInfo.initialLayout;
		ComponentName componentName = appWidgetProviderInfo.provider;
		final String widgetProviderClassName = componentName.getClassName();

		if (widgetProviderClassName.equals(FirstWidgetProvider.class.getName())) {
			widgetCreator = new FirstWidgetCreator(getApplicationContext(), this, appWidgetId);
		} else if (widgetProviderClassName.equals(SecondWidgetProvider.class.getName())) {
			widgetCreator = new SecondWidgetCreator(getApplicationContext(), this, appWidgetId);
		} else if (widgetProviderClassName.equals(ThirdWidgetProvider.class.getName())) {
			widgetCreator = new ThirdWidgetCreator(getApplicationContext(), this, appWidgetId);
		} else if (widgetProviderClassName.equals(FourthWidgetProvider.class.getName())) {
			widgetCreator = new FourthWidgetCreator(getApplicationContext(), this, appWidgetId);
		} else if (widgetProviderClassName.equals(FifthWidgetProvider.class.getName())) {
			widgetCreator = new FifthWidgetCreator(getApplicationContext(), this, appWidgetId);
		} else if (widgetProviderClassName.equals(SixthWidgetProvider.class.getName())) {
			widgetCreator = new SixthWidgetCreator(getApplicationContext(), this, appWidgetId);
		} else if (widgetProviderClassName.equals(SeventhWidgetProvider.class.getName())) {
			widgetCreator = new SeventhWidgetCreator(getApplicationContext(), this, appWidgetId);
			binding.weatherDataSourceLayout.setVisibility(View.GONE);
		} else if (widgetProviderClassName.equals(EighthWidgetProvider.class.getName())) {
			widgetCreator = new EighthWidgetCreator(getApplicationContext(), this, appWidgetId);
		} else if (widgetProviderClassName.equals(NinthWidgetProvider.class.getName())) {
			widgetCreator = new NinthWidgetCreator(getApplicationContext(), this, appWidgetId);
		} else if (widgetProviderClassName.equals(TenthWidgetProvider.class.getName())) {
			widgetCreator = new TenthWidgetCreator(getApplicationContext(), this, appWidgetId);
		} else if (widgetProviderClassName.equals(EleventhWidgetProvider.class.getName())) {
			widgetCreator = new EleventhWidgetCreator(getApplicationContext(), this, appWidgetId);
			binding.kmaTopPrioritySwitch.setText(R.string.containsKma);
			binding.weatherDataSourceLayout.setVisibility(View.GONE);
		}

		widgetDto = widgetCreator.loadDefaultSettings();
		widgetHeight = (int) (appWidgetProviderInfo.minHeight * 1.9);

		FrameLayout.LayoutParams layoutParams = (FrameLayout.LayoutParams) binding.previewLayout.getLayoutParams();
		layoutParams.topMargin = MyApplication.getStatusBarHeight() + (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f,
				getResources().getDisplayMetrics());
		layoutParams.height = widgetHeight;

		binding.previewLayout.setLayoutParams(layoutParams);

		//위치, 날씨제공사, 대한민국 최우선, 자동 업데이트 간격, 날짜와 시각표시,
		//현지 시각으로 표시, 글자크기, 배경 투명도
		initLocation();
		initWeatherProvider();
		initAutoRefreshInterval();
		initDisplayDateTime();
		initTextSize();
		initBackground();
		setBackgroundImg();

		binding.currentLocationRadio.setChecked(true);

		binding.save.setOnClickListener(v -> widgetCreator.saveSettings(widgetDto, new DbQueryCallback<WidgetDto>() {
			@Override
			public void onResultSuccessful(WidgetDto result) {
				MainThreadWorker.runOnUiThread(new Runnable() {
					@Override
					public void run() {
						if (originalWidgetRefreshIntervalValueIndex != widgetRefreshIntervalValueIndex) {
							PreferenceManager.getDefaultSharedPreferences(getApplicationContext())
									.edit().putLong(getString(R.string.pref_key_widget_refresh_interval),
											widgetRefreshIntervalValues[widgetRefreshIntervalValueIndex]).commit();

							WidgetHelper widgetHelper = new WidgetHelper(getApplicationContext());
							widgetHelper.onSelectedAutoRefreshInterval(widgetRefreshIntervalValues[widgetRefreshIntervalValueIndex]);
						}

						Intent resultValue = new Intent();
						resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
						setResult(RESULT_OK, resultValue);

						Bundle initBundle = new Bundle();
						initBundle.putInt(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);

						Intent intent = new Intent(getApplicationContext(), widgetCreator.widgetProviderClass());
						intent.setAction(getString(R.string.com_lifedawn_bestweather_action_INIT));
						intent.putExtras(initBundle);

						PendingIntent pendingIntent = PendingIntent.getBroadcast(getApplicationContext(), IntentRequestCodes.WIDGET_MANUALLY_REFRESH.requestCode,
								intent, PendingIntent.FLAG_IMMUTABLE | PendingIntent.FLAG_UPDATE_CURRENT);
						try {
							pendingIntent.send();
						} catch (PendingIntent.CanceledException e) {
							e.printStackTrace();
						}

						finishAndRemoveTask();
					}
				});
			}

			@Override
			public void onResultNoData() {

			}
		}));


		binding.getRoot().getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
			@Override
			public void onGlobalLayout() {
				binding.getRoot().getViewTreeObserver().removeOnGlobalLayoutListener(this);
				widgetWidth = binding.previewLayout.getWidth();
				updatePreview();
			}
		});
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
			@SuppressLint("RestrictedApi")
			@Override
			public void onStartTrackingTouch(@NonNull Slider slider) {
				setTextSizeInWidget((int) slider.getValue());

			}

			@SuppressLint("RestrictedApi")
			@Override
			public void onStopTrackingTouch(@NonNull Slider slider) {
				setTextSizeInWidget((int) slider.getValue());
			}
		});
	}

	private void initDisplayDateTime() {
		binding.displayDatetimeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				binding.displayLocalDatetimeSwitch.setVisibility(isChecked ? View.VISIBLE : View.GONE);
				widgetDto.setDisplayClock(isChecked);
				updatePreview();
			}
		});
		binding.displayLocalDatetimeSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				widgetDto.setDisplayLocalClock(isChecked);
			}
		});
	}

	private void initAutoRefreshInterval() {
		final String[] intervalsDescription = getResources().getStringArray(R.array.AutoRefreshIntervals);
		final String[] intervalsStr = getResources().getStringArray(R.array.AutoRefreshIntervalsLong);
		widgetRefreshIntervalValues = new Long[intervalsStr.length];

		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getApplicationContext());
		Long currentValue = sharedPreferences.getLong(getString(R.string.pref_key_widget_refresh_interval), 0L);

		for (int i = 0; i < intervalsStr.length; i++) {
			widgetRefreshIntervalValues[i] = Long.parseLong(intervalsStr[i]);
		}

		SpinnerAdapter spinnerAdapter = new ArrayAdapter<String>(getApplicationContext(), android.R.layout.simple_list_item_1, intervalsDescription);
		binding.autoRefreshIntervalSpinner.setAdapter(spinnerAdapter);

		binding.autoRefreshIntervalSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
			@Override
			public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
				widgetRefreshIntervalValueIndex = position;
			}

			@Override
			public void onNothingSelected(AdapterView<?> parent) {

			}
		});

		widgetRefreshIntervalValueIndex = 0;
		for (String v : intervalsStr) {
			if (currentValue.toString().equals(v)) {
				break;
			}
			widgetRefreshIntervalValueIndex++;
		}

		originalWidgetRefreshIntervalValueIndex = widgetRefreshIntervalValueIndex;
		binding.autoRefreshIntervalSpinner.setSelection(widgetRefreshIntervalValueIndex);
	}

	private void initWeatherProvider() {
		binding.weatherDataSourceRadioGroup.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(RadioGroup group, int checkedId) {
				widgetDto.addWeatherProviderType(checkedId == R.id.owm_radio ? WeatherProviderType.OWM_ONECALL : WeatherProviderType.MET_NORWAY);
			}
		});

		binding.kmaTopPrioritySwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
			@Override
			public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
				widgetDto.setTopPriorityKma(isChecked);
			}
		});

		if (PreferenceManager.getDefaultSharedPreferences(getApplicationContext()).getBoolean(getString(R.string.pref_key_open_weather_map), false)) {
			binding.owmRadio.setChecked(true);
		} else {
			binding.metNorwayRadio.setChecked(true);
		}
	}

	private void initLocation() {
		binding.changeAddressBtn.setVisibility(View.GONE);
		binding.selectedAddressName.setVisibility(View.GONE);

		binding.locationRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
			if (checkedId == binding.currentLocationRadio.getId() && binding.currentLocationRadio.isChecked()) {
				binding.changeAddressBtn.setVisibility(View.GONE);
				binding.selectedAddressName.setVisibility(View.GONE);

				widgetDto.setLocationType(LocationType.CurrentLocation);
			} else if (checkedId == binding.selectedLocationRadio.getId() && binding.selectedLocationRadio.isChecked()) {
				binding.changeAddressBtn.setVisibility(View.VISIBLE);
				binding.selectedAddressName.setVisibility(View.VISIBLE);

				widgetDto.setLocationType(LocationType.SelectedAddress);

				if (!selectedFavoriteLocation) {
					openFavoritesFragment();
				}
			}
		});

		binding.changeAddressBtn.setOnClickListener(v -> openFavoritesFragment());

	}

	private void openFavoritesFragment() {
		MapFragment mapFragment = new MapFragment();
		Bundle bundle = new Bundle();
		bundle.putString(BundleKey.RequestFragment.name(), ConfigureWidgetActivity.class.getName());

		mapFragment.setArguments(bundle);
		mapFragment.setOnResultFavoriteListener(new MapFragment.OnResultFavoriteListener() {
			@Override
			public void onAddedNewAddress(FavoriteAddressDto newFavoriteAddressDto, List<FavoriteAddressDto> favoriteAddressDtoList, boolean removed) {
				onClickedAddress(newFavoriteAddressDto);
				getSupportFragmentManager().popBackStack();
			}

			@Override
			public void onResult(List<FavoriteAddressDto> favoriteAddressDtoList) {

			}

			@Override
			public void onClickedAddress(@Nullable FavoriteAddressDto favoriteSelectedAddressDto) {
				if (favoriteSelectedAddressDto == null) {
					if (!selectedFavoriteLocation) {
						Toast.makeText(getApplicationContext(), R.string.not_selected_address, Toast.LENGTH_SHORT).show();
						binding.currentLocationRadio.setChecked(true);
					}
				} else {
					selectedFavoriteLocation = true;

					newSelectedAddressDto = favoriteSelectedAddressDto;

					WidgetDto widgetDto = widgetCreator.getWidgetDto();
					widgetDto.setAddressName(newSelectedAddressDto.getDisplayName());
					widgetDto.setCountryCode(newSelectedAddressDto.getCountryCode());
					widgetDto.setLatitude(Double.parseDouble(newSelectedAddressDto.getLatitude()));
					widgetDto.setLongitude(Double.parseDouble(newSelectedAddressDto.getLongitude()));
					widgetDto.setTimeZoneId(newSelectedAddressDto.getZoneId());

					binding.selectedAddressName.setText(newSelectedAddressDto.getDisplayName());
					getSupportFragmentManager().popBackStack();
				}
			}
		});

		String tag = com.lifedawn.bestweather.findaddress.map.MapFragment.class.getName();
		getSupportFragmentManager().beginTransaction().add(binding.fragmentContainer.getId(), mapFragment, tag)
				.addToBackStack(tag).commitAllowingStateLoss();
	}

	private void setTextSizeInWidget(int value) {
		//widgetCreator.setTextSize(value);
		//updatePreview();
	}

	private void setBackgroundAlpha(int alpha) {
		widgetDto.setBackgroundAlpha(100 - alpha);
		updatePreview();
	}

	@Override
	public void updatePreview() {
		binding.previewLayout.removeAllViews();
		RemoteViews removeViews = widgetCreator.createTempViews(widgetWidth, widgetHeight);
		View view = removeViews.apply(getApplicationContext(), binding.previewLayout);

		binding.previewLayout.addView(view);
	}
}