package com.example.kosplus.model;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.example.kosplus.datalocal.AppDatabase;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

public class NotificationsRepository {
    private final NotificationsDao notificationsDao;
    private final DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;
    private final Executor executor = Executors.newSingleThreadExecutor();

    public NotificationsRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        notificationsDao = db.notificationsDao();
        databaseReference = FirebaseDatabase.getInstance()
                .getReference("KOS Plus")
                .child("Notifications");
    }

    // ------------------- QUERIES -------------------

    public LiveData<List<Notifications>> getAllNotifications(String userId, int limit) {
        return notificationsDao.getAll(userId, limit);
    }

    public LiveData<Integer> getUnreadCount(String userId) {
        return notificationsDao.getUnreadCount(userId);
    }

    // ------------------- FIREBASE SYNC -------------------

    // Load dữ liệu ban đầu 1 lần
    public void preloadNotifications() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;

                List<Notifications> notificationList = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Notifications notification = child.getValue(Notifications.class);
                    if (notification != null) notificationList.add(notification);
                }

                executor.execute(() -> {
                    notificationsDao.clearAll();
                    notificationsDao.insertAll(notificationList);
                });

                Log.d("PRELOAD_NOTIFICATIONS", "Preloaded: " + notificationList.size() + " notifications");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    // Đồng bộ realtime
    public void startRealtimeSync() {
        if (valueEventListener != null) return; // tránh add 2 lần

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Notifications> notificationList = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Notifications notification = child.getValue(Notifications.class);
                    if (notification != null) notificationList.add(notification);
                }

                executor.execute(() -> {
                    notificationsDao.clearAll();
                    notificationsDao.insertAll(notificationList);
                });

                Log.d("REALTIME_SYNC", "Synced: " + notificationList.size() + " notifications");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        };

        databaseReference.addValueEventListener(valueEventListener);
    }

    // Dừng đồng bộ
    public void stopRealtimeSync() {
        if (valueEventListener != null) {
            databaseReference.removeEventListener(valueEventListener);
            valueEventListener = null;
        }
    }
}

