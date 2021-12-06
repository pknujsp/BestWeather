package com.lifedawn.bestweather.alarm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.core.app.NotificationCompat;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.notification.NotificationHelper;
import com.lifedawn.bestweather.notification.NotificationType;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.AlarmDto;
import com.lifedawn.bestweather.room.repository.AlarmRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AlarmReceiver extends BroadcastReceiver {
	private ExecutorService executorService = Executors.newSingleThreadExecutor();

	@Override
	public void onReceive(Context context, Intent intent) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				final String action = intent.getAction();
				AlarmRepository alarmRepository = new AlarmRepository(context);

				if (action.equals(context.getString(R.string.com_lifedawn_bestweather_action_ALARM))) {
					NotificationHelper notificationHelper = new NotificationHelper(context);
					NotificationHelper.NotificationObj notificationObj = notificationHelper.createNotification(NotificationType.Alarm);
					notificationObj.getNotificationBuilder().setPriority(Notification.PRIORITY_MAX);
					notificationObj.getNotificationBuilder().setStyle(new NotificationCompat.BigTextStyle());
					notificationObj.getNotificationBuilder().setCategory(NotificationCompat.CATEGORY_ALARM);
					notificationObj.getNotificationBuilder().setAutoCancel(true);

					Intent alarmIntent = new Intent(context, AlarmActivity.class);
					Bundle bundle = new Bundle();
					bundle.putInt(BundleKey.dtoId.name(), intent.getExtras().getInt(BundleKey.dtoId.name()));

					PendingIntent fullscreenIntent = PendingIntent.getActivity(context, 0, alarmIntent, 0);
					notificationObj.getNotificationBuilder().setFullScreenIntent(fullscreenIntent, true);

					NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
					notificationManager.notify(notificationObj.getNotificationId(), notificationObj.getNotificationBuilder().build());
					//fullscreen intent
					//기기를 사용중이면 notification, 아니면 화면을 켠다
				} else if (action.equals(Intent.ACTION_BOOT_COMPLETED)) {
					alarmRepository.getAll(new DbQueryCallback<List<AlarmDto>>() {
						@Override
						public void onResultSuccessful(List<AlarmDto> result) {
							List<AlarmDto> alarmDtos = result;
							for (AlarmDto alarmDto : alarmDtos) {
								AlarmUtil.registerAlarm(context, alarmDto);
							}
						}

						@Override
						public void onResultNoData() {

						}
					});
				}
			}
		});

	}
}