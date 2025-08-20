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
import java.util.List;

public class UsersManageVM extends ViewModel {
    private final MutableLiveData<List<Users>> userList = new MutableLiveData<>();
    private List<Users> list = new ArrayList<>();
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
                userList.setValue(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Users Manage", "Lỗi: " + error.getMessage());
            }
        });
    }

    public void filterByCategory(String category) {

        List<Users> filteredList = new ArrayList<>();
        if (category.equals("All")) {
            filteredList.addAll(list);  // Trả lại toàn bộ dữ liệu
        } else if (category.equals("Hoạt động")){
            for (Users users : list) {
                if (users.status) {
                    filteredList.add(users);
                }
            }
        }else if (category.equals("Khóa")) {
            for (Users users : list) {
                if (!users.status) {
                    filteredList.add(users);
                }
            }
        }else {
            for (Users users : list) {
                if (users.role.equalsIgnoreCase(category)) {
                    filteredList.add(users);
                }
            }

        }
        userList.setValue(filteredList); // Cập nhật danh sách đã lọc
    }
}