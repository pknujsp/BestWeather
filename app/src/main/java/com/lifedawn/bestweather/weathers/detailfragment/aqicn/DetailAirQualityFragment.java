package com.lifedawn.bestweather.weathers.detailfragment.aqicn;

import android.content.SharedPreferences;
import android.content.res.ColorStateList;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.preference.PreferenceManager;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.databinding.FragmentAirQualityDetailBinding;
import com.lifedawn.bestweather.retrofit.responses.aqicn.GeolocalizedFeedResponse;
import com.lifedawn.bestweather.theme.AppTheme;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;
import com.lifedawn.bestweather.weathers.simplefragment.aqicn.AirQualityForecastObj;
import com.lifedawn.bestweather.weathers.simplefragment.interfaces.IWeatherValues;
import com.lifedawn.bestweather.weathers.view.AirQualityBarView;
import com.lifedawn.bestweather.weathers.view.TextValueView;

import org.jetbrains.annotations.NotNull;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.TimeZone;

public class DetailAirQualityFragment extends Fragment implements IWeatherValues {
	private GeolocalizedFeedResponse response;
	private FragmentAirQualityDetailBinding binding;
	private ValueUnits clockUnit;
	private TimeZone timeZone;

	public void setResponse(GeolocalizedFeedResponse response) {
		this.response = response;
	}

	@Override
	public void onCreate(@Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle bundle = getArguments();
		timeZone = (TimeZone) bundle.getSerializable(getString(R.string.bundle_key_timezone));
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(getContext());
		clockUnit = ValueUnits.enumOf(sharedPreferences.getString(getString(R.string.pref_key_unit_clock), ValueUnits.clock12.name()));
	}

	@Nullable
	@org.jetbrains.annotations.Nullable
	@Override
	public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
	                         @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		binding = FragmentAirQualityDetailBinding.inflate(inflater);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);
		binding.toolbar.backBtn.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				getParentFragmentManager().popBackStackImmediate();
			}
		});

		binding.toolbar.fragmentTitle.setText(R.string.detail_air_quality);
		setValuesToViews();
	}

	@Override
	public void setValuesToViews() {
		List<AirQualityForecastObj> airQualityForecastObjList = AqicnResponseProcessor.getAirQualityForecastObjList(response, timeZone);

		final int columnWidth = (int) getResources().getDimension(R.dimen.columnWidthInAirQualityBarView);
		final int viewHeight = (int) getResources().getDimension(R.dimen.viewHeightOfAirQualityBarView);
		final int columnsCount = airQualityForecastObjList.size() + 1;
		final int viewWidth = columnWidth * columnsCount;

		List<AirQualityBarView.AirQualityObj> pm10AirQualityObjList = new ArrayList<>();
		List<AirQualityBarView.AirQualityObj> pm25AirQualityObjList = new ArrayList<>();
		List<AirQualityBarView.AirQualityObj> o3AirQualityObjList = new ArrayList<>();

		List<String> dateList = new ArrayList<>();

		pm10AirQualityObjList.add(new AirQualityBarView.AirQualityObj(response.getData().getIaqi().getPm10().getValue()));
		pm25AirQualityObjList.add(new AirQualityBarView.AirQualityObj(response.getData().getIaqi().getPm25().getValue()));
		o3AirQualityObjList.add(new AirQualityBarView.AirQualityObj(response.getData().getIaqi().getO3().getValue()));
		dateList.add(getString(R.string.current));

		SimpleDateFormat dateFormat = new SimpleDateFormat("M.d E", Locale.getDefault());

		for (AirQualityForecastObj airQualityForecastObj : airQualityForecastObjList) {
			dateList.add(dateFormat.format(airQualityForecastObj.date));
			pm10AirQualityObjList.add(new AirQualityBarView.AirQualityObj(airQualityForecastObj.pm10));
			pm25AirQualityObjList.add(new AirQualityBarView.AirQualityObj(airQualityForecastObj.pm25));
			o3AirQualityObjList.add(new AirQualityBarView.AirQualityObj(airQualityForecastObj.o3));
		}

		final int dateRowHeight = (int) getResources().getDimension(R.dimen.dateValueRowHeightInCOMMON);
		TextValueView dateRow = new TextValueView(getContext(), viewWidth, dateRowHeight, columnWidth);
		AirQualityBarView pm10BarView = new AirQualityBarView(getContext(), viewWidth, viewHeight, columnWidth, pm10AirQualityObjList);
		AirQualityBarView pm25BarView = new AirQualityBarView(getContext(), viewWidth, viewHeight, columnWidth, pm25AirQualityObjList);
		AirQualityBarView o3BarView = new AirQualityBarView(getContext(), viewWidth, viewHeight, columnWidth, o3AirQualityObjList);

		dateRow.setValueList(dateList);

		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT,
				ViewGroup.LayoutParams.WRAP_CONTENT);
		layoutParams.bottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, getResources().getDisplayMetrics());

		addLabelView(R.drawable.date, getString(R.string.date), dateRowHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.pm10_str), viewHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.pm25_str), viewHeight);
		addLabelView(R.drawable.temp_icon, getString(R.string.o3_str), viewHeight);

		binding.forecastView.addView(dateRow, layoutParams);
		binding.forecastView.addView(pm10BarView, layoutParams);
		binding.forecastView.addView(pm25BarView, layoutParams);
		binding.forecastView.addView(o3BarView, layoutParams);

		GeolocalizedFeedResponse.Data.IAqi iAqi = response.getData().getIaqi();
		Integer co = (int) Double.parseDouble(iAqi.getCo().getValue());
		Integer so2 = (int) Double.parseDouble(iAqi.getSo2().getValue());
		Integer no2 = (int) Double.parseDouble(iAqi.getNo2().getValue());

		addGridItem(co, AqicnResponseProcessor.getGradeDescription(co), R.string.co_str, R.drawable.temp_icon);
		addGridItem(so2, AqicnResponseProcessor.getGradeDescription(so2), R.string.so2_str, R.drawable.temp_icon);
		addGridItem(no2, AqicnResponseProcessor.getGradeDescription(no2), R.string.no2_str, R.drawable.temp_icon);

		binding.measuringStationName.setText(response.getData().getCity().getName());
		binding.updatedTime.setText(response.getData().getCity().getName());

		// time : 2021-10-22T11:16:41+09:00
		SimpleDateFormat syncDateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ", Locale.US);
		Date syncDateTime = new Date();
		try {
			syncDateTime = syncDateTimeFormat.parse(response.getData().getTime().getIso());
		} catch (Exception e) {

		}
		Calendar sync = Calendar.getInstance();
		sync.setTimeInMillis(syncDateTime.getTime());
		SimpleDateFormat updatedTimeFormat = new SimpleDateFormat(clockUnit == ValueUnits.clock12 ? "M.d E a H:mm" : "M.d E HH:mm",
				Locale.getDefault());
		binding.updatedTime.setText(updatedTimeFormat.format(sync.getTime()));
	}

	protected ImageView addLabelView(int labelImgId, String labelDescription, int viewHeight) {
		ImageView labelView = new ImageView(getContext());
		labelView.setImageDrawable(ContextCompat.getDrawable(getContext(), labelImgId));
		labelView.setClickable(true);
		labelView.setScaleType(ImageView.ScaleType.CENTER);
		labelView.setImageTintList(ColorStateList.valueOf(AppTheme.getColor(getContext(), R.attr.iconColor)));
		labelView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				Toast.makeText(getContext(), labelDescription, Toast.LENGTH_SHORT).show();
			}
		});

		int width = (int) getResources().getDimension(R.dimen.labelIconColumnWidthInCOMMON);
		LinearLayout.LayoutParams layoutParams = new LinearLayout.LayoutParams(width, viewHeight);
		layoutParams.gravity = Gravity.CENTER;
		layoutParams.bottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 8f, getResources().getDisplayMetrics());
		labelView.setLayoutParams(layoutParams);

		binding.labels.addView(labelView);
		return labelView;
	}

	protected final View addGridItem(Integer valueInt, String valueStr, int labelDescriptionId, @NonNull Integer labelIconId) {
		View gridItem = getLayoutInflater().inflate(R.layout.air_quality_item, null);
		((ImageView) gridItem.findViewById(R.id.label_icon)).setImageResource(labelIconId);
		((TextView) gridItem.findViewById(R.id.label)).setText(labelDescriptionId);
		((TextView) gridItem.findViewById(R.id.value_int)).setText(valueInt.toString());
		((TextView) gridItem.findViewById(R.id.value_str)).setText(valueStr);

		binding.grid.addView(gridItem);
		return gridItem;
	}

}
