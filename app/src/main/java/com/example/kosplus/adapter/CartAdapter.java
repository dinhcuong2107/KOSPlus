package com.example.kosplus.adapter;

import android.app.Application;
import android.graphics.Color;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kosplus.databinding.ItemProductCartBinding;
import com.example.kosplus.func.Utils;
import com.example.kosplus.livedata.ItemCartsLiveData;
import com.example.kosplus.model.ItemCarts;
import com.example.kosplus.model.OrderItems;
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
        void onCartDataChanged(List<OrderItems> orderItemsList);
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
                if (p.status) {
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
                                holder.binding.total.setText(Utils.formatCurrencyVND(finalPrice.get()));
                            } else {
                                DatabaseReference promoRef = FirebaseDatabase.getInstance()
                                        .getReference("KOS Plus").child("Promotions").child(p.promotion);

                                promoRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                    @Override
                                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                                        Promotions promo = snapshot.getValue(Promotions.class);
                                        int priceToSet = p.price;

//                                        if (promo != null && promo.status &&
//                                                Utils.checkTime(timeString, promo.start_date, promo.end_date)) {
//                                            if ("amount".equals(promo.type)) {
//                                                priceToSet = p.price - promo.discount;
//                                            } else {
//                                                priceToSet = p.price - (p.price * promo.discount / 100);
//                                            }
//                                        }

                                        finalPrice.set(priceToSet);
                                        holder.binding.total.setText(Utils.formatCurrencyVND(finalPrice.get()));
                                    }

                                    @Override
                                    public void onCancelled(@NonNull DatabaseError error) {
                                    }
                                });
                            }
                        });
                    }).start();
                } else {
                    holder.binding.name.setText(p.name);
                    holder.binding.description.setText("Sản phẩm đã hết hàng / ngừng kinh doanh");
                    holder.binding.description.setTextColor(Color.parseColor("#FF0000"));
                    Picasso.get().load(p.imageUrl).into(holder.binding.imageView);

                    holder.binding.increase.setVisibility(View.GONE);
                    holder.binding.checkbox.setVisibility(View.GONE);

                }


            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });


        holder.binding.decrease.setOnClickListener(v -> {
            long quantity = Utils.parseCurrencyVND(holder.binding.quatity.getText().toString());
            if (quantity > 1) {
                if (holder.binding.checkbox.isChecked()) {
                    quantity--;
                    holder.binding.quatity.setText("" + quantity);
                    holder.binding.total.setText(Utils.formatCurrencyVND(finalPrice.get() * (quantity)));
                }
                notifyCartChanged();

            } else {
                Utils.showVerificationDialog(v.getContext(), "Verification","Xác nhận", "Xóa sản phẩm khỏi giỏ hàng?", () -> {
                    ItemCartsLiveData itemCartsLiveData = new ItemCartsLiveData(new Application());
                    itemCartsLiveData.clearItem(itemCart.productId);

                    list.remove(holder.getAdapterPosition());
                    notifyItemRemoved(holder.getAdapterPosition());
                    notifyCartChanged();
                });
            }
        });

        holder.binding.increase.setOnClickListener(v -> {
            if (holder.binding.checkbox.isChecked()) {
                long quantity = Utils.parseCurrencyVND(holder.binding.quatity.getText().toString());
                quantity ++;
                holder.binding.quatity.setText("" + quantity);
                holder.binding.total.setText(Utils.formatCurrencyVND(finalPrice.get() * (quantity)));
                notifyCartChanged();
            }
        });

        holder.binding.checkbox.setOnCheckedChangeListener((buttonView, isChecked) -> {
                notifyCartChanged();
        });
    }

    private void notifyCartChanged() {
        if (recyclerView == null || listener == null) return;

        List<OrderItems> orderItemsList = new ArrayList<>();

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
                        int price = Integer.parseInt("" + Utils.parseCurrencyVND(holder.binding.total.getText().toString()));

                        orderItemsList.add(new OrderItems( ""+i, pid, quantity, price));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                }
            }
        }

        listener.onCartDataChanged(orderItemsList);
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