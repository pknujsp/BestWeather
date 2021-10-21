package com.lifedawn.bestweather.weathers.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.View;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.theme.AppTheme;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;

import java.util.ArrayList;
import java.util.List;

public class AirQualityBarView extends View {
	private final int viewWidth;
	private final int viewHeight;
	private final int columnWidth;
	private final int barWidth;
	private final int barTopBottomMargin;
	private final int barMinHeight;

	private Paint barPaint;
	private TextPaint gradeValuePaint;
	private TextPaint gradePaint;

	private Rect barRect = new Rect();
	private Rect gradeValueRect = new Rect();
	private Point gradeValuePoint = new Point();
	private Rect gradeRect = new Rect();
	private Point gradePoint = new Point();

	private final int minGradeValue;
	private final int maxGradeValue;

	private List<AirQualityObj> airQualityObjList = new ArrayList<>();

	public AirQualityBarView(Context context, int viewWidth, int viewHeight, int columnWidth, List<AirQualityObj> airQualityObjList) {
		super(context);
		this.viewWidth = viewWidth;
		this.viewHeight = viewHeight;
		this.columnWidth = columnWidth;
		this.airQualityObjList = airQualityObjList;
		this.barWidth = (int) getResources().getDimension(R.dimen.barWidthInAirQualityBarView);
		this.barTopBottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, getResources().getDisplayMetrics());
		this.barMinHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, getResources().getDisplayMetrics());

		String tempStr = "1";

		barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		barPaint.setColor(Color.WHITE);

		gradeValuePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		gradeValuePaint.setTextAlign(Paint.Align.CENTER);
		gradeValuePaint.setColor(AppTheme.getColor(context, R.attr.textColor));
		gradeValuePaint.setTextSize(getResources().getDimension(R.dimen.gradeValueTextSizeInAirQualityBarView));
		gradeValuePaint.getTextBounds(tempStr, 0, tempStr.length(), gradeValueRect);

		gradePaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		gradePaint.setTextAlign(Paint.Align.CENTER);
		gradePaint.setColor(AppTheme.getColor(context, R.attr.textColor));
		gradePaint.setTextSize(getResources().getDimension(R.dimen.gradeTextSizeInAirQualityBarView));
		gradePaint.getTextBounds(tempStr, 0, tempStr.length(), gradeRect);

		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;

		for (AirQualityObj airQualityObj : airQualityObjList) {
			if (airQualityObj.val == null) {
				airQualityObj.grade = "?";
				airQualityObj.gradeColor = AqicnResponseProcessor.getGradeColorId(0);
				continue;
			}

			if (airQualityObj.val < min) {
				min = airQualityObj.val;
			}
			if (airQualityObj.val > max) {
				max = airQualityObj.val;
			}

			//color, gradeDescription
			airQualityObj.grade = AqicnResponseProcessor.getGradeDescription(airQualityObj.val);
			airQualityObj.gradeColor = AqicnResponseProcessor.getGradeColorId(airQualityObj.val);
		}

		this.minGradeValue = min;
		this.maxGradeValue = max;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(viewWidth, viewHeight);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		final int topFreeSpaceHeight = gradeValueRect.height() + barTopBottomMargin;
		final int bottomFreeSpaceHeight = getHeight() - gradeValueRect.height() - topFreeSpaceHeight;
		barRect.set(columnWidth / 2 - barWidth / 2, topFreeSpaceHeight, columnWidth / 2 + barWidth / 2, bottomFreeSpaceHeight);

		final int barMaxHeight = bottomFreeSpaceHeight - topFreeSpaceHeight;
		final int barAvailableHeight = barMaxHeight - barMinHeight;
		final int heightPer1 = barAvailableHeight / (maxGradeValue - minGradeValue);

		int i = 0;
		int newTop = 0;

		for (AirQualityObj airQualityObj : airQualityObjList) {
			if (airQualityObj.val == null) {
				newTop = barRect.bottom - barMinHeight;
			} else {
				newTop = barRect.bottom - barMinHeight - (barAvailableHeight - heightPer1 * (maxGradeValue - airQualityObj.val));
			}

			barRect.set(barRect.left + (columnWidth * i), newTop, barRect.right + (columnWidth * i), barRect.bottom);
			gradeValuePoint.set(barRect.centerX(), barRect.bottom + barTopBottomMargin + gradeValueRect.height() / 2);
			gradePoint.set(barRect.centerX(), barRect.top - barTopBottomMargin - gradeValueRect.height() / 2);

			gradePaint.setColor(airQualityObj.gradeColor);

			canvas.drawRect(barRect, barPaint);
			canvas.drawText(airQualityObj.val == null ? "?" : airQualityObj.val.toString(), gradeValuePoint.x, gradeValuePoint.y,
					gradeValuePaint);
			canvas.drawText(airQualityObj.grade, gradePoint.x, gradePoint.y, gradePaint);
			i++;
		}
	}

	public static class AirQualityObj {
		final Integer val;
		int gradeColor;
		String grade;

		public AirQualityObj(String valStr) {
			if (valStr != null) {
				val = (int) Double.parseDouble(valStr);
			} else {
				val = null;
			}
		}
	}
}
