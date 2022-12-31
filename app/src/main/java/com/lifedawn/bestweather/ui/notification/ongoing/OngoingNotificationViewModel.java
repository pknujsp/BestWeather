package com.lifedawn.bestweather.ui.notification.ongoing;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;

import com.lifedawn.bestweather.commons.interfaces.BackgroundWorkCallback;
import com.lifedawn.bestweather.ui.notification.model.OngoingNotificationDto;
import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback;

public class OngoingNotificationViewModel extends AndroidViewModel {
	private final OngoingNotificationRepository repository;

	public OngoingNotificationViewModel(@NonNull Application application) {
		super(application);
		repository = OngoingNotificationRepository.getINSTANCE();
	}

	public void getOngoingNotificationDto(DbQueryCallback<OngoingNotificationDto> callback) {
		repository.getOngoingNotificationDto(callback);
	}


	public void save(OngoingNotificationDto ongoingNotificationDto, @Nullable BackgroundWorkCallback callback) {
		repository.save(ongoingNotificationDto, callback);
	}

	public void remove() {
		repository.remove();
	}

}
