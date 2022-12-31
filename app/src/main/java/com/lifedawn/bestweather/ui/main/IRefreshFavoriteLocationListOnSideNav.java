package com.lifedawn.bestweather.ui.main;

import android.os.Bundle;

import androidx.annotation.Nullable;

import com.lifedawn.bestweather.data.local.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.data.local.room.dto.FavoriteAddressDto;

import java.io.Serializable;
import java.util.List;

public interface IRefreshFavoriteLocationListOnSideNav extends Serializable {
	void onRefreshedFavoriteLocationsList(String requestKey, Bundle bundle);

	void refreshFavorites(DbQueryCallback<List<FavoriteAddressDto>> callback);

	void createLocationsList(List<FavoriteAddressDto> result);

	void onResultMapFragment(@Nullable FavoriteAddressDto newFavoriteAddressDto);
}
