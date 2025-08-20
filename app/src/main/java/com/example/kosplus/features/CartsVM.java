package com.example.kosplus.features;

import static android.widget.Toast.LENGTH_LONG;

import android.content.Intent;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.ObservableField;
import androidx.lifecycle.ViewModel;

import com.example.kosplus.datalocal.DataLocalManager;
import com.example.kosplus.func.OneSignalNotification;
import com.example.kosplus.func.Utils;
import com.example.kosplus.model.ItemCarts;
import com.example.kosplus.model.Orders;
import com.example.kosplus.model.Products;
import com.example.kosplus.model.TransactionHistory;
import com.example.kosplus.model.Users;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.atomic.AtomicInteger;

public class CartsVM extends ViewModel {
    ObservableField<List<String>> productIdList = new ObservableField<>();
    ObservableField<List<Integer>> quantityList = new ObservableField<>();
    ObservableField<List<Integer>> priceList = new ObservableField<>();
    public ObservableField<Integer> total = new ObservableField<>();
    public ObservableField<String> address1 = new ObservableField<>();
    public ObservableField<String> address2 = new ObservableField<>();
    public ObservableField<String> note = new ObservableField<>();
    public ObservableField<String> paymentMethod = new ObservableField<>(); // CASH, BANK, WALLET
    public ObservableField<Boolean> delivery = new ObservableField<>(); // true: delivery, false: in shop;

    public CartsVM() {
        delivery.set(false);
        paymentMethod.set("Wallet");
        note.set("");
        address1.set("CS1");
        address2.set("");
    }

    public void setData(List<String> productIdList, List<Integer> quantities, List<Integer> finalPrices) {
        this.productIdList.set(productIdList);
        this.quantityList.set(quantities);
        this.priceList.set(finalPrices);

        int total = 0;
        for (int p : finalPrices) total += p;
        this.total.set(total);
    }

    public void onOrder(View view) {
        if (productIdList.get() == null || productIdList.get().isEmpty()) {
            Toast.makeText(view.getContext(), "Vui lòng chọn sản phẩm", LENGTH_LONG).show();
            return;
        }
        if (address1.get() == null || address1.get().isEmpty() || address2.get() == null || address2.get().isEmpty()) {
            Toast.makeText(view.getContext(), "Vui lòng chọn CS và bàn", LENGTH_LONG).show();
            return;
        }
        if (paymentMethod.get().equals("Wallet")) {
            DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Wallets").child(DataLocalManager.getUid());
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    if (!snapshot.exists()) {
                        // Nếu ID chưa tồn tại -> tạo mới với giá trị
                        databaseReference.setValue(0);
                        Toast.makeText(view.getContext(), "Tài khoản không đủ tiền", LENGTH_LONG).show();
                    } else {
                        // Nếu ID đã tồn tại
                        long balance = Long.parseLong(String.valueOf(snapshot.getValue()));

                        if (balance < total.get()) {
                            Toast.makeText(view.getContext(), "Tài khoản không đủ tiền", LENGTH_LONG).show();
                        } else {
                            formatOrderDetailsAsync(result -> {
                                Log.e("ItemCart Manage", "onOrder: " + result);
                                Utils.showVerificationDialog(view.getContext(), "Xác nhận đặt hàng", "" + address1.get() + "-" + address2.get(), "" + result, () -> {
                                    completeOrd(view);
                                });
                            });
                        }
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {
                    Log.e("Firebase", "Lỗi truy vấn: " + error.getMessage());
                }
            });
        } else {

            formatOrderDetailsAsync(result -> {
                Log.e("ItemCart Manage", "onOrder: " + result);
                Utils.showVerificationDialog(view.getContext(), "Xác nhận đặt hàng", "" + address1.get() + "-" + address2.get(), "" + result, () -> {
                    completeOrd(view);
                });
            });
        }
    }
    public void onDelivery(Boolean b) {
        delivery.set(b);
        Log.e("ItemCart Manage", "onDelivery: " + b);
    }

    public void setPaymentMethod (String method) {
        paymentMethod.set(method);
    }

    private void completeOrd(View view) {
        // Lấy thời gian mạng
        new Thread(() -> {
            long internetTime = Utils.getInternetTimeMillis();
            if (internetTime <= 0) return;

            String timeString = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault())
                    .format(new Date(internetTime));

            new Handler(Looper.getMainLooper()).post(() -> {

                DatabaseReference orderRef = FirebaseDatabase.getInstance()
                        .getReference("KOS Plus")
                        .child("Orders");
                String orderId = orderRef.push().getKey();
                String address = address1.get() + "-" + address2.get();

                Orders order = new Orders(orderId, DataLocalManager.getUid(), productIdList.get(), quantityList.get(), priceList.get(), address,note.get(), paymentMethod.get(), ""+timeString, "","","","", "", total.get(), delivery.get(), true);

                orderRef.child(orderId).setValue(order)
                        .addOnSuccessListener(unused -> {

                            sendToShop(timeString, orderId);

                            Utils.pushNotification("", "Đặt hàng thành công", "Mã đơn: "+orderId,DataLocalManager.getUid(),""+timeString);

                            if (paymentMethod.get().equals("Wallet")) {
                                DatabaseReference reference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("TransactionHistories");
                                String transID = reference.push().getKey();

                                TransactionHistory transactionHistory = new TransactionHistory(transID, DataLocalManager.getUid(), total.get(), timeString, "payment", "Thanh toán hóa đơn "+ orderId, DataLocalManager.getUid(), true);
                                reference.child(transID).setValue(transactionHistory, new DatabaseReference.CompletionListener() {
                                    @Override
                                    public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                        Utils.updateWallet(view.getContext(), DataLocalManager.getUid(),"payment", transactionHistory.amount, timeString);
                                    }
                                });

                            }

                            Toast.makeText(view.getContext(), "Đơn hàng đã được tạo!", Toast.LENGTH_SHORT).show();
                            deleteItemCart();
                            Intent intent = new Intent(view.getContext(), OrdersManageActivity.class);
                            view.getContext().startActivity(intent);
                        })
                        .addOnFailureListener(e -> {
                            Toast.makeText(view.getContext(), "Tạo đơn hàng thất bại: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                        });
            });
        }).start();
    }

    private void deleteItemCart() {
        DatabaseReference cartRef = FirebaseDatabase.getInstance()
                .getReference("KOS Plus")
                .child("Carts");

        String currentUserId = DataLocalManager.getUid();

        cartRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot itemSnapshot : snapshot.getChildren()) {
                    ItemCarts itemCart = itemSnapshot.getValue(ItemCarts.class);

                    if (itemCart == null) continue;

                    // So sánh đúng user và product được chọn
                    if (currentUserId.equals(itemCart.userId)
                            && productIdList.get().contains(itemCart.productId)) {

                        // Xóa sản phẩm này khỏi giỏ
                        itemSnapshot.getRef().removeValue();
                        Log.d("CartDelete", "Đã xóa sản phẩm: " + itemCart.productId);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("CartDelete", "Lỗi Firebase: " + error.getMessage());
            }
        });
    }
    public interface OrderFormatCallback {
        void onFormatted(String result);
    }

    public void formatOrderDetailsAsync(OrderFormatCallback callback) {
        List<String> lines = new ArrayList<>();
        int[] totalSum = {0};
        AtomicInteger counter = new AtomicInteger(0);

        for (int i = 0; i < productIdList.get().size(); i++) {
            String productId = productIdList.get().get(i);
            int quantity = quantityList.get().get(i);
            int subtotal = priceList.get().get(i);

            totalSum[0] += subtotal;

            FirebaseDatabase.getInstance().getReference("KOS Plus").child("Products").child(productId)
                    .addListenerForSingleValueEvent(new ValueEventListener() {
                        @Override
                        public void onDataChange(@NonNull DataSnapshot snapshot) {
                            Products product = snapshot.getValue(Products.class);
                            String name = (product != null && product.name != null) ? product.name : "Không rõ";

                            synchronized (lines) {
                                lines.add(name + " | "  + " x " + quantity + " = " + subtotal);
                            }

                            if (counter.incrementAndGet() == productIdList.get().size()) {
                                // Tất cả dữ liệu đã xong, format kết quả
                                StringBuilder result = new StringBuilder();
                                for (String line : lines) {
                                    result.append(line).append("\n");
                                }
                                result.append("-----------------------------\n");
                                result.append("Tổng cộng: ").append(totalSum[0]);

                                callback.onFormatted(result.toString());
                            }
                        }

                        @Override
                        public void onCancelled(@NonNull DatabaseError error) {
                            synchronized (lines) {
                                lines.add("Không rõ | " + " x " + quantity + " = " + subtotal);
                            }

                            if (counter.incrementAndGet() == productIdList.get().size()) {
                                StringBuilder result = new StringBuilder();
                                for (String line : lines) {
                                    result.append(line).append("\n");
                                }
                                result.append("-----------------------------\n");
                                result.append("Tổng cộng: ").append(totalSum[0]);

                                callback.onFormatted(result.toString());
                            }
                        }
                    });
        }
    }

    public void setAddress1(String string) {
        address1.set(string);
    }

    public void sendToShop(String time, String code) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Users");
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    Log.e("Users Manage", "UserID: " + dataSnapshot.getKey());
                    Users users = dataSnapshot.getValue(Users.class);

                    if ( users.role.equals("Manager") || users.role.equals("Staff")) {

                        String s = "Mã đơn: " + code + "\n"+ address1.get() + "-" + address2.get() + "\n"  + time + "\n" + users.fullname + "\n" + users.phone;

                        Utils.pushNotification("", "Đơn hàng mới", ""+s, users.id,""+time);
                        OneSignalNotification.sendNotificationToUser(users.id, "Đơn hàng mới",s);
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Users Manage", "Lỗi: " + error.getMessage());
            }
        });
    }
}
