package com.lifedawn.bestweather.weathers.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PointF;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.View;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.theme.AppTheme;

import java.util.List;

public class DetailSingleTemperatureView extends View {
	private final int viewWidth;
	private final int viewHeight;
	private final int columnWidth;
	private final int MAX_TEMP;
	private final int MIN_TEMP;
	private final TextPaint TEMP_PAINT;
	private final Paint LINE_PAINT;
	private final Paint CIRCLE_PAINT;
	private final Paint MIN_MAX_TEMP_LINE_PAINT;
	private final float RADIUS;
	private List<Integer> tempList;

	public DetailSingleTemperatureView(Context context, List<Integer> tempList, int viewWidth, int viewHeight, int columnWidth) {
		super(context);
		this.viewWidth = viewWidth;
		this.viewHeight = viewHeight;
		this.columnWidth = columnWidth;

		TEMP_PAINT = new TextPaint();
		TEMP_PAINT.setTextAlign(Paint.Align.CENTER);
		TEMP_PAINT.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, getResources().getDisplayMetrics()));
		TEMP_PAINT.setColor(AppTheme.getColor(context, R.attr.textColor));

		LINE_PAINT = new Paint();
		LINE_PAINT.setAntiAlias(true);
		LINE_PAINT.setStyle(Paint.Style.FILL);
		LINE_PAINT.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.3f, getResources().getDisplayMetrics()));
		LINE_PAINT.setColor(AppTheme.getColor(context, R.attr.textColor));

		MIN_MAX_TEMP_LINE_PAINT = new Paint();
		MIN_MAX_TEMP_LINE_PAINT.setAntiAlias(true);
		MIN_MAX_TEMP_LINE_PAINT.setStyle(Paint.Style.FILL);
		MIN_MAX_TEMP_LINE_PAINT.setStrokeWidth(
				TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, getResources().getDisplayMetrics()));

		CIRCLE_PAINT = new Paint();
		CIRCLE_PAINT.setAntiAlias(true);
		CIRCLE_PAINT.setStyle(Paint.Style.FILL);
		CIRCLE_PAINT.setColor(Color.DKGRAY);

		RADIUS = getResources().getDimension(R.dimen.circleRadiusInSingleTemperature);
		this.tempList = tempList;

		int max = Integer.MIN_VALUE;
		int min = Integer.MAX_VALUE;

		for (Integer temp : tempList) {
			// 최대,최소 기온 구하기
			if (temp >= max) {
				max = temp;
			}

			if (temp <= min) {
				min = temp;
			}
		}
		MAX_TEMP = max;
		MIN_TEMP = min;

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
		final float TEXT_HEIGHT = TEMP_PAINT.descent() - TEMP_PAINT.ascent();

		final float VIEW_WIDTH = getWidth();
		final float VIEW_HEIGHT = getHeight() - ((TEXT_HEIGHT + RADIUS) * 2);
		final float COLUMN_WIDTH = VIEW_WIDTH / tempList.size();
		final float SPACING = ((VIEW_HEIGHT) / (MAX_TEMP - MIN_TEMP)) / 10f;

		float x = 0f;
		float y = 0f;

		PointF lastColumnPoint = new PointF();
		float[] circleX = new float[tempList.size()];
		float[] circleY = new float[tempList.size()];

		int index = 0;
		for (Integer temp : tempList) {
			x = COLUMN_WIDTH / 2f + COLUMN_WIDTH * index;
			y = (MIN_TEMP == MAX_TEMP) ? getHeight() / 2f : (10f * (MAX_TEMP - temp)) * SPACING + TEXT_HEIGHT + RADIUS;
			canvas.drawText(temp.toString(), x, y + RADIUS + TEXT_HEIGHT, TEMP_PAINT);

			if (index != 0) {
				canvas.drawLine(lastColumnPoint.x, lastColumnPoint.y, x, y, LINE_PAINT);
			}
			circleX[index] = x;
			circleY[index] = y;

			lastColumnPoint.set(x, y);
			index++;
		}

		for (int i = 0; i < circleX.length; i++) {
			canvas.drawCircle(circleX[i], circleY[i], RADIUS, CIRCLE_PAINT);
		}

		//draw min max temp line
		//drawMinMaxTempLine(canvas, MIN_TEMP);
		//drawMinMaxTempLine(canvas, MAX_TEMP);
	}

	private void drawMinMaxTempLine(Canvas canvas, int temp) {
		final float TEXT_HEIGHT = TEMP_PAINT.descent() - TEMP_PAINT.ascent();
		final float RADIUS = TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, getResources().getDisplayMetrics());

		final float VIEW_WIDTH = getWidth();
		final float VIEW_HEIGHT = getHeight() - ((TEXT_HEIGHT + RADIUS) * 2);
		final float COLUMN_WIDTH = VIEW_WIDTH / tempList.size();
		final float SPACING = ((VIEW_HEIGHT) / (MAX_TEMP - MIN_TEMP)) / 10f;

		float startX = 0f;
		float stopX = 0f;
		float y = 0f;

		startX = COLUMN_WIDTH / 2f + COLUMN_WIDTH * 0;
		stopX = COLUMN_WIDTH / 2f + COLUMN_WIDTH * (tempList.size() - 1);
		y = (10f * (MAX_TEMP - temp)) * SPACING + TEXT_HEIGHT + RADIUS;

		canvas.drawLine(startX, y, stopX, y, MIN_MAX_TEMP_LINE_PAINT);
	}

}
