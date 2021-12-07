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

import java.util.List;

public class TextValueView extends View {
	private final FragmentType fragmentType;

	private final int viewWidth;
	private int viewHeight;
	private final int columnWidth;
	private final TextPaint valueTextPaint;
	private final Rect valueTextRect;
	private List<String> valueList;
	private int textSize;
	private int padding;


	public TextValueView(Context context, FragmentType fragmentType, int viewWidth, int viewHeight, int columnWidth) {
		super(context);
		this.fragmentType = fragmentType;
		this.viewWidth = viewWidth;
		this.columnWidth = columnWidth;
		this.viewHeight = viewHeight;

		valueTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		valueTextPaint.setTextAlign(Paint.Align.CENTER);
		valueTextPaint.setTextSize(context.getResources().getDimension(R.dimen.valueTextSizeInSCD));
		valueTextPaint.setColor(AppTheme.getTextColor(context, fragmentType));

		valueTextRect = new Rect();

		setWillNotDraw(false);
	}

	public TextValueView setValueList(List<String> valueList) {
		this.valueList = valueList;
		return this;
	}

	public void setTextSize(int textSizeSp) {
		this.textSize = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSizeSp, getResources().getDisplayMetrics());
		valueTextPaint.setTextSize(textSize);
		valueTextPaint.getTextBounds("A", 0, 1, valueTextRect);

		padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 6f, getResources().getDisplayMetrics());
		viewHeight = valueTextRect.height() + padding * 2;
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

		int column = 0;
		for (String value : valueList) {
			drawText(canvas, value, column);
			column++;
		}

	}

	private void drawText(Canvas canvas, String textOnCanvas, int column) {
		valueTextPaint.getTextBounds(textOnCanvas, 0, textOnCanvas.length(), valueTextRect);
		StaticLayout.Builder builder = StaticLayout.Builder.obtain(textOnCanvas, 0, textOnCanvas.length(), valueTextPaint, columnWidth);
		StaticLayout sl = builder.build();

		canvas.save();

		float textHeight = valueTextRect.height();
		float textYCoordinate = viewHeight / 2f + valueTextRect.exactCenterY() -
				((sl.getLineCount() * textHeight) / 2);

		final float columnCenterX = columnWidth / 2f;
		float textXCoordinate = columnCenterX
				+ columnWidth * column + valueTextRect.left;

		canvas.translate(textXCoordinate, textYCoordinate);

		sl.draw(canvas);
		canvas.restore();
	}


}
