package com.example.kosplus.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kosplus.databinding.ItemCheckinCodeBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.Map;

public class CheckInCodeAdapter extends RecyclerView.Adapter<CheckInCodeAdapter.CheckinCodeViewHolder> {
    private Map<String, String> checkInCodeMap;


    public CheckInCodeAdapter(Map<String, String> checkInCodeMap) {
        this.checkInCodeMap = checkInCodeMap;
        notifyDataSetChanged();
    }

    public void updateData(Map<String, String> checkInCodeMap) {
        this.checkInCodeMap = checkInCodeMap;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public CheckinCodeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCheckinCodeBinding binding = ItemCheckinCodeBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CheckInCodeAdapter.CheckinCodeViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull CheckinCodeViewHolder holder, int position) {
        Map<String, String> checkInMap = new HashMap<>();

        String shopId = (String) checkInCodeMap.keySet().toArray()[position];
        String checkinCode = checkInCodeMap.get(shopId);

        holder.binding.textview.setText(shopId);

        CheckInStatusAdapter adapter = new CheckInStatusAdapter(new HashMap<>());
        holder.binding.recyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.binding.recyclerView.setAdapter(adapter);

        DatabaseReference checkinsRef = FirebaseDatabase.getInstance().getReference("KOS Plus").child("CheckIns").child(checkinCode);
        checkinsRef.addListenerForSingleValueEvent(new ValueEventListener() {

            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    for (DataSnapshot checkinSnapshot : snapshot.getChildren()) {
                        String checkinKey = checkinSnapshot.getKey();
                        String checkinValue = checkinSnapshot.getValue(String.class);

                        checkInMap.put(checkinKey, checkinValue);
                    }

                    // Sau khi có data => set adapter
                    adapter.updateData(checkInMap);

                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @Override
    public int getItemCount() {
        if (checkInCodeMap != null) {
            return checkInCodeMap.size();
        }
        return 0;
    }

    public class CheckinCodeViewHolder extends RecyclerView.ViewHolder {
        ItemCheckinCodeBinding binding;
        public CheckinCodeViewHolder(@NonNull ItemCheckinCodeBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
