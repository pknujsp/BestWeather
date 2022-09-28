package com.lifedawn.bestweather.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import com.lifedawn.bestweather.room.callback.DbQueryCallback;
import com.lifedawn.bestweather.room.dto.FavoriteAddressDto;

import java.util.List;

@Dao
public interface FavoriteAddressDao {
	@Query("SELECT * FROM favorite_address_table")
	List<FavoriteAddressDto> getAll();

	@Query("SELECT * FROM favorite_address_table")
	LiveData<List<FavoriteAddressDto>> getAllData();

	@Query("SELECT count(*) FROM favorite_address_table")
	int size();

	@Query("SELECT * FROM favorite_address_table WHERE id = :id")
	FavoriteAddressDto get(int id);

	@Query("SELECT EXISTS (SELECT * FROM favorite_address_table WHERE latitude =:latitude AND longitude =:longitude) AS SUCCESS")
	int contains(String latitude, String longitude);

	@Delete
	void delete(FavoriteAddressDto favoriteAddressDto);

	@Insert(entity = FavoriteAddressDto.class)
	long add(FavoriteAddressDto favoriteAddressDto);
}
