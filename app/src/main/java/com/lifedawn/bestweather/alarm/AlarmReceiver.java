package com.lifedawn.bestweather.alarm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.PowerManager;
import android.view.View;
import android.widget.RemoteViews;

import androidx.core.app.NotificationCompat;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.BundleKey;
import com.lifedawn.bestweather.notification.NotificationHelper;
import com.lifedawn.bestweather.notification.NotificationType;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.AlarmDto;
import com.lifedawn.bestweather.room.repository.AlarmRepository;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Calendar;
import java.util.List;
import java.util.Set;
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
					final int alarmDtoId = intent.getExtras().getInt(BundleKey.dtoId.name());
					alarmRepository.get(alarmDtoId, new DbQueryCallback<AlarmDto>() {
						@Override
						public void onResultSuccessful(AlarmDto result) {
							AlarmUtil.registerAlarm(context, result);
							Set<Integer> daySet = AlarmUtil.parseDays(result.getAlarmDays());
							Calendar now = Calendar.getInstance();
							LocalDateTime now2 = LocalDateTime.now();
							DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("a h:mm");

							if (daySet.contains(now.get(Calendar.DAY_OF_WEEK))) {
								RemoteViews remoteViews = new RemoteViews(context.getPackageName(), R.layout.view_always_notification);
								remoteViews.setTextViewText(R.id.time, now2.format(dateTimeFormatter));

								if (result.getRepeat() == 1) {
									String text = result.getRepeatInterval() + context.getString(R.string.againAlarmAfterNMinutes);
									PendingIntent ringAgainIntent = AlarmReceiver.getRingAgainPendingIntent(context, alarmDtoId);
									remoteViews.setOnClickPendingIntent(R.id.ringAgainBtn, ringAgainIntent);
								}

								//알람 종료 처리 코드 추가하기
								remoteViews.setViewVisibility(R.id.ringAgainBtn, result.getRepeat() == 1 ? View.VISIBLE : View.GONE);

								NotificationHelper notificationHelper = new NotificationHelper(context);
								NotificationHelper.NotificationObj notificationObj = notificationHelper.createNotification(NotificationType.Alarm);
								notificationObj.getNotificationBuilder().setPriority(Notification.PRIORITY_MAX);
								notificationObj.getNotificationBuilder().setStyle(new NotificationCompat.BigTextStyle());
								notificationObj.getNotificationBuilder().setCategory(NotificationCompat.CATEGORY_ALARM);
								notificationObj.getNotificationBuilder().setSmallIcon(R.drawable.alarm);
								notificationObj.getNotificationBuilder().setCustomContentView(remoteViews);
								notificationObj.getNotificationBuilder().setCustomBigContentView(remoteViews);
								notificationObj.getNotificationBuilder().setVibrate(new long[]{0L});
								notificationObj.getNotificationBuilder().setContentIntent(null);

								NotificationManager notificationManager = (NotificationManager) context.getSystemService(Context.NOTIFICATION_SERVICE);
								notificationManager.notify(notificationObj.getNotificationId(), notificationObj.getNotificationBuilder().build());

								Intent alarmIntent = new Intent(context, AlarmActivity.class);
								alarmIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
								Bundle bundle = new Bundle();
								bundle.putInt(BundleKey.dtoId.name(), alarmDtoId);
								alarmIntent.putExtras(bundle);

								context.startActivity(alarmIntent);
								//fullscreen intent
							}
						}

						@Override
						public void onResultNoData() {

						}
					});


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
				} else if (action.equals(context.getString(R.string.com_lifedawn_bestweather_action_RING_AGAIN))) {
					final int alarmDtoId = intent.getExtras().getInt(BundleKey.dtoId.name());
					alarmRepository.get(alarmDtoId, new DbQueryCallback<AlarmDto>() {
						@Override
						public void onResultSuccessful(AlarmDto result) {
							AlarmUtil.registerRepeatAlarm(context, result);
						}

						@Override
						public void onResultNoData() {

						}
					});

				} else if (action.equals(context.getString(R.string.com_lifedawn_bestweather_action_END_ALARM))) {
					NotificationHelper notificationHelper = new NotificationHelper(context);
					notificationHelper.cancelNotification(NotificationType.Alarm.getNotificationId());
				}
			}
		});

	}

	public static PendingIntent getRingAgainPendingIntent(Context context, int alarmDtoId) {
		Intent intent = new Intent(context, AlarmReceiver.class);
		intent.setAction(context.getString(R.string.com_lifedawn_bestweather_action_RING_AGAIN));
		Bundle bundle = new Bundle();
		bundle.putInt(BundleKey.dtoId.name(), alarmDtoId);
		intent.putExtras(bundle);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 5000, intent, 0);
		return pendingIntent;
	}

	public static PendingIntent getEndAlarmPendingIntent(Context context, int alarmDtoId) {
		Intent intent = new Intent(context, AlarmReceiver.class);
		intent.setAction(context.getString(R.string.com_lifedawn_bestweather_action_END_ALARM));
		Bundle bundle = new Bundle();
		bundle.putInt(BundleKey.dtoId.name(), alarmDtoId);
		intent.putExtras(bundle);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 6000, intent, 0);
		return pendingIntent;
	}
}