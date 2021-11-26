package com.lifedawn.bestweather.notification.always;

import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.IBinder;
import android.widget.RemoteViews;

import androidx.annotation.Nullable;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.RequestWeatherDataType;
import com.lifedawn.bestweather.commons.enums.WeatherSourceType;
import com.lifedawn.bestweather.notification.NotificationKey;
import com.lifedawn.bestweather.widget.WidgetCreator;

public class AlwaysNotiService extends Service {
	
	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}
	
	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		final String action = intent.getAction();
		
		NotiViewCreator notiViewCreator = new NotiViewCreator(getApplicationContext());
		
		
		
		return START_STICKY;
	}
	
	@Override
	public void onCreate() {
		super.onCreate();
	}
	
	@Override
	public void onDestroy() {
		super.onDestroy();
	}
	
	private void setDataViews(WeatherSourceType weatherSourceType, RemoteViews remoteViews) {
		if (weatherSourceType == WeatherSourceType.KMA) {
		} else if (weatherSourceType == WeatherSourceType.ACCU_WEATHER) {
		} else if (weatherSourceType == WeatherSourceType.OPEN_WEATHER_MAP) {
		}
	}
}
