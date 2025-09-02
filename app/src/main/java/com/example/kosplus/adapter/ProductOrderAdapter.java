package com.example.kosplus.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kosplus.databinding.ItemProductOrderBinding;
import com.example.kosplus.model.Products;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.List;

public class ProductOrderAdapter extends RecyclerView.Adapter<ProductOrderAdapter.ProductOrderViewHolder> {
    private List<String> productIds;
    private List<Integer> quantities;
    private List<Integer> prices;


    public ProductOrderAdapter(List<String> productIds, List<Integer> quantities, List<Integer> prices) {
        this.productIds = productIds;
        this.quantities = quantities;
        this.prices = prices;
        notifyDataSetChanged();
    }

    public void updateData(List<String> productIds, List<Integer> quantities, List<Integer> prices) {
        this.productIds = productIds;
        this.quantities = quantities;
        this.prices = prices;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public ProductOrderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductOrderBinding binding = ItemProductOrderBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new ProductOrderAdapter.ProductOrderViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ProductOrderViewHolder holder, int position) {
        String id = productIds.get(position);
        int quantity = quantities.get(position);
        holder.binding.textquantity.setText(" x "+quantity);

        if (prices == null || prices.isEmpty())
        {
            holder.binding.textprice.setVisibility(View.GONE);
        } else {
            holder.binding.textprice.setVisibility(View.VISIBLE);
            int price = prices.get(position);
            holder.binding.textprice.setText(" = " + price);
        }

        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("KOS Plus")
                .child("Products")
                .child(id);
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
        if (productIds != null) {
            return productIds.size();
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
