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

import java.util.List;

public class IconTextView extends View {
	private final FragmentType fragmentType;

	private final int viewWidth;
	private final int columnWidth;
	private final int valueTextHeight;
	private final TextPaint valueTextPaint;
	private final int iconSize;
	private final Rect iconRect;
	private final int margin;

	private Drawable icon;
	private List<String> valueList;
	private Rect textRect = new Rect();


	public IconTextView(Context context, FragmentType fragmentType, int viewWidth, int columnWidth,
	                    int iconId) {
		super(context);
		this.fragmentType = fragmentType;
		this.viewWidth = viewWidth;
		this.columnWidth = columnWidth;

		valueTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		valueTextPaint.setTextAlign(Paint.Align.CENTER);
		valueTextPaint.setTextSize(context.getResources().getDimension(R.dimen.iconValueTextSizeInSCD));
		valueTextPaint.setColor(AppTheme.getTextColor(context, fragmentType));

		Rect rect = new Rect();
		valueTextPaint.getTextBounds("0", 0, 1, rect);
		valueTextHeight = rect.height();

		iconSize = valueTextHeight;
		iconRect = new Rect();

		icon = ContextCompat.getDrawable(context, iconId);

		margin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, getResources().getDisplayMetrics());
		setWillNotDraw(false);
	}

	public IconTextView setValueList(List<String> valueList) {
		this.valueList = valueList;
		return this;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(viewWidth, iconSize);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
	}

	@Override
	protected void onDraw(Canvas canvas) {
		super.onDraw(canvas);

		int column = 0;
		int itemWidth = 0;
		iconRect.top = getHeight() / 2 - iconSize / 2;
		iconRect.bottom = getHeight() / 2 + iconSize / 2;

		for (String value : valueList) {
			valueTextPaint.getTextBounds(value, 0, value.length(), textRect);
			itemWidth = iconSize + margin + textRect.width();
			iconRect.left = (column * columnWidth) + (columnWidth - itemWidth) / 2;
			iconRect.right = iconRect.left + iconSize;

			icon.setBounds(iconRect);
			icon.draw(canvas);

			canvas.drawText(value, iconRect.right + margin, iconRect.centerY() + textRect.height() / 2f, valueTextPaint);
			column++;
		}
	}
}
