package com.example.kosplus.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kosplus.databinding.ItemLuckyrewardBinding;
import com.example.kosplus.func.Utils;
import com.example.kosplus.model.LuckyRewards;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;

public class LuckyRewardAdapter extends RecyclerView.Adapter<LuckyRewardAdapter.ItemLuckyrewardViewHolder> {
    private List<LuckyRewards> list;

    public LuckyRewardAdapter(List<LuckyRewards> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public void updateData(List<LuckyRewards> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ItemLuckyrewardViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemLuckyrewardBinding binding = ItemLuckyrewardBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new LuckyRewardAdapter.ItemLuckyrewardViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemLuckyrewardViewHolder holder, int position) {
        LuckyRewards luckyReward = list.get(position);
        holder.binding.reward.setText(luckyReward.reward);
        holder.binding.point.setText(String.valueOf(luckyReward.point));

        holder.binding.delete.setOnClickListener(v -> {
            Utils.showVerificationDialog(v.getContext(), "Bạn có muốn xóa phần thưởng này không?", "Bạn có muốn xóa phần thưởng này không?",""+luckyReward.reward, ()->{
                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("LuckyRewards").child(luckyReward.id);
                databaseReference.removeValue();
            });
        });
    }

    @Override
    public int getItemCount() {
        if (list != null) {
            return list.size();
        }
        return 0;
    }

    public class ItemLuckyrewardViewHolder extends RecyclerView.ViewHolder {
        ItemLuckyrewardBinding binding;

        public ItemLuckyrewardViewHolder(@NonNull ItemLuckyrewardBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
