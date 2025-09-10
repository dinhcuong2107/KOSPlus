package com.example.kosplus.livedata;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.Transformations;
import androidx.lifecycle.ViewModel;

import com.example.kosplus.datalocal.DataLocalManager;
import com.example.kosplus.model.OrderItems;
import com.example.kosplus.model.Orders;
import com.example.kosplus.model.OrdersRepository;
import com.example.kosplus.model.ProductSalesTotal;
import com.example.kosplus.model.Users;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class OrdersLiveData extends AndroidViewModel {
    private final OrdersRepository repository;
    private final MediatorLiveData<List<Orders>> filteredOrders = new MediatorLiveData<>();
    private final MutableLiveData<String> selectedCategory = new MutableLiveData<>();
    private final MutableLiveData<String> selectedCode = new MutableLiveData<>();

    private LiveData<List<Orders>> currentOrders;

    public OrdersLiveData(@NonNull Application application) {
        super(application);
        repository = new OrdersRepository(application);

        selectedCategory.setValue("Tất cả");
        selectedCode.setValue("");

        repository.startRealtimeSync();

        filteredOrders.addSource(selectedCategory, category -> loadFilteredOrders());
        filteredOrders.addSource(selectedCode, code -> loadFilteredOrders());
    }

    public void setCategory(String category) { selectedCategory.setValue(category);}
    public void setCode(String code) { selectedCode.setValue(code);}

    private void loadFilteredOrders() {
        String category = selectedCategory.getValue();
        String code = selectedCode.getValue();

        LiveData<List<Orders>> source;

        if (category == null || category.equals("Tất cả")) {
            source = repository.getAllOrders(code);
        } else {
            source = repository.getOrdersByCategory(category, code);
        }

        if (currentOrders != null) {
            filteredOrders.removeSource(currentOrders);
        }
        currentOrders = source;
        filteredOrders.addSource(currentOrders, orders -> {
            filteredOrders.setValue(orders);
        });
    }

    public LiveData<List<Orders>> getFilteredOrders() {
        return filteredOrders;
    }
    public LiveData<Integer> getCountOrdersAll() {
        return repository.getCountOrders(selectedCode.getValue());
    }
    public LiveData<Integer> getCountOrdersCreated() {
        return repository.getCountOrdersCreated(selectedCode.getValue());
    }
    public LiveData<Integer> getCountOrdersConfirmed() {
        return repository.getCountOrdersConfirmed(selectedCode.getValue());
    }
    public LiveData<Integer> getCountOrdersDelivery() {
        return repository.getCountOrdersDelivery(selectedCode.getValue());
    }
    public LiveData<Integer> getCountOrdersCompleted() {
        return repository.getCountOrdersCompleted(selectedCode.getValue());
    }
    public LiveData<Integer> getCountOrdersCanceled() {
        return repository.getCountOrdersCanceled(selectedCode.getValue());
    }

    public LiveData<List<ProductSalesTotal>> getProductsAndRevenueInMonth(long startOfMonth, long endOfMonth) {
        return repository.getProductsAndRevenueInMonth(startOfMonth, endOfMonth);
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        repository.stopRealtimeSync();
    }
}
