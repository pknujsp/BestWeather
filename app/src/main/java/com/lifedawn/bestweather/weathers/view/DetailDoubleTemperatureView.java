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

import java.util.ArrayList;
import java.util.LinkedList;
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
	private final Paint circlePaint;
	
	private List<Integer> maxTempList;
	private List<Integer> minTempList;
	
	public DetailDoubleTemperatureView(Context context, int viewWidth, int viewHeight, int columnWidth, List<Integer> minTempList,
			List<Integer> maxTempList) {
		super(context);
		this.viewWidth = viewWidth;
		this.viewHeight = viewHeight;
		this.columnWidth = columnWidth;
		
		tempPaint = new TextPaint();
		tempPaint.setTextAlign(Paint.Align.CENTER);
		tempPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 12f, getResources().getDisplayMetrics()));
		tempPaint.setColor(Color.BLACK);
		
		linePaint = new Paint();
		linePaint.setAntiAlias(true);
		linePaint.setStyle(Paint.Style.FILL);
		linePaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1.3f, getResources().getDisplayMetrics()));
		linePaint.setColor(Color.GRAY);
		
		minMaxTempLinePaint = new Paint();
		minMaxTempLinePaint.setAntiAlias(true);
		minMaxTempLinePaint.setStyle(Paint.Style.FILL);
		minMaxTempLinePaint.setStrokeWidth(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, getResources().getDisplayMetrics()));
		
		circlePaint = new Paint();
		circlePaint.setAntiAlias(true);
		circlePaint.setStyle(Paint.Style.FILL);
		circlePaint.setColor(Color.DKGRAY);
		
		this.maxTempList = maxTempList;
		this.minTempList = minTempList;
		
		int max = Integer.MIN_VALUE;
		int min = Integer.MAX_VALUE;
		int maxTemp = 0;
		int minTemp = 0;
		
		for (int i = 0; i < minTempList.size(); i++) {
			maxTemp = maxTempList.get(i);
			minTemp = minTempList.get(i);
			
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
		final float RADIUS = (int) getResources().getDimension(R.dimen.circle_radius_in_detail_temperature_view);
		
		final float VIEW_WIDTH = getWidth();
		final float VIEW_HEIGHT = getHeight() - ((TEXT_HEIGHT + RADIUS) * 2);
		final float SPACING = ((VIEW_HEIGHT) / (maxTemp - minTemp)) / 10f;
		
		int min = 0;
		int max = 0;
		float x = 0f;
		float minY = 0f;
		float maxY = 0f;
		
		PointF lastMinColumnPoint = new PointF();
		PointF lastMaxColumnPoint = new PointF();
		
		for (int index = 0; index < maxTempList.size(); index++) {
			min = minTempList.get(index);
			max = maxTempList.get(index);
			
			x = columnWidth / 2f + columnWidth * index;
			minY = (10f * (maxTemp - min)) * SPACING + TEXT_HEIGHT + RADIUS;
			maxY = (10f * (maxTemp - max)) * SPACING + TEXT_HEIGHT + RADIUS;
			
			canvas.drawCircle(x, minY, RADIUS, circlePaint);
			canvas.drawText(minTempList.get(index).toString(), x, minY + RADIUS + TEXT_HEIGHT, tempPaint);
			
			canvas.drawCircle(x, maxY, RADIUS, circlePaint);
			canvas.drawText(maxTempList.get(index).toString(), x, maxY - RADIUS - TEXT_HEIGHT + TEXT_ASCENT, tempPaint);
			
			if (index != 0) {
				canvas.drawLine(lastMinColumnPoint.x, lastMinColumnPoint.y, x, minY, linePaint);
				canvas.drawLine(lastMaxColumnPoint.x, lastMaxColumnPoint.y, x, maxY, linePaint);
			}
			
			lastMinColumnPoint.set(x, minY);
			lastMaxColumnPoint.set(x, maxY);
		}
		
	}
	
	
}
