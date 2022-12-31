package com.lifedawn.bestweather.data.local.room.repository;

import android.content.Context;

import androidx.lifecycle.LiveData;

import com.lifedawn.bestweather.data.MyApplication;
import com.lifedawn.bestweather.data.local.room.AppDb;
import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.data.local.room.dao.FavoriteAddressDao;
import com.lifedawn.bestweather.data.local.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.data.local.room.queryinterfaces.FavoriteAddressQuery;

import java.util.List;
import java.util.concurrent.ExecutorService;

public class FavoriteAddressRepository implements FavoriteAddressQuery {
	private FavoriteAddressDao favoriteAddressDao;
	private ExecutorService executors = MyApplication.getExecutorService();

	private static FavoriteAddressRepository INSTANCE;

	public static void initialize(Context context) {
		if (INSTANCE == null) {
			INSTANCE = new FavoriteAddressRepository(context);
		}
	}

	public static FavoriteAddressRepository getINSTANCE() {
		return INSTANCE;
	}

	private FavoriteAddressRepository(Context context) {
		favoriteAddressDao = AppDb.getInstance(context).favoriteAddressDao();
	}


	@Override
	public void getAll(DbQueryCallback<List<FavoriteAddressDto>> callback) {
		executors.execute(() -> callback.processResult(favoriteAddressDao.getAll()));
	}

	public LiveData<List<FavoriteAddressDto>> getAllData() {
		return favoriteAddressDao.getAllData();
	}

	@Override
	public void get(int id, DbQueryCallback<FavoriteAddressDto> callback) {
		executors.execute(() -> callback.processResult(favoriteAddressDao.get(id)));
	}

	@Override
	public void size(DbQueryCallback<Integer> callback) {
		executors.execute(() -> callback.processResult(favoriteAddressDao.size()));
	}

	@Override
	public void contains(String latitude, String longitude, DbQueryCallback<Boolean> callback) {
		executors.execute(() -> callback.processResult(favoriteAddressDao.contains(latitude, longitude) == 1));
	}

	@Override
	public void add(FavoriteAddressDto favoriteAddressDto, DbQueryCallback<Long> callback) {
		executors.execute(() -> {
			long id = favoriteAddressDao.add(favoriteAddressDto);
			callback.processResult(id);
		});
	}

	@Override
	public void delete(FavoriteAddressDto favoriteAddressDto) {
		executors.execute(() -> favoriteAddressDao.delete(favoriteAddressDto));
	}

	@Override
	public void delete(FavoriteAddressDto favoriteAddressDto, DbQueryCallback<Boolean> callback) {
		executors.execute(() -> {
			favoriteAddressDao.delete(favoriteAddressDto);
			callback.onResultSuccessful(true);
		});
	}

}