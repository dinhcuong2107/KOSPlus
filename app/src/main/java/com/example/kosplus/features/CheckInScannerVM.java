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

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

import com.example.kosplus.databinding.CustomDialogLoadingBinding;
import com.example.kosplus.datalocal.DataLocalManager;
import com.example.kosplus.func.Utils;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

public class CheckInScannerVM extends ViewModel {
    public CheckInScannerVM() {}

    public void setCheckIn(View view, String qrCode) {

        Dialog dialog = new Dialog(view.getContext());
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

        Log.d("CheckInScannerVM", "Code"+ qrCode);
        // Lấy thời gian mạng
        new Thread(() -> {
            long internetTime = Utils.getInternetTimeMillis();
            if (internetTime <= 0) return;
            String dateString = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    .format(new Date(internetTime));
            String timeString = new SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    .format(new Date(internetTime));

            new Handler(Looper.getMainLooper()).post(() -> {

                DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("CheckInCodes").child(dateString);
                databaseReference.addValueEventListener(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        boolean isValid = false;

                        if (snapshot.exists()) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                String code = dataSnapshot.getValue(String.class);
                                if (qrCode.equals(code)) {
                                    isValid = true;
                                    break;
                                }
                            }
                        }

                        if (isValid) {
                            Log.d("CheckInScannerVM", "Code Hợp lệ");
                            // TODO: xử lý khi code hợp lệ (ví dụ: check-in)

                            DatabaseReference checkInRef = FirebaseDatabase.getInstance()
                                    .getReference("KOS Plus")
                                    .child("CheckIns")
                                    .child(qrCode)
                                    .child(DataLocalManager.getUid());

                            checkInRef.addListenerForSingleValueEvent(new ValueEventListener() {
                                @Override
                                public void onDataChange(@NonNull DataSnapshot snapshot) {
                                    if (snapshot.exists()) {
                                        // Người dùng đã checkin trước đó

                                        // Đóng sau 1 giây
                                        new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                            if (dialog.isShowing()) {
                                                dialog.dismiss();
                                            }
                                            Utils.showNotificationDialog(
                                                    view.getContext(),
                                                    "",
                                                    "Bạn đã check-in thành công trước đó",
                                                    "" + snapshot.getValue()
                                            );
                                        }, 1000); // 1000ms = 1 giây


                                    } else {
                                        // Lưu thời gian check-in
                                        checkInRef.setValue(timeString)
                                                .addOnSuccessListener(aVoid -> {

                                                    // Đóng sau 1 giây
                                                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                                        if (dialog.isShowing()) {
                                                            dialog.dismiss();
                                                        }

                                                        // Hiển thị thông báo thành công
                                                        Utils.showNotificationDialog(
                                                                view.getContext(),
                                                                "",
                                                                "Check-in thành công!",
                                                                timeString
                                                        );
                                                    }, 1000); // 1000ms = 1 giây

                                                })
                                                .addOnFailureListener(e -> {

                                                    // Đóng sau 1 giây
                                                    new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                                        if (dialog.isShowing()) {
                                                            dialog.dismiss();
                                                        }
                                                        Utils.showNotificationDialog(
                                                                view.getContext(),
                                                                "Lỗi",
                                                                "Không thể check-in. Vui lòng thử lại.",
                                                                e.getMessage()
                                                        );
                                                    }, 1000);

                                                });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e("CheckInScannerVM", "Firebase error: " + error.getMessage());
                                }
                            });


                        } else {
                            // Đóng sau 1 giây
                            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                                if (dialog.isShowing()) {
                                    dialog.dismiss();

                                    Log.d("CheckInScannerVM", "Code không hợp lệ");
                                    Utils.showNotificationDialog(view.getContext(), "", "Code không hợp lệ", "");
                                }
                            }, 1000); // 1000ms = 1 giây

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
