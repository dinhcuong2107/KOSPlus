package com.example.kosplus.model;

import android.app.Application;
import android.support.annotation.NonNull;
import android.util.Log;

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

public class UsersRepository {
    private final UsersDao usersDao;
    private final DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;

    public UsersRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        usersDao = db.usersDao();
        databaseReference = FirebaseDatabase.getInstance()
                .getReference("KOS Plus")
                .child("Users");
    }

    // Lấy từ Room
    public LiveData<List<Users>> getAllUsers() {
        return usersDao.getAllUsers();
    }
    public LiveData<List<Users>> getUsersByCategory(String category) {
        return usersDao.getUsersByCategory(category);
    }

    public LiveData<Integer> getCountAll() { return usersDao.getCountAll(); }
    public LiveData<Integer> getCountActive() { return usersDao.getCountActive(); }
    public LiveData<Integer> getCountBlocked() { return usersDao.getCountBlocked(); }
    public LiveData<Integer> getCountByRole(String role) { return usersDao.getCountByRole(role); }


    // Preload once
    public void preloadUsers() {
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) return;
                List<Users> userList = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Users user = child.getValue(Users.class);
                    if (user != null) userList.add(user);
                }
                Executors.newSingleThreadExecutor().execute(() -> {
                    usersDao.clearAll();
                    usersDao.insertAll(userList);
                });
                Log.d("PRELOAD_USERS", "Preload: " + userList.size() + " users");
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("PRELOAD_USERS", "Firebase error: " + error.getMessage());
            }
        });
    }

    // Realtime sync
    public void startRealtimeSync() {
        if (valueEventListener != null) return;

        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Users> userList = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Users user = child.getValue(Users.class);
                    if (user != null) userList.add(user);
                }
                Executors.newSingleThreadExecutor().execute(() -> {
                    usersDao.clearAll();
                    usersDao.insertAll(userList);
                });
                Log.d("REALTIME_SYNC", "Users synced: " + userList.size());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("REALTIME_SYNC", error.getMessage());
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
