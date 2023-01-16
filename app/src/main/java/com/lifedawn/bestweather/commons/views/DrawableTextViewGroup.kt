package com.lifedawn.bestweather.commons.views;

import android.content.Context;
import android.content.res.ColorStateList;
import android.content.res.TypedArray;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.databinding.ViewgroupDrawableWithTextBinding;

public class DrawableTextViewGroup extends ViewGroup {

	/*
	        <attr name="drawableSize" format="dimension" />
        <attr name="src" format="reference" />
        <attr name="drawableTint" format="color" />
        <attr name="text" format="string" />
        <attr name="textColor" format="color" />
        <attr name="textSize" format="dimension" />
	 */
	private ViewgroupDrawableWithTextBinding binding;
	private float drawableSize;
	private int drawableSrc;
	private int drawableTint;
	private String text;
	private int textColor;

	public DrawableTextViewGroup(@NonNull Context context) {
		super(context);
		init(null);
	}

	public DrawableTextViewGroup(@NonNull Context context, @Nullable AttributeSet attrs) {
		super(context, attrs);
		init(attrs);
	}

	public DrawableTextViewGroup(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr) {
		super(context, attrs, defStyleAttr);
		init(attrs);

	}

	public DrawableTextViewGroup(@NonNull Context context, @Nullable AttributeSet attrs, int defStyleAttr, int defStyleRes) {
		super(context, attrs, defStyleAttr, defStyleRes);
		init(attrs);

	}

	@Override
	protected void onLayout(boolean changed, int l, int t, int r, int b) {

	}

	private void init(AttributeSet attrs) {
		TypedArray typedArray = getContext().getTheme().obtainStyledAttributes(attrs, R.styleable.DrawableTextViewGroup, 0, 0);
		try {
			drawableSize = typedArray.getDimension(R.styleable.DrawableTextViewGroup_drawableSize,
					getResources().getDimension(R.dimen.defaultImageSizeInDrawableWithText));
			drawableSrc = typedArray.getResourceId(R.styleable.DrawableTextViewGroup_drawableSrc, R.drawable.temp_icon);
			drawableTint = typedArray.getColor(R.styleable.DrawableTextViewGroup_drawableColor, 0);
			text = typedArray.getString(R.styleable.DrawableTextViewGroup_text);
			textColor = typedArray.getColor(R.styleable.DrawableTextViewGroup_textColor, ContextCompat.getColor(getContext(),
					R.color.defaultTextColorInDrawableWithText));
		} finally {
			typedArray.recycle();
		}

		binding = ViewgroupDrawableWithTextBinding.inflate(LayoutInflater.from(getContext()), this, true);
		LinearLayout.LayoutParams imgLayoutParams = (LinearLayout.LayoutParams) binding.drawable.getLayoutParams();
		imgLayoutParams.width = (int) drawableSize;
		imgLayoutParams.height = (int) drawableSize;

		binding.drawable.setImageResource(drawableSrc);
		if (drawableTint != 0) {
			binding.drawable.setImageTintList(ColorStateList.valueOf(drawableTint));
		}
		binding.text.setText(text);
		binding.text.setTextColor(textColor);
	}

}
