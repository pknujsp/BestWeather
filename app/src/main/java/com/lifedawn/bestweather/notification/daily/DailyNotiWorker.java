package com.lifedawn.bestweather.notification.daily;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.WorkerParameters;

import com.lifedawn.bestweather.notification.BaseNotiWorker;

import org.jetbrains.annotations.NotNull;

public class DailyNotiWorker extends BaseNotiWorker {
	public DailyNotiWorker(@NonNull @NotNull Context context, @NonNull @NotNull WorkerParameters workerParams) {
		super(context, workerParams);
	}

	@Override
	protected void refreshNotification() {

	}

	@NonNull
	@NotNull
	@Override
	public Result doWork() {
		return null;
	}
}
