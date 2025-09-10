package com.example.kosplus.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kosplus.databinding.ItemProductOrderBinding;
import com.example.kosplus.func.Utils;
import com.example.kosplus.model.OrderItems;
import com.example.kosplus.model.Products;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class OrderItemAdapter extends RecyclerView.Adapter<OrderItemAdapter.ProductOrderViewHolder> {
    List<OrderItems> list;
    public OrderItemAdapter(List<OrderItems> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public void updateData(List<OrderItems> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductOrderBinding binding = ItemProductOrderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new OrderItemAdapter.ProductOrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductOrderViewHolder holder, int position) {
        OrderItems orderItems = list.get(position);

        holder.binding.textquantity.setText(" x "+ orderItems.quantity);
        holder.binding.textprice.setText("= " + Utils.formatCurrencyVND(orderItems.price));

        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("KOS Plus")
                .child("Products")
                .child(orderItems.productId);
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Products product = snapshot.getValue(Products.class);
                if (product != null) {
                    holder.binding.textname.setText(product.name);
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {

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

    public class ProductOrderViewHolder extends RecyclerView.ViewHolder {
        ItemProductOrderBinding binding;

        public ProductOrderViewHolder(@NonNull ItemProductOrderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
