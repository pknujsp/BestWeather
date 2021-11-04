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

import androidx.core.content.ContextCompat;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.theme.AppTheme;
import com.lifedawn.bestweather.weathers.dataprocessing.response.AqicnResponseProcessor;

import java.util.ArrayList;
import java.util.List;

public class AirQualityBarView extends View {
	private final FragmentType fragmentType;
	private final int viewWidth;
	private final int viewHeight;
	private final int columnWidth;
	private final int barWidth;
	private final int barTopBottomMargin;
	private final int barMinHeight;

	private Paint barPaint;
	private TextPaint gradeValueIntPaint;
	private TextPaint gradeStrPaint;

	private Rect barRect = new Rect();
	private Rect gradeValueIntRect = new Rect();
	private Point gradeValueIntPoint = new Point();
	private Rect gradeStrRect = new Rect();
	private Point gradeStrPoint = new Point();

	private final int minGradeValue;
	private final int maxGradeValue;

	private List<AirQualityObj> airQualityObjList;

	public AirQualityBarView(Context context, FragmentType fragmentType, int viewWidth, int viewHeight, int columnWidth, List<AirQualityObj> airQualityObjList) {
		super(context);
		this.fragmentType = fragmentType;
		this.viewWidth = viewWidth;
		this.viewHeight = viewHeight;
		this.columnWidth = columnWidth;
		this.airQualityObjList = airQualityObjList;
		this.barWidth = (int) getResources().getDimension(R.dimen.barWidthInAirQualityBarView);
		this.barTopBottomMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, getResources().getDisplayMetrics());
		this.barMinHeight = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 16f, getResources().getDisplayMetrics());

		String gradeValueIntStr = "10";
		String gradeStr = context.getString(R.string.good);
		barPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		barPaint.setColor(Color.WHITE);

		gradeValueIntPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		gradeValueIntPaint.setTextAlign(Paint.Align.CENTER);
		gradeValueIntPaint.setColor(AppTheme.getColor(context, R.attr.textColor));
		gradeValueIntPaint.setTextSize(getResources().getDimension(R.dimen.gradeValueTextSizeInAirQualityBarView));
		gradeValueIntPaint.getTextBounds(gradeValueIntStr, 0, gradeValueIntStr.length(), gradeValueIntRect);

		gradeStrPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		gradeStrPaint.setTextAlign(Paint.Align.CENTER);
		gradeStrPaint.setColor(AppTheme.getColor(context, R.attr.textColor));
		gradeStrPaint.setTextSize(getResources().getDimension(R.dimen.gradeTextSizeInAirQualityBarView));
		gradeStrPaint.getTextBounds(gradeStr, 0, gradeStr.length(), gradeStrRect);

		int min = Integer.MAX_VALUE;
		int max = Integer.MIN_VALUE;

		for (AirQualityObj airQualityObj : airQualityObjList) {
			if (airQualityObj.val == null) {
				airQualityObj.grade = "?";
				airQualityObj.gradeColor = ContextCompat.getColor(context, R.color.not_data_color);
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

		if (max - min == 0) {
			max++;
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
		final int topFreeSpaceHeight = barTopBottomMargin * 2 + gradeValueIntRect.height();
		final int bottomFreeSpaceHeight = gradeStrRect.height() + barTopBottomMargin * 2;
		barRect.set(columnWidth / 2 - barWidth / 2, topFreeSpaceHeight, columnWidth / 2 + barWidth / 2,
				getHeight() - bottomFreeSpaceHeight);

		gradeStrPoint.y = barRect.bottom + barTopBottomMargin - (int) gradeStrPaint.ascent();

		final int barMaxHeight = getHeight() - bottomFreeSpaceHeight - topFreeSpaceHeight;
		final int barAvailableHeight = barMaxHeight - barMinHeight;
		final int heightPer1 = barAvailableHeight / (maxGradeValue - minGradeValue);

		int i = 0;
		final int barCenterXInColumn = barRect.centerX();

		for (AirQualityObj airQualityObj : airQualityObjList) {
			if (airQualityObj.val == null) {
				barRect.top = barRect.bottom - barMinHeight;
			} else {
				barRect.top = barRect.bottom - barMinHeight - (barAvailableHeight - heightPer1 * (maxGradeValue - airQualityObj.val));
			}
			barRect.left = (columnWidth * i) + barCenterXInColumn - barWidth / 2;
			barRect.right = (columnWidth * i) + barCenterXInColumn + barWidth / 2;

			gradeStrPoint.x = barRect.centerX();
			gradeValueIntPoint.x = barRect.centerX();
			gradeValueIntPoint.y = barRect.top - barTopBottomMargin - (int) gradeValueIntPaint.descent();

			barPaint.setColor(airQualityObj.gradeColor);

			canvas.drawRect(barRect, barPaint);
			canvas.drawText(airQualityObj.val == null ? "?" : airQualityObj.val.toString(), gradeValueIntPoint.x, gradeValueIntPoint.y,
					gradeValueIntPaint);
			canvas.drawText(airQualityObj.grade, gradeStrPoint.x, gradeStrPoint.y, gradeStrPaint);
			i++;
		}
	}

	public static class AirQualityObj {
		final Integer val;
		int gradeColor;
		String grade;

		public AirQualityObj(Integer val) {
			this.val = val;
		}
	}
}
