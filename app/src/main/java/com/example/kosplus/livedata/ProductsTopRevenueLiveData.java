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

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ProductsTopRevenueLiveData extends ViewModel {
    private MutableLiveData<List<Products>> liveData;

    public MutableLiveData<List<Products>> getLiveData(){
        if (liveData == null){
            liveData = new MutableLiveData<>();
            loadData();
        }
        return liveData;
    }

    private void loadData() {
        DatabaseReference ordersRef = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Orders");
        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, Integer> productSales = new HashMap<>();
                for (DataSnapshot orderSnap : snapshot.getChildren()) {
                    Orders order = orderSnap.getValue(Orders.class);
                    if (order != null && order.productId != null && order.quantity != null) {
                        for (int i = 0; i < order.productId.size(); i++) {
                            String productId = order.productId.get(i);
                            int quantity = order.quantity.get(i);

                            int currentTotal = productSales.getOrDefault(productId, 0);
                            productSales.put(productId, currentTotal + quantity);
                        }
                    }
                }

                // Sắp xếp theo doanh số giảm dần
                List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(productSales.entrySet());
                sortedList.sort((e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()));

                // Lấy top 10
                List<String> topProductIds = new ArrayList<>();
                for (int i = 0; i < Math.min(10, sortedList.size()); i++) {
                    topProductIds.add(sortedList.get(i).getKey());
                }

                fetchTopProductDetails(topProductIds);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("TOP_PRODUCTS", "Lỗi: " + error.getMessage());
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

                // Cập nhật UI (RecyclerView chẳng hạn)
                liveData.setValue(topProducts);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("FETCH_PRODUCT", "Lỗi: " + error.getMessage());
            }
        });
    }



    public void setLiveData(MutableLiveData<List<Products>> liveData) {
        this.liveData = liveData;
    }
}
