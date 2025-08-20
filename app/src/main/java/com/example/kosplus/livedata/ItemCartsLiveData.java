package com.example.kosplus.livedata;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.kosplus.datalocal.DataLocalManager;
import com.example.kosplus.model.ItemCarts;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class ItemCartsLiveData extends ViewModel {
    private MutableLiveData<List<ItemCarts>> liveData;

    public MutableLiveData<List<ItemCarts>> getLiveData(){
        if (liveData == null){
            liveData = new MutableLiveData<>();
            loadData();
        }
        return liveData;
    }

    private void loadData() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Carts");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<ItemCarts> list = new ArrayList<>();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    ItemCarts cart = dataSnapshot.getValue(ItemCarts.class);
                    if (cart.userId.equals(DataLocalManager.getUid()))
                    {
                        list.add(cart);
                    }
                }
                liveData.setValue(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
    }

    public void setLiveData(MutableLiveData<List<ItemCarts>> liveData) {
        this.liveData = liveData;
    }
}
