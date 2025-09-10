package com.example.kosplus.model;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;

import com.example.kosplus.datalocal.AppDatabase;
import com.example.kosplus.datalocal.DataLocalManager;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class OrdersRepository {
    private final OrdersDao ordersDao;
    private final DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;
    private final ExecutorService executorService = Executors.newSingleThreadExecutor();

    public OrdersRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        ordersDao = db.ordersDao();
        databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Orders");
    }

    public LiveData<List<Orders>> getAllOrders(String prefix) {
        if (DataLocalManager.getRole().equals("Customer")) {
            return ordersDao.getOrdersByUserId(DataLocalManager.getUid());
        }
        return ordersDao.getAllOrders(prefix);
    }

    public LiveData<Integer> getCountOrders(String prefix) {
        if (DataLocalManager.getRole().equals("Customer")) {
            return ordersDao.getCountOrdersByUserId(DataLocalManager.getUid());
        }
        return ordersDao.getCountOrders(prefix);
    }

    public LiveData<Integer> getCountOrdersCreated(String prefix) {
        if (DataLocalManager.getRole().equals("Customer")) {
            return ordersDao.getCountOrdersCreatedByUserID(DataLocalManager.getUid());
        }
        return ordersDao.getCountOrdersCreated(prefix);
    }

    public LiveData<Integer> getCountOrdersConfirmed(String prefix) {
        if (DataLocalManager.getRole().equals("Customer")) {
            return ordersDao.getCountOrdersConfirmedByUserID(DataLocalManager.getUid());
        }
        return ordersDao.getCountOrdersConfirmed(prefix);
    }

    public LiveData<Integer> getCountOrdersDelivery(String prefix) {
        if (DataLocalManager.getRole().equals("Customer")) {
            return ordersDao.getCountOrdersDeliveryByUserID(DataLocalManager.getUid());
        }
        return ordersDao.getCountOrdersDelivery(prefix);
    }

    public LiveData<Integer> getCountOrdersCompleted(String prefix) {
        if (DataLocalManager.getRole().equals("Customer")) {
            return ordersDao.getCountOrdersCompletedByUserID(DataLocalManager.getUid());
        }
        return ordersDao.getCountOrdersCompleted(prefix);
    }

    public LiveData<Integer> getCountOrdersCanceled(String prefix) {
        if (DataLocalManager.getRole().equals("Customer")) {
            return ordersDao.getCountOrdersCanceledByUserID(DataLocalManager.getUid());
        }
        return ordersDao.getCountOrdersCanceled(prefix);
    }

    public LiveData<List<ProductSalesTotal>> getProductsAndRevenueInMonth(long startOfMonth, long endOfMonth) {
        return ordersDao.getProductsAndRevenueInMonth(startOfMonth, endOfMonth);
    }

    public LiveData<List<Orders>> getOrdersByCategory(String category, String prefix) {
        return ordersDao.getOrdersByCategory(category, prefix);
    }
    public void startRealtimeSync() {
        if (valueEventListener != null) return;
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Orders> orders = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Orders order = snapshot.getValue(Orders.class);
                    orders.add(order);
                }
                executorService.execute(() -> {
                    ordersDao.insertAll(orders);
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        databaseReference.addValueEventListener(valueEventListener);
    }

    public void stopRealtimeSync() {
        if (valueEventListener != null) {
            databaseReference.removeEventListener(valueEventListener);
            valueEventListener = null;
        }
    }
}
