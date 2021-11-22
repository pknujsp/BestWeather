package com.lifedawn.bestweather.weathers.detailfragment.base;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.RelativeLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.ForecastViewType;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.commons.interfaces.OnClickedListViewItemListener;
import com.lifedawn.bestweather.databinding.BaseLayoutDetailForecastBinding;
import com.lifedawn.bestweather.databinding.ViewDetailDailyForecastListBinding;
import com.lifedawn.bestweather.databinding.ViewDetailHourlyForecastListBinding;
import com.lifedawn.bestweather.theme.AppTheme;
import com.lifedawn.bestweather.weathers.view.DateView;

import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public abstract class BaseDetailForecastFragment extends Fragment {
	protected BaseLayoutDetailForecastBinding binding;
	protected SharedPreferences sharedPreferences;
	protected ValueUnits tempUnit;
	protected ValueUnits windUnit;
	protected ValueUnits visibilityUnit;
	protected ValueUnits clockUnit;
	protected String addressName;
	protected DateView dateRow;
	protected ZoneId zoneId;
	protected Double latitude;
	protected Double longitude;
	protected ForecastViewType forecastViewType;
	protected ExecutorService executorService = Executors.newSingleThreadExecutor();

	@Override
	public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

		tempUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_temp), ValueUnits.celsius.name()));
		windUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_wind), ValueUnits.mPerSec.name()));
		visibilityUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_visibility), ValueUnits.km.name()));
		clockUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_clock), ValueUnits.clock24.name()));
		forecastViewType = ForecastViewType.valueOf(sharedPreferences.getString(getString(R.string.pref_key_forecast_view_type),
				ForecastViewType.List.name()));

		Bundle bundle = getArguments();
		addressName = bundle.getString(getString(R.string.bundle_key_address_name));
		zoneId = (ZoneId) bundle.getSerializable(getString(R.string.bundle_key_timezone));
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

		binding.rootScrollView.setVisibility(View.GONE);
		binding.listview.setVisibility(View.GONE);

		MobileAds.initialize(getContext());
		AdRequest adRequest = new AdRequest.Builder().build();
		binding.adViewBottom.loadAd(adRequest);

		ImageButton listLayoutBtn = new ImageButton(getContext());
		listLayoutBtn.setImageResource(R.drawable.settings);
		listLayoutBtn.setImageTintList(ColorStateList.valueOf(AppTheme.getColor(getContext(), R.attr.toolbarIconColor)));
		listLayoutBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				final String[] items = new String[]{getString(R.string.list_type), getString(R.string.table_type)};

				int clickedType = 0;
				if (forecastViewType == ForecastViewType.Table) {
					clickedType = 1;
				}
				int finalClickedType = clickedType;
				new AlertDialog.Builder(getActivity()).setTitle(R.string.pref_title_forecast_view_type)
						.setSingleChoiceItems(items, clickedType, new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int which) {
								if (finalClickedType != which) {
									if (which == 0) {
										forecastViewType = ForecastViewType.List;
									} else {
										forecastViewType = ForecastViewType.Table;
									}
									sharedPreferences.edit().putString(getString(R.string.pref_key_forecast_view_type),
											forecastViewType.name()).apply();
									onSelectedViewType();
								}
								dialog.dismiss();
							}
						}).create().show();
			}
		});

		int btnSize = (int) getResources().getDimension(R.dimen.btnSizeInToolbar);
		RelativeLayout.LayoutParams btnLayoutParams = new RelativeLayout.LayoutParams(btnSize, btnSize);
		btnLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_RIGHT);
		binding.toolbar.getRoot().addView(listLayoutBtn, btnLayoutParams);

		binding.toolbar.backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getParentFragmentManager().popBackStackImmediate();
			}
		});
		binding.scrollView.setOnScrollChangeListener(new View.OnScrollChangeListener() {
			@Override
			public void onScrollChange(View v, int scrollX, int scrollY, int oldScrollX, int oldScrollY) {
				if (dateRow != null) {
					dateRow.reDraw(scrollX);
				}
			}
		});

		onSelectedViewType();
	}

	private void onSelectedViewType() {
		if (forecastViewType == ForecastViewType.List) {
			binding.rootScrollView.setVisibility(View.GONE);
			binding.listview.setVisibility(View.VISIBLE);
			setDataViewsByList();
		} else {
			binding.rootScrollView.setVisibility(View.VISIBLE);
			binding.listview.setVisibility(View.GONE);
			setDataViewsByTable();
		}
	}

	abstract protected void setDataViewsByList();

	abstract protected void setDataViewsByTable();

	protected ImageView addLabelView(int labelImgId, String labelDescription, int viewHeight) {
		ImageView labelView = new ImageView(getContext());
		labelView.setImageDrawable(ContextCompat.getDrawable(getContext(), labelImgId));
		labelView.setClickable(true);
		labelView.setScaleType(ImageView.ScaleType.FIT_CENTER);

		labelView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(getContext(), labelDescription, Toast.LENGTH_SHORT).show();
			}
		});

		int width = (int) getResources().getDimension(R.dimen.labelIconColumnWidthInCOMMON);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, viewHeight);
		layoutParams.gravity = Gravity.CENTER;
		labelView.setLayoutParams(layoutParams);

		binding.labels.addView(labelView);
		return labelView;
	}

	public static class HourlyForecastListAdapter extends RecyclerView.Adapter<HourlyForecastListAdapter.ViewHolder> {
		private List<HourlyForecastListItemObj> hourlyForecastListItemObjs = new ArrayList<>();
		private Context context;
		private OnClickedListViewItemListener<Integer> onClickedForecastItem;

		public HourlyForecastListAdapter(Context context, @Nullable OnClickedListViewItemListener<Integer> onClickedForecastItem) {
			this.context = context;
			this.onClickedForecastItem = onClickedForecastItem;
		}

		public void setHourlyForecastListItemObjs(List<HourlyForecastListItemObj> hourlyForecastListItemObjs) {
			this.hourlyForecastListItemObjs = hourlyForecastListItemObjs;
		}

		@NonNull
		@NotNull
		@Override
		public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
			return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.view_detail_hourly_forecast_list, null, false));
		}

		@Override
		public void onBindViewHolder(@NonNull @NotNull BaseDetailForecastFragment.HourlyForecastListAdapter.ViewHolder holder, int position) {
			holder.onBind(hourlyForecastListItemObjs.get(position));
		}

		@Override
		public int getItemCount() {
			return hourlyForecastListItemObjs.size();
		}

		public class ViewHolder extends RecyclerView.ViewHolder {
			private ViewDetailHourlyForecastListBinding binding;

			public ViewHolder(@NonNull @NotNull View itemView) {
				super(itemView);
				binding = ViewDetailHourlyForecastListBinding.bind(itemView);
				onClickedForecastItem.onClickedItem(getAdapterPosition());
			}

			public void onBind(HourlyForecastListItemObj hourlyForecastListItemObj) {
				binding.datetime.setText(hourlyForecastListItemObj.dateTime);
				binding.pop.setText(hourlyForecastListItemObj.pop);
				binding.weatherIcon.setImageResource(hourlyForecastListItemObj.weatherIconId);
				binding.temp.setText(hourlyForecastListItemObj.temp);

				if (hourlyForecastListItemObj.snowVolume != null) {
					binding.snowVolume.setText(hourlyForecastListItemObj.snowVolume);
					binding.snowVolumeLayout.setVisibility(View.VISIBLE);
				} else {
					binding.snowVolumeLayout.setVisibility(View.GONE);
				}

				if (hourlyForecastListItemObj.rainVolume == null) {
					binding.rainVolumeLayout.setVisibility(View.GONE);
				} else {
					binding.rainVolume.setText(hourlyForecastListItemObj.rainVolume);
					binding.rainVolumeLayout.setVisibility(View.VISIBLE);
				}
			}
		}
	}

	public static class DailyForecastListAdapter extends RecyclerView.Adapter<DailyForecastListAdapter.ViewHolder> {
		private List<DailyForecastListItemObj> dailyForecastListItemObjs = new ArrayList<>();
		private Context context;
		private OnClickedListViewItemListener<Integer> onClickedForecastItem;

		public DailyForecastListAdapter(Context context, @Nullable OnClickedListViewItemListener<Integer> onClickedForecastItem) {
			this.context = context;
			this.onClickedForecastItem = onClickedForecastItem;
		}

		public void setDailyForecastListItemObjs(List<DailyForecastListItemObj> dailyForecastListItemObjs) {
			this.dailyForecastListItemObjs = dailyForecastListItemObjs;
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
			holder.onBind(dailyForecastListItemObjs.get(position));
		}

		@Override
		public int getItemCount() {
			return dailyForecastListItemObjs.size();
		}

		public class ViewHolder extends RecyclerView.ViewHolder {
			private ViewDetailDailyForecastListBinding binding;

			public ViewHolder(@NonNull @NotNull View itemView) {
				super(itemView);
				binding = ViewDetailDailyForecastListBinding.bind(itemView);
				onClickedForecastItem.onClickedItem(getAdapterPosition());
			}

			public void onBind(DailyForecastListItemObj dailyForecastListItemObj) {
				binding.datetime.setText(dailyForecastListItemObj.dateTime);
				binding.pop.setText(dailyForecastListItemObj.pop);
				if (dailyForecastListItemObj.snowVolume == null) {
					binding.snowVolumeLayout.setVisibility(View.GONE);
				} else {
					binding.snowVolume.setText(dailyForecastListItemObj.snowVolume);
					binding.snowVolumeLayout.setVisibility(View.VISIBLE);
				}
				if (dailyForecastListItemObj.rainVolume == null) {
					binding.rainVolumeLayout.setVisibility(View.GONE);
				} else {
					binding.rainVolume.setText(dailyForecastListItemObj.rainVolume);
					binding.rainVolumeLayout.setVisibility(View.VISIBLE);
				}

				if (dailyForecastListItemObj.isSingle) {
					binding.rightWeatherIcon.setVisibility(View.GONE);
				} else {
					binding.rightWeatherIcon.setImageResource(dailyForecastListItemObj.rightWeatherIconId);
					binding.rightWeatherIcon.setVisibility(View.VISIBLE);
				}
				binding.leftWeatherIcon.setImageResource(dailyForecastListItemObj.leftWeatherIconId);
				binding.minTemp.setText(dailyForecastListItemObj.minTemp);
				binding.maxTemp.setText(dailyForecastListItemObj.maxTemp);
			}
		}
	}

	public static class HourlyForecastListItemObj {
		String dateTime;
		String pop;
		String rainVolume;
		String snowVolume;
		int weatherIconId;
		String temp;

		public String getDateTime() {
			return dateTime;
		}

		public HourlyForecastListItemObj setDateTime(String dateTime) {
			this.dateTime = dateTime;
			return this;
		}

		public String getPop() {
			return pop;
		}

		public HourlyForecastListItemObj setPop(String pop) {
			this.pop = pop;
			return this;
		}

		public String getRainVolume() {
			return rainVolume;
		}

		public HourlyForecastListItemObj setRainVolume(String rainVolume) {
			this.rainVolume = rainVolume;
			return this;
		}

		public String getSnowVolume() {
			return snowVolume;
		}

		public HourlyForecastListItemObj setSnowVolume(String snowVolume) {
			this.snowVolume = snowVolume;
			return this;
		}

		public int getWeatherIconId() {
			return weatherIconId;
		}

		public HourlyForecastListItemObj setWeatherIconId(int weatherIconId) {
			this.weatherIconId = weatherIconId;
			return this;
		}

		public String getTemp() {
			return temp;
		}

		public HourlyForecastListItemObj setTemp(String temp) {
			this.temp = temp;
			return this;
		}
	}

	public static class DailyForecastListItemObj {
		String dateTime;
		String pop;
		String rainVolume;
		String snowVolume;
		int leftWeatherIconId;
		int rightWeatherIconId;
		boolean isSingle;
		String minTemp;
		String maxTemp;

		public String getDateTime() {
			return dateTime;
		}

		public DailyForecastListItemObj setDateTime(String dateTime) {
			this.dateTime = dateTime;
			return this;
		}

		public String getPop() {
			return pop;
		}

		public DailyForecastListItemObj setPop(String pop) {
			this.pop = pop;
			return this;
		}

		public String getRainVolume() {
			return rainVolume;
		}

		public DailyForecastListItemObj setRainVolume(String rainVolume) {
			this.rainVolume = rainVolume;
			return this;
		}

		public String getSnowVolume() {
			return snowVolume;
		}

		public DailyForecastListItemObj setSnowVolume(String snowVolume) {
			this.snowVolume = snowVolume;
			return this;
		}

		public int getLeftWeatherIconId() {
			return leftWeatherIconId;
		}

		public DailyForecastListItemObj setLeftWeatherIconId(int leftWeatherIconId) {
			this.leftWeatherIconId = leftWeatherIconId;
			return this;
		}

		public int getRightWeatherIconId() {
			return rightWeatherIconId;
		}

		public DailyForecastListItemObj setRightWeatherIconId(int rightWeatherIconId) {
			this.rightWeatherIconId = rightWeatherIconId;
			return this;
		}

		public boolean isSingle() {
			return isSingle;
		}

		public DailyForecastListItemObj setSingle(boolean single) {
			isSingle = single;
			return this;
		}

		public String getMinTemp() {
			return minTemp;
		}

		public DailyForecastListItemObj setMinTemp(String minTemp) {
			this.minTemp = minTemp;
			return this;
		}

		public String getMaxTemp() {
			return maxTemp;
		}

		public DailyForecastListItemObj setMaxTemp(String maxTemp) {
			this.maxTemp = maxTemp;
			return this;
		}
	}
}
