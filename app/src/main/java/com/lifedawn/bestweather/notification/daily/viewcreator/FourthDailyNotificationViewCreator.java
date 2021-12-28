package com.lifedawn.bestweather.notification.daily.viewcreator;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.RemoteViews;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.gridlayout.widget.GridLayout;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.RequestWeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.forremoteviews.RemoteViewProcessor;
import com.lifedawn.bestweather.retrofit.util.MultipleRestApiDownloader;
import com.lifedawn.bestweather.room.dto.DailyPushNotificationDto;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.dataprocessing.response.WeatherResponseProcessor;
import com.lifedawn.bestweather.weathers.models.AirQualityDto;

import java.time.ZonedDateTime;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FourthDailyNotificationViewCreator extends AbstractDailyNotiViewCreator {

	public FourthDailyNotificationViewCreator(Context context) {
		super(context);
	}

	public void setDataViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime,
	                         AirQualityDto airQualityDto) {
		drawViews(remoteViews, addressName, lastRefreshDateTime, airQualityDto);
	}

	@Override
	public void setTempDataViews(RemoteViews remoteViews) {
		drawViews(remoteViews, context.getString(R.string.address_name), ZonedDateTime.now().toString(),
				WeatherResponseProcessor.getTempAirQualityDto());
	}

	private void drawViews(RemoteViews remoteViews, String addressName, String lastRefreshDateTime, AirQualityDto airQualityDto) {
		RelativeLayout rootLayout = new RelativeLayout(context);
		final LayoutInflater layoutInflater = (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);

		View headerView = makeHeaderViews(layoutInflater, addressName, lastRefreshDateTime);
		headerView.setId(R.id.header);

		final ViewGroup sixWidgetView = (ViewGroup) layoutInflater.inflate(R.layout.view_sixth_widget, null, false);
		sixWidgetView.findViewById(R.id.weatherIcon).setVisibility(View.GONE);
		sixWidgetView.findViewById(R.id.temperature).setVisibility(View.GONE);
		sixWidgetView.findViewById(R.id.precipitation).setVisibility(View.GONE);

		((TextView) sixWidgetView.findViewById(R.id.measuring_station_name))
				.setText(new String(context.getString(R.string.measuring_station_name) + ": " + airQualityDto.getCityName()));

		String simpleAirQuality = context.getString(R.string.currentAirQuality) + ": " +
				AqicnResponseProcessor.getGradeDescription(airQualityDto.getAqi());
		((TextView) sixWidgetView.findViewById(R.id.airQuality)).setText(simpleAirQuality);

		GridLayout airQualityGridLayout = sixWidgetView.findViewById(R.id.airQualityGrid);

		//pm10, pm2.5, o3, co, so2, no2 순서로
		final String[] particleNames = {context.getString(R.string.pm10_str), context.getString(R.string.pm25_str),
				context.getString(R.string.o3_str), context.getString(R.string.co_str), context.getString(R.string.so2_str),
				context.getString(R.string.no2_str)};
		final int[] iconIds = {R.drawable.pm10, R.drawable.pm25, R.drawable.o3, R.drawable.co, R.drawable.so2, R.drawable.no2};

		List<String> gradeValueList = new ArrayList<>();
		List<String> gradeDescriptionList = new ArrayList<>();
		List<Integer> gradeTextColorList = new ArrayList<>();

		gradeValueList.add(airQualityDto.getCurrent().getPm10().toString());
		gradeDescriptionList.add(AqicnResponseProcessor.getGradeDescription(airQualityDto.getCurrent().getPm10()));
		gradeTextColorList.add(AqicnResponseProcessor.getGradeColorId(airQualityDto.getCurrent().getPm10()));

		gradeValueList.add(airQualityDto.getCurrent().getPm25().toString());
		gradeDescriptionList.add(AqicnResponseProcessor.getGradeDescription(airQualityDto.getCurrent().getPm25()));
		gradeTextColorList.add(AqicnResponseProcessor.getGradeColorId(airQualityDto.getCurrent().getPm25()));

		gradeValueList.add(airQualityDto.getCurrent().getO3().toString());
		gradeDescriptionList.add(AqicnResponseProcessor.getGradeDescription(airQualityDto.getCurrent().getO3()));
		gradeTextColorList.add(AqicnResponseProcessor.getGradeColorId(airQualityDto.getCurrent().getO3()));

		gradeValueList.add(airQualityDto.getCurrent().getCo().toString());
		gradeDescriptionList.add(AqicnResponseProcessor.getGradeDescription(airQualityDto.getCurrent().getCo()));
		gradeTextColorList.add(AqicnResponseProcessor.getGradeColorId(airQualityDto.getCurrent().getCo()));

		gradeValueList.add(airQualityDto.getCurrent().getSo2().toString());
		gradeDescriptionList.add(AqicnResponseProcessor.getGradeDescription(airQualityDto.getCurrent().getSo2()));
		gradeTextColorList.add(AqicnResponseProcessor.getGradeColorId(airQualityDto.getCurrent().getSo2()));

		gradeValueList.add(airQualityDto.getCurrent().getNo2().toString());
		gradeDescriptionList.add(AqicnResponseProcessor.getGradeDescription(airQualityDto.getCurrent().getNo2()));
		gradeTextColorList.add(AqicnResponseProcessor.getGradeColorId(airQualityDto.getCurrent().getNo2()));

		for (int i = 0; i < 6; i++) {
			addAirQualityGridItem(layoutInflater, airQualityGridLayout, particleNames[i], gradeValueList.get(i), gradeDescriptionList.get(i),
					gradeTextColorList.get(i), iconIds[i]);
		}

		RelativeLayout.LayoutParams headerViewLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		RelativeLayout.LayoutParams sixWidgetViewLayoutParams = new RelativeLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT,
				ViewGroup.LayoutParams.MATCH_PARENT);

		headerViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_TOP);
		sixWidgetViewLayoutParams.addRule(RelativeLayout.BELOW, R.id.header);
		sixWidgetViewLayoutParams.addRule(RelativeLayout.ALIGN_PARENT_BOTTOM);

		rootLayout.addView(headerView, headerViewLayoutParams);
		rootLayout.addView(sixWidgetView, sixWidgetViewLayoutParams);

		drawBitmap(rootLayout, remoteViews);
	}

	private void addAirQualityGridItem(LayoutInflater layoutInflater, GridLayout gridLayout, String label, String gradeValue,
	                                   String gradeDescription, int textColor, int iconId) {
		RelativeLayout view = (RelativeLayout) layoutInflater.inflate(R.layout.air_quality_item, null);
		((ImageView) view.findViewById(R.id.label_icon)).setImageResource(iconId);

		TextView labelTextView = view.findViewById(R.id.label);
		TextView gradeValueTextView = view.findViewById(R.id.value_int);
		TextView gradeDescriptionTextView = view.findViewById(R.id.value_str);

		labelTextView.setText(label);
		gradeValueTextView.setText(gradeValue);
		gradeDescriptionTextView.setText(gradeDescription);
		gradeDescriptionTextView.setTextColor(textColor);

		gridLayout.addView(view);
	}

	@Override
	public void setResultViews(RemoteViews remoteViews, DailyPushNotificationDto dailyPushNotificationDto, Set<WeatherSourceType> weatherSourceTypeSet, @Nullable @org.jetbrains.annotations.Nullable MultipleRestApiDownloader multipleRestApiDownloader, Set<RequestWeatherDataType> requestWeatherDataTypeSet) {
		final String refreshDateTime = multipleRestApiDownloader.getRequestDateTime().toString();

		final AirQualityDto airQualityDto = WeatherResponseProcessor.getAirQualityDto(context, multipleRestApiDownloader, null);
		final boolean successful = airQualityDto != null;

		if (successful) {
			setDataViews(remoteViews, dailyPushNotificationDto.getAddressName(), refreshDateTime, airQualityDto);
			RemoteViewProcessor.onSuccessfulProcess(remoteViews);

		} else {
			RemoteViewProcessor.onErrorProcess(remoteViews, context, RemoteViewProcessor.ErrorType.FAILED_LOAD_WEATHER_DATA);
		}
		makeNotification(remoteViews, dailyPushNotificationDto.getId());
	}

	@Override
	public Set<RequestWeatherDataType> getRequestWeatherDataTypeSet() {
		Set<RequestWeatherDataType> set = new HashSet<>();
		set.add(RequestWeatherDataType.airQuality);
		return set;
	}
}
