package com.example.kosplus.livedata;

import android.app.Application;
import android.support.annotation.NonNull;

import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;

import com.example.kosplus.model.OrderItems;
import com.example.kosplus.model.OrderItemsRepository;

import java.util.List;

public class OrderItemsLiveData extends AndroidViewModel {
    private final OrderItemsRepository repository;
    private final LiveData<List<OrderItems>> liveData;

    public OrderItemsLiveData(@NonNull Application application) {
        super(application);
        repository = new OrderItemsRepository(application);
        repository.preloadOrderItems();
        repository.startRealtimeSync();

        liveData = new MutableLiveData<>();;

    }

    public LiveData<List<OrderItems>> getLiveDataByOrderId(String orderId) {
        return repository.getByOrderId(orderId);
    }
}
