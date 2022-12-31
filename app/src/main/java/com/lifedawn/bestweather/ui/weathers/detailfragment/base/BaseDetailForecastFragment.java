package com.lifedawn.bestweather.ui.weathers.detailfragment.base;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.google.android.gms.ads.AdRequest;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.constants.BundleKey;
import com.lifedawn.bestweather.commons.constants.ValueUnits;
import com.lifedawn.bestweather.commons.constants.WeatherProviderType;
import com.lifedawn.bestweather.commons.interfaces.OnClickedListViewItemListener;
import com.lifedawn.bestweather.databinding.BaseLayoutDetailForecastBinding;
import com.lifedawn.bestweather.databinding.ViewDetailDailyForecastListBinding;
import com.lifedawn.bestweather.databinding.ViewDetailHourlyForecastListBinding;
import com.lifedawn.bestweather.data.MyApplication;
import com.lifedawn.bestweather.ui.weathers.models.DailyForecastDto;
import com.lifedawn.bestweather.ui.weathers.models.HourlyForecastDto;

import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.ExecutorService;

public abstract class BaseDetailForecastFragment extends Fragment implements OnClickedListViewItemListener<Integer> {
	protected BaseLayoutDetailForecastBinding binding;
	protected ValueUnits tempUnit;
	protected ValueUnits windUnit;
	protected ValueUnits visibilityUnit;
	protected ValueUnits clockUnit;
	protected String addressName;
	protected ZoneId zoneId;
	protected Double latitude;
	protected Double longitude;
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

		tempUnit = MyApplication.VALUE_UNIT_OBJ.getTempUnit();
		windUnit = MyApplication.VALUE_UNIT_OBJ.getWindUnit();
		visibilityUnit = MyApplication.VALUE_UNIT_OBJ.getVisibilityUnit();
		clockUnit = MyApplication.VALUE_UNIT_OBJ.getClockUnit();

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
		binding = BaseLayoutDetailForecastBinding.inflate(inflater, container, false);
		binding.toolbar.backBtn.setOnClickListener(v -> requireActivity().getOnBackPressedDispatcher().onBackPressed());

		ConstraintLayout.LayoutParams layoutParams = (ConstraintLayout.LayoutParams) binding.toolbar.getRoot().getLayoutParams();
		layoutParams.topMargin = MyApplication.getStatusBarHeight();
		binding.toolbar.getRoot().setLayoutParams(layoutParams);

		return binding.getRoot();
	}

	@SuppressLint("MissingPermission")
	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		AdRequest adRequest = new AdRequest.Builder().build();
		binding.adViewBottom.loadAd(adRequest);

		setDataViewsByList();
	}

	@Override
	public void onDestroy() {
		getChildFragmentManager().unregisterFragmentLifecycleCallbacks(fragmentLifecycleCallbacks);
		super.onDestroy();
	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		binding = null;
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
		public List<HourlyForecastDto> hourlyForecastDtoList = new ArrayList<>();
		private OnClickedListViewItemListener<Integer> onClickedForecastItem;
		private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M.d E");
		private DateTimeFormatter hourFormatter = DateTimeFormatter.ofPattern("H");
		private final String tempDegree;
		private final String degree = "°";
		private final ShowDataType showDataType;

		public enum ShowDataType {
			Precipitation, Humidity, Wind
		}

		public HourlyForecastListAdapter(@Nullable OnClickedListViewItemListener<Integer> onClickedForecastItem, ShowDataType showDataType) {
			this.onClickedForecastItem = onClickedForecastItem;
			tempDegree = MyApplication.VALUE_UNIT_OBJ.getTempUnitText();
			setHasStableIds(true);

			this.showDataType = showDataType;
		}

		@Override
		public long getItemId(int position) {
			return (long) position;
		}

		public void setHourlyForecastDtoList(List<HourlyForecastDto> hourlyForecastDtoList) {
			this.hourlyForecastDtoList = hourlyForecastDtoList;
		}

		@NonNull
		@NotNull
		@Override
		public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
			return new ViewHolder(
					ViewDetailHourlyForecastListBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false));
		}

		@Override
		public void onBindViewHolder(@NonNull @NotNull BaseDetailForecastFragment.HourlyForecastListAdapter.ViewHolder holder, int position) {
			holder.onBind(hourlyForecastDtoList.get(position));
		}

		@Override
		public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
			super.onViewDetachedFromWindow(holder);
			Glide.with(holder.itemView.getContext()).clear(holder.binding.weatherIcon);
		}

		@Override
		public int getItemCount() {
			return hourlyForecastDtoList.size();
		}

		private class ViewHolder extends RecyclerView.ViewHolder {
			private final ViewDetailHourlyForecastListBinding binding;
			private final String noData;

			public ViewHolder(@NonNull @NotNull ViewDetailHourlyForecastListBinding binding) {
				super(binding.getRoot());
				this.binding = binding;
				this.binding.getRoot().setOnClickListener(new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						onClickedForecastItem.onClickedItem(getBindingAdapterPosition());
					}
				});
				noData = binding.getRoot().getContext().getString(R.string.noData);
			}

			public void onBind(HourlyForecastDto hourlyForecastDto) {
				binding.date.setText(hourlyForecastDto.getHours().format(dateFormatter));
				binding.hours.setText(hourlyForecastDto.getHours().format(hourFormatter));

				Glide.with(binding.weatherIcon).load(hourlyForecastDto.getWeatherIcon())
						.into(binding.weatherIcon);

				binding.temp.setText(hourlyForecastDto.getTemp().replace(tempDegree, degree));
				binding.pop.setText(hourlyForecastDto.getPop() == null ? "-" : hourlyForecastDto.getPop());

				if (showDataType == ShowDataType.Precipitation) {
					if (hourlyForecastDto.isHasSnow()) {
						binding.snowVolume.setText(hourlyForecastDto.getSnowVolume());
						binding.snowVolumeLayout.setVisibility(View.VISIBLE);
					} else {
						binding.snowVolumeLayout.setVisibility(View.GONE);
					}

					if (hourlyForecastDto.isHasRain() || hourlyForecastDto.isHasPrecipitation()) {
						binding.rainVolume.setText(hourlyForecastDto.isHasRain() ? hourlyForecastDto.getRainVolume() :
								hourlyForecastDto.getPrecipitationVolume());
						binding.rainVolumeLayout.setVisibility(View.VISIBLE);
					} else {
						binding.rainVolumeLayout.setVisibility(View.GONE);
					}

					binding.topIcon.setImageResource(R.drawable.raindrop);
					binding.topIcon.setVisibility(View.VISIBLE);

					binding.bottomIcon.setImageResource(R.drawable.snowparticle);
					binding.bottomIcon.setRotation(0);
				} else if (showDataType == ShowDataType.Wind) {
					binding.topIcon.setVisibility(View.GONE);
					binding.bottomIcon.setImageResource(R.drawable.arrow);
					binding.bottomIcon.setRotation(hourlyForecastDto.getWindDirectionVal() + 180);

					binding.rainVolume.setText(hourlyForecastDto.getWindSpeed() == null ?
							noData : hourlyForecastDto.getWindSpeed());
					binding.snowVolume.setText(hourlyForecastDto.getWindDirection() == null ?
							noData : hourlyForecastDto.getWindDirection());

					binding.rainVolumeLayout.setVisibility(View.VISIBLE);
					binding.snowVolumeLayout.setVisibility(View.VISIBLE);
				} else if (showDataType == ShowDataType.Humidity) {
					binding.rainVolumeLayout.setVisibility(View.VISIBLE);
					binding.snowVolumeLayout.setVisibility(View.GONE);
					binding.topIcon.setVisibility(View.VISIBLE);

					binding.topIcon.setImageResource(R.drawable.humidity);
					binding.rainVolume.setText(hourlyForecastDto.getHumidity());
				}

			}
		}
	}


	public static class DailyForecastListAdapter extends RecyclerView.Adapter<DailyForecastListAdapter.ViewHolder> {
		public List<DailyForecastDto> dailyForecastDtoList = new ArrayList<>();
		private final OnClickedListViewItemListener<Integer> onClickedForecastItem;
		private final String tempDegree;
		private final String degree = "°";

		private boolean hasPrecipitationVolume;

		private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M.d");
		private final DateTimeFormatter dayFormatter = DateTimeFormatter.ofPattern("E");

		public DailyForecastListAdapter(@Nullable OnClickedListViewItemListener<Integer> onClickedForecastItem) {
			this.onClickedForecastItem = onClickedForecastItem;
			setHasStableIds(true);
			tempDegree = MyApplication.VALUE_UNIT_OBJ.getTempUnitText();
		}

		public void setDailyForecastDtoList(List<DailyForecastDto> dailyForecastDtoList, boolean hasPrecipitationVolume) {
			this.dailyForecastDtoList = dailyForecastDtoList;
			this.hasPrecipitationVolume = hasPrecipitationVolume;
		}

		@Override
		public long getItemId(int position) {
			return (long) position;
		}

		@NonNull
		@NotNull
		@Override
		public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
			return new ViewHolder(ViewDetailDailyForecastListBinding.
					inflate(LayoutInflater.from(parent.getContext()), parent, false));
		}

		@Override
		public void onBindViewHolder(@NonNull @NotNull BaseDetailForecastFragment.DailyForecastListAdapter.ViewHolder holder,
		                             int position) {
			holder.onBind(dailyForecastDtoList.get(position));
		}

		@Override
		public void onViewDetachedFromWindow(@NonNull ViewHolder holder) {
			super.onViewDetachedFromWindow(holder);
			Context context = holder.binding.getRoot().getContext().getApplicationContext();

			Glide.with(context).clear(holder.binding.leftWeatherIcon);
			Glide.with(context).clear(holder.binding.rightWeatherIcon);
		}

		@Override
		public int getItemCount() {
			return dailyForecastDtoList.size();
		}

		private class ViewHolder extends RecyclerView.ViewHolder {
			private final ViewDetailDailyForecastListBinding binding;

			public ViewHolder(@NonNull @NotNull ViewDetailDailyForecastListBinding binding) {
				super(binding.getRoot());
				this.binding = binding;
				this.binding.getRoot().setOnClickListener(v ->
						onClickedForecastItem.onClickedItem(getBindingAdapterPosition()));
			}

			public void onBind(DailyForecastDto daily) {
				binding.volumeLayout.setVisibility(hasPrecipitationVolume ? View.VISIBLE : View.GONE);

				binding.date.setText(daily.getDate().format(dateFormatter));
				binding.day.setText(daily.getDate().format(dayFormatter));

				if (daily.getValuesList().size() == 1) {
					binding.pop.setText(daily.getValuesList().get(0).getPop());

					if (!daily.getValuesList().get(0).isHasSnowVolume()) {
						binding.snowVolumeLayout.setVisibility(View.GONE);
					} else {
						binding.snowVolume.setText(daily.getValuesList().get(0).getSnowVolume());
						binding.snowVolumeLayout.setVisibility(View.VISIBLE);
					}

					if (!daily.getValuesList().get(0).isHasRainVolume()) {
						binding.rainVolumeLayout.setVisibility(View.GONE);
					} else {
						binding.rainVolume.setText(daily.getValuesList().get(0).getRainVolume());
						binding.rainVolumeLayout.setVisibility(View.VISIBLE);
					}

					binding.leftWeatherIcon.setImageResource(daily.getValuesList().get(0).getWeatherIcon());
					binding.rightWeatherIcon.setVisibility(View.GONE);
				} else if (daily.getValuesList().size() == 2) {
					binding.leftWeatherIcon.setImageResource(daily.getValuesList().get(0).getWeatherIcon());
					binding.rightWeatherIcon.setImageResource(daily.getValuesList().get(1).getWeatherIcon());
					binding.rightWeatherIcon.setVisibility(View.VISIBLE);

					String pop = daily.getValuesList().get(0).getPop() + " / " + daily.getValuesList().get(1).getPop();
					binding.pop.setText(pop);

					if (!daily.getValuesList().get(0).isHasSnowVolume() &&
							!daily.getValuesList().get(1).isHasSnowVolume()) {
						binding.snowVolumeLayout.setVisibility(View.GONE);
					} else {
						String snow = daily.getValuesList().get(0).getSnowVolume() + " / " +
								daily.getValuesList().get(1).getSnowVolume();
						binding.snowVolume.setText(snow);
						binding.snowVolumeLayout.setVisibility(View.VISIBLE);
					}

					if (!daily.getValuesList().get(0).isHasRainVolume() &&
							!daily.getValuesList().get(1).isHasRainVolume()) {
						binding.rainVolumeLayout.setVisibility(View.GONE);
					} else {
						String rain = daily.getValuesList().get(0).getRainVolume() + " / " +
								daily.getValuesList().get(1).getRainVolume();
						binding.rainVolume.setText(rain);
						binding.rainVolumeLayout.setVisibility(View.VISIBLE);
					}
				} else if (daily.getValuesList().size() == 4) {
					binding.leftWeatherIcon.setImageResource(daily.getValuesList().get(1).getWeatherIcon());
					binding.rightWeatherIcon.setImageResource(daily.getValuesList().get(2).getWeatherIcon());
					binding.rightWeatherIcon.setVisibility(View.VISIBLE);
					binding.pop.setText("-");

					if (daily.getValuesList().get(0).isHasPrecipitationVolume() || daily.getValuesList().get(1).isHasPrecipitationVolume() ||
							daily.getValuesList().get(2).isHasPrecipitationVolume() || daily.getValuesList().get(3).isHasPrecipitationVolume()) {
						String mm = "mm";

						float leftVol =
								Float.parseFloat(daily.getValuesList().get(0).getPrecipitationVolume().replace(mm, ""))
										+ Float.parseFloat(daily.getValuesList().get(1).getPrecipitationVolume().replace(mm, ""));
						float rightVol =
								Float.parseFloat(daily.getValuesList().get(2).getPrecipitationVolume().replace(mm, ""))
										+ Float.parseFloat(daily.getValuesList().get(3).getPrecipitationVolume().replace(mm, ""));

						String rain = String.format(Locale.getDefault(), "%.1fmm", leftVol) + " / " +
								String.format(Locale.getDefault(), "%.1fmm", rightVol);
						binding.rainVolume.setText(rain);
						binding.rainVolumeLayout.setVisibility(View.VISIBLE);

					} else {
						binding.rainVolumeLayout.setVisibility(View.GONE);
					}

					binding.snowVolumeLayout.setVisibility(View.GONE);
				}

				binding.minTemp.setText(daily.getMinTemp().replace(tempDegree, degree));
				binding.maxTemp.setText(daily.getMaxTemp().replace(tempDegree, degree));
			}
		}
	}

}
