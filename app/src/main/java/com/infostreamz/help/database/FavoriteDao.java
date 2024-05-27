package com.infostreamz.help.database;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import androidx.room.Update;

import java.util.List;

/**
 * Created by delaroy on 9/6/18.
 */

@Dao
public interface FavoriteDao {

    @Query("SELECT * FROM favoritetable")
    LiveData<List<FavoriteEntry>> loadAllFavorite();

    @Query("SELECT * FROM favoritetable WHERE title = :title")
    List<FavoriteEntry> loadAll(String title);

    @Insert
    void insertFavorite(FavoriteEntry favoriteEntry);

    @Update(onConflict = OnConflictStrategy.REPLACE)
    void updateFavorite(FavoriteEntry favoriteEntry);

    @Delete
    void deleteFavorite(FavoriteEntry favoriteEntry);

    @Query("DELETE FROM favoritetable WHERE movieid = :movie_id")
    void deleteFavoriteWithId(int movie_id);

    @Query("SELECT * FROM favoritetable WHERE id = :id")
    LiveData<FavoriteEntry> loadFavoriteById(int id);
}
