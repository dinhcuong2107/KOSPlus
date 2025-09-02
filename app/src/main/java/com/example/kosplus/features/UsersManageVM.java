package com.example.kosplus.features;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.kosplus.datalocal.DataLocalManager;
import com.example.kosplus.model.Users;
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

public class UsersManageVM extends ViewModel {
    private final MutableLiveData<List<Users>> userList = new MutableLiveData<>();
    private List<Users> list = new ArrayList<>();
    private String currentCategory = "Tất cả";
    public UsersManageVM() {
        loadUsers();
    }

    public LiveData<List<Users>> getUserList() {
        return userList;
    }

    private void loadUsers() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                list.clear();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Log.e("Users Manage", "UserID: " + dataSnapshot.getKey());
                    Users users = dataSnapshot.getValue(Users.class);

                    if (DataLocalManager.getRole().equals("Admin")) {
                        list.add(users);
                    }
                    if (DataLocalManager.getRole().equals("Manager")) {
                        if (users.role.equals("Staff") || users.role.equals("Customer")) {
                            list.add(users);
                        }
                    }
                    if (DataLocalManager.getRole().equals("Staff")) {
                        if (users.role.equals("Customer")) {
                            list.add(users);
                        }
                    }

                }

                // Sắp xếp theo tên (ascending)
                Collections.sort(list, (u1, u2) -> u1.fullname.compareToIgnoreCase(u2.fullname));

                userList.setValue(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Users Manage", "Lỗi: " + error.getMessage());
            }
        });
    }

    public void filterByCategory(String category) {
        currentCategory = category;
        List<Users> filteredList = new ArrayList<>();

        switch (category) {
            case "Tất cả":
                filteredList.addAll(list);
                break;
            case "Hoạt động":
                for (Users u : list) {
                    if (u.status) filteredList.add(u);
                }
                break;
            case "Khóa":
                for (Users u : list) {
                    if (!u.status) filteredList.add(u);
                }
                break;
            case "Admin":
                for (Users u : list) {
                    if ("admin".equalsIgnoreCase(u.role)) filteredList.add(u);
                }
                break;
            case "Manager":
                for (Users u : list) {
                    if ("manager".equalsIgnoreCase(u.role)) filteredList.add(u);
                }
                break;
            case "Staff":
                for (Users u : list) {
                    if ("staff".equalsIgnoreCase(u.role)) filteredList.add(u);
                }
                break;
            case "Customer":
                for (Users u : list) {
                    if ("customer".equalsIgnoreCase(u.role)) filteredList.add(u);
                }
                break;
        }

        userList.setValue(filteredList); // Cập nhật danh sách đã lọc
    }

    // Đếm số lượng từng loại
    public Map<String, Integer> countAllUserCategories() {
        Map<String, Integer> counts = new HashMap<>();
        int tatCa = list.size();
        int hoatDong = 0, khoa = 0, admin = 0, manager = 0, staff = 0, customer = 0;

        for (Users user : list) {
            if (user.status) hoatDong++; else khoa++;

            switch (user.role.toLowerCase()) {
                case "admin": admin++; break;
                case "manager": manager++; break;
                case "staff": staff++; break;
                case "customer": customer++; break;
            }
        }

        counts.put("Tất cả", tatCa);
        counts.put("Hoạt động", hoatDong);
        counts.put("Khóa", khoa);
        counts.put("Admin", admin);
        counts.put("Manager", manager);
        counts.put("Staff", staff);
        counts.put("Customer", customer);

        return counts;
    }
}