package com.lifedawn.bestweather.weathers.view;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.lifedawn.bestweather.weathers.FragmentType;

import java.util.ArrayList;
import java.util.List;

public class SingleWeatherIconView extends View implements ICleaner {
	private final FragmentType fragmentType;

	private final int viewWidth;
	private final int viewHeight;
	private final int columnWidth;
	private final int imgSize;

	private List<WeatherIconObj> weatherIconObjList = new ArrayList<>();
	private Rect imgRect = new Rect();

	public SingleWeatherIconView(Context context, FragmentType fragmentType, int viewWidth, int viewHeight, int columnWidth) {
		super(context);
		this.fragmentType = fragmentType;

		this.viewWidth = viewWidth;
		this.viewHeight = viewHeight;
		this.columnWidth = columnWidth;

		int tempImgSize = viewHeight;
		imgSize = tempImgSize;

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
						Toast.makeText(getContext(), weatherIconObjList.get((int) (event.getX() / columnWidth)).description, Toast.LENGTH_SHORT).show();
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
		imgRect.set((columnWidth - imgSize) / 2, 0, (columnWidth - imgSize) / 2 + imgSize, getHeight());

		for (WeatherIconObj weatherIconObj : weatherIconObjList) {
			weatherIconObj.img.setBounds(imgRect);
			weatherIconObj.img.draw(canvas);

			imgRect.offset(columnWidth, 0);
		}
	}


	public void setWeatherImgs(List<WeatherIconObj> weatherIconObjList) {
		this.weatherIconObjList = weatherIconObjList;
	}

	@Override
	public void clear() {
		weatherIconObjList.clear();
	}


	public static class WeatherIconObj {
		Drawable img;
		String description;

		public WeatherIconObj(Drawable drawable) {
			img = drawable;
		}

		public WeatherIconObj(Drawable img, String description) {
			this.img = img;
			this.description = description;
		}

		public Drawable getImg() {
			return img;
		}

		public String getDescription() {
			return description;
		}
	}
}
