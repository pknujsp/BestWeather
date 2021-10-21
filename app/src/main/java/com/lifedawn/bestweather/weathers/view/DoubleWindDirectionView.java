package com.lifedawn.bestweather.weathers.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.theme.AppTheme;

import java.util.ArrayList;
import java.util.List;

public class DoubleWindDirectionView extends View {
	private final int viewWidth;
	private final int viewHeight;
	private final int columnWidth;
	private final int imgSize;
	private final int margin;
	private final int dividerWidth;

	private Rect leftImgRect = new Rect();
	private Rect rightImgRect = new Rect();
	private Rect dividerRect = new Rect();
	private Paint dividerPaint;

	private List<WindDirectionObj> windDirectionObjList = new ArrayList<>();

	public DoubleWindDirectionView(Context context, int viewWidth, int viewHeight, int columnWidth) {
		super(context);
		this.viewWidth = viewWidth;
		this.viewHeight = viewHeight;
		this.columnWidth = columnWidth;
		this.dividerWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, getResources().getDisplayMetrics());

		int tempMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, getResources().getDisplayMetrics());
		int tempImgSize = columnWidth / 2 - tempMargin * 2;

		if (tempImgSize > viewHeight) {
			tempImgSize = tempImgSize - (tempImgSize - viewHeight);
			if (columnWidth / 2 - tempImgSize > 0) {
				tempMargin = (columnWidth / 2 - tempImgSize) / 2;
			} else {
				tempMargin = 0;
			}
		}
		imgSize = tempImgSize;
		margin = tempMargin;

		dividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		dividerPaint.setColor(AppTheme.getColor(context, R.attr.lineColor));
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(viewWidth, viewHeight);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		leftImgRect.set(margin, getHeight() / 2 - imgSize / 2, margin + imgSize, getHeight() / 2 + imgSize / 2);
		rightImgRect.set(leftImgRect.right + margin * 2, leftImgRect.top, leftImgRect.right + margin * 2 + imgSize, leftImgRect.bottom);
		dividerRect.set(leftImgRect.right + margin - dividerWidth / 2, leftImgRect.top, leftImgRect.right + margin + dividerWidth / 2
				, leftImgRect.bottom);

		for (WindDirectionObj windDirectionObj : windDirectionObjList) {
			leftImgRect.offset(columnWidth, 0);
			rightImgRect.offset(columnWidth, 0);
			dividerRect.offset(columnWidth, 0);

			windDirectionObj.leftImg.setBounds(leftImgRect);
			windDirectionObj.rightImg.setBounds(rightImgRect);
			windDirectionObj.leftImg.draw(canvas);
			windDirectionObj.rightImg.draw(canvas);
			canvas.drawRect(dividerRect, dividerPaint);
		}
	}


	public void setIcons(List<WindDirectionObj> windDirectionObjList) {
		this.windDirectionObjList = windDirectionObjList;
		for (WindDirectionObj windDirectionObj : windDirectionObjList) {
			windDirectionObj.leftImg = ContextCompat.getDrawable(getContext(), R.drawable.temp_icon);
			windDirectionObj.rightImg = ContextCompat.getDrawable(getContext(), R.drawable.temp_icon);
		}
	}


	public static class WindDirectionObj {
		final int leftDirectionDegree;
		final int rightDirectionDegree;
		Drawable leftImg;
		Drawable rightImg;

		public WindDirectionObj(Integer leftCode, Integer rightCode) {
			this.leftDirectionDegree = leftCode;
			this.rightDirectionDegree = rightCode;
		}
	}
}
