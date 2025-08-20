package com.example.kosplus.features;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
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
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class CheckInCodeVM extends ViewModel {
    public ObservableField<String> status = new ObservableField<>();
    public ObservableField<String> date = new ObservableField<>();
    public ObservableField<String> selectedShop = new ObservableField<>();
    public ObservableField<String> checkinKey = new ObservableField<>();

    public CheckInCodeVM() {
        loadDailyCheckinKeys();
    }

    public void loadDailyCheckinKeys() {
        // Lấy thời gian mạng
        new Thread(() -> {
            long internetTime = Utils.getInternetTimeMillis();
            if (internetTime <= 0) return;
            String dateString = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    .format(new Date(internetTime));
            date.set(dateString);
            String timeString = new SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    .format(new Date(internetTime));

            new Handler(Looper.getMainLooper()).post(() -> {
                DatabaseReference databaseReference = FirebaseDatabase.getInstance()
                        .getReference("KOS Plus")
                        .child("CheckInCodes")
                        .child(dateString);

                databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            status.set(null);
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                String shopId = dataSnapshot.getKey();
                                String checkinCode = dataSnapshot.getValue(String.class);

                                Log.d("CheckInCodeVM", "ShopId: " + shopId + " | CheckinCode: " + checkinCode);

                                if (selectedShop != null && !TextUtils.isEmpty(selectedShop.get())
                                        && selectedShop.get().equals(shopId)) {
                                    checkinKey.set(checkinCode);  // <- Set key hợp lệ
                                    break; // Đã tìm thấy, không cần lặp tiếp
                                }
                            }

                        } else {
                            Log.d("CheckInCodeVM", dateString + " chưa tạo checkincode ");
                            status.set("Check In Code chưa được tạo");
                        }

                        Log.d("CheckInCodeVM", "Trạng thái: " + status.get());
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("CheckInCodeVM", "Lỗi Firebase: " + error.getMessage());
                    }
                });

            });
        }).start();
    }

    public void onShowQRCheckIn (View view) {

        ProgressDialog progressDialog = new ProgressDialog(view.getContext());
        progressDialog.setMessage("Đang xử lý...");
        progressDialog.setCancelable(false); // Không cho bấm ra ngoài để tắt
        progressDialog.show();

// Đóng sau 3 giây
        new Handler(Looper.getMainLooper()).postDelayed(() -> {
            if (progressDialog.isShowing()) {
                progressDialog.dismiss();
                Log.d("CheckInCodeVM", "QR: " + checkinKey.get());
                Utils.showQRDialog(view.getContext(), checkinKey.get());
            }
        }, 500); // 1000ms = 1 giây
    }

    public void generateDailyCheckinKeys(View view) {
        ProgressDialog progressDialog = new ProgressDialog(view.getContext());
        progressDialog.setMessage("Đang xử lý...");
        progressDialog.setCancelable(false); // Không cho bấm ra ngoài để tắt
        progressDialog.show();

        // Lấy thời gian mạng
        new Thread(() -> {
            long internetTime = Utils.getInternetTimeMillis();
            if (internetTime <= 0) return;
            String dateString = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault())
                    .format(new Date(internetTime));
            String timeString = new SimpleDateFormat("HH:mm:ss", Locale.getDefault())
                    .format(new Date(internetTime));

            new Handler(Looper.getMainLooper()).post(() -> {
                DatabaseReference shopRef = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Shops");
                DatabaseReference checkinsRef = FirebaseDatabase.getInstance().getReference("KOS Plus").child("CheckIns");
                DatabaseReference codeCheckinsRef = FirebaseDatabase.getInstance().getReference("KOS Plus").child("CheckInCodes").child(dateString);

                shopRef.addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (!snapshot.exists()) {
                            progressDialog.dismiss();
                            return;}


                        Map<String, Object> codeMap = new HashMap<>();

                        for (DataSnapshot branchSnapshot : snapshot.getChildren()) {
                            String branchId = branchSnapshot.getKey();

                            // Tạo key random (chỉ chứa branchId)
                            DatabaseReference newCheckinRef = checkinsRef.push();
                            String randomKey = newCheckinRef.getKey();


                            newCheckinRef.child(DataLocalManager.getUid()).setValue("00:00:00");

                            // Lưu key tương ứng branchId vào CodeCheckins
                            codeMap.put(branchId, randomKey);
                        }

                        codeCheckinsRef.setValue(codeMap);
                        status.set(null);

                        Context context = view.getContext();
                        Intent intent = new Intent(context, CheckInCodeActivity.class);
                        ((Activity) context).finish(); // đóng Activity cũ
                        ((Activity) context).overridePendingTransition(0, 0); // tắt hiệu ứng khi đóng

                        context.startActivity(intent);
                        ((Activity) context).overridePendingTransition(0, 0); // tắt hiệu ứng khi mở lại

                        Log.d("CheckInCodeVM", "onDataChange: " + status.get());
                        progressDialog.dismiss();
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        Log.e("CheckinKeys", "Lỗi tạo mã: " + error.getMessage());
                    }
                });
            });
        }).start();
    }
    public void setSelectedShop(String code) {
        selectedShop.set(code);
        loadDailyCheckinKeys();
    }
}