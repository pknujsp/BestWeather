package com.lifedawn.bestweather.ui.weathers.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.graphics.PointF;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.View;

import com.lifedawn.bestweather.R;

import java.util.ArrayList;
import java.util.List;

public class DetailSingleTemperatureView extends View implements ICleaner {
	private final TextPaint tempPaint;
	private final Paint linePaint;
	private final Paint circlePaint;
	private final int circleRadius;
	private final String tempUnit = "°";
	private final Rect textRect = new Rect();

	private List<Integer> tempList = new ArrayList<>();

	private final int minTemp;
	private final int maxTemp;

	public DetailSingleTemperatureView(Context context, List<Integer> tempList) {
		super(context);
		circleRadius = (int) getResources().getDimension(R.dimen.circleRadiusInDoubleTemperature);

		tempPaint = new TextPaint();
		tempPaint.setTextAlign(Paint.Align.CENTER);
		tempPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, getResources().getDisplayMetrics()));
		tempPaint.setColor(Color.BLACK);

		linePaint = new Paint();
		linePaint.setAntiAlias(true);
		linePaint.setStyle(Paint.Style.STROKE);
		linePaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.3f, getResources().getDisplayMetrics()));
		linePaint.setColor(Color.DKGRAY);

		circlePaint = new Paint();
		circlePaint.setAntiAlias(true);
		circlePaint.setStyle(Paint.Style.FILL);
		circlePaint.setColor(Color.DKGRAY);

		this.tempList.addAll(tempList);

		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;
		int temp = 0;

		for (int i = 0; i < this.tempList.size(); i++) {
			temp = this.tempList.get(i);

			if (temp >= max) {
				max = temp;
			}
			if (temp <= min) {
				min = temp;
			}
		}

		minTemp = min;
		maxTemp = max;

		setWillNotDraw(false);
	}

	public void setTempTextSizeSp(int textSizeSp) {
		tempPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSizeSp, getResources().getDisplayMetrics()));
	}

	public void setTempTextSizePx(int textSize) {
		tempPaint.setTextSize(textSize);
	}

	public void setTextColor(int textColor) {
		tempPaint.setColor(textColor);
	}

	public void setLineColor(int color) {
		linePaint.setColor(color);
	}

	public void setCircleColor(int color) {
		circlePaint.setColor(color);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		String test = "20°";
		tempPaint.getTextBounds(test, 0, test.length(), textRect);
		super.onMeasure(widthMeasureSpec, heightMeasureSpec);
	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {
		super.onLayout(changed, l, t, r, b);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		drawGraph(canvas);
	}

	private void drawGraph(Canvas canvas) {
		final int tempsCount = tempList.size();
		final float textHeight = textRect.height();
		final float margin = circleRadius * 2f;
		final float spacingBetweenTextAndCircle = circleRadius * 3;

		final float viewHeight = getHeight() - margin * 2 - textHeight - spacingBetweenTextAndCircle;
		final float spacing = (viewHeight) / (maxTemp - minTemp);
		final int columnWidth = getWidth() / tempsCount;

		int temp = 0;
		float x = 0f;
		float y = 0f;

		PointF lastColumnPoint = new PointF();

		float[] circleXArr = new float[tempsCount];
		float[] circleYArr = new float[tempsCount];

		List<PointF> linePointList = new ArrayList<>();

		for (int index = 0; index < tempsCount; index++) {
			temp = tempList.get(index);

			x = columnWidth / 2f + columnWidth * index;
			y = (minTemp == maxTemp) ? getHeight() / 2f : (maxTemp - temp) * spacing + margin + textHeight + spacingBetweenTextAndCircle;

			canvas.drawText(temp + tempUnit, x, y - spacingBetweenTextAndCircle + tempPaint.descent(), tempPaint);

			lastColumnPoint.set(x, y);

			circleXArr[index] = x;
			circleYArr[index] = y;

			linePointList.add(new PointF(lastColumnPoint.x, lastColumnPoint.y));
		}
		Path path = new Path();
		path.moveTo(linePointList.get(0).x, linePointList.get(0).y);

		PointF[] points1 = new PointF[tempsCount];
		PointF[] points2 = new PointF[tempsCount];

		for (int i = 1; i < tempsCount; i++) {
			points1[i] = new PointF((linePointList.get(i).x + linePointList.get(i - 1).x) / 2, linePointList.get(i - 1).y);
			points2[i] = new PointF((linePointList.get(i).x + linePointList.get(i - 1).x) / 2, linePointList.get(i).y);

			path.cubicTo(points1[i].x, points1[i].y, points2[i].x, points2[i].y, linePointList.get(i).x,
					linePointList.get(i).y);
		}

		canvas.drawPath(path, linePaint);

		for (int i = 0; i < circleXArr.length; i++) {
			canvas.drawCircle(circleXArr[i], circleYArr[i], circleRadius, circlePaint);
		}

	}


	@Override
	public void clear() {
		tempList.clear();
	}
}
