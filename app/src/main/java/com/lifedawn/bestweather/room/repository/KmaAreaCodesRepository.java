package com.lifedawn.bestweather.room.repository;

import android.content.Context;

import com.lifedawn.bestweather.room.AppDb;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dao.KmaAreaCodesDao;
import com.lifedawn.bestweather.room.dto.KmaAreaCodeDto;
import com.lifedawn.bestweather.room.queryinterfaces.KmaAreaCodesQuery;

import java.util.List;

public class KmaAreaCodesRepository implements KmaAreaCodesQuery {
	private KmaAreaCodesDao kmaAreaCodesDao;

	public KmaAreaCodesRepository(Context context) {
		this.kmaAreaCodesDao = AppDb.getInstance(context).kmaAreaCodesDao();
	}

	@Override
	public void getAreaCodes(double latitude, double longitude, DbQueryCallback<List<KmaAreaCodeDto>> callback) {
		new Thread(new Runnable() {
			@Override
			public void run() {
				List<KmaAreaCodeDto> list = kmaAreaCodesDao.getAreaCodes(latitude, longitude);
				if (list == null) {
					callback.onResultNoData();
				} else {
					callback.onResultSuccessful(list);
				}
			}
		}).start();
	}

	@Override
	public void getCodeOfProximateArea(double latitude, double longitude, DbQueryCallback<KmaAreaCodeDto> callback) {

	}
}
