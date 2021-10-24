package com.lifedawn.bestweather.weathers.viewmodels;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.room.queryinterfaces.FavoriteAddressQuery;
import com.lifedawn.bestweather.room.repository.FavoriteAddressRepository;
import com.lifedawn.bestweather.weathers.dataprocessing.request.MainProcessing;

import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.TimeZone;

public class WeatherViewModel extends AndroidViewModel implements FavoriteAddressQuery {
	private ILoadImgOfCurrentConditions iLoadImgOfCurrentConditions;
	private FavoriteAddressRepository favoriteAddressRepository;
	
	private MutableLiveData<FavoriteAddressDto> addAddressesLiveData;
	private MutableLiveData<FavoriteAddressDto> deleteAddressesLiveData;
	private MutableLiveData<String> currentLocationLiveData = new MutableLiveData<>();
	
	public WeatherViewModel(@NonNull @NotNull Application application) {
		super(application);
		favoriteAddressRepository = new FavoriteAddressRepository(application.getApplicationContext());
		addAddressesLiveData = favoriteAddressRepository.getAddAddressesLiveData();
		deleteAddressesLiveData = favoriteAddressRepository.getDeleteAddressesLiveData();
	}
	
	public void setiLoadImgOfCurrentConditions(ILoadImgOfCurrentConditions iLoadImgOfCurrentConditions) {
		this.iLoadImgOfCurrentConditions = iLoadImgOfCurrentConditions;
	}
	
	public ILoadImgOfCurrentConditions getiLoadImgOfCurrentConditions() {
		return iLoadImgOfCurrentConditions;
	}
	
	public LiveData<FavoriteAddressDto> getDeleteAddressesLiveData() {
		return deleteAddressesLiveData;
	}
	
	public LiveData<FavoriteAddressDto> getAddAddressesLiveData() {
		return addAddressesLiveData;
	}
	
	@Override
	public void getAll(DbQueryCallback<List<FavoriteAddressDto>> callback) {
		favoriteAddressRepository.getAll(callback);
	}
	
	@Override
	public void contains(String latitude, String longitude, DbQueryCallback<Integer> callback) {
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
	
	public LiveData<String> getCurrentLocationLiveData() {
		return currentLocationLiveData;
	}
	
	public void setCurrentLocationAddressName(String addressName) {
		currentLocationLiveData.setValue(addressName);
	}
	
	public interface ILoadImgOfCurrentConditions {
		void loadImgOfCurrentConditions(MainProcessing.WeatherSourceType weatherSourceType, String val, Double latitude, Double longitude, TimeZone timeZone);
	}
}
