package com.lifedawn.bestweather.notification.daily.viewmodel;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.DailyPushNotificationDto;
import com.lifedawn.bestweather.room.repository.DailyPushNotificationRepository;

import java.util.List;

public class DailyNotificationViewModel extends AndroidViewModel {
	private DailyPushNotificationRepository repository;
	public final LiveData<List<DailyPushNotificationDto>> listLiveData;

	public DailyNotificationViewModel(@NonNull Application application) {
		super(application);
		repository = DailyPushNotificationRepository.getINSTANCE();
		listLiveData = repository.listLiveData();
	}


	public void getAll(DbQueryCallback<List<DailyPushNotificationDto>> callback) {
		repository.getAll(callback);
	}

	public void size(DbQueryCallback<Integer> callback) {
		repository.size(callback);
	}

	public void get(int id, DbQueryCallback<DailyPushNotificationDto> callback) {
		repository.get(id, callback);
	}

	public void delete(DailyPushNotificationDto dailyPushNotificationDto, @Nullable DbQueryCallback<Boolean> callback) {
		repository.delete(dailyPushNotificationDto, callback);
	}

	public void add(DailyPushNotificationDto dailyPushNotificationDto, DbQueryCallback<DailyPushNotificationDto> callback) {
		repository.add(dailyPushNotificationDto, callback);
	}

	public void update(DailyPushNotificationDto dailyPushNotificationDto, @Nullable DbQueryCallback<DailyPushNotificationDto> callback) {
		repository.update(dailyPushNotificationDto, callback);
	}

}
