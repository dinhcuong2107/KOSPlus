package com.example.kosplus.features;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.kosplus.datalocal.DataLocalManager;
import com.example.kosplus.model.Users;
import com.example.kosplus.model.UsersRepository;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class UsersManageVM extends AndroidViewModel {
    private final UsersRepository repository;
    private final LiveData<List<Users>> allUsers;
    // LiveData category
    private final MutableLiveData<String> selectedCategory = new MutableLiveData<>();

    // LiveData filtered dựa trên category
    public final LiveData<List<Users>> filteredUsers;

    public UsersManageVM(@NonNull Application application) {
        super(application);
        repository = new UsersRepository(application);
        selectedCategory.setValue("Tất cả");

        repository.preloadUsers();
        allUsers = repository.getAllUsers();
        repository.startRealtimeSync();

        // switchMap category → query Room
        filteredUsers = Transformations.switchMap(selectedCategory, category -> {
            if (category == null || category.equals("Tất cả")) {
                return allUsers;
            } else {
                return repository.getUsersByCategory(category);
            }
        });
    }

    public void setCategory(String category) {
        selectedCategory.setValue(category);
    }

    public LiveData<Integer> getCountAll() { return repository.getCountAll(); }
    public LiveData<Integer> getCountActive() { return repository.getCountActive(); }
    public LiveData<Integer> getCountBlocked() { return repository.getCountBlocked(); }
    public LiveData<Integer> getCountByRole(String role) { return repository.getCountByRole(role); }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.stopRealtimeSync();
    }
}