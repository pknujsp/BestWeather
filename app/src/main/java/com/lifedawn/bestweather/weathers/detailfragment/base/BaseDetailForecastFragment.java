package com.lifedawn.bestweather.weathers.detailfragment.base;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.enums.ForecastViewType;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.commons.interfaces.OnClickedListViewItemListener;
import com.lifedawn.bestweather.databinding.BaseLayoutDetailForecastBinding;
import com.lifedawn.bestweather.databinding.ViewDetailDailyForecastListBinding;
import com.lifedawn.bestweather.databinding.ViewDetailHourlyForecastListBinding;
import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.weathers.models.DailyForecastDto;
import com.lifedawn.bestweather.weathers.models.HourlyForecastDto;

import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;

public abstract class BaseDetailForecastFragment extends Fragment implements OnClickedListViewItemListener<Integer> {
	protected BaseLayoutDetailForecastBinding binding;
	protected SharedPreferences sharedPreferences;
	protected ValueUnits tempUnit;
	protected ValueUnits windUnit;
	protected ValueUnits visibilityUnit;
	protected ValueUnits clockUnit;
	protected String addressName;
	protected ZoneId zoneId;
	protected Double latitude;
	protected Double longitude;
	protected ForecastViewType forecastViewType;
	protected ExecutorService executorService = MyApplication.getExecutorService();
	protected WeatherProviderType mainWeatherProviderType;
	protected Bundle bundle;

	protected boolean clickableItem = true;

	private final FragmentManager.FragmentLifecycleCallbacks fragmentLifecycleCallbacks = new FragmentManager.FragmentLifecycleCallbacks() {
		@Override
		public void onFragmentStarted(@NonNull FragmentManager fm, @NonNull Fragment f) {
			super.onFragmentStarted(fm, f);
			BaseDetailForecastFragment.this.onFragmentStarted(f);
		}
	};

	protected void onFragmentStarted(Fragment fragment) {
		clickableItem = true;
	}

	@Override
	public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		getChildFragmentManager().registerFragmentLifecycleCallbacks(fragmentLifecycleCallbacks, false);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

		tempUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_temp), ValueUnits.celsius.name()));
		windUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_wind), ValueUnits.mPerSec.name()));
		visibilityUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_visibility), ValueUnits.km.name()));
		clockUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_clock), ValueUnits.clock24.name()));
		forecastViewType = ForecastViewType.valueOf(sharedPreferences.getString(getString(R.string.pref_key_forecast_view_type),
				ForecastViewType.List.name()));

		bundle = savedInstanceState != null ? savedInstanceState : getArguments();
		addressName = bundle.getString(BundleKey.AddressName.name());
		zoneId = (ZoneId) bundle.getSerializable(BundleKey.TimeZone.name());
		latitude = bundle.getDouble(BundleKey.Latitude.name());
		longitude = bundle.getDouble(BundleKey.Longitude.name());
		mainWeatherProviderType = (WeatherProviderType) bundle.getSerializable(
				BundleKey.WeatherProvider.name());
	}

	@Nullable
	@org.jetbrains.annotations.Nullable
	@Override
	public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		binding = BaseLayoutDetailForecastBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		RelativeLayout.LayoutParams layoutParams = (RelativeLayout.LayoutParams) binding.toolbar.getRoot().getLayoutParams();
		layoutParams.topMargin = MyApplication.getStatusBarHeight();
		binding.toolbar.getRoot().setLayoutParams(layoutParams);

		AdRequest adRequest = new AdRequest.Builder().build();
		binding.adViewBottom.loadAd(adRequest);

		binding.toolbar.backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getParentFragmentManager().popBackStackImmediate();
			}
		});

		setDataViewsByList();
	}

	@Override
	public void onDestroy() {
		getChildFragmentManager().unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallbacks);
		super.onDestroy();
	}

	@Override
	public void onSaveInstanceState(@NonNull Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putAll(bundle);
	}

	abstract protected void setDataViewsByList();

	abstract protected void setDataViewsByTable();

	@Override
	public void onClickedItem(Integer position) {

	}

	public static class HourlyForecastListAdapter extends RecyclerView.Adapter<HourlyForecastListAdapter.ViewHolder> {
		private List<HourlyForecastDto> hourlyForecastDtoList = new ArrayList<>();
		private Context context;
		private OnClickedListViewItemListener<Integer> onClickedForecastItem;
		private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M.d E");
		private DateTimeFormatter hourFormatter = DateTimeFormatter.ofPattern("H");
		private final String tempDegree;
		private final String degree = "°";

		public HourlyForecastListAdapter(Context context, @Nullable OnClickedListViewItemListener<Integer> onClickedForecastItem) {
			this.context = context;
			this.onClickedForecastItem = onClickedForecastItem;
			tempDegree = MyApplication.VALUE_UNIT_OBJ.getTempUnitText();
		}

		public void setHourlyForecastDtoList(List<HourlyForecastDto> hourlyForecastDtoList) {
			this.hourlyForecastDtoList = hourlyForecastDtoList;
		}

		@NonNull
		@NotNull
		@Override
		public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
			return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.view_detail_hourly_forecast_list, null, false));
		}

		@Override
		public void onBindViewHolder(@NonNull @NotNull BaseDetailForecastFragment.HourlyForecastListAdapter.ViewHolder holder, int position) {
			holder.onBind(hourlyForecastDtoList.get(position));
		}

		@Override
		public int getItemCount() {
			return hourlyForecastDtoList.size();
		}

		public class ViewHolder extends RecyclerView.ViewHolder {
			private ViewDetailHourlyForecastListBinding binding;

			public ViewHolder(@NonNull @NotNull View itemView) {
				super(itemView);
				binding = ViewDetailHourlyForecastListBinding.bind(itemView);
				binding.getRoot().setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onClickedForecastItem.onClickedItem(getBindingAdapterPosition());
					}
				});
			}

			public void onBind(HourlyForecastDto hourlyForecastDto) {
				binding.date.setText(hourlyForecastDto.getHours().format(dateFormatter));
				binding.hours.setText(hourlyForecastDto.getHours().format(hourFormatter));
				binding.weatherIcon.setImageResource(hourlyForecastDto.getWeatherIcon());
				binding.temp.setText(hourlyForecastDto.getTemp().replace(tempDegree, degree));
				binding.pop.setText(hourlyForecastDto.getPop());

				if (hourlyForecastDto.isHasSnow()) {
					binding.snowVolume.setText(hourlyForecastDto.getSnowVolume());
					binding.snowVolumeLayout.setVisibility(View.VISIBLE);
				} else {
					binding.snowVolumeLayout.setVisibility(View.GONE);
				}

				if (hourlyForecastDto.isHasRain()) {
					binding.rainVolume.setText(hourlyForecastDto.getRainVolume());
					binding.rainVolumeLayout.setVisibility(View.VISIBLE);
				} else {
					binding.rainVolumeLayout.setVisibility(View.GONE);
				}
			}
		}
	}


	public static class DailyForecastListAdapter extends RecyclerView.Adapter<DailyForecastListAdapter.ViewHolder> {
		private List<DailyForecastDto> dailyForecastDtoList = new ArrayList<>();
		private Context context;
		private OnClickedListViewItemListener<Integer> onClickedForecastItem;
		private final String tempDegree;
		private final String degree = "°";

		private boolean hasPrecipitationVolume;

		private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M.d");
		private DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("E");

		public DailyForecastListAdapter(Context context,
		                                @Nullable OnClickedListViewItemListener<Integer> onClickedForecastItem) {
			this.context = context;
			this.onClickedForecastItem = onClickedForecastItem;
			tempDegree = MyApplication.VALUE_UNIT_OBJ.getTempUnitText();
		}

		public void setDailyForecastDtoList(List<DailyForecastDto> dailyForecastDtoList, boolean hasPrecipitationVolume) {
			this.dailyForecastDtoList = dailyForecastDtoList;
			this.hasPrecipitationVolume = hasPrecipitationVolume;
		}

		@NonNull
		@NotNull
		@Override
		public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
			return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.view_detail_daily_forecast_list, null, false));
		}

		@Override
		public void onBindViewHolder(@NonNull @NotNull BaseDetailForecastFragment.DailyForecastListAdapter.ViewHolder holder,
		                             int position) {
			holder.onBind(dailyForecastDtoList.get(position));
		}

		@Override
		public int getItemCount() {
			return dailyForecastDtoList.size();
		}

		public class ViewHolder extends RecyclerView.ViewHolder {
			private ViewDetailDailyForecastListBinding binding;

			public ViewHolder(@NonNull @NotNull View itemView) {
				super(itemView);
				binding = ViewDetailDailyForecastListBinding.bind(itemView);
				binding.getRoot().setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onClickedForecastItem.onClickedItem(getBindingAdapterPosition());
					}
				});
			}

			public void onBind(DailyForecastDto daily) {
				binding.volumeLayout.setVisibility(hasPrecipitationVolume ? View.VISIBLE : View.GONE);

				binding.date.setText(daily.getDate().format(dateFormatter));
				binding.day.setText(daily.getDate().format(dayFormatter));

				if (daily.isSingle()) {
					binding.pop.setText(daily.getSingleValues().getPop());

					if (!daily.getSingleValues().isHasSnowVolume()) {
						binding.snowVolumeLayout.setVisibility(View.GONE);
					} else {
						binding.snowVolume.setText(daily.getSingleValues().getSnowVolume());
						binding.snowVolumeLayout.setVisibility(View.VISIBLE);
					}

					if (!daily.getSingleValues().isHasRainVolume()) {
						binding.rainVolumeLayout.setVisibility(View.GONE);
					} else {
						binding.rainVolume.setText(daily.getSingleValues().getRainVolume());
						binding.rainVolumeLayout.setVisibility(View.VISIBLE);
					}

					binding.leftWeatherIcon.setImageResource(daily.getSingleValues().getWeatherIcon());
					binding.rightWeatherIcon.setVisibility(View.GONE);
				} else {
					binding.leftWeatherIcon.setImageResource(daily.getAmValues().getWeatherIcon());
					binding.rightWeatherIcon.setImageResource(daily.getPmValues().getWeatherIcon());
					binding.rightWeatherIcon.setVisibility(View.VISIBLE);

					String pop = daily.getAmValues().getPop() + " / " + daily.getPmValues().getPop();
					binding.pop.setText(pop);

					if (!daily.getAmValues().isHasSnowVolume() &&
							!daily.getPmValues().isHasSnowVolume()) {
						binding.snowVolumeLayout.setVisibility(View.GONE);
					} else {
						String snow = daily.getAmValues().getSnowVolume() + " / " +
								daily.getPmValues().getSnowVolume();
						binding.snowVolume.setText(snow);
						binding.snowVolumeLayout.setVisibility(View.VISIBLE);
					}

					if (!daily.getAmValues().isHasRainVolume() &&
							!daily.getPmValues().isHasRainVolume()) {
						binding.rainVolumeLayout.setVisibility(View.GONE);
					} else {
						String rain = daily.getAmValues().getRainVolume() + " / " +
								daily.getPmValues().getRainVolume();
						binding.snowVolume.setText(rain);
						binding.rainVolumeLayout.setVisibility(View.VISIBLE);
					}
				}

				binding.minTemp.setText(daily.getMinTemp().replace(tempDegree, degree));
				binding.maxTemp.setText(daily.getMaxTemp().replace(tempDegree, degree));
			}
		}
	}

}
