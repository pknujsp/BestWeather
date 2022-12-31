package com.lifedawn.bestweather.ui.notification.ongoing;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.annotation.Nullable;
import androidx.preference.PreferenceManager;

import com.google.gson.Gson;
import com.lifedawn.bestweather.commons.interfaces.BackgroundWorkCallback;
import com.lifedawn.bestweather.data.MyApplication;
import com.lifedawn.bestweather.ui.notification.model.OngoingNotificationDto;
import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback;

public class OngoingNotificationRepository {
	private static OngoingNotificationRepository INSTANCE;
	private final Context context;
	private final String key = "ongoing_notification";

	public static OngoingNotificationRepository getINSTANCE() {
		return INSTANCE;
	}

	private OngoingNotificationRepository(Context context) {
		this.context = context;
	}

	public static void initialize(Context context) {
		if (INSTANCE == null)
			INSTANCE = new OngoingNotificationRepository(context);
	}

	public void getOngoingNotificationDto(DbQueryCallback<OngoingNotificationDto> callback) {
		MyApplication.getExecutorService().execute(new Runnable() {
			@Override
			public void run() {
				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
				final String json = sharedPreferences.getString(key, "");

				if (json.isEmpty())
					callback.processResult(null);
				else {
					OngoingNotificationDto dto = new Gson().fromJson(json, OngoingNotificationDto.class);
					callback.processResult(dto);
				}
			}
		});

	}

	public void save(OngoingNotificationDto ongoingNotificationDto, @Nullable BackgroundWorkCallback callback) {
		MyApplication.getExecutorService().execute(new Runnable() {
			@Override
			public void run() {
				SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
				sharedPreferences.edit().putString(key, new Gson().toJson(ongoingNotificationDto)).commit();

				if (callback != null) {
					callback.onFinished();
				}

			}

		});
	}

	public void remove() {
		SharedPreferences sharedPreferences = PreferenceManager.getDefaultSharedPreferences(context);
		sharedPreferences.edit().remove(key).apply();
	}
}
