package com.example.kosplus.adapter;

import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.GradientDrawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kosplus.R;
import com.example.kosplus.databinding.ItemNotificationBinding;
import com.example.kosplus.datalocal.DataLocalManager;
import com.example.kosplus.func.Utils;
import com.example.kosplus.model.Notifications;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.util.List;
public class NotificationAdapter extends RecyclerView.Adapter<NotificationAdapter.NotificationViewHolder> {
    List<Notifications> list;
    public NotificationAdapter(List<Notifications> list) {
        this.list = list;
        notifyDataSetChanged();
    }

    public void updateData(List<Notifications> list){
        this.list = list;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public NotificationViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemNotificationBinding binding = ItemNotificationBinding.inflate(LayoutInflater.from(parent.getContext()),parent,false);
        return new NotificationAdapter.NotificationViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull NotificationViewHolder holder, int position) {
        Notifications notification = list.get(position);
        holder.binding.title.setText(notification.title);
        holder.binding.content.setText(notification.content);
        holder.binding.time.setText(Utils.longToTimeString(notification.time));

        if (notification.userId.equals("All")) {
            GradientDrawable drawable = new GradientDrawable();
            drawable.setShape(GradientDrawable.RECTANGLE);
            drawable.setCornerRadius(24);
            drawable.setColor(Color.parseColor("#ffdcbf"));
            holder.binding.layout.setBackground(drawable);
        } else {
            if (!notification.status) {
                holder.binding.layout.setBackgroundColor(Color.parseColor("#FFFFFF"));
            } else {
                GradientDrawable drawable = new GradientDrawable();
                drawable.setShape(GradientDrawable.RECTANGLE);
                drawable.setCornerRadius(24);
                drawable.setColor(Color.parseColor("#ffebdb"));
                holder.binding.layout.setBackground(drawable);
                //   holder.binding.layout.setBackgroundColor(Color.parseColor("#ebfffe"));
            }
        }

        holder.binding.imageView.setOnClickListener(view -> {
            if (DataLocalManager.getRole().equals("Admin")) {
                Utils.showVerificationDialog(view.getContext(), "Verification", "Xóa thông báo", "Xác nhận xóa "+ notification.title, () -> {
                    DatabaseReference reference = FirebaseDatabase.getInstance()
                            .getReference("KOS Plus")
                            .child("Notifications");

                    reference.child(notification.id)
                            .removeValue()
                            .addOnSuccessListener(aVoid -> Log.d("Firebase", "Xóa thông báo thành công"))
                            .addOnFailureListener(e -> Log.e("Firebase", "Lỗi khi xóa thông báo", e));
                });
            }
        });



        holder.binding.layout.setOnClickListener(v -> {
            if (notification.userId.equals("All")) {
                Utils.showNotificationDialog(v.getContext(), notification.imageUrl, notification.title, notification.content);
            }else {
                Utils.showNotificationDialog(v.getContext(), notification.imageUrl, notification.title, notification.content);
                DatabaseReference reference = FirebaseDatabase.getInstance()
                        .getReference("KOS Plus")
                        .child("Notifications");
                reference.child(notification.id).child("status").setValue(false);
            }
        });
    }

    @Override
    public int getItemCount() {
        if (list != null){return list.size();}
        return 0;
    }

    public class NotificationViewHolder extends RecyclerView.ViewHolder {
        ItemNotificationBinding binding;
        public NotificationViewHolder(@NonNull ItemNotificationBinding binding ) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
