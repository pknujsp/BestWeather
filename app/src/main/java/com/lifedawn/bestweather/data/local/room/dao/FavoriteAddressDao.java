package com.lifedawn.bestweather.data.local.room.dao;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;

import com.lifedawn.bestweather.data.local.room.dto.FavoriteAddressDto;

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

	@Insert(entity = FavoriteAddressDto.class, onConflict = OnConflictStrategy.IGNORE)
	long add(FavoriteAddressDto favoriteAddressDto);
}