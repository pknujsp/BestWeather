package com.lifedawn.bestweather.room.repository;

import android.content.Context;

import androidx.annotation.Nullable;

import com.lifedawn.bestweather.room.AppDb;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dao.DailyPushNotificationDao;
import com.lifedawn.bestweather.room.dto.DailyPushNotificationDto;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class DailyPushNotificationRepository {
	private Context context;
	private DailyPushNotificationDao dailyPushNotificationDao;
	private ExecutorService executorService = Executors.newSingleThreadExecutor();

	public DailyPushNotificationRepository(Context context) {
		this.context = context;
		dailyPushNotificationDao = AppDb.getInstance(context).dailyPushNotificationDao();
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

	public void update(DailyPushNotificationDto dailyPushNotificationDto, DbQueryCallback<DailyPushNotificationDto> callback) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				int id = (int) dailyPushNotificationDao.update(dailyPushNotificationDto);
				callback.processResult(dailyPushNotificationDao.get(id));
			}
		});
	}

}
