package com.example.kosplus.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kosplus.databinding.ItemBannerBinding;
import com.example.kosplus.model.Banners;
import com.squareup.picasso.Picasso;

import java.util.List;

public class BannerAdapter extends RecyclerView.Adapter<BannerAdapter.BannerViewHolder> {
    List<Banners> list;
    public BannerAdapter(List<Banners> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public void updateData(List<Banners> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public BannerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBannerBinding binding = ItemBannerBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new BannerAdapter.BannerViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerViewHolder holder, int position) {
        Banners banner = list.get(position);
        Picasso.get().load(banner.imageUrl).into(holder.binding.imageView);
        holder.binding.layout.setOnClickListener(v -> {
        });
    }

    @Override
    public int getItemCount() {
        if (list != null){return list.size();}
        return 0;
    }

    public class BannerViewHolder extends RecyclerView.ViewHolder {
        ItemBannerBinding binding;
        public BannerViewHolder(@NonNull ItemBannerBinding binding ) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
