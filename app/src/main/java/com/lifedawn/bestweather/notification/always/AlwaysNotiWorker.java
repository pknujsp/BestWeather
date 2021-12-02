package com.lifedawn.bestweather.notification.always;

import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.work.WorkRequest;
import androidx.work.Worker;
import androidx.work.WorkerParameters;
import androidx.work.impl.model.WorkSpec;

import com.lifedawn.bestweather.R;
import com.lifedawn.bestweather.notification.NotificationReceiver;
import com.lifedawn.bestweather.notification.NotificationType;

import org.jetbrains.annotations.NotNull;

import java.util.Set;
import java.util.UUID;

public class AlwaysNotiWorker extends Worker {

	public AlwaysNotiWorker(@NonNull @NotNull Context context, @NonNull @NotNull WorkerParameters workerParams) {
		super(context, workerParams);
	}

	@NonNull
	@NotNull
	@Override
	public Result doWork() {
		Context context = getApplicationContext();

		Intent refreshIntent = new Intent(context, NotificationReceiver.class);
		refreshIntent.setAction(context.getString(R.string.com_lifedawn_bestweather_action_REFRESH));
		Bundle bundle = new Bundle();
		bundle.putString(NotificationType.class.getName(), NotificationType.Always.name());

		refreshIntent.putExtras(bundle);

		PendingIntent pendingIntent = PendingIntent.getBroadcast(context, 11, refreshIntent,
				PendingIntent.FLAG_UPDATE_CURRENT);
		try {
			pendingIntent.send();
		} catch (PendingIntent.CanceledException e) {
			e.printStackTrace();
		}
		return Result.success();
	}
}
