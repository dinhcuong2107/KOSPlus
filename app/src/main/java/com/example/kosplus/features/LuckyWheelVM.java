package com.example.kosplus.features;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableField;
import androidx.lifecycle.ViewModel;

import com.example.kosplus.databinding.CustomDialogLoadingBinding;
import com.example.kosplus.datalocal.DataLocalManager;
import com.example.kosplus.func.Utils;
import com.example.kosplus.model.LuckyRewards;
import com.example.kosplus.model.RewardHistory;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class LuckyWheelVM extends ViewModel {
    public ObservableField<Integer> quantityTicket = new ObservableField<>();
    public ObservableField<String> reward = new ObservableField<>();
    public ObservableField<String> point = new ObservableField<>();

    public ObservableField<Boolean> admin = new ObservableField<>();

    Dialog dialog;

    public LuckyWheelVM() {
        if (DataLocalManager.getRole().equals("Admin")) {
            admin.set(true);
        } else {
            admin.set(false);
        }
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Ticket").child(DataLocalManager.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    quantityTicket.set(snapshot.getValue(Integer.class));
                } else {
                    databaseReference.setValue(0);
                    quantityTicket.set(0);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
            }
        });
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
            if (internetTime <= 0) {
                Log.e("KOS Plus", "LuckyWheelVM saveRewardToFirebase: " + internetTime);
                return;
            }

            String timeString = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault())
                    .format(new Date(internetTime));

            new Handler(Looper.getMainLooper()).post(() -> {
                String usageTime = "";
                if (!DataLocalManager.getRole().equals("Customer")) {
                    usageTime = timeString;
                }

                DatabaseReference ref = FirebaseDatabase.getInstance()
                        .getReference("KOS Plus")
                        .child("RewardHistories");
                String id = ref.push().getKey();
                RewardHistory data = new RewardHistory(id, reward, DataLocalManager.getUid(), timeString, usageTime);
                ref.child(id).setValue(data);
                Utils.pushNotification("", "Thông báo", "Bạn đã trúng " + reward, DataLocalManager.getUid(), internetTime);

                DatabaseReference ref2 = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Ticket").child(DataLocalManager.getUid());
                ref2.setValue(quantityTicket.get() - 1);
            });
        }).start();
    }

    public void checkQR(View view, String id) {

        dialog = new Dialog(view.getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        CustomDialogLoadingBinding dialogLoadingBinding = CustomDialogLoadingBinding.inflate(LayoutInflater.from(view.getContext()));
        dialog.setContentView(dialogLoadingBinding.getRoot());

        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams win = window.getAttributes();
        win.gravity = Gravity.CENTER;
        window.setAttributes(win);
        dialog.setCancelable(false);

        dialog.show();

        Log.d("LuckyWheel", "checkQR: " + id);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                .getReference("KOS Plus")
                .child("RewardHistories");
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                boolean isExist = false;
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    if (dataSnapshot.getKey().equals(id)) {
                        saveUsageTimeReward(view, id);
                        isExist = true;
                        break;
                    }
                }
                if (!isExist) {
                    dialog.dismiss();
                    Utils.showError(view.getContext(), "Không tìm thấy thông tin quà tặng!");
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }
    public void saveUsageTimeReward(View view, String id) {

                        // Lấy thời gian mạng
        new Thread(() -> {
            long internetTime = Utils.getInternetTimeMillis();
            if (internetTime <= 0) return;

            String timeString = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault())
                    .format(new Date(internetTime));
            new Handler(Looper.getMainLooper()).post(() -> {
                DatabaseReference ref = FirebaseDatabase.getInstance()
                        .getReference("KOS Plus")
                        .child("RewardHistories").child(id);

                ref.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            RewardHistory data = snapshot.getValue(RewardHistory.class);
                            if (data.usageTime == null || data.usageTime.isEmpty()) {
                                ref.child("usageTime").setValue(timeString, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                        // Đóng sau 1 giây
                                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                            if (dialog.isShowing()) {
                                                dialog.dismiss();
                                            }
                                        }, 1000); // 1000ms = 1 giây
                                        Utils.showNotificationDialog(view.getContext(), "", "Thông báo", "Xác nhận sử dụng quà tặng thành công!" + "\nQuà tặng: " + data.reward + "\nThời gian sử dụng: " + timeString);
                                        return;

                                    }
                                });
                            } else {
                                dialog.dismiss();
                                if (!data.usageTime.equals(timeString)) {
                                    Utils.showNotificationDialog(view.getContext(), "", "Thông báo", "Quà tặng đã được xác nhận trước đó!" + "\nQuà tặng: " + data.reward + "\nThời gian sử dụng: " + data.usageTime);
                                }
                            }
                        } else {
                            dialog.dismiss();
                            Utils.showError(view.getContext(), "Không tìm thấy thông tin quà tặng!");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                    }

                    });
                });
        }).start();
    }
}
