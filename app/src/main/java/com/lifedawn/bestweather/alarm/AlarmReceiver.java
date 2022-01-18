package com.lifedawn.bestweather.alarm;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.Message;
import android.os.PowerManager;
import android.view.View;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationCompat;
import androidx.core.os.HandlerCompat;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.alarm.alarmnotifications.AlarmService;
import com.lifedawn.bestweather.commons.classes.MainThreadWorker;
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
							MainThreadWorker.runOnUiThread(new Runnable() {
								@Override
								public void run() {
									AlarmUtil.registerAlarm(context, result);
									Set<Integer> daySet = AlarmUtil.parseDays(result.getAlarmDays());
									Calendar now = Calendar.getInstance();

									if (daySet.contains(now.get(Calendar.DAY_OF_WEEK))) {
										Intent alarmIntent = new Intent(context, AlarmActivity.class);
										alarmIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);
										Bundle bundle = new Bundle();
										bundle.putInt(BundleKey.dtoId.name(), alarmDtoId);
										alarmIntent.putExtras(bundle);

										Intent alarmService = new Intent(context, AlarmService.class);
										Bundle alarmBundle = new Bundle();
										alarmBundle.putSerializable(AlarmDto.class.getName(), result);
										alarmBundle.putInt(BundleKey.dtoId.name(), alarmDtoId);

										alarmService.putExtras(alarmBundle);

										if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
											context.startForegroundService(alarmService);
										} else {
											context.startService(alarmService);
										}

										context.startActivity(alarmIntent);
									}
								}
							});
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
					AlarmUtil.upRepeatCount(context, alarmDtoId);

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
					final int alarmDtoId = intent.getExtras().getInt(BundleKey.dtoId.name());

					Intent alarmService = new Intent(context, AlarmService.class);
					context.stopService(alarmService);

					AlarmUtil.clearRepeatCount(context, alarmDtoId);

					Intent endAlarmActivityIntent = new Intent(context.getString(R.string.com_lifedawn_bestweather_action_END_ALARM));
					context.sendBroadcast(endAlarmActivityIntent);

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

		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 5000, intent, Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ?
				PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
				PendingIntent.FLAG_UPDATE_CURRENT);
		return pendingIntent;
	}

	public static PendingIntent getEndAlarmPendingIntent(Context context, int alarmDtoId) {
		Intent intent = new Intent(context, AlarmReceiver.class);
		intent.setAction(context.getString(R.string.com_lifedawn_bestweather_action_END_ALARM));
		Bundle bundle = new Bundle();
		bundle.putInt(BundleKey.dtoId.name(), alarmDtoId);
		intent.putExtras(bundle);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 6000, intent, Build.VERSION.SDK_INT >= Build.VERSION_CODES.S ?
				PendingIntent.FLAG_UPDATE_CURRENT | PendingIntent.FLAG_IMMUTABLE :
				PendingIntent.FLAG_UPDATE_CURRENT);
		return pendingIntent;
	}
}