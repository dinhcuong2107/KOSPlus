package com.example.kosplus.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kosplus.databinding.ItemRewardHistoryBinding;
import com.example.kosplus.func.Utils;
import com.example.kosplus.model.RewardHistory;
import com.example.kosplus.model.Users;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class RewardHistoryAdapter extends RecyclerView.Adapter<RewardHistoryAdapter.ItemRewardHistoryViewHolder> {
    private List<RewardHistory> list;

    public RewardHistoryAdapter(List<RewardHistory> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public void updateData(List<RewardHistory> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemRewardHistoryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRewardHistoryBinding binding = ItemRewardHistoryBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new RewardHistoryAdapter.ItemRewardHistoryViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemRewardHistoryViewHolder holder, int position) {
        RewardHistory history = list.get(position);
        holder.binding.textviewTime.setText(history.time);
        holder.binding.textviewReward.setText(history.reward);
        if (history.usageTime != null) {
            holder.binding.textviewUsageTime.setText("Đã nhận quà vào " + history.usageTime);
        } else {
            holder.binding.textviewUsageTime.setVisibility(View.GONE);
        }

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("KOS Plus");
        reference.child("Users").child(history.userId).addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Users users = snapshot.getValue(Users.class);
                    holder.binding.textviewUser.setText(users.fullname);
                } else {
                    holder.binding.textviewUser.setText("Không tìm thấy người dùng");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        holder.binding.layout.setOnClickListener(view -> {
            if (history.usageTime == null || history.usageTime.isEmpty()) {
                Utils.showQRDialog(view.getContext(), history.id);
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

    public class ItemRewardHistoryViewHolder extends RecyclerView.ViewHolder {
        ItemRewardHistoryBinding binding;

        public ItemRewardHistoryViewHolder(@NonNull ItemRewardHistoryBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
