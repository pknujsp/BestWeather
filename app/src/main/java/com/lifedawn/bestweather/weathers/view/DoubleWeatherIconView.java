package com.lifedawn.bestweather.weathers.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.TypedValue;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import com.lifedawn.bestweather.weathers.FragmentType;

import java.util.ArrayList;
import java.util.List;

public class DoubleWeatherIconView extends View {
	private final FragmentType fragmentType;

	private final int viewWidth;
	private final int viewHeight;
	private final int columnWidth;
	private final int imgSize;
	private final int singleImgSize;
	private final int margin;
	private final int dividerWidth;

	private Rect leftImgRect = new Rect();
	private Rect rightImgRect = new Rect();
	private Rect singleImgRect = new Rect();
	private Rect dividerRect = new Rect();
	private Paint dividerPaint;

	private List<WeatherIconObj> weatherIconObjList = new ArrayList<>();

	public DoubleWeatherIconView(Context context, FragmentType fragmentType, int viewWidth, int viewHeight, int columnWidth) {
		super(context);
		this.fragmentType = fragmentType;
		this.viewWidth = viewWidth;
		this.viewHeight = viewHeight;
		this.columnWidth = columnWidth;
		this.dividerWidth = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 1f, getResources().getDisplayMetrics());

		int tempMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 3f, getResources().getDisplayMetrics());

		singleImgSize = viewHeight - tempMargin * 2;
		int tempImgSize = columnWidth / 2 - tempMargin * 2;

		if (tempImgSize > viewHeight) {
			tempImgSize = tempImgSize - (tempImgSize - viewHeight);
			if (columnWidth / 2 - tempImgSize > 0) {
				tempMargin = (columnWidth / 2 - tempImgSize) / 2;
			} else {
				tempMargin = 0;
			}
		}
		imgSize = tempImgSize;
		margin = tempMargin;

		dividerPaint = new Paint(Paint.ANTI_ALIAS_FLAG);
		switch (fragmentType) {
			case Simple:
				dividerPaint.setColor(Color.WHITE);
				break;
			default:
				dividerPaint.setColor(Color.BLACK);
				break;
		}

		setClickable(true);
		setFocusable(true);

		setOnTouchListener(new OnTouchListener() {
			long actionDownMillis;

			@Override
			public boolean onTouch(View v, MotionEvent event) {
				if (event.getAction() == MotionEvent.ACTION_DOWN) {
					actionDownMillis = System.currentTimeMillis();
				} else if (event.getAction() == MotionEvent.ACTION_UP) {
					if (System.currentTimeMillis() - actionDownMillis < 500 && weatherIconObjList.size() > 0) {
						int index = (int) (event.getX() / columnWidth);
						boolean isSingle = !weatherIconObjList.get(index).isDouble;
						String text = null;
						if (isSingle) {
							text = weatherIconObjList.get(index).singleDescription;
						} else {
							text = weatherIconObjList.get(index).leftDescription + " / " + weatherIconObjList.get(index).rightDescription;
						}

						Toast.makeText(getContext(), text, Toast.LENGTH_SHORT).show();
					}
				}

				return true;
			}
		});
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
		singleImgRect.set(columnWidth / 2 - singleImgSize / 2, margin, columnWidth / 2 + singleImgSize / 2, getHeight() - margin);
		leftImgRect.set(margin, getHeight() / 2 - imgSize / 2, margin + imgSize, getHeight() / 2 + imgSize / 2);
		rightImgRect.set(leftImgRect.right + margin * 2, leftImgRect.top, leftImgRect.right + margin * 2 + imgSize, leftImgRect.bottom);
		dividerRect.set(leftImgRect.right + margin - dividerWidth / 2, leftImgRect.top, leftImgRect.right + margin + dividerWidth / 2,
				leftImgRect.bottom);

		for (WeatherIconObj weatherIconObj : weatherIconObjList) {
			if (weatherIconObj.isDouble) {
				weatherIconObj.leftImg.setBounds(leftImgRect);
				weatherIconObj.rightImg.setBounds(rightImgRect);
				weatherIconObj.leftImg.draw(canvas);
				weatherIconObj.rightImg.draw(canvas);
				canvas.drawRect(dividerRect, dividerPaint);
			} else {
				weatherIconObj.singleImg.setBounds(singleImgRect);
				weatherIconObj.singleImg.draw(canvas);
			}
			singleImgRect.offset(columnWidth, 0);
			leftImgRect.offset(columnWidth, 0);
			rightImgRect.offset(columnWidth, 0);
			dividerRect.offset(columnWidth, 0);
		}
	}


	public void setIcons(List<WeatherIconObj> weatherIconObjList) {
		this.weatherIconObjList = weatherIconObjList;
	}


	public static class WeatherIconObj {
		final boolean isDouble;
		Drawable leftImg;
		Drawable rightImg;
		Drawable singleImg;
		String leftDescription;
		String rightDescription;
		String singleDescription;


		public WeatherIconObj(Drawable leftDrawable, Drawable rightDrawable, String leftDescription, String rightDescription) {
			this.leftImg = leftDrawable;
			this.rightImg = rightDrawable;
			this.leftDescription = leftDescription;
			this.rightDescription = rightDescription;
			this.isDouble = true;
		}

		public WeatherIconObj(Drawable drawable, String singleDescription) {
			this.singleImg = drawable;
			this.singleDescription = singleDescription;
			this.isDouble = false;
		}

	}
}
