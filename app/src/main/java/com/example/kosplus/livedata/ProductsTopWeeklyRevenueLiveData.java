package com.example.kosplus.livedata;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

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
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class ProductsTopWeeklyRevenueLiveData extends ViewModel {
    private MutableLiveData<List<Products>> liveData;

    public MutableLiveData<List<Products>> getLiveData(){
        if (liveData == null){
            liveData = new MutableLiveData<>();
            loadData();
        }
        return liveData;
    }

    private void loadData() {
        Calendar calendar = Calendar.getInstance();
        calendar.set(Calendar.DAY_OF_WEEK, calendar.getFirstDayOfWeek()); // Thứ 2 đầu tuần
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfWeek = calendar.getTimeInMillis();

        calendar.add(Calendar.DAY_OF_WEEK, 6); // Chủ nhật cuối tuần
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

                    if (order.productId != null && order.quantity != null && order.productId != null) {
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

                // Sắp xếp doanh thu giảm dần
                List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(productRevenueMap.entrySet());
                sortedList.sort((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()));

                // Lấy danh sách top 10 sản phẩm
                List<String> topProductIds = new ArrayList<>();
                for (int i = 0; i < Math.min(10, sortedList.size()); i++) {
                    topProductIds.add(sortedList.get(i).getKey());
                }

                if (topProductIds.size() < 10) {
                    int needMore = 10 - topProductIds.size();

                    // Nếu muốn random thêm sản phẩm
                    fetchRandomProducts(needMore, topProductIds);
                }

                fetchTopProductDetails(topProductIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TOP_WEEKLY_REVENUE", "Lỗi: " + error.getMessage());
            }
        });
    }

    private void fetchRandomProducts(int needMore, List<String> weeklyTopProductIds) {
        DatabaseReference productsRef = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Products");

        productsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<String> allProducts = new ArrayList<>();

                for (DataSnapshot productSnap : snapshot.getChildren()) {
                    String productId = productSnap.getKey();
                    if (productId != null && !weeklyTopProductIds.contains(productId)) {
                        allProducts.add(productId);
                    }
                }

                // Random chọn thêm sản phẩm
                Collections.shuffle(allProducts); // xáo trộn danh sách
                List<String> finalIds = new ArrayList<>(weeklyTopProductIds);

                for (int i = 0; i < Math.min(needMore, allProducts.size()); i++) {
                    finalIds.add(allProducts.get(i));
                }

                fetchTopProductDetails(finalIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("RANDOM_PRODUCTS", "Lỗi: " + error.getMessage());
            }
        });
    }


    private void fetchTopProductDetails(List<String> topProductIds) {
        DatabaseReference productRef = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Products");

        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Products> topProducts = new ArrayList<>();
                for (DataSnapshot productSnap : snapshot.getChildren()) {
                    Products product = productSnap.getValue(Products.class);
                    if (product != null && topProductIds.contains(product.id)) {
                        topProducts.add(product);
                    }
                }

                liveData.setValue(topProducts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FETCH_PRODUCT", "Lỗi: " + error.getMessage());
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
