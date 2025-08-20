package com.example.kosplus.livedata;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.example.kosplus.model.Orders;
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

public class OrdersLiveData extends ViewModel {
    private MutableLiveData<List<Orders>> liveData;
    private ObservableField<String> code = new ObservableField<>();

    public MutableLiveData<List<Orders>> getLiveData(){
        if (liveData == null){
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
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Orders");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Orders> list = new ArrayList<>();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    Orders order = dataSnapshot.getValue(Orders.class);
                    if (code.get() == null || code.get().isEmpty())
                    {
                        list.add(order);
                    } else if (order.address.split("-")[0].equals(code.get())) {
                        list.add(order);
                    }

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
                }

                liveData.setValue(list);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void setLiveData(MutableLiveData<List<Orders>> liveData) {
        this.liveData = liveData;
    }
}
