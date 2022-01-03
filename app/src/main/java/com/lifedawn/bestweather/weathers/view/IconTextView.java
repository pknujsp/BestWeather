package com.lifedawn.bestweather.weathers.view;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.text.Layout;
import android.text.StaticLayout;
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

	private int viewHeight;

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
		valueTextPaint.setTextAlign(Paint.Align.CENTER);
		valueTextPaint.setTextSize(context.getResources().getDimension(R.dimen.iconValueTextSizeInSCD));
		valueTextPaint.setColor(AppTheme.getTextColor(context, fragmentType));
		valueTextPaint.setTypeface(Typeface.create("sans-serif-condensed", Typeface.NORMAL));

		icon = ContextCompat.getDrawable(context, iconId);
		icon.setTintList(ColorStateList.valueOf(ContextCompat.getColor(context, R.color.blue)));

		spacingBetweenIconAndValue = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, getResources().getDisplayMetrics());
		padding = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 2f, getResources().getDisplayMetrics());
	}

	public IconTextView setValueList(List<String> valueList) {
		this.valueList = valueList;
		return this;
	}

	public void setValueTextSize(int textSizeSp) {
		valueTextPaint.setTextSize(TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_SP, textSizeSp, getResources().getDisplayMetrics()));
	}

	public void setTextColor(int textColor) {
		valueTextPaint.setColor(textColor);
	}

	public List<String> getValueList() {
		return valueList;
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		iconSize = (int) valueTextPaint.getTextSize();

		StaticLayout staticLayout = null;
		viewHeight = Integer.MIN_VALUE;
		final int availableTextWidth = columnWidth - iconSize - spacingBetweenIconAndValue;

		for (String val : valueList) {
			StaticLayout.Builder builder = StaticLayout.Builder.obtain(val, 0, val.length(), valueTextPaint, availableTextWidth);
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
		iconRect.top = getHeight() / 2 - iconSize / 2;
		iconRect.bottom = iconRect.top + iconSize;
		StaticLayout staticLayout = null;
		final int availableTextWidth = columnWidth - iconSize - spacingBetweenIconAndValue;
		int moveDistance;
		Rect textRect = new Rect();
		final String tab = "\n";
		String separatedStr;

		for (String value : valueList) {
			separatedStr = value.split(tab)[0];
			valueTextPaint.getTextBounds(separatedStr, 0, separatedStr.length(), textRect);
			StaticLayout.Builder builder = StaticLayout.Builder.obtain(value, 0, value.length(), valueTextPaint, availableTextWidth);
			staticLayout = builder.build();

			moveDistance = (columnWidth - textRect.width()) / 4;

			float x = (column * columnWidth) + (columnWidth / 2f) + moveDistance;
			float y = viewHeight / 2f - (staticLayout.getHeight() / 2f);

			iconRect.right = (int) (x - spacingBetweenIconAndValue - textRect.width() / 2);
			iconRect.left = iconRect.right - iconSize;

			icon.setBounds(iconRect);
			icon.draw(canvas);

			canvas.save();
			canvas.translate(x, y);
			staticLayout.draw(canvas);
			canvas.restore();

			column++;
		}
	}
}
