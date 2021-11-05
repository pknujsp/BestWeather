package com.lifedawn.bestweather.weathers.view;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Matrix;
import android.graphics.Paint;
import android.graphics.Point;
import android.graphics.Rect;
import android.graphics.drawable.Drawable;
import android.view.View;

import androidx.appcompat.widget.DrawableUtils;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;

import com.lifedawn.bestweather.R;

import java.util.ArrayList;
import java.util.List;

public class SingleWindDirectionView extends View {
	private final FragmentType fragmentType;

	private final int viewWidth;
	private final int viewHeight;
	private final int columnWidth;
	private final int iconSize;

	private List<WindDirectionObj> windDirectionObjList = new ArrayList<>();
	private Rect imgRect = new Rect();
	private Drawable icon;
	private Paint iconPaint;
	private Bitmap iconBitmap;


	public SingleWindDirectionView(Context context, FragmentType fragmentType, int viewWidth, int viewHeight, int columnWidth) {
		super(context);
		this.fragmentType = fragmentType;
		this.viewWidth = viewWidth;
		this.viewHeight = viewHeight;
		this.columnWidth = columnWidth;

		int tempImgSize = viewHeight;
		iconSize = tempImgSize;
		icon = ContextCompat.getDrawable(context, R.drawable.arrow);
		ColorStateList colorStateList = ColorStateList.valueOf(Color.BLACK);
		icon.setTintList(colorStateList);

		iconPaint = new Paint(Paint.ANTI_ALIAS_FLAG);

		iconBitmap = Bitmap.createBitmap(icon.getIntrinsicWidth(),
				icon.getIntrinsicHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(iconBitmap);
		icon.setBounds(0, 0, iconSize, iconSize);
		icon.draw(canvas);
	}

	@Override
	protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
		setMeasuredDimension(viewWidth, viewHeight);
	}

	@Override
	protected void onLayout(boolean changed, int left, int top, int right, int bottom) {
		super.onLayout(changed, left, top, right, bottom);
	}

	@SuppressLint("DrawAllocation")
	@Override
	protected void onDraw(Canvas canvas) {
		imgRect.set(columnWidth / 2 - iconSize / 2, 0, columnWidth / 2 + iconSize / 2, getHeight());
		Matrix matrix = new Matrix();

		for (WindDirectionObj windDirectionObj : windDirectionObjList) {
		//	matrix.postRotate(-windDirectionObj.directionDegree - 90, newCenter.x, newCenter.y);
			Bitmap bitmap = Bitmap.createBitmap(iconBitmap, 0, 0, iconBitmap.getWidth(), iconBitmap.getHeight(), matrix, false);
			canvas.drawBitmap(bitmap, imgRect.left, imgRect.top, iconPaint);
			imgRect.offset(columnWidth, 0);
		}
	}


	public void setIcons(List<WindDirectionObj> windDirectionObjList) {
		this.windDirectionObjList = windDirectionObjList;
	}


	public static class WindDirectionObj {
		final int directionDegree;

		public WindDirectionObj(int directionDegree) {
			this.directionDegree = directionDegree;
		}
	}
}
