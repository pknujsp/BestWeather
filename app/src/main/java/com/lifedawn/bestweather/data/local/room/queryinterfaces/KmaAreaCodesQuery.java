package com.lifedawn.bestweather.data.local.room.queryinterfaces;

import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.data.local.room.dto.KmaAreaCodeDto;

import java.util.List;

public interface KmaAreaCodesQuery {
	void getAreaCodes(double latitude, double longitude, DbQueryCallback<List<KmaAreaCodeDto>> callback);

	void getCodeOfProximateArea(double latitude, double longitude, DbQueryCallback<KmaAreaCodeDto> callback);

}
