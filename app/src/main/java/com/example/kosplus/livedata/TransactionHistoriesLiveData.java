package com.example.kosplus.livedata;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.kosplus.datalocal.DataLocalManager;
import com.example.kosplus.model.TransactionHistory;
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

public class TransactionHistoriesLiveData extends ViewModel {
    private MutableLiveData<List<TransactionHistory>> liveData;

    public MutableLiveData<List<TransactionHistory>> getLiveData(){
        if (liveData == null){
            liveData = new MutableLiveData<>();
            loadData();
        }
        return liveData;
    }

    private void loadData() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("TransactionHistories");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<TransactionHistory> list = new ArrayList<>();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    TransactionHistory transaction = dataSnapshot.getValue(TransactionHistory.class);
                    if (transaction.userId.equals(DataLocalManager.getUid())) {
                        list.add(transaction);
                    }
                }

                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault());
                Collections.sort(list, (o1, o2) -> {
                    try {
                        Date d1 = sdf.parse(o1.time);
                        Date d2 = sdf.parse(o2.time);
                        return d2.compareTo(d1); // mới nhất trước
                    } catch (ParseException e) {
                        e.printStackTrace();
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

    public void setLiveData(MutableLiveData<List<TransactionHistory>> liveData) {
        this.liveData = liveData;
    }
}