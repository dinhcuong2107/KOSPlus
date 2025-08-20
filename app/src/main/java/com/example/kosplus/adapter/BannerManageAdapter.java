package com.example.kosplus.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kosplus.databinding.ItemBannerManageBinding;
import com.example.kosplus.func.Utils;
import com.example.kosplus.model.Banners;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class BannerManageAdapter extends RecyclerView.Adapter<BannerManageAdapter.BannerManageViewHolder> {
    List<Banners> list, listEnable;
    public BannerManageAdapter(List<Banners> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public void updateData(List<Banners> list) {
        this.list = list;
        listEnable = new ArrayList<>();
        if (list != null) {
            for (Banners banner : list) {
                if (banner.status) {
                    listEnable.add(banner);
                }
            }
        }
        notifyDataSetChanged();
    }

    private void resetPos(List<Banners> list) {
        boolean needUpdate = false;

        // Kiểm tra xem position có đúng chưa
        for (int i = 0; i < list.size(); i++) {
            if (list.get(i).getPosition() != i) {
                needUpdate = true;
                break;
            }
        }

        // Nếu cần update thì tiến hành reset
        if (needUpdate) {
            DatabaseReference ref = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Banners");

            for (int i = 0; i < list.size(); i++) {
                Banners banner = list.get(i);
                banner.setPosition(i);
                ref.child(banner.id).child("position").setValue(i);
            }
        }
    }

    @NonNull
    @Override
    public BannerManageViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemBannerManageBinding binding = ItemBannerManageBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new BannerManageAdapter.BannerManageViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull BannerManageViewHolder holder, int position) {
        Banners banner = list.get(position);
        resetPos(listEnable);

        if (banner.position == 0) {
            holder.binding.up.setVisibility(View.GONE);
        } else {
            holder.binding.down.setVisibility(View.VISIBLE);
        }

        if (position >= listEnable.size() - 1) {
            holder.binding.down.setVisibility(View.GONE);
        } else {
            holder.binding.up.setVisibility(View.VISIBLE);
        }

        holder.binding.up.setOnClickListener(view -> {
            if (position > 0) {
                Banners above = list.get(position - 1);
                // Gửi lên Firebase
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Banners");
                ref.child(banner.id).child("position").setValue(position - 1);
                ref.child(above.id).child("position").setValue(position);
            }
        });

        holder.binding.down.setOnClickListener(view -> {
            if (position < list.size() - 1) {
                Banners below = list.get(position + 1);
                // Gửi lên Firebase
                DatabaseReference ref = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Banners");
                ref.child(banner.id).child("position").setValue(position + 1);
                ref.child(below.id).child("position").setValue(position);
            }
        });

        Picasso.get().load(banner.imageUrl).into(holder.binding.imageView);
        holder.binding.title.setText(banner.title);
        holder.binding.position.setText(""+banner.position);
        holder.binding.statusSwitch.setChecked(banner.status);

        if (banner.status) {
            holder.binding.layout.setVisibility(ViewGroup.VISIBLE);
        } else {
            holder.binding.layout.setVisibility(View.GONE);
        }

        holder.binding.statusSwitch.setOnClickListener(v -> {
            if (banner.status) {
                Utils.showVerificationDialog(v.getContext(),"Verification","Tắt QC", ""+ banner.title,()->{
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Banners").child(""+banner.id).child("status");
                    databaseReference.setValue(false);
                });
            } else {
                Utils.showVerificationDialog(v.getContext(),"Verification","Bật QC", ""+ banner.title,()->{
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Banners").child(""+banner.id);
                    databaseReference.child("status").setValue(true);
                    databaseReference.child("position").setValue(listEnable.size());
                });
            }

        });
    }


    @Override
    public int getItemCount() {
        if (list != null){return list.size();}
        return 0;
    }

    public class BannerManageViewHolder extends RecyclerView.ViewHolder {
        ItemBannerManageBinding binding;
        public BannerManageViewHolder(@NonNull ItemBannerManageBinding binding ) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
