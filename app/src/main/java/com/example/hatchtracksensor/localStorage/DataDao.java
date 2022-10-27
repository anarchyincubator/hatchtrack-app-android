package com.example.hatchtracksensor.localStorage;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Insert;
import androidx.room.OnConflictStrategy;
import androidx.room.Query;
import java.util.List;

@Dao
public interface DataDao {

    @Query("SELECT * FROM word_table ORDER BY word ASC")
    LiveData<List<DataTable>> getAlphabetizedWords();

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    void insert(DataTable word);

    @Query("DELETE FROM word_table")
    void deleteAll();
}
