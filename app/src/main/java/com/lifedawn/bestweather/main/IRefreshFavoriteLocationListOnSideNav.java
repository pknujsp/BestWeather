package com.lifedawn.bestweather.main;

import android.os.Bundle;

import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;

import java.io.Serializable;
import java.util.List;

public interface IRefreshFavoriteLocationListOnSideNav extends Serializable {
	void onRefreshedFavoriteLocationsList(String requestKey, Bundle bundle);

	void refreshFavorites(DbQueryCallback<List<FavoriteAddressDto>> callback);
}
