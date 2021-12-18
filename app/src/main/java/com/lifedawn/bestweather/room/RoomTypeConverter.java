package com.lifedawn.bestweather.room;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

import androidx.room.TypeConverter;

import java.io.ByteArrayOutputStream;

public class RoomTypeConverter {

	@TypeConverter
	public byte[] toByteArr(Bitmap bitmap) {
		if (bitmap == null) {
			return new byte[0];
		}
		ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream();
		bitmap.compress(Bitmap.CompressFormat.PNG, 100, byteArrayOutputStream);
		return byteArrayOutputStream.toByteArray();
	}

	@TypeConverter
	public Bitmap toBitmap(byte[] byteArr) {
		if (byteArr == null) {
			return null;
		}
		return BitmapFactory.decodeByteArray(byteArr, 0, byteArr.length);
	}
}
