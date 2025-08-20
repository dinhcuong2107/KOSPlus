package com.example.kosplus.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kosplus.databinding.ItemTransactionBinding;
import com.example.kosplus.model.TransactionHistory;

import java.util.List;

public class TransactionHistoryAdapter extends RecyclerView.Adapter<TransactionHistoryAdapter.TransactionViewHolder> {
     List<TransactionHistory> list;
    public TransactionHistoryAdapter(List<TransactionHistory> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public void updateData(List<TransactionHistory> list){
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public TransactionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemTransactionBinding binding = ItemTransactionBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new TransactionHistoryAdapter.TransactionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull TransactionViewHolder holder, int position) {
        TransactionHistory transaction = list.get(position);
        holder.binding.title.setText(transaction.type);
        holder.binding.description.setText(transaction.description);
        holder.binding.time.setText(transaction.time);
        String text = "";
        if (transaction.type.equals("deposit") || transaction.type.equals("refund")) {
            text = "+ ";
        } else {
            text = "- ";
        }
        holder.binding.amount.setText(text + transaction.amount);
    }

    @Override
    public int getItemCount() {
        if (list != null){return list.size();}
        return 0;
    }

    public class TransactionViewHolder extends RecyclerView.ViewHolder {
        ItemTransactionBinding binding;
        public TransactionViewHolder(@NonNull ItemTransactionBinding binding ) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
