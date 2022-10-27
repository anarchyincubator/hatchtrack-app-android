package com.example.hatchtracksensor.localStorage.NameViewModel;

import android.app.Application;
import android.arch.lifecycle.AndroidViewModel;

import androidx.lifecycle.LiveData;

import com.example.hatchtracksensor.localStorage.DataTable;
import com.example.hatchtracksensor.localStorage.dataRepository.DataRepository;
import java.util.List;


public class NameViewModel extends AndroidViewModel {

    private DataRepository mRepository;

    private final LiveData<List<DataTable>> mAllWords;

    public NameViewModel(Application application) {
        super(application);
        mRepository = new DataRepository(application);
        mAllWords = mRepository.getAllWords();
    }

    LiveData<List<DataTable>> getAllWords() {
        return mAllWords;
    }

    void insert(DataTable word) {
        mRepository.insert(word);
    }
}
