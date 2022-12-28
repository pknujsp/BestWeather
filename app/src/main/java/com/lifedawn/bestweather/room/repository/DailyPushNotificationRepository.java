package com.lifedawn.bestweather.room.repository;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.lifecycle.LiveData;

import com.lifedawn.bestweather.data.MyApplication;
import com.lifedawn.bestweather.room.AppDb;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dao.DailyPushNotificationDao;
import com.lifedawn.bestweather.room.dto.DailyPushNotificationDto;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class DailyPushNotificationRepository {
	private static DailyPushNotificationRepository INSTANCE;

	private final DailyPushNotificationDao dailyPushNotificationDao;
	private final ExecutorService executorService = MyApplication.getExecutorService();

	public static DailyPushNotificationRepository getINSTANCE() {
		return INSTANCE;
	}

	public static void initialize(Context context) {
		if (INSTANCE == null)
			INSTANCE = new DailyPushNotificationRepository(context);
	}


	private DailyPushNotificationRepository(Context context) {
		dailyPushNotificationDao = AppDb.getInstance(context).dailyPushNotificationDao();
	}

	public LiveData<List<DailyPushNotificationDto>> listLiveData() {
		return dailyPushNotificationDao.list();
	}


	public void getAll(DbQueryCallback<List<DailyPushNotificationDto>> callback) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				callback.processResult(dailyPushNotificationDao.getAll());
			}
		});
	}

	public void size(DbQueryCallback<Integer> callback) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				callback.processResult(dailyPushNotificationDao.size());
			}
		});
	}

	public void get(int id, DbQueryCallback<DailyPushNotificationDto> callback) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				callback.processResult(dailyPushNotificationDao.get(id));
			}
		});
	}

	public void delete(DailyPushNotificationDto dailyPushNotificationDto, @Nullable DbQueryCallback<Boolean> callback) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				dailyPushNotificationDao.delete(dailyPushNotificationDto);
				if (callback != null) {
					callback.processResult(true);
				}
			}
		});
	}

	public void add(DailyPushNotificationDto dailyPushNotificationDto, DbQueryCallback<DailyPushNotificationDto> callback) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				int id = (int) dailyPushNotificationDao.add(dailyPushNotificationDto);
				callback.processResult(dailyPushNotificationDao.get(id));
			}
		});
	}

	public void update(DailyPushNotificationDto dailyPushNotificationDto, @Nullable DbQueryCallback<DailyPushNotificationDto> callback) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				dailyPushNotificationDao.update(dailyPushNotificationDto);
				if (callback != null) {
					DailyPushNotificationDto updated = dailyPushNotificationDao.get(dailyPushNotificationDto.getId());
					callback.onResultSuccessful(updated);
				}
			}
		});
	}

}
