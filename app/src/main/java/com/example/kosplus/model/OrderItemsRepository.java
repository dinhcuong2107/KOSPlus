package com.example.kosplus.model;

import android.app.Application;
import android.support.annotation.NonNull;

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

public class OrderItemsRepository {
    private final OrderItemsDao orderItemsDao;
    private final DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;

    public OrderItemsRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        orderItemsDao = db.orderItemsDao();
        databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("OrderItems");
    }

    public LiveData<List<OrderItems>> getByOrderId(String orderId) {
        return orderItemsDao.getByOrderId(orderId);
    }

    public void preloadOrderItems() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;
                List<OrderItems> orderItemsList = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    OrderItems orderItem = child.getValue(OrderItems.class);
                    if (orderItem != null) orderItemsList.add(orderItem);
                }
                Executors.newSingleThreadExecutor().execute(() -> {
                    orderItemsDao.clearAll();
                    orderItemsDao.insertAll(orderItemsList);
                });
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

            }
        });
    }

    public void startRealtimeSync() {
        if (valueEventListener != null) return;
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<OrderItems> orderItemsList = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    OrderItems orderItem = child.getValue(OrderItems.class);
                    if (orderItem != null) orderItemsList.add(orderItem);
                }
                Executors.newSingleThreadExecutor().execute(() -> {
                    orderItemsDao.clearAll();
                    orderItemsDao.insertAll(orderItemsList);
                });
            }

            @Override
            public void onCancelled(@androidx.annotation.NonNull DatabaseError error) {

            }
        };
        databaseReference.addValueEventListener(valueEventListener);
    }

    public void stopRealtimeSync() {
        if (valueEventListener != null) {
            databaseReference.removeEventListener(valueEventListener);
        }
    }
}
