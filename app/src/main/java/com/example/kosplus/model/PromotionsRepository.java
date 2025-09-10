package com.example.kosplus.model;

import android.app.Application;

import androidx.lifecycle.LiveData;

import com.example.kosplus.datalocal.AppDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executors;

public class PromotionsRepository {
    private final PromotionsDao promotionsDao;
    private final DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;

    public PromotionsRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        promotionsDao = db.promotionsDao();
        databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Promotions");
    }
    public LiveData<List<Promotions>> getAllPromotions() {
        return promotionsDao.getAll();
    }
    public void insertAllPromotions(List<Promotions> promotions) {
        promotionsDao.insertAll(promotions);
    }
    public void clearAllPromotions() {
        promotionsDao.clearAll();
    }
    public void preloadPromotions() {
        valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Promotions> promotions = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Promotions promotion = snapshot.getValue(Promotions.class);
                    promotions.add(promotion);
                }
                Executors.newSingleThreadExecutor().execute(() -> {
                    promotionsDao.clearAll();
                    promotionsDao.insertAll(promotions);
                });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        });
    }
    public void startRealtimeSync() {
        if (valueEventListener != null) return;
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Promotions> promotions = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Promotions promotion = snapshot.getValue(Promotions.class);
                    promotions.add(promotion);
                }
                Executors.newSingleThreadExecutor().execute(() -> {
                    promotionsDao.clearAll();
                    promotionsDao.insertAll(promotions);
                });
            }
            @Override
            public void onCancelled(DatabaseError databaseError) {
            }
        };
            databaseReference.addValueEventListener(valueEventListener);
    }
    public void stopRealtimeSync() {
        if (valueEventListener != null) databaseReference.removeEventListener(valueEventListener);
    }
}
