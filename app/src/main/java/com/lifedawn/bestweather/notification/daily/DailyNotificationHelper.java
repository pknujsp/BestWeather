package com.lifedawn.bestweather.notification.daily;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.commons.interfaces.BackgroundWorkCallback;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.DailyPushNotificationDto;
import com.lifedawn.bestweather.room.repository.DailyPushNotificationRepository;

import java.time.LocalTime;
import java.util.Calendar;
import java.util.List;

public class DailyNotificationHelper {
	private Context context;
	private AlarmManager alarmManager;

	public DailyNotificationHelper(Context context) {
		this.context = context;
		alarmManager = (AlarmManager) context.getSystemService(Context.ALARM_SERVICE);
	}

	public PendingIntent getRefreshPendingIntent(DailyPushNotificationDto dailyPushNotificationDto, int flags) {
		Intent refreshIntent = new Intent(context, DailyPushNotificationReceiver.class);
		refreshIntent.setAction(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH));
		Bundle bundle = new Bundle();
		bundle.putInt(BundleKey.dtoId.name(), dailyPushNotificationDto.getId());
		bundle.putString("time", dailyPushNotificationDto.getAlarmClock());
		bundle.putString("DailyPushNotificationType", dailyPushNotificationDto.getNotificationType().name());

		refreshIntent.putExtras(bundle);
		return PendingIntent.getBroadcast(context, dailyPushNotificationDto.getId() + 10000, refreshIntent, flags);
	}

	public void enablePushNotification(DailyPushNotificationDto dailyPushNotificationDto) {
		LocalTime localTime = LocalTime.parse(dailyPushNotificationDto.getAlarmClock());

		Calendar alarmCalendar = Calendar.getInstance();
		alarmCalendar.set(Calendar.HOUR_OF_DAY, localTime.getHour());
		alarmCalendar.set(Calendar.MINUTE, localTime.getMinute());

		Calendar current = Calendar.getInstance();

		if (alarmCalendar.before(current)) {
			alarmCalendar.add(Calendar.DATE, 1);
		}

		alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, alarmCalendar.getTimeInMillis(),
				getRefreshPendingIntent(dailyPushNotificationDto, PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_MUTABLE));
	}

	public void disablePushNotification(DailyPushNotificationDto dailyPushNotificationDto) {
		PendingIntent pendingIntent = getRefreshPendingIntent(dailyPushNotificationDto, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_MUTABLE);
		if (pendingIntent != null) {
			alarmManager.cancel(pendingIntent);
			pendingIntent.cancel();
		}
	}

	public void modifyPushNotification(DailyPushNotificationDto dailyPushNotificationDto) {
		disablePushNotification(dailyPushNotificationDto);

		if (dailyPushNotificationDto.isEnabled()) {
			enablePushNotification(dailyPushNotificationDto);
		}
	}

	public boolean isRepeating(DailyPushNotificationDto dailyPushNotificationDto) {
		PendingIntent pendingIntent = getRefreshPendingIntent(dailyPushNotificationDto, PendingIntent.FLAG_NO_CREATE | PendingIntent.FLAG_MUTABLE);

		if (pendingIntent != null) {
			return true;
		} else {
			return false;
		}
	}

	public void reStartNotifications(@Nullable BackgroundWorkCallback backgroundWorkCallback) {
		DailyPushNotificationRepository repository = new DailyPushNotificationRepository(context);
		repository.getAll(new DbQueryCallback<List<DailyPushNotificationDto>>() {
			@Override
			public void onResultSuccessful(List<DailyPushNotificationDto> result) {
				if (result.size() > 0) {
					for (DailyPushNotificationDto dto : result) {
						if (dto.isEnabled()) {
							if (!isRepeating(dto)) {
								enablePushNotification(dto);
							}
						}
					}
				}

				if (backgroundWorkCallback != null)
					backgroundWorkCallback.onFinished();
			}

			@Override
			public void onResultNoData() {
				if (backgroundWorkCallback != null)
					backgroundWorkCallback.onFinished();
			}
		});
	}
}
