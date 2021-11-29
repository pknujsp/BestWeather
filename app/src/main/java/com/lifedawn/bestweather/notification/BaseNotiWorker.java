package com.lifedawn.bestweather.notification;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.work.Worker;
import androidx.work.WorkerParameters;

import org.jetbrains.annotations.NotNull;

public abstract class BaseNotiWorker extends Worker {
	public BaseNotiWorker(@NonNull @NotNull Context context, @NonNull @NotNull WorkerParameters workerParams) {
		super(context, workerParams);
	}

	protected abstract void refreshNotification();
}
