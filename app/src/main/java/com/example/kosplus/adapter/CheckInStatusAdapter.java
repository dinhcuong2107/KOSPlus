package com.example.kosplus.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kosplus.databinding.ItemCheckinStatusBinding;
import com.example.kosplus.model.Users;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Map;

public class CheckInStatusAdapter extends RecyclerView.Adapter<CheckInStatusAdapter.ItemCheckinStatusViewHolder> {
    private Map<String, String> checkInCodeMap;


    public CheckInStatusAdapter(Map<String, String> checkInCodeMap) {
        this.checkInCodeMap = checkInCodeMap;
        notifyDataSetChanged();
    }

    public void updateData(Map<String, String> checkInCodeMap) {
        this.checkInCodeMap = checkInCodeMap;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemCheckinStatusViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemCheckinStatusBinding binding = ItemCheckinStatusBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CheckInStatusAdapter.ItemCheckinStatusViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemCheckinStatusViewHolder holder, int position) {
        String userID = (String) checkInCodeMap.keySet().toArray()[position];
        String checkinTime = checkInCodeMap.get(userID);

        holder.binding.textviewTime.setText(checkinTime);

        DatabaseReference checkinsRef = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Users").child(userID);
        checkinsRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Users users = snapshot.getValue(Users.class);
                    holder.binding.textviewUserName.setText(users.fullname);
                    holder.binding.textviewUserPhone.setText(users.phone);
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

    public class ItemCheckinStatusViewHolder extends RecyclerView.ViewHolder {
        ItemCheckinStatusBinding binding;

        public ItemCheckinStatusViewHolder(@NonNull ItemCheckinStatusBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
