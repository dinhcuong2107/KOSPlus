package com.example.kosplus.features;

import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableField;
import androidx.lifecycle.ViewModel;

import com.example.kosplus.datalocal.DataLocalManager;
import com.example.kosplus.func.Utils;
import com.example.kosplus.model.LuckyRewards;
import com.example.kosplus.model.RewardHistory;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LuckyWheelVM extends ViewModel {
    public ObservableField<String> reward = new ObservableField<>();
    public ObservableField<String> point = new ObservableField<>();

    public ObservableField<Boolean> admin = new ObservableField<>();

    public LuckyWheelVM() {
        if (DataLocalManager.getRole().equals("Admin")) {
            admin.set(true);
        } else {
            admin.set(false);
        }

        reward.set("");
        point.set("0");
    }

    public void onSaveReward(View view) {

        if (reward.get().isEmpty()) {
            Toast.makeText(view.getContext(), "Vui lòng nhập tên quà tặng", Toast.LENGTH_SHORT).show();
            return;
        }

        if (Integer.parseInt(point.get()) == 0) {
            Toast.makeText(view.getContext(), "Vui lòng nhập tỷ lệ quà tặng", Toast.LENGTH_SHORT).show();
            return;
        }

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("LuckyRewards");
        String UID = databaseReference.push().getKey();

        LuckyRewards luckyReward = new LuckyRewards(UID, reward.get(), Integer.parseInt(point.get()));
        databaseReference.child(UID).setValue(luckyReward, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null) {
                    Toast.makeText(view.getContext(), "Lưu thành công", Toast.LENGTH_SHORT).show();
                    reward.set("");
                    point.set("0");
                } else {
                    Toast.makeText(view.getContext(), "" + error, Toast.LENGTH_SHORT).show();
                }
            }
        });
    }
    public void saveRewardToFirebase(String reward) {
        // Lấy thời gian mạng
        new Thread(() -> {
            long internetTime = Utils.getInternetTimeMillis();
            if (internetTime <= 0) return;

            String timeString = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault())
                    .format(new Date(internetTime));

            new Handler(Looper.getMainLooper()).post(() -> {
                DatabaseReference ref = FirebaseDatabase.getInstance()
                        .getReference("KOS Plus")
                        .child("RewardHistories");
                String id = ref.push().getKey();
                RewardHistory data = new RewardHistory(id, reward, DataLocalManager.getUid(), timeString);
                ref.child(id).setValue(data);
                Utils.pushNotification("", "Thông báo", "Bạn đã trúng " + reward, DataLocalManager.getUid(), timeString);
            });
        }).start();
    }
}
