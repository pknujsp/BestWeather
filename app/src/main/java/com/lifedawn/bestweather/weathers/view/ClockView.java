package com.lifedawn.bestweather.weathers.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.text.StaticLayout;
import android.text.TextPaint;
import android.util.TypedValue;
import android.view.View;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.theme.AppTheme;
import com.lifedawn.bestweather.weathers.FragmentType;

import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class ClockView extends View {
	private final FragmentType fragmentType;

	private final int viewWidth;
	private int viewHeight;
	private final int columnWidth;
	private final TextPaint clockPaint;
	private final DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("H");

	private Rect rect = new Rect();
	private List<ZonedDateTime> clockList;
	private int padding;

	public ClockView(Context context, FragmentType fragmentType, int viewWidth, int columnWidth) {
		super(context);
		this.fragmentType = fragmentType;
		this.viewWidth = viewWidth;
		this.columnWidth = columnWidth;

		clockPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		clockPaint.setTextAlign(Paint.Align.CENTER);
		clockPaint.setTextSize(context.getResources().getDimension(R.dimen.clockValueTextSizeInSCD));
		clockPaint.setColor(AppTheme.getTextColor(context, fragmentType));
		padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6f, getResources().getDisplayMetrics());

		setWillNotDraw(false);
	}

	public ClockView setClockList(List<ZonedDateTime> clockList) {
		this.clockList = clockList;
		return this;
	}


	public void setTextSize(int textSizeSp) {
		clockPaint.setTextSize((int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSizeSp, getResources().getDisplayMetrics()));
	}

	public void setTextColor(int textColor) {
		clockPaint.setColor(textColor);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		String val = "24";
		clockPaint.getTextBounds(val, 0, val.length(), rect);
		StaticLayout.Builder builder = StaticLayout.Builder.obtain(val, 0, val.length(), clockPaint, columnWidth);
		StaticLayout sl = builder.build();
		viewHeight = rect.height() * sl.getLineCount() + padding * 2;

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
		final float y = getHeight() / 2f + rect.height() / 2f;

		int column = 0;
		for (ZonedDateTime clock : clockList) {
			x = columnCenterX + (columnWidth * column++);
			canvas.drawText(clock.format(dateTimeFormatter), x, y, clockPaint);
		}
	}
}
