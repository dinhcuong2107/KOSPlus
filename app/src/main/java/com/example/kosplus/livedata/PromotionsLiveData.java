package com.example.kosplus.livedata;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.kosplus.model.Promotions;
import com.example.kosplus.model.PromotionsRepository;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class PromotionsLiveData extends AndroidViewModel {
    private final PromotionsRepository repository;
    private final LiveData<List<Promotions>> liveData;

    public PromotionsLiveData(@NonNull Application application) {
        super(application);
        repository = new PromotionsRepository(application);

        // Tải dữ liệu lần đầu
        repository.preloadPromotions();

        liveData = repository.getAllPromotions();

        // Bật sync realtime
        repository.startRealtimeSync();
    }

    public LiveData<List<Promotions>> getLiveData() {
        return liveData;
    }
}
