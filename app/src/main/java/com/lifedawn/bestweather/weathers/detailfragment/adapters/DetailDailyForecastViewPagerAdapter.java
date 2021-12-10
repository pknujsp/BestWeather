package com.lifedawn.bestweather.weathers.detailfragment.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.RequestWeatherDataType;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.databinding.HeaderviewDetailHourlyforecastBinding;
import com.lifedawn.bestweather.databinding.ItemviewDetailDailyForecastBinding;
import com.lifedawn.bestweather.databinding.ItemviewDetailForecastBinding;
import com.lifedawn.bestweather.weathers.detailfragment.dto.DailyForecastDto;
import com.lifedawn.bestweather.weathers.detailfragment.dto.GridItemDto;
import com.lifedawn.bestweather.weathers.detailfragment.dto.HourlyForecastDto;

import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DetailDailyForecastViewPagerAdapter extends RecyclerView.Adapter<DetailDailyForecastViewPagerAdapter.ViewHolder> {
	private Context context;
	private LayoutInflater layoutInflater;
	private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M.d E");
	private final String zeroPercent = "0%";

	private List<DailyForecastDto> dailyForecastDtoList;

	public DetailDailyForecastViewPagerAdapter(Context context) {
		this.context = context;
		layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
	}

	public void setDailyForecastDtoList(List<DailyForecastDto> dailyForecastDtoList) {
		this.dailyForecastDtoList = dailyForecastDtoList;
	}

	@NonNull
	@NotNull
	@Override
	public ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
		return new ViewHolder(layoutInflater.inflate(R.layout.itemview_detail_daily_forecast, parent, false));
	}

	@Override
	public void onBindViewHolder(@NonNull @NotNull DetailDailyForecastViewPagerAdapter.ViewHolder holder, int position) {
		holder.onBind(dailyForecastDtoList.get(position));
	}

	@Override
	public int getItemCount() {
		return dailyForecastDtoList.size();
	}

	public class ViewHolder extends RecyclerView.ViewHolder {
		private ItemviewDetailDailyForecastBinding binding;

		public ViewHolder(@NonNull @NotNull View itemView) {
			super(itemView);
			binding = ItemviewDetailDailyForecastBinding.bind(itemView);
		}

		public void onBind(DailyForecastDto dailyForecastDto) {
			binding.grid.removeAllViews();
			binding.amList.removeAllViews();

			//싱글이면 amList에 값 입력
			if (dailyForecastDto.isSingle()) {
				binding.pmWeatherIcon.setVisibility(View.GONE);
				binding.amLabel.setText(R.string.today);
			} else {
				binding.pmWeatherIcon.setVisibility(View.VISIBLE);
				String text = context.getString(R.string.am) + " / " + context.getString(R.string.pm);
				binding.amLabel.setText(text);
			}

			binding.date.setText(dailyForecastDto.getDate().format(dateFormatter));
			List<LabelValueItem> labelValueItemList = new ArrayList<>();

			//날씨 아이콘, 최저/최고 기온, 강수확률, 강수량, 강우량, 강설량 설정
			if (dailyForecastDto.isSingle()) {
				binding.amWeatherIcon.setImageResource(dailyForecastDto.getSingleValues().getWeatherIcon());

				addListItem(labelValueItemList, context.getString(R.string.probability_of_precipitation), dailyForecastDto.getSingleValues().getPop());

				if (dailyForecastDto.getSingleValues().isHasPrecipitationVolume()) {
					addListItem(labelValueItemList, context.getString(R.string.precipitation_volume),
							dailyForecastDto.getSingleValues().getPrecipitationVolume());
				}
				if (dailyForecastDto.getSingleValues().isHasRainVolume()) {
					addListItem(labelValueItemList, context.getString(R.string.rain_volume),
							dailyForecastDto.getSingleValues().getRainVolume());
				}
				if (dailyForecastDto.getSingleValues().isHasSnowVolume()) {
					addListItem(labelValueItemList, context.getString(R.string.snow_volume),
							dailyForecastDto.getSingleValues().getSnowVolume());
				}
			} else {
				binding.amWeatherIcon.setImageResource(dailyForecastDto.getAmValues().getWeatherIcon());
				binding.pmWeatherIcon.setImageResource(dailyForecastDto.getPmValues().getWeatherIcon());

				addListItem(labelValueItemList, context.getString(R.string.probability_of_precipitation),
						dailyForecastDto.getAmValues().getPop(),
						dailyForecastDto.getPmValues().getPop());

				if (dailyForecastDto.getAmValues().isHasPrecipitationVolume() ||
						dailyForecastDto.getPmValues().isHasPrecipitationVolume()) {
					addListItem(labelValueItemList, context.getString(R.string.precipitation_volume),
							dailyForecastDto.getAmValues().getPrecipitationVolume(),
							dailyForecastDto.getPmValues().getPrecipitationVolume());
				}

				if (dailyForecastDto.getAmValues().isHasRainVolume() ||
						dailyForecastDto.getPmValues().isHasRainVolume()) {
					addListItem(labelValueItemList, context.getString(R.string.rain_volume),
							dailyForecastDto.getAmValues().getRainVolume(),
							dailyForecastDto.getPmValues().getRainVolume());
				}

				if (dailyForecastDto.getAmValues().isHasSnowVolume() ||
						dailyForecastDto.getPmValues().isHasSnowVolume()) {
					addListItem(labelValueItemList, context.getString(R.string.snow_volume),
							dailyForecastDto.getAmValues().getSnowVolume(),
							dailyForecastDto.getPmValues().getSnowVolume());
				}
			}
			binding.temp.setText(dailyForecastDto.getMinTemp() + " / " + dailyForecastDto.getMaxTemp());

			for (LabelValueItem labelValueItem : labelValueItemList) {
				View view = layoutInflater.inflate(R.layout.value_itemview, null, false);
				((TextView) view.findViewById(R.id.label)).setText(labelValueItem.label);
				((TextView) view.findViewById(R.id.value)).setText(labelValueItem.value);

				binding.grid.addView(view);
			}

			labelValueItemList.clear();

			if (dailyForecastDto.isSingle()) {
				addListItem(labelValueItemList, context.getString(R.string.wind_direction), dailyForecastDto.getSingleValues().getWindDirection());
				addListItem(labelValueItemList, context.getString(R.string.wind_speed), dailyForecastDto.getSingleValues().getWindSpeed());
				addListItem(labelValueItemList, context.getString(R.string.wind_strength), dailyForecastDto.getSingleValues().getWindStrength());
				addListItem(labelValueItemList, context.getString(R.string.wind_gust), dailyForecastDto.getSingleValues().getWindGust());
				addListItem(labelValueItemList, context.getString(R.string.pressure), dailyForecastDto.getSingleValues().getPressure());
				addListItem(labelValueItemList, context.getString(R.string.humidity), dailyForecastDto.getSingleValues().getHumidity());
				addListItem(labelValueItemList, context.getString(R.string.dew_point), dailyForecastDto.getSingleValues().getDewPointTemp());
				addListItem(labelValueItemList, context.getString(R.string.cloud_cover), dailyForecastDto.getSingleValues().getCloudiness());
				addListItem(labelValueItemList, context.getString(R.string.uv_index), dailyForecastDto.getSingleValues().getUvIndex());

				for (LabelValueItem labelValueItem : labelValueItemList) {
					View view = layoutInflater.inflate(R.layout.value_itemview, null, false);
					((TextView) view.findViewById(R.id.label)).setText(labelValueItem.label);
					((TextView) view.findViewById(R.id.value)).setText(labelValueItem.value);
					binding.amList.addView(view);
				}
			} else {
				addListItem(labelValueItemList, context.getString(R.string.probability_of_rain),
						dailyForecastDto.getAmValues().getPor(), dailyForecastDto.getPmValues().getPor());

				if (dailyForecastDto.getAmValues().getPos() != null || dailyForecastDto.getPmValues().getPos() != null) {
					if (!dailyForecastDto.getAmValues().getPos().equals(zeroPercent) || !dailyForecastDto.getPmValues().getPos().equals(zeroPercent)) {
						addListItem(labelValueItemList, context.getString(R.string.probability_of_snow),
								dailyForecastDto.getAmValues().getPos(), dailyForecastDto.getPmValues().getPos());
					}
				}

				addListItem(labelValueItemList, context.getString(R.string.wind_direction),
						dailyForecastDto.getAmValues().getWindDirection(), dailyForecastDto.getPmValues().getWindDirection());
				addListItem(labelValueItemList, context.getString(R.string.wind_speed), dailyForecastDto.getAmValues().getWindSpeed(), dailyForecastDto.getPmValues().getWindSpeed());
				addListItem(labelValueItemList, context.getString(R.string.wind_strength), dailyForecastDto.getAmValues().getWindStrength(), dailyForecastDto.getPmValues().getWindStrength());
				addListItem(labelValueItemList, context.getString(R.string.wind_gust), dailyForecastDto.getAmValues().getWindGust(), dailyForecastDto.getPmValues().getWindGust());
				addListItem(labelValueItemList, context.getString(R.string.pressure), dailyForecastDto.getAmValues().getPressure(), dailyForecastDto.getPmValues().getPressure());
				addListItem(labelValueItemList, context.getString(R.string.humidity), dailyForecastDto.getAmValues().getHumidity(), dailyForecastDto.getPmValues().getHumidity());
				addListItem(labelValueItemList, context.getString(R.string.dew_point), dailyForecastDto.getAmValues().getDewPointTemp(), dailyForecastDto.getPmValues().getDewPointTemp());
				addListItem(labelValueItemList, context.getString(R.string.cloud_cover), dailyForecastDto.getAmValues().getCloudiness(), dailyForecastDto.getPmValues().getCloudiness());
				addListItem(labelValueItemList, context.getString(R.string.uv_index), dailyForecastDto.getAmValues().getUvIndex(), dailyForecastDto.getPmValues().getUvIndex());

				for (LabelValueItem labelValueItem : labelValueItemList) {
					View view = layoutInflater.inflate(R.layout.value_itemview, null, false);
					((TextView) view.findViewById(R.id.label)).setText(labelValueItem.label);
					((TextView) view.findViewById(R.id.value)).setText(labelValueItem.value);
					binding.amList.addView(view);
				}
			}


		}
	}

	protected void addListItem(List<LabelValueItem> list, String label, @Nullable String val) {
		if (val != null) {
			list.add(new LabelValueItem(label, val));
		}
	}

	protected void addListItem(List<LabelValueItem> list, String label, @Nullable String val1, @Nullable String val2) {
		if (val1 != null || val2 != null) {
			list.add(new LabelValueItem(label, val1 + " / " + val2));
		}
	}


	protected static class LabelValueItem {
		private String label;
		private String value;

		public LabelValueItem(String label, String value) {
			this.label = label;
			this.value = value;
		}

		public String getLabel() {
			return label;
		}

		public LabelValueItem setLabel(String label) {
			this.label = label;
			return this;
		}

		public String getValue() {
			return value;
		}

		public LabelValueItem setValue(String value) {
			this.value = value;
			return this;
		}
	}
}
