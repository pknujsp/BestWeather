package com.lifedawn.bestweather.ui.weathers.simplefragment.sunsetrise.graph;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.DisplayMetrics;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.constants.BundleKey;
import com.lifedawn.bestweather.commons.constants.SunRiseSetType;
import com.lifedawn.bestweather.commons.constants.ValueUnits;
import com.lifedawn.bestweather.commons.constants.WeatherProviderType;
import com.lifedawn.bestweather.databinding.FragmentSunsetriseBinding;
import com.lifedawn.bestweather.ui.weathers.WeatherFragment;
import com.lifedawn.bestweather.ui.weathers.detailfragment.sunsetrise.DetailSunRiseSetFragment;
import com.lifedawn.bestweather.ui.weathers.simplefragment.interfaces.IWeatherValues;
import com.luckycatlabs.sunrisesunset.dto.Location;

import org.jetbrains.annotations.NotNull;

import java.time.ZoneId;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Map;

public class SimpleGraphSunRiseSetFragment extends Fragment implements IWeatherValues {
	private FragmentSunsetriseBinding binding;
	private Location location;
	private Double latitude;
	private Double longitude;
	private String addressName;
	private String countryCode;
	private WeatherProviderType mainWeatherProviderType;
	private ZoneId zoneId;

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		Bundle bundle = getArguments();
		latitude = bundle.getDouble(BundleKey.Latitude.name());
		longitude = bundle.getDouble(BundleKey.Longitude.name());
		addressName = bundle.getString(BundleKey.AddressName.name());
		countryCode = bundle.getString(BundleKey.CountryCode.name());
		mainWeatherProviderType = (WeatherProviderType) bundle.getSerializable(
				BundleKey.WeatherProvider.name());
		zoneId = (ZoneId) bundle.getSerializable(BundleKey.TimeZone.name());

		location = new Location(latitude, longitude);
	}

	@Nullable
	@org.jetbrains.annotations.Nullable
	@Override
	public View onCreateView(@NonNull @NotNull LayoutInflater inflater, @Nullable @org.jetbrains.annotations.Nullable ViewGroup container,
	                         @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		binding = FragmentSunsetriseBinding.inflate(inflater, container, false);
		return binding.getRoot();
	}

	@Override
	public void onViewCreated(@NonNull @NotNull View view, @Nullable @org.jetbrains.annotations.Nullable Bundle savedInstanceState) {
		super.onViewCreated(view, savedInstanceState);

		binding.weatherCardViewHeader.detailForecast.setVisibility(View.VISIBLE);
		binding.weatherCardViewHeader.compareForecast.setVisibility(View.INVISIBLE);
		binding.weatherCardViewHeader.forecastName.setText(R.string.sun_set_rise);
		binding.weatherCardViewHeader.detailForecast.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				DetailSunRiseSetFragment detailSunRiseSetFragment = new DetailSunRiseSetFragment();
				detailSunRiseSetFragment.setArguments(getArguments());
				String tag = DetailSunRiseSetFragment.class.getName();

				FragmentManager fragmentManager = getParentFragment().getParentFragmentManager();

				fragmentManager.beginTransaction().hide(
						fragmentManager.findFragmentByTag(WeatherFragment.class.getName())).add(R.id.fragment_container,
						detailSunRiseSetFragment, tag).addToBackStack(tag).commit();
			}
		});


		IntentFilter intentFilter = new IntentFilter();
		intentFilter.addAction(Intent.ACTION_TIME_TICK);

		requireActivity().registerReceiver(broadcastReceiver, intentFilter);
	}

	private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			if (intent.getAction() != null) {
				if (Intent.ACTION_TIME_TICK.equals(intent.getAction())) {
					//sunSetRiseViewGroup.refresh();
				}
			}
		}
	};

	@Override
	public void setValuesToViews() {
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		requireActivity().unregisterReceiver(broadcastReceiver);
	}

	private static final class GraphBackgroundView extends FrameLayout {
		private final DateTimeFormatter DATETIME_FORMATTER;
		private final int GRAPH_HEIGHT;
		private Rect currentViewBoxRect;
		private final ValueUnits CLOCK_UNIT;
		private final ZoneOffset ZONE_OFFSET;

		public GraphBackgroundView(Context context, ValueUnits CLOCK_UNIT, ZoneOffset ZONE_OFFSET) {
			super(context);
			this.CLOCK_UNIT = CLOCK_UNIT;
			this.ZONE_OFFSET = ZONE_OFFSET;
			this.DATETIME_FORMATTER = DateTimeFormatter.ofPattern(CLOCK_UNIT == ValueUnits.clock12 ? "M.d E\na h:mm" : "M.d E\nH:mm");
			this.GRAPH_HEIGHT = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 80f, context.getResources().getDisplayMetrics());

			setWillNotDraw(false);
		}

		@SuppressLint("DrawAllocation")
		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			GraphCurrentView graphCurrentView = new GraphCurrentView(getContext(), CLOCK_UNIT);
			currentViewBoxRect = graphCurrentView.getCurrentTimeInfoBoxSize();
			final int height = GRAPH_HEIGHT * 2 + currentViewBoxRect.height() + graphCurrentView.BOX_MARGIN;
			setMeasuredDimension(MeasureSpec.getSize(widthMeasureSpec), (int) height);
		}

		@Override
		protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
			super.onLayout(changed, left, top, right, bottom);
		}

		@Override
		protected void onDraw(Canvas canvas) {

		}
	}

	private static final class GraphCurrentView extends View {
		private Drawable img;
		private final DateTimeFormatter DATETIME_FORMATTER;
		private final int BOX_PADDING;
		private final int TEXT_SPACING;
		private final int BOX_MARGIN;
		private final String CURRENT_TEXT;

		private Map<String, Point> currentTimeMap;
		private int baselineY;

		private Paint boxPaint = new Paint();
		private Paint linePaint = new Paint();
		private TextPaint timeTextPaint = new TextPaint();
		private TextPaint currentTextPaint = new TextPaint();

		public GraphCurrentView(Context context, ValueUnits clockUnit) {
			super(context);
			this.DATETIME_FORMATTER = DateTimeFormatter.ofPattern(clockUnit == ValueUnits.clock12 ? "M.d E\na h:mm" : "M.d E\nH:mm");

			final DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();

			boxPaint.setColor(ContextCompat.getColor(context, R.color.gray));
			linePaint.setColor(Color.BLUE);
			timeTextPaint.setColor(Color.BLACK);
			timeTextPaint.setTextAlign(Paint.Align.CENTER);
			timeTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12f, displayMetrics));

			currentTextPaint.setColor(Color.BLACK);
			currentTextPaint.setTextAlign(Paint.Align.CENTER);
			currentTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, displayMetrics));

			BOX_PADDING = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 4f, displayMetrics);
			TEXT_SPACING = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, displayMetrics);
			BOX_MARGIN = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, displayMetrics);

			CURRENT_TEXT = context.getString(R.string.current);
		}

		public void setCurrentTimeMap(Map<String, Point> currentTimeMap) {
			this.currentTimeMap = currentTimeMap;
		}

		public void setBaselineY(int baselineY) {
			this.baselineY = baselineY;
		}

		public void setImg(SunRiseSetType sunRiseSetType) {
			this.img = ContextCompat.getDrawable(getContext(), sunRiseSetType == SunRiseSetType.RISE ? R.drawable.day_clear :
					R.drawable.night_clear);
		}

		public Rect getCurrentTimeInfoBoxSize() {
			Rect currentTextRect = new Rect();
			currentTextPaint.getTextBounds(CURRENT_TEXT, 0, CURRENT_TEXT.length(), currentTextRect);

			Rect timeTextRect = new Rect();

			final String time = ZonedDateTime.now().format(DATETIME_FORMATTER);
			final String[] separatedText = time.split("\n");
			if (separatedText[0].length() > separatedText[1].length()) {
				timeTextPaint.getTextBounds(separatedText[0], 0, separatedText[0].length(), timeTextRect);
			} else {
				timeTextPaint.getTextBounds(separatedText[1], 0, separatedText[1].length(), timeTextRect);
			}

			final int columnWidth = timeTextRect.width();

			StaticLayout.Builder builder = StaticLayout.Builder.obtain(time, 0, time.length(), timeTextPaint, columnWidth);
			StaticLayout staticLayout = builder.build();

			Rect boxRect = new Rect();

			boxRect.left = 0;
			boxRect.right = BOX_PADDING * 2 + timeTextRect.width();
			boxRect.top = 0;
			boxRect.bottom = BOX_PADDING * 2 + currentTextRect.height() + TEXT_SPACING + staticLayout.getHeight();

			return boxRect;
		}

		@Override
		protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
			super.onMeasure(widthMeasureSpec, heightMeasureSpec);
		}

		@Override
		protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
			super.onLayout(changed, left, top, right, bottom);
		}

		@Override
		protected void onDraw(Canvas canvas) {

		}
	}

}