package com.lifedawn.bestweather.alarm.alarmnotifications;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.media.AudioManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.os.VibrationEffect;
import android.os.Vibrator;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.alarm.AlarmActivity;
import com.lifedawn.bestweather.commons.constants.BundleKey;
import com.lifedawn.bestweather.notification.NotificationHelper;
import com.lifedawn.bestweather.notification.NotificationType;
import com.lifedawn.bestweather.room.dto.AlarmDto;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class AlarmService extends Service {
	private Vibrator vibrator;
	private int originalAlarmVolume;
	private AudioManager audioManager;
	private MediaPlayer mediaPlayer;
	private NotificationHelper notificationHelper;


	public AlarmService() {
		super();
	}

	@Nullable
	@Override
	public IBinder onBind(Intent intent) {
		return null;
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		AlarmDto alarmDto = (AlarmDto) intent.getExtras().getSerializable(AlarmDto.class.getName());
		notificationHelper = new NotificationHelper(getApplicationContext());

		NotificationHelper.NotificationObj notificationObj = notificationHelper.createNotification(NotificationType.Alarm);
		notificationObj.getNotificationBuilder().setPriority(Notification.PRIORITY_MAX);
		notificationObj.getNotificationBuilder().setCategory(NotificationCompat.CATEGORY_ALARM);
		notificationObj.getNotificationBuilder().setSmallIcon(R.drawable.alarm);
		notificationObj.getNotificationBuilder().setOngoing(true);

		LocalDateTime now2 = LocalDateTime.now();
		DateTimeFormatter dateTimeFormatter = DateTimeFormatter.ofPattern("a h:mm");
		String contentText = now2.format(dateTimeFormatter) + ", "
				+ getString(R.string.clickToSnoozeOrCancelAlarm);

		notificationObj.getNotificationBuilder().setContentTitle(getString(R.string.alarm));
		notificationObj.getNotificationBuilder().setContentText(contentText);
		notificationObj.getNotificationBuilder().setVibrate(new long[]{0L});

		Intent alarmIntent = new Intent(getApplicationContext(), AlarmActivity.class);
		Bundle bundle = new Bundle();
		bundle.putInt(BundleKey.dtoId.name(), alarmDto.getId());
		alarmIntent.putExtras(bundle);
		alarmIntent.addFlags(Intent.FLAG_ACTIVITY_SINGLE_TOP);

		notificationObj.getNotificationBuilder().setContentIntent(PendingIntent.getActivity(getApplicationContext(), 32323, alarmIntent, PendingIntent.FLAG_MUTABLE));

		if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
			startForeground((int) System.currentTimeMillis(), notificationObj.getNotificationBuilder().build());

		} else {
			NotificationManager notificationManager = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
			notificationManager.notify(notificationObj.getNotificationId(), notificationObj.getNotificationBuilder().build());
		}

		if (alarmDto.getAlarmVibration() == 1) {
			vibrator = (Vibrator) getSystemService(Context.VIBRATOR_SERVICE);
			final long[] vibratePattern = new long[]{600, 1000, 500, 1100};

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
				vibrator.vibrate(VibrationEffect.createWaveform(vibratePattern, 0));
			} else {
				vibrator.vibrate(vibratePattern, 0);
			}
		}

		if (alarmDto.getEnableSound() == 1) {
			audioManager = (AudioManager) getSystemService(Context.AUDIO_SERVICE);
			originalAlarmVolume = audioManager.getStreamVolume(AudioManager.STREAM_ALARM);

			final float volume = alarmDto.getAlarmSoundVolume() / 100f;
			int newVolume = (int) (volume * audioManager.getStreamMaxVolume(AudioManager.STREAM_ALARM));
			audioManager.setStreamVolume(AudioManager.STREAM_ALARM, newVolume, AudioManager.FLAG_PLAY_SOUND);

			mediaPlayer = new MediaPlayer();
			try {
				Uri uri = Uri.parse(alarmDto.getAlarmSoundUri());

				mediaPlayer.setDataSource(getApplicationContext(), uri);
				mediaPlayer.setAudioStreamType(AudioManager.STREAM_ALARM);
				mediaPlayer.setLooping(true);
				mediaPlayer.setVolume(1f, 1f);

				mediaPlayer.prepare();
			} catch (IOException e) {
				e.printStackTrace();
			}
			mediaPlayer.start();
		}

		return super.onStartCommand(intent, flags, startId);
	}

	@Override
	public void onDestroy() {
		silent();
		super.onDestroy();
	}

	public void silent() {
		if (vibrator != null) {
			vibrator.cancel();
			vibrator = null;
		}
		if (mediaPlayer != null) {
			mediaPlayer.stop();
			audioManager.setStreamVolume(AudioManager.STREAM_ALARM, originalAlarmVolume, AudioManager.FLAG_REMOVE_SOUND_AND_VIBRATE);
			mediaPlayer = null;
		}
	}
}