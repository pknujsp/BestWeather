package com.lifedawn.bestweather.commons.views;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.Nullable;

public class ViewUtil {
	private ViewUtil() {
	}

	public static Bitmap toBitmap(Context context, View view, @Nullable Integer width, @Nullable Integer height) {
		DisplayMetrics displayMetrics = context.getResources().getDisplayMetrics();
		view.setLayoutParams(new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT));
		view.measure(width == null ? displayMetrics.widthPixels : width, height == null ? displayMetrics.heightPixels : height);
		view.layout(0, 0, width == null ? displayMetrics.widthPixels : width, height == null ? displayMetrics.heightPixels : height);
		view.buildDrawingCache();

		Bitmap bitmap = Bitmap.createBitmap(view.getMeasuredWidth(), view.getMeasuredHeight(), Bitmap.Config.ARGB_8888);
		Canvas canvas = new Canvas(bitmap);
		view.draw(canvas);

		return bitmap;
	}
}
