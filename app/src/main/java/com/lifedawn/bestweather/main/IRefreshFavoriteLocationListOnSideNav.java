package com.lifedawn.bestweather.main;

import android.os.Bundle;

import java.io.Serializable;

public interface IRefreshFavoriteLocationListOnSideNav extends Serializable {
	void onRefreshedFavoriteLocationsList(String requestKey, Bundle bundle);
}
