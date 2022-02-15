package com.lifedawn.bestweather.weathers.detailfragment.adapters;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.gridlayout.widget.GridLayout;
import androidx.preference.PreferenceManager;
import androidx.recyclerview.widget.RecyclerView;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.databinding.HeaderviewDetailHourlyforecastBinding;
import com.lifedawn.bestweather.databinding.ItemviewDetailForecastBinding;
import com.lifedawn.bestweather.weathers.detailfragment.dto.GridItemDto;
import com.lifedawn.bestweather.weathers.models.HourlyForecastDto;

import org.jetbrains.annotations.NotNull;

import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DetailHourlyForecastViewPagerAdapter extends RecyclerView.Adapter<DetailHourlyForecastViewPagerAdapter.ViewHolder> {
	private Context context;
	private LayoutInflater layoutInflater;
	private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M.d E");
	private DateTimeFormatter hoursFormatter;
	private final String feelsLikeTempLabel;

	private List<HourlyForecastDto> hourlyForecastDtoList;

	public DetailHourlyForecastViewPagerAdapter(Context context) {
		this.context = context;
		layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ValueUnits clockUnit =
				ValueUnits.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_key_unit_clock),
						ValueUnits.clock24.name()));
		hoursFormatter = DateTimeFormatter.ofPattern(clockUnit == ValueUnits.clock12 ? "h a" : "H");
		feelsLikeTempLabel = context.getString(R.string.feelsLike) + ": ";
	}

	public DetailHourlyForecastViewPagerAdapter setHourlyForecastDtoList(List<HourlyForecastDto> hourlyForecastDtoList) {
		this.hourlyForecastDtoList = hourlyForecastDtoList;
		return this;
	}

	@NonNull
	@NotNull
	@Override
	public DetailHourlyForecastViewPagerAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
		return new ViewHolder(layoutInflater.inflate(R.layout.itemview_detail_forecast, parent, false));
	}

	@Override
	public void onViewRecycled(@NonNull ViewHolder holder) {
		holder.clear();
		super.onViewRecycled(holder);
	}

	@Override
	public void onBindViewHolder(@NonNull @NotNull DetailHourlyForecastViewPagerAdapter.ViewHolder holder, int position) {
		holder.onBind(hourlyForecastDtoList.get(position));
	}

	@Override
	public int getItemCount() {
		return hourlyForecastDtoList.size();
	}

	class ViewHolder extends RecyclerView.ViewHolder {
		private ItemviewDetailForecastBinding binding;
		private HeaderviewDetailHourlyforecastBinding headerBinding;

		public ViewHolder(@NonNull @NotNull View itemView) {
			super(itemView);
			binding = ItemviewDetailForecastBinding.bind(itemView);
			headerBinding = HeaderviewDetailHourlyforecastBinding.inflate(layoutInflater, null, false);
			binding.header.addView(headerBinding.getRoot());
		}

		public void clear() {
			headerBinding.precipitationGridLayout.removeAllViews();
			binding.detailGridView.removeAllViews();
		}

		public void onBind(HourlyForecastDto hourlyForecastDto) {
			//header 화면 구성
			headerBinding.date.setText(hourlyForecastDto.getHours().format(dateFormatter));
			headerBinding.hours.setText(hourlyForecastDto.getHours().format(hoursFormatter));
			headerBinding.weatherIcon.setImageResource(hourlyForecastDto.getWeatherIcon());
			headerBinding.temp.setText(hourlyForecastDto.getTemp());
			headerBinding.feelsLikeTemp.setText(new String(feelsLikeTempLabel + hourlyForecastDto.getFeelsLikeTemp()));
			headerBinding.weatherDescription.setText(hourlyForecastDto.getWeatherDescription());

			addPrecipitationGridItem(layoutInflater, hourlyForecastDto);

			//gridviewLayout
			//공통 - 날씨, 기온, 강수량, 강수확률, 풍향, 풍속, 바람세기, 습도
			List<GridItemDto> gridItemDtos = new ArrayList<>();

			if (hourlyForecastDto.getFeelsLikeTemp() != null) {
				gridItemDtos.add(new GridItemDto(context.getString(R.string.wind_chill_temperature_of_grid), hourlyForecastDto.getFeelsLikeTemp(),
						null));
			}
			if (hourlyForecastDto.getPrecipitationType() != null) {
				gridItemDtos.add(new GridItemDto(context.getString(R.string.precipitation_type),
						hourlyForecastDto.getPrecipitationType(),
						ContextCompat.getDrawable(context, hourlyForecastDto.getPrecipitationTypeIcon())));
			}

			gridItemDtos.add(new GridItemDto(context.getString(R.string.wind_direction), hourlyForecastDto.getWindDirection() == null ?
					context.getString(R.string.noData) : hourlyForecastDto.getWindDirection(),
					ContextCompat.getDrawable(context, R.drawable.arrow), hourlyForecastDto.getWindDirectionVal() + 180));

			gridItemDtos.add(new GridItemDto(context.getString(R.string.wind_speed), hourlyForecastDto.getWindSpeed() == null ?
					context.getString(R.string.noData) : hourlyForecastDto.getWindSpeed(), null));

			gridItemDtos.add(new GridItemDto(context.getString(R.string.wind_strength), hourlyForecastDto.getWindStrength() == null ?
					context.getString(R.string.noData) : hourlyForecastDto.getWindStrength(), null));

			if (hourlyForecastDto.getWindGust() != null) {
				gridItemDtos.add(new GridItemDto(context.getString(R.string.wind_gust), hourlyForecastDto.getWindGust(), null));
			}
			if (hourlyForecastDto.getPressure() != null) {
				gridItemDtos.add(new GridItemDto(context.getString(R.string.pressure), hourlyForecastDto.getPressure(), null));
			}

			if (hourlyForecastDto.getHumidity() != null) {
				gridItemDtos.add(new GridItemDto(context.getString(R.string.humidity), hourlyForecastDto.getHumidity(), null));
			}

			//나머지 - 돌풍, 기압, 이슬점, 운량, 시정, 자외선, 체감기온

			if (hourlyForecastDto.getDewPointTemp() != null) {
				gridItemDtos.add(new GridItemDto(context.getString(R.string.dew_point), hourlyForecastDto.getDewPointTemp(), null));
			}
			if (hourlyForecastDto.getCloudiness() != null) {
				gridItemDtos.add(new GridItemDto(context.getString(R.string.cloud_cover), hourlyForecastDto.getCloudiness(), null));
			}
			if (hourlyForecastDto.getVisibility() != null) {
				gridItemDtos.add(new GridItemDto(context.getString(R.string.visibility), hourlyForecastDto.getVisibility(), null));
			}
			if (hourlyForecastDto.getUvIndex() != null) {
				gridItemDtos.add(new GridItemDto(context.getString(R.string.uv_index), hourlyForecastDto.getUvIndex(), null));
			}

			addGridItems(gridItemDtos);
		}

		private void addPrecipitationGridItem(LayoutInflater layoutInflater, HourlyForecastDto hourlyForecastDto) {
			View gridItem = layoutInflater.inflate(R.layout.view_detail_weather_data_item, null);
			final int blueColor = ContextCompat.getColor(context, R.color.blue);
			//강수확률
			((TextView) gridItem.findViewById(R.id.label)).setText(context.getString(R.string.probability_of_precipitation));
			((TextView) gridItem.findViewById(R.id.value)).setText(hourlyForecastDto.getPop());
			((TextView) gridItem.findViewById(R.id.value)).setTextColor(blueColor);
			gridItem.findViewById(R.id.label_icon).setVisibility(View.GONE);
			headerBinding.precipitationGridLayout.addView(gridItem);

			//강우확률
			if (hourlyForecastDto.isHasPor()) {
				gridItem = layoutInflater.inflate(R.layout.view_detail_weather_data_item, null);

				((TextView) gridItem.findViewById(R.id.label)).setText(context.getString(R.string.probability_of_rain));
				((TextView) gridItem.findViewById(R.id.value)).setText(hourlyForecastDto.getPor());
				((TextView) gridItem.findViewById(R.id.value)).setTextColor(blueColor);

				gridItem.findViewById(R.id.label_icon).setVisibility(View.GONE);

				headerBinding.precipitationGridLayout.addView(gridItem);
			}

			//강설확률
			if (hourlyForecastDto.isHasPos()) {
				gridItem = layoutInflater.inflate(R.layout.view_detail_weather_data_item, null);

				((TextView) gridItem.findViewById(R.id.label)).setText(context.getString(R.string.probability_of_snow));
				((TextView) gridItem.findViewById(R.id.value)).setText(hourlyForecastDto.getPos());
				((TextView) gridItem.findViewById(R.id.value)).setTextColor(blueColor);

				gridItem.findViewById(R.id.label_icon).setVisibility(View.GONE);

				headerBinding.precipitationGridLayout.addView(gridItem);
			}

			//강수량
			if (hourlyForecastDto.isHasPrecipitation()) {
				gridItem = layoutInflater.inflate(R.layout.view_detail_weather_data_item, null);

				((TextView) gridItem.findViewById(R.id.label)).setText(context.getString(R.string.precipitation_volume_of_grid));
				((TextView) gridItem.findViewById(R.id.value)).setText(hourlyForecastDto.getPrecipitationVolume());
				((TextView) gridItem.findViewById(R.id.value)).setTextColor(blueColor);

				gridItem.findViewById(R.id.label_icon).setVisibility(View.GONE);

				headerBinding.precipitationGridLayout.addView(gridItem);
			}

			//강우량
			if (hourlyForecastDto.isHasRain()) {
				gridItem = layoutInflater.inflate(R.layout.view_detail_weather_data_item, null);

				((TextView) gridItem.findViewById(R.id.label)).setText(context.getString(R.string.rain_volume_of_grid));
				((TextView) gridItem.findViewById(R.id.value)).setText(hourlyForecastDto.getRainVolume());
				((TextView) gridItem.findViewById(R.id.value)).setTextColor(blueColor);

				gridItem.findViewById(R.id.label_icon).setVisibility(View.GONE);

				headerBinding.precipitationGridLayout.addView(gridItem);
			}

			//강설량
			if (hourlyForecastDto.isHasSnow()) {
				gridItem = layoutInflater.inflate(R.layout.view_detail_weather_data_item, null);

				((TextView) gridItem.findViewById(R.id.label)).setText(context.getString(R.string.snow_volume_of_grid));
				((TextView) gridItem.findViewById(R.id.value)).setText(hourlyForecastDto.getSnowVolume());
				((TextView) gridItem.findViewById(R.id.value)).setTextColor(blueColor);

				gridItem.findViewById(R.id.label_icon).setVisibility(View.GONE);

				headerBinding.precipitationGridLayout.addView(gridItem);
			}
		}

		private void addGridItems(List<GridItemDto> gridItemDtos) {
			TextView label = null;
			TextView value = null;
			ImageView icon = null;
			View convertView = null;

			for (GridItemDto gridItem : gridItemDtos) {
				convertView = layoutInflater.inflate(R.layout.view_detail_weather_data_item, null, false);

				label = convertView.findViewById(R.id.label);
				value = convertView.findViewById(R.id.value);
				icon = convertView.findViewById(R.id.label_icon);

				label.setText(gridItem.label);
				value.setText(gridItem.value);

				if (gridItem.img != null) {
					icon.setImageDrawable(gridItem.img);
					if (gridItem.imgRotate != null) {
						icon.setRotation(gridItem.imgRotate);
					}
				} else {
					icon.setVisibility(View.GONE);
				}

				int cellCount = binding.detailGridView.getChildCount();
				int row = cellCount / 3;
				int column = cellCount % 3;

				GridLayout.LayoutParams layoutParams = new GridLayout.LayoutParams();

				layoutParams.columnSpec = GridLayout.spec(column, GridLayout.FILL, 1);
				layoutParams.rowSpec = GridLayout.spec(row, GridLayout.FILL, 1);

				binding.detailGridView.addView(convertView, layoutParams);
			}
		}

	}
}
