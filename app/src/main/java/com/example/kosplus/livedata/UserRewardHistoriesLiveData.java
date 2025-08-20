package com.example.kosplus.livedata;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.kosplus.datalocal.DataLocalManager;
import com.example.kosplus.model.RewardHistory;
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
import java.util.List;
import java.util.Locale;

public class UserRewardHistoriesLiveData extends ViewModel {
    private MutableLiveData<List<RewardHistory>> liveData;

    public MutableLiveData<List<RewardHistory>> getLiveData(){
        if (liveData == null){
            liveData = new MutableLiveData<>();
            loadData();
        }
        return liveData;
    }

    private void loadData() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("RewardHistories");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<RewardHistory> list = new ArrayList<>();
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault());
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.exists()) {
                        RewardHistory history = dataSnapshot.getValue(RewardHistory.class);
                        Log.d("UserRewardHistoriesLiveData", "onDataChange: " + history.toString());
                        if (history.userId.equals(DataLocalManager.getUid())) {
                            list.add(history);
                        }
                    }
                }

                // Sắp xếp giảm dần theo thời gian (mới nhất trước)
                Collections.sort(list, (o1, o2) -> {
                    try {
                        Date d1 = sdf.parse(o1.time);
                        Date d2 = sdf.parse(o2.time);
                        return d2.compareTo(d1); // Mới nhất lên đầu
                    } catch (ParseException e) {
                        return 0;
                    }
                });
                liveData.setValue(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void setLiveData(MutableLiveData<List<RewardHistory>> liveData) {
        this.liveData = liveData;
    }
}