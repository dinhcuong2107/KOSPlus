package com.example.kosplus.livedata;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.kosplus.model.Products;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ProductsLiveData extends ViewModel {
    private MutableLiveData<List<Products>> liveData;

    public MutableLiveData<List<Products>> getLiveData(){
        if (liveData == null){
            liveData = new MutableLiveData<>();
            loadData();
        }
        return liveData;
    }

    private void loadData() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Products");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Products> list = new ArrayList<>();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    Products product = dataSnapshot.getValue(Products.class);
                    list.add(product);
                }
                // Sắp xếp theo tên
                Collections.sort(list, new Comparator<Products>() {
                    @Override
                    public int compare(Products p1, Products p2) {
                        return p1.name.compareToIgnoreCase(p2.name);
                    }
                });
                liveData.setValue(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void setLiveData(MutableLiveData<List<Products>> liveData) {
        this.liveData = liveData;
    }
}
