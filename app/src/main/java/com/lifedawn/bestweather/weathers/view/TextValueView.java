package com.lifedawn.bestweather.weathers.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.text.TextPaint;
import android.view.View;

public class TextValueView extends View {
	private TextPaint textPaint;
	private int viewWidth;
	private int viewHeight;
	private String value;

	public TextValueView(Context context, int viewWidth, int viewHeight, String value) {
		super(context);
		this.textPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		textPaint.setTextAlign(Paint.Align.CENTER);
		this.viewWidth = viewWidth;
		this.viewHeight = viewHeight;
		this.value = value;
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
		super.onDraw(canvas);
	}
}
