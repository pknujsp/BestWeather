package com.lifedawn.bestweather.commons.classes;

import android.os.Handler;
import android.os.Looper;

import androidx.core.os.HandlerCompat;

public class MainThreadWorker {
	private final static Handler handler = new Handler(Looper.getMainLooper());

	public static void runOnUiThread(Runnable action) {
		handler.post(action);
	}

}
