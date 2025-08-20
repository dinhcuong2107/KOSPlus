package com.example.kosplus.livedata;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.kosplus.model.LuckyRewards;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class LuckyRewardsLiveData extends ViewModel {
    private MutableLiveData<List<LuckyRewards>> liveData;

    public MutableLiveData<List<LuckyRewards>> getLiveData(){
        if (liveData == null){
            liveData = new MutableLiveData<>();
            loadData();
        }
        return liveData;
    }

    private void loadData() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("LuckyRewards");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<LuckyRewards> list = new ArrayList<>();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    LuckyRewards luckyReward = dataSnapshot.getValue(LuckyRewards.class);
                    list.add(luckyReward);
                }

                liveData.setValue(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void setLiveData(MutableLiveData<List<LuckyRewards>> liveData) {
        this.liveData = liveData;
    }
}
