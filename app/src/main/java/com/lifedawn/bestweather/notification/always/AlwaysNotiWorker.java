package com.lifedawn.bestweather.notification.always;

import android.app.NotificationManager;
import android.content.Context;
import android.widget.RemoteViews;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import com.lifedawn.bestweather.notification.BaseNotiWorker;
import com.lifedawn.bestweather.notification.NotificationHelper;
import com.lifedawn.bestweather.notification.NotificationType;

import org.jetbrains.annotations.NotNull;

public class AlwaysNotiWorker extends BaseNotiWorker {
	public AlwaysNotiWorker(@NonNull @NotNull Context context, @NonNull @NotNull WorkerParameters workerParams) {
		super(context, workerParams);
	}

	@NonNull
	@Override
	public @NotNull Result doWork() {
		refreshNotification();
		return Result.success();
	}

	@Override
	protected void refreshNotification() {
		Context context = getApplicationContext();
		NotificationHelper notificationHelper = new NotificationHelper(context);
		NotificationHelper.NotificationObj notificationObj = notificationHelper.createNotification(NotificationType.Always);

		AlwaysNotiViewCreator alwaysNotiViewCreator = new AlwaysNotiViewCreator(context, null);
		RemoteViews remoteViews = alwaysNotiViewCreator.createRemoteViews(false);

		NotificationManager notificationManager = context.getSystemService(NotificationManager.class);
		notificationManager.notify(notificationObj.getNotificationId(), notificationObj.getNotificationBuilder().build());
	}
}
