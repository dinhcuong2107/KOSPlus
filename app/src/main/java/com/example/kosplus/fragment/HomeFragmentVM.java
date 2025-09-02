package com.example.kosplus.fragment;

import android.app.Application;
import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;

import com.example.kosplus.datalocal.DataLocalManager;
import com.example.kosplus.features.CartsActivity;
import com.example.kosplus.features.LuckyWheelActivity;
import com.example.kosplus.features.OrdersManageActivity;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class HomeFragmentVM extends AndroidViewModel {
    public ObservableField<String> supportId = new ObservableField<>();
    public HomeFragmentVM(@NonNull Application application) {
        super(application);
        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Support");
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    String id = snapshot.getValue(String.class);
                    supportId.set(id);
                    Log.d("Messenger", "Supporter Đã sẵn sàng");
                } else {
                    supportId.set("");
                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void onLuckyWheel(View view) {
        Intent intent = new Intent(view.getContext(), LuckyWheelActivity.class);
        view.getContext().startActivity(intent);
    }

    public void onItemCartManage(View view) {
        Intent intent = new Intent(view.getContext(), CartsActivity.class);
        view.getContext().startActivity(intent);
    }

    public void onOrderManage(View view) {
        Intent intent = new Intent(view.getContext(), OrdersManageActivity.class);
        view.getContext().startActivity(intent);
    }

    public void onMessenger(View view) {
        if (supportId.get() == null || supportId.get().isEmpty()) {
            Toast.makeText(view.getContext(), "Đang bảo trì", Toast.LENGTH_SHORT).show();
        } else {
            openMessenger(view, supportId.get());
        }
    }

    public void openMessenger(View view, String supportId) {
        try {
            Uri uri = Uri.parse("https://m.me/" + supportId);
            Intent intent = new Intent(Intent.ACTION_VIEW, uri);
            intent.setPackage("com.facebook.orca"); // chỉ mở Messenger app
            if (intent.resolveActivity(view.getContext().getPackageManager()) != null) {
                view.getContext().startActivity(intent);
            } else {
                Toast.makeText(view.getContext(), "Vui lòng cài đặt Messenger", Toast.LENGTH_SHORT).show();
            }
        } catch (Exception e) {
            Toast.makeText(view.getContext(), "Không thể mở Messenger", Toast.LENGTH_SHORT).show();
            e.printStackTrace();
        }
    }

}