package com.lifedawn.bestweather.weathers.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.TextPaint;
import android.view.View;

import androidx.core.content.ContextCompat;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.theme.AppTheme;

import java.util.List;

public class TextValueView extends View {
	private final int viewWidth;
	private final int viewHeight;
	private final int columnWidth;
	private final int valueTextHeight;
	private final TextPaint valueTextPaint;
	private List<String> valueList;
	
	
	public TextValueView(Context context, int viewWidth, int viewHeight, int columnWidth) {
		super(context);
		this.viewWidth = viewWidth;
		this.viewHeight = viewHeight;
		this.columnWidth = columnWidth;
		valueTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		valueTextPaint.setTextAlign(Paint.Align.CENTER);
		valueTextPaint.setTextSize(context.getResources().getDimension(R.dimen.valueTextSizeInSCD));
		valueTextPaint.setColor(AppTheme.getColor(context, R.attr.textColorInWeatherCard));
		
		Rect rect = new Rect();
		valueTextPaint.getTextBounds("0", 0, 1, rect);
		valueTextHeight = rect.height();
		
		setWillNotDraw(false);
	}
	
	public TextValueView setValueList(List<String> valueList) {
		this.valueList = valueList;
		return this;
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
		
		float x = 0f;
		final float columnCenterX = columnWidth / 2f;
		final float y = getHeight() / 2f + valueTextHeight / 2f;
		
		int column = 0;
		for (String value : valueList) {
			x = columnCenterX + (columnWidth * column++);
			canvas.drawText(value == null ? "-" : value, x, y, valueTextPaint);
		}
	}
}
