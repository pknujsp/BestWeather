package com.lifedawn.bestweather.room.queryinterfaces;

import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.KmaAreaCodeDto;

import java.util.List;

public interface KmaAreaCodesQuery {
	void getAreaCodes(double latitude, double longitude, DbQueryCallback<List<KmaAreaCodeDto>> callback);

	void getCodeOfProximateArea(double latitude, double longitude, DbQueryCallback<KmaAreaCodeDto> callback);

}
