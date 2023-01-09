package com.lifedawn.bestweather.data.local.room.repository;

import android.content.Context;

import androidx.annotation.Nullable;

import com.lifedawn.bestweather.data.local.room.AppDb;
import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.data.local.room.dao.AlarmDao;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class AlarmRepository {
	private Context context;
	private AlarmDao alarmDao;
	private ExecutorService executorService = Executors.newSingleThreadExecutor();

	public AlarmRepository(Context context) {
		this.context = context;
		alarmDao = AppDb.getInstance(context).alarmDao();
	}


	public void getAll(DbQueryCallback<List<AlarmDto>> callback) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				callback.processResult(alarmDao.getAll());
			}
		});
	}

	public void size(DbQueryCallback<Integer> callback) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				callback.processResult(alarmDao.size());
			}
		});
	}

	public void get(int id, DbQueryCallback<AlarmDto> callback) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				callback.processResult(alarmDao.get(id));
			}
		});
	}

	public void delete(AlarmDto alarmDto, @Nullable DbQueryCallback<Boolean> callback) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				alarmDao.delete(alarmDto);
				if (callback != null) {
					callback.processResult(true);
				}
			}
		});
	}

	public void add(AlarmDto alarmDto, DbQueryCallback<AlarmDto> callback) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				int id = (int) alarmDao.add(alarmDto);
				callback.processResult(alarmDao.get(id));
			}
		});
	}

	public void update(AlarmDto alarmDto, DbQueryCallback<AlarmDto> callback) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				int id = (int) alarmDao.update(alarmDto);
				callback.processResult(alarmDao.get(id));
			}
		});
	}

}
