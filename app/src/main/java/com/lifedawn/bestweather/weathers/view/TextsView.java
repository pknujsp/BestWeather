package com.lifedawn.bestweather.weathers.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.View;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.theme.AppTheme;
import com.lifedawn.bestweather.weathers.FragmentType;

import java.util.List;

public class TextsView extends View implements ICleaner {
	protected final int columnWidth;
	protected final TextPaint valueTextPaint;
	protected final Rect valueTextRect = new Rect();
	protected final int viewWidth;

	protected int viewHeight;
	protected int padding;
	protected List<String> valueList;

	public TextsView(Context context, int viewWidth, int columnWidth, List<String> valueList) {
		super(context);
		this.viewWidth = viewWidth;
		this.columnWidth = columnWidth;
		this.valueList = valueList;

		valueTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		valueTextPaint.setTextAlign(Paint.Align.CENTER);
		valueTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, 14f, getResources().getDisplayMetrics()));
		valueTextPaint.setColor(Color.WHITE);
	}


	public void setValueTextSize(int textSizeSp) {
		valueTextPaint.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSizeSp, getResources().getDisplayMetrics()));
	}


	public void setValueTextColor(int textColor) {
		valueTextPaint.setColor(textColor);
	}

	public void setValueList(List<String> valueList) {
		this.valueList = valueList;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		StaticLayout staticLayout = null;
		viewHeight = Integer.MIN_VALUE;

		for (String val : valueList) {
			StaticLayout.Builder builder = StaticLayout.Builder.obtain(val, 0, val.length(), valueTextPaint, columnWidth);
			staticLayout = builder.build();
			viewHeight = Math.max(staticLayout.getHeight(), viewHeight);
		}

		viewHeight += padding * 2;
		setMeasuredDimension(viewWidth, viewHeight);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		int column = 0;
		for (String value : valueList) {
			drawText(canvas, value, column);
			column++;
		}
	}

	private void drawText(Canvas canvas, String textOnCanvas, int column) {
		StaticLayout.Builder builder = StaticLayout.Builder.obtain(textOnCanvas, 0, textOnCanvas.length(), valueTextPaint, columnWidth);
		StaticLayout staticLayout = builder.build();

		float x = columnWidth / 2f + columnWidth * column + valueTextRect.left;
		float y = viewHeight / 2f - (staticLayout.getHeight() / 2f);

		canvas.save();
		canvas.translate(x, y);
		staticLayout.draw(canvas);
		canvas.restore();
	}

	@Override
	public void clear() {
		if (valueList != null)
			valueList.clear();
	}
}
