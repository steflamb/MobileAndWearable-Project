package com.example.pathsocial;

import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;

import java.util.List;
import java.util.Map;

@Dao
public interface TrailDao {
    @Query("SELECT * FROM trail")
    List<Trail> getAll();

    @Query("SELECT * FROM trail WHERE pathid IN (:pathId)")
    List<Trail> loadAllByIds(int[] pathId);

//    @Query("SELECT strftime('%Y-%m-%d', datetime), SUM(steps)  FROM trail GROUP BY strftime('%Y-%m-%d', datetime) ORDER BY strftime('%Y-%m-%d', datetime) ASC")
//    Map<String, Integer> getStepsDate();

    @Insert
    void insertAll(Trail... trails);

    @Delete
    void delete(Trail trail);
}