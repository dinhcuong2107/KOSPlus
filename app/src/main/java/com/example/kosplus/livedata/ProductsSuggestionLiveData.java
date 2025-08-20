package com.example.kosplus.livedata;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.kosplus.datalocal.DataLocalManager;
import com.example.kosplus.model.Orders;
import com.example.kosplus.model.Products;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProductsSuggestionLiveData extends ViewModel {
    private MutableLiveData<List<Products>> liveData;

    public MutableLiveData<List<Products>> getLiveData() {
        if (liveData == null) {
            liveData = new MutableLiveData<>();
            loadUserTopProducts();
        }
        return liveData;
    }

    // B1. Lấy sản phẩm user đã mua nhiều nhất
    private void loadUserTopProducts() {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance()
                .getReference("KOS Plus")
                .child("Orders");

        ordersRef.orderByChild("userId").equalTo(DataLocalManager.getUid())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        Map<String, Integer> productCountMap = new HashMap<>();
                        for (DataSnapshot orderSnap : snapshot.getChildren()) {
                            Orders order = orderSnap.getValue(Orders.class);
                            if (order == null || order.productId == null || order.quantity == null) continue;

                            for (int i = 0; i < order.productId.size(); i++) {
                                String productId = order.productId.get(i);
                                int quantity = order.quantity.get(i);
                                int current = productCountMap.getOrDefault(productId, 0);
                                productCountMap.put(productId, current + quantity);
                            }
                        }

                        List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(productCountMap.entrySet());
                        sortedList.sort((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()));

                        List<String> userTopProductIds = new ArrayList<>();
                        for (int i = 0; i < Math.min(10, sortedList.size()); i++) {
                            userTopProductIds.add(sortedList.get(i).getKey());
                        }

                        if (userTopProductIds.size() >= 10) {
                            fetchProductDetails(userTopProductIds);
                        } else {
                            loadWeeklyTopProducts(userTopProductIds);
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("USER_SUGGESTION", error.getMessage());
                        loadWeeklyTopProducts(new ArrayList<>());
                    }
                });
    }

    // B2. Thêm sản phẩm bán chạy tuần
    private void loadWeeklyTopProducts(List<String> currentIds) {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek());
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfWeek = calendar.getTimeInMillis();

        calendar.add(Calendar.DAY_OF_WEEK, 6);
        calendar.set(Calendar.HOUR_OF_DAY, 23);
        calendar.set(Calendar.MINUTE, 59);
        calendar.set(Calendar.SECOND, 59);
        long endOfWeek = calendar.getTimeInMillis();

        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Orders");

        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Integer> productRevenueMap = new HashMap<>();

                for (DataSnapshot orderSnap : snapshot.getChildren()) {
                    Orders order = orderSnap.getValue(Orders.class);
                    if (order == null || order.createdTime == null) continue;

                    long createdMillis = parseTime(order.createdTime);
                    if (createdMillis < startOfWeek || createdMillis > endOfWeek) continue;

                    if (order.productId != null && order.quantity != null && order.price != null) {
                        for (int i = 0; i < order.productId.size(); i++) {
                            String productId = order.productId.get(i);
                            int quantity = order.quantity.get(i);
                            int price = order.price.get(i);
                            int revenue = quantity * price;
                            int current = productRevenueMap.getOrDefault(productId, 0);
                            productRevenueMap.put(productId, current + revenue);
                        }
                    }
                }

                List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(productRevenueMap.entrySet());
                sortedList.sort((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()));

                List<String> combinedIds = new ArrayList<>(currentIds);
                for (Map.Entry<String, Integer> entry : sortedList) {
                    if (combinedIds.size() >= 10) break;
                    if (!combinedIds.contains(entry.getKey())) {
                        combinedIds.add(entry.getKey());
                    }
                }

                if (combinedIds.size() >= 10) {
                    fetchProductDetails(combinedIds);
                } else {
                    fetchRandomProducts(10 - combinedIds.size(), combinedIds);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                fetchRandomProducts(10, currentIds);
            }
        });
    }

    // B3. Random sản phẩm nếu vẫn thiếu
    private void fetchRandomProducts(int needMore, List<String> existingIds) {
        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Products");

        productsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> allProducts = new ArrayList<>();
                for (DataSnapshot productSnap : snapshot.getChildren()) {
                    String productId = productSnap.getKey();
                    if (productId != null && !existingIds.contains(productId)) {
                        allProducts.add(productId);
                    }
                }

                Collections.shuffle(allProducts);
                List<String> finalIds = new ArrayList<>(existingIds);
                for (int i = 0; i < Math.min(needMore, allProducts.size()); i++) {
                    finalIds.add(allProducts.get(i));
                }

                fetchProductDetails(finalIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RANDOM_SUGGESTION", error.getMessage());
            }
        });
    }

    // Lấy thông tin sản phẩm từ danh sách id
    private void fetchProductDetails(List<String> productIds) {
        DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Products");

        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Products> products = new ArrayList<>();
                for (DataSnapshot productSnap : snapshot.getChildren()) {
                    Products product = productSnap.getValue(Products.class);
                    if (product != null && productIds.contains(product.id)) {
                        products.add(product);
                    }
                }
                liveData.setValue(products);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FETCH_SUGGESTION", error.getMessage());
            }
        });
    }

    private long parseTime(String timeString) {
        try {
            SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault());
            Date date = sdf.parse(timeString);
            return date != null ? date.getTime() : 0;
        } catch (ParseException e) {
            e.printStackTrace();
            return 0;
        }
    }

    public void setLiveData(MutableLiveData<List<Products>> liveData) {
        this.liveData = liveData;
    }
}
