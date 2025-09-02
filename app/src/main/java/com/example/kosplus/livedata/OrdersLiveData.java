package com.example.kosplus.livedata;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.kosplus.datalocal.DataLocalManager;
import com.example.kosplus.model.Orders;
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

public class OrdersLiveData extends ViewModel {
    private MutableLiveData<List<Orders>> liveData;
    private ObservableField<String> code = new ObservableField<>();
    private List<Orders> list = new ArrayList<>(); // dữ liệu gốc
    private String currentCategory = "Tất cả"; // mặc định

    public MutableLiveData<List<Orders>> getLiveData() {
        if (liveData == null) {
            liveData = new MutableLiveData<>();
            loadData();
        }
        return liveData;
    }

    public void setCode(String s) {
        code.set(s);
        loadData();
    }

    private void loadData() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("KOS Plus").child("Orders");

        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list = new ArrayList<>();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    Orders order = dataSnapshot.getValue(Orders.class);
                    if (order == null) continue;

                    if (code.get() == null || code.get().isEmpty()) {
                        if (DataLocalManager.getRole().equals("Customer")) {
                            if (order.userId.equals(DataLocalManager.getUid())) {
                                list.add(order);
                            }
                        } else {
                            list.add(order);
                        }
                    } else if (order.address.split("-")[0].equals(code.get())) {
                        if (DataLocalManager.getRole().equals("Customer")) {
                            if (order.userId.equals(DataLocalManager.getUid())) {
                                list.add(order);
                            }
                        } else {
                            list.add(order);
                        }
                    }
                }

                // Sort theo thời gian tạo
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault());
                Collections.sort(list, (o1, o2) -> {
                    try {
                        Date d1 = sdf.parse(o1.createdTime);
                        Date d2 = sdf.parse(o2.createdTime);
                        return d2.compareTo(d1); // mới nhất trước
                    } catch (ParseException e) {
                        e.printStackTrace();
                        return 0;
                    }
                });

                // ✅ Quan trọng: luôn filter lại theo category hiện tại
                filterByCategory(currentCategory);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    public void filterByCategory(String category) {
        currentCategory = category; // nhớ lại category đã chọn
        List<Orders> filteredList = new ArrayList<>();

        switch (category) {
            case "Tất cả":
                filteredList.addAll(list);
                break;
            case "Chờ xác nhận":
                for (Orders orders : list) {
                    if ((orders.confirmedTime == null || orders.confirmedTime.isEmpty())
                            && (orders.canceledTime == null || orders.canceledTime.isEmpty())) {
                        filteredList.add(orders);
                    }
                }
                break;
            case "Chờ lấy hàng":
                for (Orders orders : list) {
                    if (orders.confirmedTime != null && !orders.confirmedTime.isEmpty()
                            && (orders.deliveryTime == null || orders.deliveryTime.isEmpty())
                            && (orders.canceledTime == null || orders.canceledTime.isEmpty())) {
                        filteredList.add(orders);
                    }
                }
                break;
            case "Đang giao":
                for (Orders orders : list) {
                    if (orders.deliveryTime != null && !orders.deliveryTime.isEmpty()
                            && (orders.completedTime == null || orders.completedTime.isEmpty())
                            && (orders.canceledTime == null || orders.canceledTime.isEmpty())) {
                        filteredList.add(orders);
                    }
                }
                break;
            case "Hoàn thành":
                for (Orders orders : list) {
                    if (orders.completedTime != null && !orders.completedTime.isEmpty()) {
                        filteredList.add(orders);
                    }
                }
                break;
            case "Đã hủy":
                for (Orders orders : list) {
                    if (orders.canceledTime != null && !orders.canceledTime.isEmpty()) {
                        filteredList.add(orders);
                    }
                }
                break;
        }

        liveData.setValue(filteredList);
    }

    public Map<String, Integer> countAllCategories() {
        Map<String, Integer> counts = new HashMap<>();
        int tatCa = list.size();
        int choXacNhan = 0, choLayHang = 0, dangGiao = 0, hoanThanh = 0, daHuy = 0;

        for (Orders orders : list) {
            if ((orders.confirmedTime == null || orders.confirmedTime.isEmpty())
                    && (orders.canceledTime == null || orders.canceledTime.isEmpty())) {
                choXacNhan++;
            } else if (orders.confirmedTime != null && !orders.confirmedTime.isEmpty()
                    && (orders.deliveryTime == null || orders.deliveryTime.isEmpty())
                    && (orders.canceledTime == null || orders.canceledTime.isEmpty())) {
                choLayHang++;
            } else if (orders.deliveryTime != null && !orders.deliveryTime.isEmpty()
                    && (orders.completedTime == null || orders.completedTime.isEmpty())
                    && (orders.canceledTime == null || orders.canceledTime.isEmpty())) {
                dangGiao++;
            } else if (orders.completedTime != null && !orders.completedTime.isEmpty()) {
                hoanThanh++;
            } else if (orders.canceledTime != null && !orders.canceledTime.isEmpty()) {
                daHuy++;
            }
        }

        counts.put("Tất cả", tatCa);
        counts.put("Chờ xác nhận", choXacNhan);
        counts.put("Chờ lấy hàng", choLayHang);
        counts.put("Đang giao", dangGiao);
        counts.put("Hoàn thành", hoanThanh);
        counts.put("Đã hủy", daHuy);

        return counts;
    }

}
