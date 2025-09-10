package com.example.kosplus.livedata;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.kosplus.datalocal.DataLocalManager;
import com.example.kosplus.model.ItemCarts;
import com.example.kosplus.model.ItemCartsRepository;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;
import java.util.function.Consumer;

public class ItemCartsLiveData extends AndroidViewModel {
    private final ItemCartsRepository repository;
    public ItemCartsLiveData(@NonNull Application application) {
        super(application);
        repository = new ItemCartsRepository(application);
    }
    public LiveData<List<ItemCarts>> getLiveData() {
        return repository.getAllCartItems();
    }
    public void addToCart(String productId, Consumer<Boolean> callback) {
        repository.addToCart(productId, callback);
    }
    public void clearAll() {
        repository.clearAll();
    }
    public void clearItem(String productId) {
        repository.clearItem(productId);
    }


}