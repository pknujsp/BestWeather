package com.lifedawn.bestweather.ui.weathers.detailfragment.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.gridlayout.widget.GridLayout;
import androidx.recyclerview.widget.RecyclerView;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.classes.WeatherValueLabels;
import com.lifedawn.bestweather.commons.constants.ValueUnits;
import com.lifedawn.bestweather.commons.constants.WeatherValueType;
import com.lifedawn.bestweather.databinding.HeaderviewDetailHourlyforecastBinding;
import com.lifedawn.bestweather.databinding.ItemviewDetailForecastBinding;
import com.lifedawn.bestweather.data.MyApplication;
import com.lifedawn.bestweather.ui.weathers.detailfragment.dto.GridItemDto;
import com.lifedawn.bestweather.ui.weathers.models.HourlyForecastDto;

import org.jetbrains.annotations.NotNull;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

public class DetailHourlyForecastViewPagerAdapter extends RecyclerView.Adapter<DetailHourlyForecastViewPagerAdapter.ViewHolder> {
	private final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M.d E");
	private final DateTimeFormatter hoursFormatter;


	private List<HourlyForecastDto> hourlyForecastDtoList;

	public DetailHourlyForecastViewPagerAdapter() {
		ValueUnits clockUnit = MyApplication.VALUE_UNIT_OBJ.getClockUnit();
		hoursFormatter = DateTimeFormatter.ofPattern(clockUnit == ValueUnits.clock12 ? "h a" : "H");

	}

	public DetailHourlyForecastViewPagerAdapter setHourlyForecastDtoList(List<HourlyForecastDto> hourlyForecastDtoList) {
		this.hourlyForecastDtoList = hourlyForecastDtoList;
		return this;
	}

	@NonNull
	@NotNull
	@Override
	public DetailHourlyForecastViewPagerAdapter.ViewHolder onCreateViewHolder(@NonNull @NotNull ViewGroup parent, int viewType) {
		LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
		return new ViewHolder(ItemviewDetailForecastBinding.inflate(layoutInflater, parent, false),
				HeaderviewDetailHourlyforecastBinding.inflate(layoutInflater, null, false));
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

	protected class ViewHolder extends RecyclerView.ViewHolder {
		private final ItemviewDetailForecastBinding binding;
		private final HeaderviewDetailHourlyforecastBinding headerBinding;
		private final String feelsLikeTempLabel;
		private final String noData;

		public ViewHolder(ItemviewDetailForecastBinding binding, HeaderviewDetailHourlyforecastBinding headerBinding) {
			super(binding.getRoot());
			this.binding = binding;
			this.headerBinding = headerBinding;
			binding.header.addView(headerBinding.getRoot());

			feelsLikeTempLabel = binding.getRoot().getContext().getString(R.string.feelsLike) + ": ";
			noData = binding.getRoot().getContext().getString(R.string.noData);
		}

		public void clear() {
			headerBinding.precipitationGridLayout.removeAllViews();
			binding.detailGridView.removeAllViews();
		}

		public void onBind(HourlyForecastDto hourlyForecastDto) {
			//header 화면 구성
			if (hourlyForecastDto.isHasNext6HoursPrecipitation()) {
				ZonedDateTime dateTime = hourlyForecastDto.getHours();
				String date = dateTime.format(dateFormatter);
				String time = dateTime.format(hoursFormatter);

				dateTime = dateTime.plusHours(6);

				date += " - " + dateTime.format(dateFormatter);
				time += " - " + dateTime.format(hoursFormatter);
				headerBinding.date.setText(date);
				headerBinding.hours.setText(time);
			} else {
				headerBinding.date.setText(hourlyForecastDto.getHours().format(dateFormatter));
				headerBinding.hours.setText(hourlyForecastDto.getHours().format(hoursFormatter));
			}

			headerBinding.weatherIcon.setImageResource(hourlyForecastDto.getWeatherIcon());
			headerBinding.temp.setText(hourlyForecastDto.getTemp());


			headerBinding.feelsLikeTemp.setText(new String(feelsLikeTempLabel + hourlyForecastDto.getFeelsLikeTemp()));
			headerBinding.weatherDescription.setText(hourlyForecastDto.getWeatherDescription());

			addPrecipitationGridItem(LayoutInflater.from(binding.getRoot().getContext()), hourlyForecastDto);

			//gridviewLayout
			//공통 - 날씨, 기온, 강수량, 강수확률, 풍향, 풍속, 바람세기, 습도
			List<GridItemDto> gridItemDtos = new ArrayList<>();

			if (hourlyForecastDto.getPrecipitationType() != null) {
				gridItemDtos.add(new GridItemDto(WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.precipitationType),
						hourlyForecastDto.getPrecipitationType(),
						ContextCompat.getDrawable(binding.getRoot().getContext(), hourlyForecastDto.getPrecipitationTypeIcon())));
			}

			gridItemDtos.add(new GridItemDto(WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.windDirection),
					hourlyForecastDto.getWindDirection() == null ?
							noData : hourlyForecastDto.getWindDirection(),
					ContextCompat.getDrawable(binding.getRoot().getContext(), R.drawable.arrow), hourlyForecastDto.getWindDirectionVal() + 180));

			gridItemDtos.add(new GridItemDto(WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.windSpeed),
					hourlyForecastDto.getWindSpeed() == null ?
							noData : hourlyForecastDto.getWindSpeed(), null));

			gridItemDtos.add(new GridItemDto(WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.windStrength),
					hourlyForecastDto.getWindStrength() == null ?
							noData : hourlyForecastDto.getWindStrength(), null));

			if (hourlyForecastDto.getWindGust() != null) {
				gridItemDtos.add(new GridItemDto(WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.windGust),
						hourlyForecastDto.getWindGust(), null));
			}
			if (hourlyForecastDto.getPressure() != null) {
				gridItemDtos.add(new GridItemDto(WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.pressure),
						hourlyForecastDto.getPressure(), null));
			}

			if (hourlyForecastDto.getHumidity() != null) {
				gridItemDtos.add(new GridItemDto(WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.humidity),
						hourlyForecastDto.getHumidity(), null));
			}

			//나머지 - 돌풍, 기압, 이슬점, 운량, 시정, 자외선, 체감기온

			if (hourlyForecastDto.getDewPointTemp() != null) {
				gridItemDtos.add(new GridItemDto(WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.dewPoint),
						hourlyForecastDto.getDewPointTemp(), null));
			}
			if (hourlyForecastDto.getCloudiness() != null) {
				gridItemDtos.add(new GridItemDto(WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.cloudiness),
						hourlyForecastDto.getCloudiness(), null));
			}
			if (hourlyForecastDto.getVisibility() != null) {
				gridItemDtos.add(new GridItemDto(WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.visibility),
						hourlyForecastDto.getVisibility(), null));
			}
			if (hourlyForecastDto.getUvIndex() != null) {
				gridItemDtos.add(new GridItemDto(WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.uvIndex),
						hourlyForecastDto.getUvIndex(), null));
			}

			addGridItems(gridItemDtos);
		}

		private void addPrecipitationGridItem(LayoutInflater layoutInflater, HourlyForecastDto hourlyForecastDto) {
			View gridItem = layoutInflater.inflate(R.layout.view_detail_weather_data_item, null);
			final int blueColor = ContextCompat.getColor(binding.getRoot().getContext(), R.color.blue);

			//강수확률
			((TextView) gridItem.findViewById(R.id.label)).setText(WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.pop));
			((TextView) gridItem.findViewById(R.id.value)).setText(hourlyForecastDto.getPop() == null ? "-" : hourlyForecastDto.getPop());
			((TextView) gridItem.findViewById(R.id.value)).setTextColor(blueColor);
			gridItem.findViewById(R.id.label_icon).setVisibility(View.GONE);
			headerBinding.precipitationGridLayout.addView(gridItem);

			//강우확률
			if (hourlyForecastDto.isHasPor()) {
				gridItem = layoutInflater.inflate(R.layout.view_detail_weather_data_item, null);

				((TextView) gridItem.findViewById(R.id.label)).setText(WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.por));
				((TextView) gridItem.findViewById(R.id.value)).setText(hourlyForecastDto.getPor());
				((TextView) gridItem.findViewById(R.id.value)).setTextColor(blueColor);

				gridItem.findViewById(R.id.label_icon).setVisibility(View.GONE);

				headerBinding.precipitationGridLayout.addView(gridItem);
			}

			//강설확률
			if (hourlyForecastDto.isHasPos()) {
				gridItem = layoutInflater.inflate(R.layout.view_detail_weather_data_item, null);

				((TextView) gridItem.findViewById(R.id.label)).setText(WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.pos));
				((TextView) gridItem.findViewById(R.id.value)).setText(hourlyForecastDto.getPos());
				((TextView) gridItem.findViewById(R.id.value)).setTextColor(blueColor);

				gridItem.findViewById(R.id.label_icon).setVisibility(View.GONE);

				headerBinding.precipitationGridLayout.addView(gridItem);
			}

			//강수량
			if (hourlyForecastDto.isHasPrecipitation() && hourlyForecastDto.getPrecipitationVolume() != null) {
				gridItem = layoutInflater.inflate(R.layout.view_detail_weather_data_item, null);

				((TextView) gridItem.findViewById(R.id.label)).setText(WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.precipitationVolume));
				((TextView) gridItem.findViewById(R.id.value)).setText(hourlyForecastDto.getPrecipitationVolume());
				((TextView) gridItem.findViewById(R.id.value)).setTextColor(blueColor);

				gridItem.findViewById(R.id.label_icon).setVisibility(View.GONE);

				headerBinding.precipitationGridLayout.addView(gridItem);
			}

			//강우량
			if (hourlyForecastDto.isHasRain()) {
				gridItem = layoutInflater.inflate(R.layout.view_detail_weather_data_item, null);

				((TextView) gridItem.findViewById(R.id.label)).setText(WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.rainVolume));
				((TextView) gridItem.findViewById(R.id.value)).setText(hourlyForecastDto.getRainVolume());
				((TextView) gridItem.findViewById(R.id.value)).setTextColor(blueColor);

				gridItem.findViewById(R.id.label_icon).setVisibility(View.GONE);

				headerBinding.precipitationGridLayout.addView(gridItem);
			}

			//강설량
			if (hourlyForecastDto.isHasSnow()) {
				gridItem = layoutInflater.inflate(R.layout.view_detail_weather_data_item, null);

				((TextView) gridItem.findViewById(R.id.label)).setText(WeatherValueLabels.INSTANCE.getWeatherValueLabelsMap().get(WeatherValueType.snowVolume));
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
			LayoutInflater layoutInflater = LayoutInflater.from(binding.getRoot().getContext());

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
			layoutInflater = null;
		}

	}
}
