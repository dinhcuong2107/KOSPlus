package com.example.kosplus.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.Filter;
import android.widget.Filterable;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kosplus.databinding.ItemPromotionBinding;
import com.example.kosplus.func.Utils;
import com.example.kosplus.model.Promotions;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.ArrayList;
import java.util.List;

public class PromotionAdapter extends RecyclerView.Adapter<PromotionAdapter.PromotionViewHolder> implements Filterable {
    List<Promotions> list, list_search;

    public PromotionAdapter(List<Promotions> list) {
        this.list = list;
        this.list_search = list;
        notifyDataSetChanged();
    }

    public void updateData(List<Promotions> list) {
        this.list = list;
        this.list_search = list;
        notifyDataSetChanged();
    }


    @Override
    public Filter getFilter() {
        return new Filter() {
            @Override
            protected FilterResults performFiltering(CharSequence constraint) {
                String keySearch = constraint.toString();
                if (keySearch.isEmpty()) {
                    list = list_search;
                } else {
                    List<Promotions> promotionsList = new ArrayList<>();
                    for (Promotions promotion : list_search) {
                        if (promotion.code.toLowerCase().contains(keySearch.toLowerCase()) ||
                                promotion.title.toLowerCase().contains(keySearch.toLowerCase())
                                || promotion.id.contains(keySearch.toLowerCase())) {
                            promotionsList.add(promotion);
                        }
                    }
                    list = promotionsList;
                }
                FilterResults filterResults = new FilterResults();
                filterResults.values = list;
                return filterResults;
            }

            @Override
            protected void publishResults(CharSequence constraint, FilterResults results) {
                list = (List<Promotions>) results.values;
                notifyDataSetChanged();
            }
        };
    }

    @NonNull
    @Override
    public PromotionViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemPromotionBinding binding = ItemPromotionBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new PromotionAdapter.PromotionViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull PromotionViewHolder holder, int position) {
        Promotions promotion = list.get(position);

        holder.binding.code.setText(promotion.code);
        holder.binding.title.setText(promotion.title);
        holder.binding.startDate.setText(promotion.start_date);
        holder.binding.endDate.setText(promotion.end_date);
        holder.binding.statusSwitch.setChecked(promotion.status);
        if (promotion.type.equals("percent")) {
            holder.binding.discount.setText(promotion.discount + "%");
        } else {
            holder.binding.discount.setText(promotion.discount + " VNĐ");
        }
        holder.binding.statusSwitch.setOnClickListener(v -> {
            if (promotion.status) {
                Utils.showVerificationDialog(v.getContext(),"Verification","Tắt khuyến mãi", ""+ promotion.title,()->{
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Promotions").child(""+promotion.id).child("status");
                    databaseReference.setValue(false);
                });
            } else {
                Utils.showVerificationDialog(v.getContext(),"Verification","Bật KM", ""+ promotion.title,()->{
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Promotions").child(""+promotion.id);
                    databaseReference.child("status").setValue(true);
                });
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

    public class PromotionViewHolder extends RecyclerView.ViewHolder {
        ItemPromotionBinding binding;

        public PromotionViewHolder(@NonNull ItemPromotionBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
