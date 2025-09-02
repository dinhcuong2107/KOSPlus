package com.example.kosplus.features;

import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.lifecycle.ViewModel;

import com.example.kosplus.R;
import com.example.kosplus.databinding.CustomDialogQrcodeBinding;
import com.example.kosplus.datalocal.DataLocalManager;
import com.example.kosplus.func.Utils;
import com.example.kosplus.model.Shops;
import com.example.kosplus.model.Users;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class WalletVM extends ViewModel {
    public ObservableField<String> balance = new ObservableField<>();
    public ObservableField<String> userName = new ObservableField<>();
    public ObservableField<String> bankCode = new ObservableField<>();
    public ObservableField<String> bankNumber = new ObservableField<>();

    public WalletVM() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Wallets").child(DataLocalManager.getUid());
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // Nếu ID chưa tồn tại -> tạo mới với giá trị
                    databaseReference.setValue(0);
                    balance.set("0");
                } else {
                    // Nếu ID đã tồn tại
                    balance.set(Utils.formatCurrencyVND(Long.parseLong(snapshot.getValue().toString())));
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi truy vấn: " + error.getMessage());
            }
        });

        DatabaseReference reference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Users").child(DataLocalManager.getUid());
        reference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    Users user = snapshot.getValue(Users.class);
                    userName.set(user.fullname);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

            loadDefault();
    }

    private void loadDefault() {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus");
        databaseReference.child("ShopDefault").addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    databaseReference.child("Shops").child(snapshot.getValue(String.class)).addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                            if (dataSnapshot.exists()) {
                                Shops shop = dataSnapshot.getValue(Shops.class);
                                bankCode.set(shop.bankCode);
                                bankNumber.set(shop.bankNumber);
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {

                        }
                    });
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    public void showQR(View view) {
        Dialog dialog = new Dialog(view.getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        CustomDialogQrcodeBinding binding = CustomDialogQrcodeBinding.inflate(LayoutInflater.from(view.getContext()));
        dialog.setContentView(binding.getRoot());

        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        window.getAttributes().windowAnimations = R.style.DialogAnimationDrop;
        window.setGravity(Gravity.CENTER);
        dialog.setCancelable(false);

        String baseUrl = "https://img.vietqr.io/image/";
        String qrUrl = baseUrl + bankCode.get() + "-" + bankNumber.get() + "-compact2.png" +
                "?amount=" + 0 +
                "&addInfo=" + DataLocalManager.getUid();  // encode để tránh lỗi dấu cách

        Log.d("QR_URL", qrUrl);
        Picasso.get()
                .load(qrUrl)
                .placeholder(R.drawable.rounded_qr_code_24) // ảnh tạm khi tải
                .error(R.drawable.rounded_warning_24)             // ảnh khi lỗi
                .into(binding.imageView);

        binding.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }
}