package com.example.hatchtracksensor.localStorage.dataRepository;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.hatchtracksensor.localStorage.DataDao;
import com.example.hatchtracksensor.localStorage.DataRoomDatabase;
import com.example.hatchtracksensor.localStorage.DataTable;
import java.util.List;

public class DataRepository {

    public DataDao mWordDao;
    public LiveData<List<DataTable>> mAllWords;

    // Note that in order to unit test the WordRepository, you have to remove the Application
    // dependency. This adds complexity and much more code, and this sample is not about testing.
    // See the BasicSample in the android-architecture-components repository at
    // https://github.com/googlesamples
    public DataRepository(Application application) {
        DataRoomDatabase db = DataRoomDatabase.getDatabase(application);
        mWordDao = db.wordDao();
        mAllWords = mWordDao.getAlphabetizedWords();
    }

    // Room executes all queries on a separate thread.
    // Observed LiveData will notify the observer when the data has changed.
    public LiveData<List<DataTable>> getAllWords() {
        return mAllWords;
    }

    // You must call this on a non-UI thread or your app will throw an exception. Room ensures
    // that you're not doing any long running operations on the main thread, blocking the UI.
    public void insert(DataTable data) {
        DataRoomDatabase.databaseWriteExecutor.execute(() -> {
            mWordDao.insert(data);
        });
    }
}
