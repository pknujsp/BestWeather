package com.lifedawn.bestweather.weathers.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.View;

import androidx.preference.PreferenceManager;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.ValueUnits;
import com.lifedawn.bestweather.theme.AppTheme;

import java.util.ArrayList;
import java.util.List;

public class DetailDoubleTemperatureView extends View {
	private final int viewWidth;
	private final int viewHeight;
	private final int columnWidth;
	private final int maxTemp;
	private final int minTemp;
	private final TextPaint tempPaint;
	private final Paint linePaint;
	private final Paint minMaxTempLinePaint;
	private final Paint maxCirclePaint;
	private final Paint minCirclePaint;
	private final int circleRadius;

	private List<Integer> maxTempList = new ArrayList<>();
	private List<Integer> minTempList = new ArrayList<>();

	public DetailDoubleTemperatureView(Context context, int viewWidth, int viewHeight, int columnWidth, List<Integer> minTempList,
	                                   List<Integer> maxTempList) {
		super(context);
		this.viewWidth = viewWidth;
		this.viewHeight = viewHeight;
		this.columnWidth = columnWidth;

		circleRadius = (int) getResources().getDimension(R.dimen.circleRadiusInDoubleTemperature);

		tempPaint = new TextPaint();
		tempPaint.setTextAlign(Paint.Align.CENTER);
		tempPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 12f, getResources().getDisplayMetrics()));
		tempPaint.setColor(AppTheme.getColor(context, R.attr.textColorInWeatherCard));

		linePaint = new Paint();
		linePaint.setAntiAlias(true);
		linePaint.setStyle(Paint.Style.FILL);
		linePaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.3f, getResources().getDisplayMetrics()));
		linePaint.setColor(AppTheme.getColor(context, R.attr.textColorInWeatherCard));

		minMaxTempLinePaint = new Paint();
		minMaxTempLinePaint.setAntiAlias(true);
		minMaxTempLinePaint.setStyle(Paint.Style.FILL);
		minMaxTempLinePaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, getResources().getDisplayMetrics()));

		maxCirclePaint = new Paint();
		maxCirclePaint.setAntiAlias(true);
		maxCirclePaint.setStyle(Paint.Style.FILL);
		maxCirclePaint.setColor(Color.RED);

		minCirclePaint = new Paint();
		minCirclePaint.setAntiAlias(true);
		minCirclePaint.setStyle(Paint.Style.FILL);
		minCirclePaint.setColor(Color.BLUE);

		ValueUnits tempUnit =
				ValueUnits.enumOf(PreferenceManager.getDefaultSharedPreferences(context).getString(context.getString(R.string.pref_key_unit_temp), ValueUnits.celsius.name()));

		this.minTempList.addAll(minTempList);
		this.maxTempList.addAll(maxTempList);

		int max = Integer.MIN_VALUE;
		int min = Integer.MAX_VALUE;
		int maxTemp = 0;
		int minTemp = 0;

		for (int i = 0; i < this.minTempList.size(); i++) {
			maxTemp = this.maxTempList.get(i);
			minTemp = this.minTempList.get(i);

			// 최대,최소 기온 구하기
			if (maxTemp >= max) {
				max = maxTemp;
			}
			if (minTemp <= min) {
				min = minTemp;
			}
		}
		this.maxTemp = max;
		this.minTemp = min;

		setWillNotDraw(false);
	}


	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(viewWidth, viewHeight);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);
		drawGraph(canvas);
	}

	private void drawGraph(Canvas canvas) {
		// 텍스트의 높이+원의 반지름 만큼 뷰의 상/하단에 여백을 설정한다.
		final float TEXT_HEIGHT = tempPaint.descent() - tempPaint.ascent();
		final float TEXT_ASCENT = -tempPaint.ascent();

		final float VIEW_HEIGHT = getHeight() - ((TEXT_HEIGHT + circleRadius) * 2);
		final float SPACING = ((VIEW_HEIGHT) / (maxTemp - minTemp)) / 10f;

		int min = 0;
		int max = 0;
		float x = 0f;
		float minY = 0f;
		float maxY = 0f;

		PointF lastMinColumnPoint = new PointF();
		PointF lastMaxColumnPoint = new PointF();

		float[] minCircleXArr = new float[minTempList.size()];
		float[] minCircleYArr = new float[minTempList.size()];
		float[] maxCircleXArr = new float[minTempList.size()];
		float[] maxCircleYArr = new float[minTempList.size()];

		for (int index = 0; index < maxTempList.size(); index++) {
			min = minTempList.get(index);
			max = maxTempList.get(index);

			x = columnWidth / 2f + columnWidth * index;
			minY = (10f * (maxTemp - min)) * SPACING + TEXT_HEIGHT + circleRadius;
			maxY = (10f * (maxTemp - max)) * SPACING + TEXT_HEIGHT + circleRadius;

			if (index != 0) {
				canvas.drawLine(lastMinColumnPoint.x, lastMinColumnPoint.y, x, minY, linePaint);
				canvas.drawLine(lastMaxColumnPoint.x, lastMaxColumnPoint.y, x, maxY, linePaint);
			}

			canvas.drawCircle(x, minY, circleRadius, minCirclePaint);
			canvas.drawText(minTempList.get(index).toString(), x, minY + circleRadius + TEXT_HEIGHT, tempPaint);

			canvas.drawCircle(x, maxY, circleRadius, maxCirclePaint);
			canvas.drawText(maxTempList.get(index).toString(), x, maxY - circleRadius - TEXT_HEIGHT + TEXT_ASCENT, tempPaint);

			lastMinColumnPoint.set(x, minY);
			lastMaxColumnPoint.set(x, maxY);

			minCircleXArr[index] = x;
			minCircleYArr[index] = minY;
			maxCircleXArr[index] = x;
			maxCircleYArr[index] = maxY;
		}

		for (int i = 0; i < minCircleXArr.length; i++) {
			canvas.drawCircle(minCircleXArr[i], minCircleYArr[i], circleRadius, minCirclePaint);
			canvas.drawCircle(minCircleXArr[i], maxCircleYArr[i], circleRadius, maxCirclePaint);
		}

	}


}
