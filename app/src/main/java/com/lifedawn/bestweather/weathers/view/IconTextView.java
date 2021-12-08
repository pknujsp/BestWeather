package com.lifedawn.bestweather.weathers.view;

import android.content.Context;
import android.content.res.ColorStateList;
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
import com.lifedawn.bestweather.weathers.FragmentType;

import java.util.List;

public class IconTextView extends View {
	private final FragmentType fragmentType;

	private final int viewWidth;
	private final int columnWidth;
	private final TextPaint valueTextPaint;
	private int iconSize;
	private final Rect iconRect = new Rect();
	private final int spacingBetweenIconAndValue;

	private Drawable icon;
	private List<String> valueList;
	private Rect textRect = new Rect();
	private int padding;


	public IconTextView(Context context, FragmentType fragmentType, int viewWidth, int columnWidth,
	                    int iconId) {
		super(context);
		this.fragmentType = fragmentType;
		this.viewWidth = viewWidth;
		this.columnWidth = columnWidth;

		valueTextPaint = new TextPaint(Paint.ANTI_ALIAS_FLAG);
		valueTextPaint.setTextAlign(Paint.Align.LEFT);
		valueTextPaint.setTextSize(context.getResources().getDimension(R.dimen.iconValueTextSizeInSCD));
		valueTextPaint.setColor(AppTheme.getTextColor(context, fragmentType));
		String tempStr = "테스트";
		valueTextPaint.getTextBounds(tempStr, 0, tempStr.length(), textRect);

		icon = ContextCompat.getDrawable(context, iconId);
		icon.setTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.blue)));

		spacingBetweenIconAndValue = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, getResources().getDisplayMetrics());
		padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, getResources().getDisplayMetrics());

		setWillNotDraw(false);
	}

	public IconTextView setValueList(List<String> valueList) {
		this.valueList = valueList;
		return this;
	}

	public void setValueTextSize(int textSizeSp) {
		valueTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSizeSp, getResources().getDisplayMetrics()));
		String tempStr = "테스트";
		valueTextPaint.getTextBounds(tempStr, 0, tempStr.length(), textRect);
	}

	public void setTextColor(int textColor) {
		valueTextPaint.setColor(textColor);
	}

	public List<String> getValueList() {
		return valueList;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		iconSize = textRect.height() + padding * 2;
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
		iconRect.top = padding;
		iconRect.bottom = getHeight() - padding;

		for (String value : valueList) {
			valueTextPaint.getTextBounds(value, 0, value.length(), textRect);
			itemWidth = iconSize + spacingBetweenIconAndValue + textRect.width();
			iconRect.left = (column * columnWidth) + (columnWidth - itemWidth) / 2;
			iconRect.right = iconRect.left + iconSize;

			icon.setBounds(iconRect);
			icon.draw(canvas);

			canvas.drawText(value, iconRect.right + spacingBetweenIconAndValue, iconRect.centerY() + textRect.height() / 2f, valueTextPaint);
			column++;
		}
	}
}
