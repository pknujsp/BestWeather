package com.lifedawn.bestweather.notification.ongoing;

import android.app.Notification;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

import androidx.annotation.NonNull;
import androidx.core.app.NotificationCompat;
import androidx.work.ForegroundInfo;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import com.google.common.util.concurrent.ListenableFuture;
import com.lifedawn.bestweather.utils.DeviceUtils;
import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.interfaces.Callback;
import com.lifedawn.bestweather.notification.NotificationHelper;
import com.lifedawn.bestweather.notification.NotificationType;

import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

public class OngoingNotificationWorker extends Worker {
	private OngoingNotiViewCreator ongoingNotiViewCreator;
	private String action;

	public OngoingNotificationWorker(@NonNull Context context, @NonNull WorkerParameters workerParams) {
		super(context, workerParams);
		action = workerParams.getInputData().getString("action");
	}

	@NonNull
	@Override
	public Result doWork() {

		if (action.equals(Intent.ACTION_BOOT_COMPLETED) || action.equals(Intent.ACTION_MY_PACKAGE_REPLACED)) {
			OngoingNotificationHelper ongoingNotificationHelper = new OngoingNotificationHelper(getApplicationContext());
			ongoingNotificationHelper.reStartNotification(new Callback() {
				@Override
				public void onResult() {

				}
			});
		} else if (action.equals(getApplicationContext().getString(R.string.com_lifedawn_bestweather_action_REFRESH))) {
			if (DeviceUtils.Companion.isScreenOn(getApplicationContext())) {
				ongoingNotiViewCreator = new OngoingNotiViewCreator(getApplicationContext(), null);
				ongoingNotiViewCreator.loadSavedPreferences();

				if (ongoingNotiViewCreator.getNotificationDataObj().getUpdateIntervalMillis() > 0) {
					OngoingNotificationHelper ongoingNotificationHelper = new OngoingNotificationHelper(getApplicationContext());

					if (!ongoingNotificationHelper.isRepeating()) {
						ongoingNotificationHelper.onSelectedAutoRefreshInterval(ongoingNotiViewCreator.getNotificationDataObj().getUpdateIntervalMillis());
					}
				}

				ongoingNotiViewCreator.initNotification(new Callback() {
					@Override
					public void onResult() {
						if (ongoingNotiViewCreator.getNotificationDataObj().getLocationType() == LocationType.CurrentLocation) {
							NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
							notificationHelper.cancelNotification(NotificationType.Location.getNotificationId());
						}
					}
				});
			}

		}
		return Result.success();

	}


	@NonNull
	@Override
	public ListenableFuture<ForegroundInfo> getForegroundInfoAsync() {
		final int notificationId = (int) System.currentTimeMillis();
		NotificationHelper notificationHelper = new NotificationHelper(getApplicationContext());
		NotificationHelper.NotificationObj notificationObj = notificationHelper.createNotification(NotificationType.ForegroundService);

		NotificationCompat.Builder builder = notificationObj.getNotificationBuilder();
		builder.setSmallIcon(R.mipmap.ic_launcher_round).setContentText(getApplicationContext().getString(R.string.updatingNotification)).setContentTitle(
						getApplicationContext().getString(R.string.updatingNotification))
				.setOnlyAlertOnce(true).setWhen(0).setOngoing(true);

		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.O) {
			builder.setPriority(NotificationCompat.PRIORITY_LOW).setVisibility(NotificationCompat.VISIBILITY_PUBLIC);
		}

		Notification notification = notificationObj.getNotificationBuilder().build();
		ForegroundInfo foregroundInfo = new ForegroundInfo(notificationId, notification);

		return new ListenableFuture<ForegroundInfo>() {
			@Override
			public void addListener(Runnable listener, Executor executor) {

			}

			@Override
			public boolean cancel(boolean mayInterruptIfRunning) {
				return false;
			}

			@Override
			public boolean isCancelled() {
				return false;
			}

			@Override
			public boolean isDone() {
				return true;
			}

			@Override
			public ForegroundInfo get() throws ExecutionException, InterruptedException {
				return foregroundInfo;
			}

			@Override
			public ForegroundInfo get(long timeout, TimeUnit unit) throws ExecutionException, InterruptedException, TimeoutException {
				return foregroundInfo;
			}
		};
	}

	@Override
	public void onStopped() {
		super.onStopped();
	}

}
