package com.lifedawn.bestweather.ui.weathers.viewmodels;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.lifedawn.bestweather.commons.classes.FusedLocation;
import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.data.local.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.data.local.room.queryinterfaces.FavoriteAddressQuery;
import com.lifedawn.bestweather.data.local.room.repository.FavoriteAddressRepository;

import java.util.List;

public class WeatherViewModel extends ViewModel implements FavoriteAddressQuery {
	private FusedLocation.MyLocationCallback locationCallback;
	private FavoriteAddressRepository favoriteAddressRepository;

	private MutableLiveData<String> currentLocationLiveData = new MutableLiveData<>();


	public final LiveData<List<FavoriteAddressDto>> favoriteAddressListLiveData;

	public WeatherViewModel() {
		super();
		favoriteAddressRepository = FavoriteAddressRepository.getINSTANCE();
		favoriteAddressListLiveData = favoriteAddressRepository.getAllData();
	}

	public void setLocationCallback(FusedLocation.MyLocationCallback locationCallback) {
		this.locationCallback = locationCallback;
	}

	public FusedLocation.MyLocationCallback getLocationCallback() {
		return locationCallback;
	}


	@Override
	public void getAll(DbQueryCallback<List<FavoriteAddressDto>> callback) {
		favoriteAddressRepository.getAll(callback);
	}


	@Override
	public void get(int id, DbQueryCallback<FavoriteAddressDto> callback) {
		favoriteAddressRepository.get(id, callback);
	}

	@Override
	public void size(DbQueryCallback<Integer> callback) {
		favoriteAddressRepository.size(callback);
	}

	@Override
	public void contains(String latitude, String longitude, DbQueryCallback<Boolean> callback) {
		favoriteAddressRepository.contains(latitude, longitude, callback);
	}

	@Override
	public void add(FavoriteAddressDto favoriteAddressDto, DbQueryCallback<Long> callback) {
		favoriteAddressRepository.add(favoriteAddressDto, callback);
	}

	@Override
	public void delete(FavoriteAddressDto favoriteAddressDto) {
		favoriteAddressRepository.delete(favoriteAddressDto);
	}

	@Override
	public void delete(FavoriteAddressDto favoriteAddressDto, DbQueryCallback<Boolean> callback) {
		favoriteAddressRepository.delete(favoriteAddressDto, callback);
	}

	public LiveData<String> getCurrentLocationLiveData() {
		return currentLocationLiveData;
	}

	public void setCurrentLocationAddressName(String addressName) {
		currentLocationLiveData.setValue(addressName);
	}
}
