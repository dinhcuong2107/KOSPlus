package com.example.kosplus.adapter;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kosplus.databinding.ItemProductCartBinding;
import com.example.kosplus.func.Utils;
import com.example.kosplus.model.ItemCarts;
import com.example.kosplus.model.Products;
import com.example.kosplus.model.Promotions;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class CartAdapter extends RecyclerView.Adapter<CartAdapter.ItemProductCartViewHolder>{
    private List<ItemCarts> list;
    private OnCartDataChangedListener listener;
    private RecyclerView recyclerView;

    public interface OnCartDataChangedListener {
        void onCartDataChanged(List<String> productIds, List<Integer> quantities, List<Integer> finalPrices);
    }


    public CartAdapter(List<ItemCarts> list, OnCartDataChangedListener listener) {
        this.list = list;
        this.listener = listener;
        updateData(list);
    }

    public void updateData(List<ItemCarts> list) {
        this.list = list;
        notifyDataSetChanged();
        notifyCartChanged();
    }

    @NonNull
    @Override
    public ItemProductCartViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemProductCartBinding binding = ItemProductCartBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new CartAdapter.ItemProductCartViewHolder(binding);
    }

    @Override
    public void onAttachedToRecyclerView(@NonNull RecyclerView recyclerView) {
        super.onAttachedToRecyclerView(recyclerView);
        this.recyclerView = recyclerView;
    }

    @Override
    public void onBindViewHolder(@NonNull ItemProductCartViewHolder holder, int position) {
        ItemCarts itemCart = list.get(position);

        ObservableField<Products> product = new ObservableField<>();
        ObservableField<Integer> finalPrice = new ObservableField<>(0);

        DatabaseReference productRef = FirebaseDatabase.getInstance()
                .getReference("KOS Plus").child("Products").child(itemCart.productId);

        productRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Products p;
                p = snapshot.getValue(Products.class);
                if (p == null) return;

                product.set(p);
                holder.binding.name.setText(p.name);
                holder.binding.description.setText(p.description);
                Picasso.get().load(p.imageUrl).into(holder.binding.imageView);

                // Lấy thời gian mạng
                new Thread(() -> {
                    long internetTime = Utils.getInternetTimeMillis();
                    if (internetTime <= 0) return;

                    String timeString = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault())
                            .format(new Date(internetTime));

                    new Handler(Looper.getMainLooper()).post(() -> {
                        if (p.promotion == null || p.promotion.isEmpty()) {
                            finalPrice.set(p.price);
                            holder.binding.total.setText(""+finalPrice.get());
                        } else {
                            DatabaseReference promoRef = FirebaseDatabase.getInstance()
                                    .getReference("KOS Plus").child("Promotions").child(p.promotion);

                            promoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    Promotions promo = snapshot.getValue(Promotions.class);
                                    int priceToSet = p.price;

                                    if (promo != null && promo.status &&
                                            Utils.checkTime(timeString, promo.start_date, promo.end_date)) {
                                        if ("amount".equals(promo.type)) {
                                            priceToSet = p.price - promo.discount;
                                        } else {
                                            priceToSet = p.price - (p.price * promo.discount / 100);
                                        }
                                    }

                                    finalPrice.set(priceToSet);
                                    holder.binding.total.setText(""+finalPrice.get());
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                }
                            });
                        }
                    });
                }).start();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


        holder.binding.decrease.setOnClickListener(v -> {
            int quantity = Integer.parseInt(holder.binding.quatity.getText().toString());
            if (quantity > 1) {
                if (holder.binding.checkbox.isChecked()) {
                    quantity--;
                    holder.binding.quatity.setText("" + quantity);
                    holder.binding.total.setText("" + finalPrice.get() * (quantity));
                }
                notifyCartChanged();

            } else {
                Utils.showVerificationDialog(v.getContext(), "Verification","Xác nhận", "Xóa sản phẩm khỏi giỏ hàng?", () -> {
                    DatabaseReference databaseRef = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Carts").child(itemCart.id);
                    databaseRef.removeValue()
                            .addOnSuccessListener(aVoid -> {
                                // Dữ liệu đã được xóa thành công
                                Log.d("Firebase", "data deleted successfully!");
                            })
                            .addOnFailureListener(e -> {
                                // Xảy ra lỗi khi xóa dữ liệu
                                Log.e("Firebase", "Failed to delete data", e);
                            });
                    list.remove(holder.getAdapterPosition());
                    notifyItemRemoved(holder.getAdapterPosition());
                    notifyCartChanged();
                });
            }
        });

        holder.binding.increase.setOnClickListener(v -> {
            if (holder.binding.checkbox.isChecked()) {
                int quantity = Integer.parseInt(holder.binding.quatity.getText().toString());
                quantity ++;
                holder.binding.quatity.setText("" + quantity);
                holder.binding.total.setText("" + finalPrice.get() * (quantity));
                notifyCartChanged();
            }
        });

        holder.binding.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
            notifyCartChanged();
        });
    }

    private void notifyCartChanged() {
        if (recyclerView == null || listener == null) return;

        List<String> productIds = new ArrayList<>();
        List<Integer> quantities = new ArrayList<>();
        List<Integer> finalPrices = new ArrayList<>();

        for (int i = 0; i < list.size(); i++) {
            ItemCarts item = list.get(i);
            RecyclerView.ViewHolder vh = recyclerView.findViewHolderForAdapterPosition(i);
            if (vh instanceof ItemProductCartViewHolder) {
                ItemProductCartViewHolder holder = (ItemProductCartViewHolder) vh;
                if (holder.binding.checkbox.isChecked())
                {
                    try {
                        String pid = item.productId;
                        int quantity = Integer.parseInt(holder.binding.quatity.getText().toString());
                        int price = Integer.parseInt(holder.binding.total.getText().toString());

                        productIds.add(pid);
                        quantities.add(quantity);
                        finalPrices.add(price);
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        listener.onCartDataChanged(productIds, quantities, finalPrices);
    }

    @Override
    public int getItemCount() {
        if (list != null){return list.size();}
        return 0;
    }

    public class ItemProductCartViewHolder extends RecyclerView.ViewHolder {
        ItemProductCartBinding binding;
        public ItemProductCartViewHolder(@NonNull ItemProductCartBinding binding ) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}