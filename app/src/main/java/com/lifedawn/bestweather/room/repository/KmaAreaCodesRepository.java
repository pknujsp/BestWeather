package com.lifedawn.bestweather.room.repository;

import android.content.Context;

import com.lifedawn.bestweather.data.MyApplication;
import com.lifedawn.bestweather.room.AppDb;
import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dao.KmaAreaCodesDao;
import com.lifedawn.bestweather.room.dto.KmaAreaCodeDto;
import com.lifedawn.bestweather.room.queryinterfaces.KmaAreaCodesQuery;

import java.util.List;

public class KmaAreaCodesRepository implements KmaAreaCodesQuery {
	private final KmaAreaCodesDao dao;

	private static KmaAreaCodesRepository INSTANCE;

	public static void initialize(Context context) {
		if (INSTANCE == null) {
			INSTANCE = new KmaAreaCodesRepository(context);
		}
	}

	public static KmaAreaCodesRepository getINSTANCE() {
		return INSTANCE;
	}

	public KmaAreaCodesRepository(Context context) {
		this.dao = AppDb.getInstance(context).kmaAreaCodesDao();
	}

	@Override
	public void getAreaCodes(double latitude, double longitude, DbQueryCallback<List<KmaAreaCodeDto>> callback) {
		MyApplication.getExecutorService().submit(new Runnable() {
			@Override
			public void run() {
				List<KmaAreaCodeDto> list = dao.getAreaCodes(latitude, longitude);
				if (list == null) {
					callback.onResultNoData();
				} else {
					callback.onResultSuccessful(list);
				}
			}
		});
	}

	@Override
	public void getCodeOfProximateArea(double latitude, double longitude, DbQueryCallback<KmaAreaCodeDto> callback) {

	}
}
