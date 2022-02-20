package com.lifedawn.bestweather.widget.foreground;

import android.app.Service;
import android.appwidget.AppWidgetManager;
import android.content.Intent;
import android.os.IBinder;

import com.lifedawn.bestweather.commons.interfaces.BackgroundCallback;
import com.lifedawn.bestweather.room.repository.WidgetRepository;
import com.lifedawn.bestweather.widget.creator.AbstractWidgetCreator;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AbstractWidgetForegroundService extends Service {
	protected WidgetRepository widgetRepository;
	protected AppWidgetManager appWidgetManager;
	protected Map<Integer, AbstractWidgetCreator> widgetCreatorMap = new HashMap<>();
	protected Map<Integer, BackgroundCallback> backgroundCallbackMap = new HashMap<>();
	protected Map<Integer, Class<?>> widgetClassMap = new HashMap<>();
	protected ExecutorService executorService = Executors.newFixedThreadPool(3);

	public AbstractWidgetForegroundService() {
	}

	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public void onCreate() {
		super.onCreate();
		if (widgetRepository == null) {
			widgetRepository = new WidgetRepository(getApplicationContext());
		}
		if (appWidgetManager == null) {
			appWidgetManager = AppWidgetManager.getInstance(getApplicationContext());
		}
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		final String action = intent.getAction();
		return START_NOT_STICKY;
	}


}