package com.lifedawn.bestweather.notification.daily.viewmodel;

import android.app.Application;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import com.lifedawn.bestweather.commons.enums.LocationType;
import com.lifedawn.bestweather.commons.enums.WeatherProviderType;
import com.lifedawn.bestweather.notification.daily.DailyNotificationHelper;
import com.lifedawn.bestweather.notification.daily.DailyPushNotificationType;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.DailyPushNotificationDto;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.room.repository.DailyPushNotificationRepository;

import java.time.LocalTime;
import java.util.List;

public class DailyNotificationViewModel extends AndroidViewModel {
	private final DailyPushNotificationRepository repository = DailyPushNotificationRepository.getINSTANCE();
	public final LiveData<List<DailyPushNotificationDto>> listLiveData;

	private DailyNotificationHelper dailyNotificationHelper;
	private DailyPushNotificationDto savedNotificationDto;
	private DailyPushNotificationDto newNotificationDto;
	private DailyPushNotificationDto editingNotificationDto;

	private boolean newNotificationSession;
	private boolean selectedFavoriteLocation;

	private Bundle bundle;

	private WeatherProviderType mainWeatherProviderType;
	private FavoriteAddressDto selectedFavoriteAddressDto;

	public DailyNotificationViewModel(@NonNull Application application) {
		super(application);
		listLiveData = repository.listLiveData();
		dailyNotificationHelper = new DailyNotificationHelper(application.getApplicationContext());
	}

	public DailyNotificationHelper getDailyNotificationHelper() {
		return dailyNotificationHelper;
	}

	public DailyNotificationViewModel setDailyNotificationHelper(DailyNotificationHelper dailyNotificationHelper) {
		this.dailyNotificationHelper = dailyNotificationHelper;
		return this;
	}

	public DailyPushNotificationDto getSavedNotificationDto() {
		return savedNotificationDto;
	}

	public DailyNotificationViewModel setSavedNotificationDto(DailyPushNotificationDto savedNotificationDto) {
		this.savedNotificationDto = savedNotificationDto;
		return this;
	}

	public DailyPushNotificationDto getNewNotificationDto() {
		return newNotificationDto;
	}

	public DailyNotificationViewModel setNewNotificationDto(DailyPushNotificationDto newNotificationDto) {
		this.newNotificationDto = newNotificationDto;
		return this;
	}

	public DailyPushNotificationDto getEditingNotificationDto() {
		return editingNotificationDto;
	}

	public DailyNotificationViewModel setEditingNotificationDto(DailyPushNotificationDto editingNotificationDto) {
		this.editingNotificationDto = editingNotificationDto;
		return this;
	}

	public boolean isNewNotificationSession() {
		return newNotificationSession;
	}

	public void setNotificationSession(boolean newNotificationSession) {
		this.newNotificationSession = newNotificationSession;
		if (newNotificationSession) {
			newNotificationDto = new DailyPushNotificationDto();
			newNotificationDto.setEnabled(true);
			newNotificationDto.setAlarmClock(LocalTime.of(8, 0).toString());
			newNotificationDto.addWeatherSourceType(getMainWeatherProviderType());
			newNotificationDto.setNotificationType(DailyPushNotificationType.First);
			newNotificationDto.setLocationType(LocationType.CurrentLocation);

			editingNotificationDto = newNotificationDto;
		} else {
			savedNotificationDto = (DailyPushNotificationDto) bundle.getSerializable("dto");
			editingNotificationDto = savedNotificationDto;

			if (editingNotificationDto.getLocationType() == LocationType.SelectedAddress) {
				selectedFavoriteAddressDto = new FavoriteAddressDto();
				selectedFavoriteAddressDto.setDisplayName(editingNotificationDto.getAddressName());
				selectedFavoriteAddressDto.setCountryCode(editingNotificationDto.getCountryCode());
				selectedFavoriteAddressDto.setLatitude(String.valueOf(editingNotificationDto.getLatitude()));
				selectedFavoriteAddressDto.setLongitude(String.valueOf(editingNotificationDto.getLongitude()));

				selectedFavoriteLocation = true;
			}
		}
	}

	public boolean isSelectedFavoriteLocation() {
		return selectedFavoriteLocation;
	}

	public DailyNotificationViewModel setSelectedFavoriteLocation(boolean selectedFavoriteLocation) {
		this.selectedFavoriteLocation = selectedFavoriteLocation;
		return this;
	}

	public Bundle getBundle() {
		return bundle;
	}

	public DailyNotificationViewModel setBundle(Bundle bundle) {
		this.bundle = bundle;
		return this;
	}

	public WeatherProviderType getMainWeatherProviderType() {
		return mainWeatherProviderType;
	}

	public DailyNotificationViewModel setMainWeatherProviderType(WeatherProviderType mainWeatherProviderType) {
		this.mainWeatherProviderType = mainWeatherProviderType;
		return this;
	}

	public FavoriteAddressDto getSelectedFavoriteAddressDto() {
		return selectedFavoriteAddressDto;
	}

	public DailyNotificationViewModel setSelectedFavoriteAddressDto(FavoriteAddressDto selectedFavoriteAddressDto) {
		this.selectedFavoriteAddressDto = selectedFavoriteAddressDto;
		return this;
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
