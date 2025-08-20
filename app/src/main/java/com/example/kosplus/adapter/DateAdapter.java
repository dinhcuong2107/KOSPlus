package com.example.kosplus.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kosplus.databinding.ItemDateBinding;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DateAdapter extends RecyclerView.Adapter<DateAdapter.ItemDateViewHolder> {
    private List<String> list;


    public DateAdapter(List<String> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public void updateData(List<String> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemDateViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemDateBinding binding = ItemDateBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new DateAdapter.ItemDateViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemDateViewHolder holder, int position) {
        Map<String, String> checkInCodeMap = new HashMap<>();
        String date = list.get(position);
        holder.binding.textview.setText(date);

        CheckInCodeAdapter adapter = new CheckInCodeAdapter(new HashMap<>());
        holder.binding.recyclerView.setLayoutManager(new LinearLayoutManager(holder.itemView.getContext()));
        holder.binding.recyclerView.setAdapter(adapter);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("KOS Plus")
                .child("CheckInCodes")
                .child(date);

        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    checkInCodeMap.clear();
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        String shopId = dataSnapshot.getKey();
                        String checkinCode = dataSnapshot.getValue(String.class);
                        checkInCodeMap.put(shopId, checkinCode);

                        Log.d("CheckInCodeVM", "ShopId: " + shopId + " | CheckinCode: " + checkinCode);
                    }

                    // Sau khi có data => set adapter
                    adapter.updateData(checkInCodeMap);

                } else {
                    holder.binding.textview.setText(date + " Nghỉ");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CheckInCodeVM", "Lỗi Firebase: " + error.getMessage());
            }
        });

    }

    @Override
    public int getItemCount() {
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    public class ItemDateViewHolder extends RecyclerView.ViewHolder {
        ItemDateBinding binding;

        public ItemDateViewHolder(@NonNull ItemDateBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
