package com.lifedawn.bestweather.room.queryinterfaces;

import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;

import java.util.List;

public interface FavoriteAddressQuery {
	void getAll(DbQueryCallback<List<FavoriteAddressDto>> callback);

	void contains(String latitude, String longitude, DbQueryCallback<Integer> callback);

	void add(FavoriteAddressDto favoriteAddressDto, DbQueryCallback<Long> callback);

	void delete(FavoriteAddressDto favoriteAddressDto);
}
