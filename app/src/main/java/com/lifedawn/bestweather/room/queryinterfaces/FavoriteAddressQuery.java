package com.lifedawn.bestweather.room.queryinterfaces;

import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;

import java.util.List;

public interface FavoriteAddressQuery {
	void getAll(DbQueryCallback<List<FavoriteAddressDto>> callback);

	void get(int id, DbQueryCallback<FavoriteAddressDto> callback);

	void size(DbQueryCallback<Integer> callback);

	void contains(String latitude, String longitude, DbQueryCallback<Boolean> callback);

	void add(FavoriteAddressDto favoriteAddressDto, DbQueryCallback<Long> callback);

	void delete(FavoriteAddressDto favoriteAddressDto);

	void delete(FavoriteAddressDto favoriteAddressDto, DbQueryCallback<Boolean> callback);
}
