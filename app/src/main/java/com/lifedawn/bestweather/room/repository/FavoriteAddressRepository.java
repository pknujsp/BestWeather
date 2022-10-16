package com.lifedawn.bestweather.room.repository;

import android.content.Context;
import android.content.SharedPreferences;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.preference.PreferenceManager;

import com.lifedawn.bestweather.main.MyApplication;
import com.lifedawn.bestweather.room.AppDb;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dao.FavoriteAddressDao;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;
import com.lifedawn.bestweather.room.queryinterfaces.FavoriteAddressQuery;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

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
		executors.execute(new Runnable() {
			@Override
			public void run() {
				callback.processResult(favoriteAddressDao.getAll());
			}
		});
	}

	public LiveData<List<FavoriteAddressDto>> getAllData() {
		return favoriteAddressDao.getAllData();
	}

	@Override
	public void get(int id, DbQueryCallback<FavoriteAddressDto> callback) {
		executors.execute(new Runnable() {
			@Override
			public void run() {
				callback.processResult(favoriteAddressDao.get(id));

			}
		});
	}

	@Override
	public void size(DbQueryCallback<Integer> callback) {
		executors.execute(new Runnable() {
			@Override
			public void run() {
				callback.processResult(favoriteAddressDao.size());

			}
		});
	}

	@Override
	public void contains(String latitude, String longitude, DbQueryCallback<Boolean> callback) {
		executors.execute(new Runnable() {
			@Override
			public void run() {
				callback.processResult(favoriteAddressDao.contains(latitude, longitude) == 1);
			}
		});
	}

	@Override
	public void add(FavoriteAddressDto favoriteAddressDto, DbQueryCallback<Long> callback) {
		executors.execute(new Runnable() {
			@Override
			public void run() {
				long id = favoriteAddressDao.add(favoriteAddressDto);
				callback.processResult(id);
			}
		});
	}

	@Override
	public void delete(FavoriteAddressDto favoriteAddressDto) {
		executors.execute(new Runnable() {
			@Override
			public void run() {
				favoriteAddressDao.delete(favoriteAddressDto);

			}
		});
	}

	@Override
	public void delete(FavoriteAddressDto favoriteAddressDto, DbQueryCallback<Boolean> callback) {
		executors.execute(new Runnable() {
			@Override
			public void run() {
				favoriteAddressDao.delete(favoriteAddressDto);
				callback.onResultSuccessful(true);
			}
		});
	}

}
