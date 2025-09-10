package com.example.kosplus.model;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MediatorLiveData;

import com.example.kosplus.datalocal.AppDatabase;
import com.example.kosplus.datalocal.DataLocalManager;
import com.example.kosplus.func.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicLong;

public class ProductsRepository {
    private final ProductsDao productsDao;
    private final DatabaseReference databaseReference;
    private ValueEventListener valueEventListener;
    private final Executor executor = Executors.newSingleThreadExecutor();
    public ProductsRepository(Application application) {
        AppDatabase db = AppDatabase.getInstance(application);
        productsDao = db.productsDao();
        databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Products");
    }
    public LiveData<List<Products>> getAllProducts() {
        return productsDao.getAll();
    }
    public LiveData<List<Products>> getProductsByType(String type) {
        return productsDao.getProductsByType(type);
    }
    public LiveData<Integer> getSoldQuantityByProduct(String productId) {
        return productsDao.getSoldQuantityByProduct(productId);
    }
    public LiveData<List<Products>> getTop10Products() {
        MediatorLiveData<List<Products>> result = new MediatorLiveData<>();

        LiveData<List<ProductSales>> topSalesLive = productsDao.getTop10BestSellingProducts();

        result.addSource(topSalesLive, productSalesList -> {
            if (productSalesList != null && !productSalesList.isEmpty()) {
                executor.execute(() -> {
                    List<String> ids = new ArrayList<>();
                    for (ProductSales ps : productSalesList) ids.add(ps.productId);

                    List<Products> productsList = productsDao.getProductsByIds(ids);

                    // Sắp xếp theo thứ tự totalSold
                    Map<String, Products> map = new HashMap<>();
                    for (Products p : productsList) map.put(p.id, p);

                    List<Products> sortedList = new ArrayList<>();
                    for (ProductSales ps : productSalesList) {
                        if (map.containsKey(ps.productId)) sortedList.add(map.get(ps.productId));
                    }

                    result.postValue(sortedList);
                });
            } else {
                result.postValue(new ArrayList<>());
            }
        });

        return result;
    }
    public LiveData<List<Products>> getTop10BestSellingProductsOfTime(long start, long end) {
        MediatorLiveData<List<Products>> result = new MediatorLiveData<>();

        // Lấy top 10 ProductSales trong khoảng thời gian start → end
        LiveData<List<ProductSales>> topSalesLive = productsDao.getTop10BestSellingProductsOfTime(start, end);

        result.addSource(topSalesLive, productSalesList -> {
            if (productSalesList != null && !productSalesList.isEmpty()) {
                executor.execute(() -> {
                    List<String> ids = new ArrayList<>();
                    for (ProductSales ps : productSalesList) ids.add(ps.productId);

                    // Lấy danh sách Products từ Room theo id
                    List<Products> productsList = productsDao.getProductsByIds(ids);

                    // Sắp xếp Products theo thứ tự totalSold từ ProductSales
                    Map<String, Products> map = new HashMap<>();
                    for (Products p : productsList) map.put(p.id, p);

                    List<Products> sortedList = new ArrayList<>();
                    for (ProductSales ps : productSalesList) {
                        if (map.containsKey(ps.productId)) sortedList.add(map.get(ps.productId));
                    }

                    result.postValue(sortedList);
                });
            } else {
                result.postValue(new ArrayList<>());
            }
        });

        return result;
    }
    public LiveData<List<Products>> getRandomProducts() {
        return productsDao.getRandomProducts();
    }
    public LiveData<List<Products>> getActivePromotions() {
        AtomicLong now = new AtomicLong();
        new Thread(() -> {
            long internetTime = Utils.getInternetTimeMillis();
            if (internetTime > 0) {
                String timeString = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault())
                        .format(new Date(internetTime));
                Log.d("REAL_TIME", "Thời gian Internet: " + timeString);
                now.set(internetTime);
            }
        }).start();
        if (now.get() == 0) {
            return productsDao.getActivePromotions(System.currentTimeMillis());
        } else {
            return productsDao.getActivePromotions(now.get());
        }
    }

    public void preloadProducts() {
        valueEventListener = databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(DataSnapshot dataSnapshot) {
                List<Products> products = new ArrayList<>();
                for (DataSnapshot snapshot : dataSnapshot.getChildren()) {
                    Products product = snapshot.getValue(Products.class);
                    products.add(product);
                }
                Executors.newSingleThreadExecutor().execute(() -> {
                    productsDao.clearAll();
                    productsDao.insertAll(products);
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void startRealtimeSync() {
        if (valueEventListener != null) return;
        valueEventListener = new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Products> products = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Products product = dataSnapshot.getValue(Products.class);
                    products.add(product);
                }
                Executors.newSingleThreadExecutor().execute(() -> {
                    productsDao.clearAll();
                    productsDao.insertAll(products);
                });
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        };
        databaseReference.addValueEventListener(valueEventListener);
    }
    public void stopRealtimeSync() {
        if (valueEventListener != null) databaseReference.removeEventListener(valueEventListener);
    }
}
