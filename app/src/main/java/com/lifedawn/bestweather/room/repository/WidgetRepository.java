package com.lifedawn.bestweather.room.repository;

import android.content.Context;

import androidx.annotation.Nullable;

import com.lifedawn.bestweather.room.AppDb;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dao.WidgetDao;
import com.lifedawn.bestweather.room.dto.WidgetDto;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WidgetRepository {
	private Context context;
	private final WidgetDao widgetDao;
	private final ExecutorService executorService = Executors.newSingleThreadExecutor();

	public WidgetRepository(Context context) {
		this.context = context;
		widgetDao = AppDb.getInstance(context).widgetDao();
	}

	public void add(WidgetDto widgetDto, DbQueryCallback<WidgetDto> callback) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				long newDtoId = widgetDao.add(widgetDto);
				callback.onResultSuccessful(widgetDao.get(newDtoId));
			}
		});
	}

	public void get(int appWidgetId, DbQueryCallback<WidgetDto> callback) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				callback.onResultSuccessful(widgetDao.get(appWidgetId));
			}
		});
	}

	public void get(long widgetDtoId, DbQueryCallback<WidgetDto> callback) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				callback.onResultSuccessful(widgetDao.get(widgetDtoId));
			}
		});
	}

	public void update(WidgetDto widgetDto, @Nullable DbQueryCallback<WidgetDto> callback) {
		executorService.execute(new Runnable() {
			@Override
			public void run() {
				widgetDao.update(widgetDto);
				if (callback != null) {
					callback.onResultSuccessful(widgetDao.get(widgetDto.getId()));
				}
			}
		});
	}
}
