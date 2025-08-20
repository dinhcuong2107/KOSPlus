package com.example.kosplus.features;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.lifecycle.ViewModel;

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
                                        Utils.showNotificationDialog(
                                                view.getContext(),
                                                "",
                                                "Bạn đã check-in thành công trước đó",
                                                "" + snapshot.getValue()
                                        );
                                    } else {
                                        // Lưu thời gian check-in
                                        checkInRef.setValue(timeString)
                                                .addOnSuccessListener(aVoid -> {
                                                    Utils.showNotificationDialog(
                                                            view.getContext(),
                                                            "",
                                                            "Check-in thành công!",
                                                            timeString
                                                    );
                                                })
                                                .addOnFailureListener(e -> {
                                                    Utils.showNotificationDialog(
                                                            view.getContext(),
                                                            "Lỗi",
                                                            "Không thể check-in. Vui lòng thử lại.",
                                                            e.getMessage()
                                                    );
                                                });
                                    }
                                }

                                @Override
                                public void onCancelled(@NonNull DatabaseError error) {
                                    Log.e("CheckInScannerVM", "Firebase error: " + error.getMessage());
                                }
                            });


                        } else {
                            Log.d("CheckInScannerVM", "Code không hợp lệ");
                            Utils.showNotificationDialog(view.getContext(), "", "Code không hợp lệ", "");
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
