package com.lifedawn.bestweather.weathers.detailfragment.base;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.ads.AdRequest;
import com.google.android.gms.ads.MobileAds;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.databinding.BaseLayoutDetailForecastBinding;
import com.lifedawn.bestweather.databinding.ViewDetailHourlyForecastListBinding;
import com.lifedawn.bestweather.weathers.simplefragment.interfaces.IWeatherValues;
import com.lifedawn.bestweather.weathers.view.DateView;

import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.util.ArrayList;
import java.util.List;

public class BaseDetailForecastFragment extends Fragment implements IWeatherValues {
	protected BaseLayoutDetailForecastBinding binding;
	protected SharedPreferences sharedPreferences;
	protected ValueUnits tempUnit;
	protected ValueUnits windUnit;
	protected ValueUnits visibilityUnit;
	protected ValueUnits clockUnit;
	protected String addressName;
	protected DateView dateRow;
	protected ZoneId zoneId;


	@Override
	public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());

		tempUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_temp), ValueUnits.celsius.name()));
		windUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_wind), ValueUnits.mPerSec.name()));
		visibilityUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_visibility), ValueUnits.km.name()));
		clockUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_clock), ValueUnits.clock24.name()));

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
		MobileAds.initialize(getContext());
		AdRequest adRequest = new AdRequest.Builder().build();
		binding.adViewBottom.loadAd(adRequest);

		binding.addressName.setText(addressName);
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
	}

	@Override
	public void setValuesToViews() {

	}

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

	protected static class HourlyForecastListAdapter extends RecyclerView.Adapter<HourlyForecastListAdapter.ViewHolder> {
		private List<HourlyForecastListItemObj> hourlyForecastListItemObjs = new ArrayList<>();
		private Context context;
		private OnClickedForecastItem onClickedForecastItem;

		public HourlyForecastListAdapter(Context context, @Nullable OnClickedForecastItem onClickedForecastItem) {
			this.context = context;
			this.onClickedForecastItem = onClickedForecastItem;
		}

		public HourlyForecastListAdapter(List<HourlyForecastListItemObj> hourlyForecastListItemObjs) {
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
				onClickedForecastItem.onClickedForecastItem(getAdapterPosition());
			}

			public void onBind(HourlyForecastListItemObj hourlyForecastListItemObj) {
				binding.datetime.setText(hourlyForecastListItemObj.weatherIconId);
				binding.pop.setText(hourlyForecastListItemObj.pop);
				binding.weatherIcon.setImageResource(hourlyForecastListItemObj.weatherIconId);
				binding.temp.setText(hourlyForecastListItemObj.temp);
			}
		}
	}

	static interface OnClickedForecastItem {
		void onClickedForecastItem(int position);
	}

	static class HourlyForecastListItemObj {
		private String dateTime;
		private String pop;
		private String rainVolume;
		private String snowVolume;
		private int weatherIconId;
		private String temp;

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
}
