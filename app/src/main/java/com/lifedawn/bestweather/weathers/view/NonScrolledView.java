package com.lifedawn.bestweather.weathers.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.theme.AppTheme;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

public class NonScrolledView extends View {
	private final FragmentType fragmentType;
	private final TextPaint textPaint;
	private final int viewWidth;
	private final int columnWidth;
	private final int textHeight;
	private final int viewHeight;
	private final String value;
	private int currentX;
	private int firstColX;

	public NonScrolledView(Context context, FragmentType fragmentType, int viewWidth, int columnWidth, String value) {
		super(context);
		this.fragmentType = fragmentType;
		this.viewWidth = viewWidth;
		this.columnWidth = columnWidth;
		this.value = value;

		textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setTextAlign(Paint.Align.LEFT);
		textPaint.setTextSize(context.getResources().getDimension(R.dimen.valueTextSizeInSCD));
		textPaint.setColor(AppTheme.getTextColor(context, fragmentType));

		Rect rect = new Rect();
		textPaint.getTextBounds(value, 0, value.length(), rect);
		textHeight = rect.height();

		int padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, context.getResources().getDisplayMetrics());
		viewHeight = textHeight + padding * 2;
		this.firstColX = 0;
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
		canvas.drawText(value, currentX + firstColX, getHeight() / 2f + textHeight / 2f, textPaint);
	}


	public void reDraw(int newX) {
		this.currentX = newX;
		invalidate();
	}

}