package com.lifedawn.bestweather.weathers.detailfragment.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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
import java.util.List;

public class DetailHourlyForecastViewPagerAdapter extends RecyclerView.Adapter<DetailHourlyForecastViewPagerAdapter.ViewHolder> {
	private Context context;
	private LayoutInflater layoutInflater;
	private DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern("M.d E");
	private DateTimeFormatter hoursFormatter;

	private List<HourlyForecastDto> hourlyForecastDtoList;

	public DetailHourlyForecastViewPagerAdapter(Context context) {
		this.context = context;
		layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		ValueUnits clockUnit =
				ValueUnits.valueOf(PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_key_unit_clock),
						ValueUnits.clock24.name()));
		hoursFormatter = DateTimeFormatter.ofPattern(clockUnit == ValueUnits.clock12 ? "a hh:00" : "HH:00");
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
	public void onBindViewHolder(@NonNull @NotNull DetailHourlyForecastViewPagerAdapter.ViewHolder holder, int position) {
		holder.onBind(hourlyForecastDtoList.get(position));
	}

	@Override
	public int getItemCount() {
		return hourlyForecastDtoList.size();
	}

	protected class ViewHolder extends RecyclerView.ViewHolder {
		private ItemviewDetailForecastBinding binding;
		private HeaderviewDetailHourlyforecastBinding headerBinding;

		public ViewHolder(@NonNull @NotNull View itemView) {
			super(itemView);
			binding = ItemviewDetailForecastBinding.bind(itemView);
			LayoutInflater layoutInflater = (LayoutInflater) itemView.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			headerBinding = HeaderviewDetailHourlyforecastBinding.inflate(layoutInflater, null, false);
			binding.header.addView(headerBinding.getRoot());
		}

		public void onBind(HourlyForecastDto hourlyForecastDto) {
			binding.gridView.setAdapter(null);

			//header 화면 구성
			headerBinding.date.setText(hourlyForecastDto.getHours().format(dateFormatter));
			headerBinding.hours.setText(hourlyForecastDto.getHours().format(hoursFormatter));
			headerBinding.weatherIcon.setImageResource(hourlyForecastDto.getWeatherIcon());
			headerBinding.temp.setText(hourlyForecastDto.getTemp());
			headerBinding.pop.setText(hourlyForecastDto.getPop());

			if (!hourlyForecastDto.isHasSnow()) {
				headerBinding.snowVolumeLayout.setVisibility(View.GONE);
			} else {
				headerBinding.snowVolumeLayout.setVisibility(View.VISIBLE);
				headerBinding.snowVolume.setText(hourlyForecastDto.getSnowVolume());
			}
			if (!hourlyForecastDto.isHasRain()) {
				headerBinding.rainVolumeLayout.setVisibility(View.GONE);
			} else {
				headerBinding.rainVolumeLayout.setVisibility(View.VISIBLE);
				headerBinding.rainVolume.setText(hourlyForecastDto.getRainVolume());
			}
			if (!hourlyForecastDto.isHasPrecipitation()) {
				headerBinding.precipitationVolumeLayout.setVisibility(View.GONE);
			} else {
				headerBinding.precipitationVolumeLayout.setVisibility(View.VISIBLE);
				headerBinding.precipitationVolume.setText(hourlyForecastDto.getPrecipitationVolume());
			}

			//gridview구성
			WeatherDataGridViewAdapter adapter = new WeatherDataGridViewAdapter(context);
			//공통 - 날씨, 기온, 강수량, 강수확률, 풍향, 풍속, 바람세기, 습도

			adapter.addItem(new GridItemDto(context.getString(R.string.weather), hourlyForecastDto.getWeatherDescription(),
					ContextCompat.getDrawable(context, hourlyForecastDto.getWeatherIcon())));
			adapter.addItem(new GridItemDto(context.getString(R.string.temperature), hourlyForecastDto.getTemp(),
					ContextCompat.getDrawable(context, R.drawable.temperature)));

			if (hourlyForecastDto.getFeelsLikeTemp() != null) {
				adapter.addItem(new GridItemDto(context.getString(R.string.real_feel_temperature), hourlyForecastDto.getFeelsLikeTemp(),
						ContextCompat.getDrawable(context, R.drawable.realfeeltemperature)));
			}
			if (hourlyForecastDto.getPrecipitationType() != null) {
				adapter.addItem(new GridItemDto(context.getString(R.string.precipitation_type), hourlyForecastDto.getPrecipitationType(),
						ContextCompat.getDrawable(context, hourlyForecastDto.getPrecipitationTypeIcon())));
			}

			if (hourlyForecastDto.isHasPrecipitation()) {
				adapter.addItem(new GridItemDto(context.getString(R.string.precipitation_volume), hourlyForecastDto.getPrecipitationVolume(),
						ContextCompat.getDrawable(context, R.drawable.precipitationvolume)));
			}

			adapter.addItem(new GridItemDto(context.getString(R.string.probability_of_precipitation), hourlyForecastDto.getPop(),
					ContextCompat.getDrawable(context, R.drawable.pop)));

			if (hourlyForecastDto.getPor() != null) {
				adapter.addItem(new GridItemDto(context.getString(R.string.probability_of_rain), hourlyForecastDto.getPor(),
						ContextCompat.getDrawable(context, R.drawable.por)));
			}
			if (hourlyForecastDto.getPos() != null) {
				adapter.addItem(new GridItemDto(context.getString(R.string.probability_of_snow), hourlyForecastDto.getPos(),
						ContextCompat.getDrawable(context, R.drawable.pos)));
			}

			if (hourlyForecastDto.isHasRain()) {
				adapter.addItem(new GridItemDto(context.getString(R.string.rain_volume), hourlyForecastDto.getRainVolume(),
						ContextCompat.getDrawable(context, R.drawable.raindrop)));
			}
			if (hourlyForecastDto.isHasSnow()) {
				adapter.addItem(new GridItemDto(context.getString(R.string.snow_volume), hourlyForecastDto.getSnowVolume(),
						ContextCompat.getDrawable(context, R.drawable.snowparticle)));
			}
			adapter.addItem(new GridItemDto(context.getString(R.string.wind_direction), hourlyForecastDto.getWindDirection(),
					ContextCompat.getDrawable(context, R.drawable.arrow), hourlyForecastDto.getWindDirectionVal()));

			adapter.addItem(new GridItemDto(context.getString(R.string.wind_speed), hourlyForecastDto.getWindSpeed(),
					ContextCompat.getDrawable(context, R.drawable.windspeed)));

			if (hourlyForecastDto.getWindGust() != null) {
				adapter.addItem(new GridItemDto(context.getString(R.string.wind_gust), hourlyForecastDto.getWindGust(),
						ContextCompat.getDrawable(context, R.drawable.windgust)));
			}
			if (hourlyForecastDto.getPressure() != null) {
				adapter.addItem(new GridItemDto(context.getString(R.string.pressure), hourlyForecastDto.getPressure(),
						ContextCompat.getDrawable(context, R.drawable.pressure)));
			}
			adapter.addItem(new GridItemDto(context.getString(R.string.wind_strength), hourlyForecastDto.getWindStrength(),
					ContextCompat.getDrawable(context, R.drawable.windstrength)));
			adapter.addItem(new GridItemDto(context.getString(R.string.humidity), hourlyForecastDto.getHumidity(),
					ContextCompat.getDrawable(context, R.drawable.humidity)));

			//나머지 - 강우확률, 강설확률, 강우량, 강설량, 돌풍, 기압, 이슬점, 운량, 시정, 자외선, 체감기온


			if (hourlyForecastDto.getDewPointTemp() != null) {
				adapter.addItem(new GridItemDto(context.getString(R.string.dew_point), hourlyForecastDto.getDewPointTemp(),
						ContextCompat.getDrawable(context, R.drawable.dewpoint)));
			}
			if (hourlyForecastDto.getCloudiness() != null) {
				adapter.addItem(new GridItemDto(context.getString(R.string.cloud_cover), hourlyForecastDto.getCloudiness(),
						ContextCompat.getDrawable(context, R.drawable.cloudiness)));
			}
			if (hourlyForecastDto.getVisibility() != null) {
				adapter.addItem(new GridItemDto(context.getString(R.string.visibility), hourlyForecastDto.getVisibility(),
						ContextCompat.getDrawable(context, R.drawable.visibility)));
			}
			if (hourlyForecastDto.getUvIndex() != null) {
				adapter.addItem(new GridItemDto(context.getString(R.string.uv_index), hourlyForecastDto.getUvIndex(),
						ContextCompat.getDrawable(context, R.drawable.uv)));
			}

			binding.gridView.setAdapter(adapter);
		}
	}
}
