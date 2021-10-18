package com.lifedawn.bestweather.room.repository;

import android.content.Context;

import androidx.lifecycle.MutableLiveData;

import com.lifedawn.bestweather.room.AppDb;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dao.FavoriteAddressDao;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.room.queryinterfaces.FavoriteAddressQuery;

import java.util.List;

public class FavoriteAddressRepository implements FavoriteAddressQuery {
	private FavoriteAddressDao favoriteAddressDao;
	private MutableLiveData<FavoriteAddressDto> addAddressesLiveData = new MutableLiveData<>();
	private MutableLiveData<FavoriteAddressDto> deleteAddressesLiveData = new MutableLiveData<>();
	
	public FavoriteAddressRepository(Context context) {
		favoriteAddressDao = AppDb.getInstance(context).favoriteAddressDao();
	}
	
	@Override
	public void getAll(DbQueryCallback<List<FavoriteAddressDto>> callback) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				callback.processResult(favoriteAddressDao.getAll());
			}
		}).start();
	}
	
	@Override
	public void contains(String latitude, String longitude, DbQueryCallback<Integer> callback) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				callback.processResult(favoriteAddressDao.contains(latitude, longitude));
			}
		}).start();
	}
	
	@Override
	public void add(FavoriteAddressDto favoriteAddressDto, DbQueryCallback<Long> callback) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				long id = favoriteAddressDao.add(favoriteAddressDto);
				callback.processResult(id);
				addAddressesLiveData.postValue(favoriteAddressDao.get((int) id));
			}
		}).start();
	}
	
	@Override
	public void delete(FavoriteAddressDto favoriteAddressDto) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				favoriteAddressDao.delete(favoriteAddressDto);
				deleteAddressesLiveData.postValue(favoriteAddressDto);
			}
		}).start();
	}
	
	public MutableLiveData<FavoriteAddressDto> getAddAddressesLiveData() {
		return addAddressesLiveData;
	}
	
	public MutableLiveData<FavoriteAddressDto> getDeleteAddressesLiveData() {
		return deleteAddressesLiveData;
	}
}
