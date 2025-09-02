package com.example.kosplus.func;

import static android.content.Context.MODE_PRIVATE;
import static android.widget.Toast.LENGTH_LONG;

import android.Manifest;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import com.example.kosplus.LoginActivity;
import com.example.kosplus.R;
import com.example.kosplus.databinding.CustomDialogDepositBinding;
import com.example.kosplus.databinding.CustomDialogErrorBinding;
import com.example.kosplus.databinding.CustomDialogNotificationBinding;
import com.example.kosplus.databinding.CustomDialogProfileBinding;
import com.example.kosplus.databinding.CustomDialogQrcodeBinding;
import com.example.kosplus.databinding.CustomDialogVerificationBinding;
import com.example.kosplus.datalocal.DataLocalManager;
import com.example.kosplus.features.ProfileEditActivity;
import com.example.kosplus.model.Notifications;
import com.example.kosplus.model.Orders;
import com.example.kosplus.model.TransactionHistory;
import com.example.kosplus.model.Users;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.BarcodeFormat;
import com.google.zxing.WriterException;
import com.google.zxing.common.BitMatrix;
import com.google.zxing.qrcode.QRCodeWriter;
import com.journeyapps.barcodescanner.BarcodeEncoder;
import com.squareup.picasso.Picasso;

import java.net.URL;
import java.net.URLConnection;
import java.text.DecimalFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.CountDownLatch;
import java.util.regex.Pattern;

public class Utils {

    public static void requestPermissions (Context context)  {
        List<String> permissionList = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_MEDIA_IMAGES) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(Manifest.permission.READ_MEDIA_IMAGES);
            }
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.POST_NOTIFICATIONS) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(Manifest.permission.POST_NOTIFICATIONS);
            }
        }else {
            // Android < 13
            if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                permissionList.add(Manifest.permission.READ_EXTERNAL_STORAGE);
            }
        }
        // Camera
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CAMERA) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.CAMERA);
        }

        // Location
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_FINE_LOCATION);
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_COARSE_LOCATION);
        }

        // Wifi
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.ACCESS_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.ACCESS_WIFI_STATE);
        }
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.CHANGE_WIFI_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.CHANGE_WIFI_STATE);
        }

        // Phone
        if (ActivityCompat.checkSelfPermission(context, Manifest.permission.READ_PHONE_STATE) != PackageManager.PERMISSION_GRANTED) {
            permissionList.add(Manifest.permission.READ_PHONE_STATE);
        }

        if (!permissionList.isEmpty()) {
            ActivityCompat.requestPermissions((android.app.Activity) context, permissionList.toArray(new String[permissionList.size()]), 100);
        }
    }
    public static void pushNotification(String imgUrl, String title, String content, String userID, String time ) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference().child("KOS Plus").child("Notifications");
        String UID = databaseReference.push().getKey();

        Notifications notifications = new Notifications(UID, imgUrl, title, content, userID,time, true);
        databaseReference.child(UID).setValue(notifications, new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                if (error == null) {
                }
            }
        });
    }

    public static long getInternetTimeMillis() {
        String[] TIME_SERVERS = {
                "https://www.google.com",
                "https://www.microsoft.com",
                "https://www.baidu.com"
        };

        for (String server : TIME_SERVERS) {
            try {
                URL url = new URL(server);
                URLConnection conn = url.openConnection();
                conn.setConnectTimeout(5000); // tránh chờ lâu
                conn.connect();

                long serverTime = conn.getDate();

                Log.e("getInternetTimeMillis: ", "Lỗi lấy thời gian: " + server + " time"+serverTime);
                if (serverTime > 0) {
                    return serverTime;
                }

            } catch (Exception e) {
                e.printStackTrace(); // tiếp tục thử server khác
            }
        }

        // Nếu tất cả thất bại, dùng thời gian thiết bị
        Log.e("getInternetTimeMillis: ", "Lỗi lấy thời gian");
        return System.currentTimeMillis();
    }
    public static boolean checkTime(String nowStr, String startStr, String endStr) {

        if (nowStr == null || startStr == null || endStr == null ||
                nowStr.trim().isEmpty() || startStr.trim().isEmpty() || endStr.trim().isEmpty()) {
            return false;
        }

        SimpleDateFormat sdf = new SimpleDateFormat("dd/MM/yyyy", Locale.getDefault());
        try {
            // Chuyển đổi chuỗi thành đối tượng Date
            Date nowDate = sdf.parse(nowStr);
            Date startDate = sdf.parse(startStr);
            Date endDate = sdf.parse(endStr);

            if (nowDate == null || startDate == null || endDate == null) {
                Log.e( "checkTime: ", "" + nowDate + " " + startDate + " " + endDate);
                return false; // Nếu có date nào bị null thì trả về false
            }

            // So sánh: now nằm giữa start và end (bao gồm cả hai)
            return !nowDate.before(startDate) && !nowDate.after(endDate);
        } catch (ParseException e) {
            e.printStackTrace();
            return false;
        }
    }
    public static void showProfileDialog(View view, Users users) {
        Dialog dialog = new Dialog(view.getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        CustomDialogProfileBinding binding = CustomDialogProfileBinding.inflate(LayoutInflater.from(view.getContext()));
        dialog.setContentView(binding.getRoot());

        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams win = window.getAttributes();
        win.gravity = Gravity.CENTER;
        window.setAttributes(win);
        dialog.setCancelable(false);

        if (DataLocalManager.getRole().equals("Admin") || DataLocalManager.getUid().equals(users.id)){
            binding.editprofile.setVisibility(View.VISIBLE);
        } else {
            binding.editprofile.setVisibility(View.GONE);
        }

        Picasso.get().load(users.imageUrl).into(binding.imageView);

        binding.userName.setText(users.fullname);
        if (users.sex.equals("male")) {
            binding.userSex.setBackground(ContextCompat.getDrawable(view.getContext(), R.drawable.rounded_male_24));
        } else {
            binding.userSex.setBackground(ContextCompat.getDrawable(view.getContext(), R.drawable.rounded_female_24));
        }
        binding.userRole.setText(users.role);
        binding.userPhone.setText(users.phone);

        binding.userDob.setText(users.dob);

        if (users.status) {
            binding.status.setText("Khóa tài khoản");
        } else {
            binding.status.setText("Mở khóa tài khoản");
        }

        binding.status.setOnClickListener(v -> {
            if (users.status) {
                Utils.showVerificationDialog(v.getContext(),"","","Bạn muốn khóa tài khoản", () -> {
                    DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Users").child(users.id).child("status");
                    databaseReference.setValue(false);
                    dialog.dismiss();
                });
            } else {
                if (DataLocalManager.getRole().equals("Admin") || DataLocalManager.getRole().equals("Manager") || DataLocalManager.getUid().equals(users.id)) {
                    Utils.showVerificationDialog(v.getContext(),"","","Bạn muốn mở khóa tài khoản", () -> {
                        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Users").child(users.id).child("status");
                        databaseReference.setValue(true);
                        dialog.dismiss();
                    });
                } else {
                    Toast.makeText(v.getContext(), "Chỉ admin và manager mới đc quyền mở khóa \n Vui lòng liên hệ chăm sóc khách hàng để được hỗ trợ", LENGTH_LONG).show();
                }
            }
        });

        binding.deposit.setOnClickListener(v -> {
            dialog.dismiss();
            showDepositDialog(v.getContext(), users.id);
        });

        binding.imageQR.setImageBitmap(Utils.convertQRCode(users.id));

        binding.editprofile.setOnClickListener(v -> {
            Intent intent = new Intent(v.getContext(), ProfileEditActivity.class);
            intent.putExtra("ID", users.id);
            v.getContext().startActivity(intent);
        });

        binding.cancel.setOnClickListener(v -> {
            dialog.dismiss();
        });

        dialog.show();
    }


    public static void showDepositDialog(Context context, String id) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        CustomDialogDepositBinding binding = CustomDialogDepositBinding.inflate(LayoutInflater.from(context));
        dialog.setContentView(binding.getRoot());

        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        window.getAttributes().windowAnimations = R.style.DialogAnimationDrop;
        window.setGravity(Gravity.CENTER);
        dialog.setCancelable(false);

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Users").child(id);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users users = snapshot.getValue(Users.class);
                String info = "ID:  "+ users.id + "\n" + "Tên: " + users.fullname + "\n" + "Số điện thoại: " + users.phone;

                binding.infoUser.setText(info);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });

        binding.done.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (binding.amount.getText() == null || binding.amount.getText().toString().isEmpty()) {
                    Toast.makeText(context, "Vui lòng nhập số tiền cần nạp", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (Long.parseLong(binding.amount.getText().toString()) <= 0) {
                    Toast.makeText(context, "Vui lòng nhập số tiền lớn hơn 0", Toast.LENGTH_SHORT).show();
                    return;
                }
                if (binding.description.getText() == null || binding.description.getText().toString().isEmpty()) {
                    Toast.makeText(context, "Vui lòng nhập chi tiết nạp", Toast.LENGTH_SHORT).show();
                    return;
                }
                showVerificationDialog(v.getContext(),"Verification", "Xác nhận nạp tiền "," + "+ Utils.formatCurrencyVND(Long.parseLong(binding.amount.getText().toString())), () ->{
                    // Lấy thời gian mạng
                    new Thread(() -> {
                        long internetTime = Utils.getInternetTimeMillis();
                        if (internetTime <= 0) return;

                        String timeString = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault())
                                .format(new Date(internetTime));

                        new Handler(Looper.getMainLooper()).post(() -> {
                            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("TransactionHistories");
                            String transID = reference.push().getKey();

                            TransactionHistory transactionHistory = new TransactionHistory(transID, DataLocalManager.getUid(), Long.parseLong(binding.amount.getText().toString()), timeString, "deposit", ""+ binding.description.getText(), DataLocalManager.getUid(), true);
                            reference.child(transID).setValue(transactionHistory, new DatabaseReference.CompletionListener() {
                                @Override
                                public void onComplete(@Nullable DatabaseError error, @NonNull DatabaseReference ref) {
                                    OneSignalNotification.sendNotificationToUser(id, "Biến động số dư: +" + Utils.formatCurrencyVND(Long.parseLong(binding.amount.getText().toString())), "Nạp thành công. Vui lòng kiểm tra lịch sử giao dịch");
                                    dialog.dismiss();
                                    updateWallet(context, id, "deposit", Long.parseLong(binding.amount.getText().toString()), timeString);
                                }
                            });
                        });
                    }).start();
                });
            }
        });

        binding.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    };
    public static void updateWallet(Context context, String id, String type, long amount, String time) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Wallets").child(id);
        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (!snapshot.exists()) {
                    // Nếu ID chưa tồn tại -> tạo mới với giá trị
                    databaseReference.setValue(0);
                } else {
                    // Nếu đã tồn tại -> có thể log hoặc xử lý gì đó nếu cần
                    Log.d("Firebase", "ID đã tồn tại: " + id);

                    long currentAmount = snapshot.getValue(Long.class);
                    // "deposit", "payment", "refund"
                    if (type.equals("deposit")) {
                        currentAmount += amount;

                        OneSignalNotification.sendNotificationToUser(id, "Biến động số dư: +" + Utils.formatCurrencyVND(amount), "Nạp thành công. Vui lòng kiểm tra lịch sử giao dịch");
                        Utils.pushNotification("", "Biến động số dư: +" + Utils.formatCurrencyVND(amount), "Nạp thành công. Vui lòng kiểm tra lịch sử giao dịch",id,time);
                    } else if (type.equals("payment")) {
                        currentAmount -= amount;

                        OneSignalNotification.sendNotificationToUser(id, "Biến động số dư: -" + Utils.formatCurrencyVND(amount), "Thanh toán thành công. Vui lòng kiểm tra lịch sử giao dịch");
                        Utils.pushNotification("", "Biến động số dư: -" + Utils.formatCurrencyVND(amount), "Thanh toán thành công. Vui lòng kiểm tra lịch sử giao dịch",id,time);
                    } else if (type.equals("refund")) {
                        currentAmount += amount;

                        OneSignalNotification.sendNotificationToUser(id, "Biến động số dư: +" + Utils.formatCurrencyVND(amount), "Hoàn tiền thành công. Vui lòng kiểm tra lịch sử giao dịch");
                        Utils.pushNotification("", "Biến động số dư: +" + Utils.formatCurrencyVND(amount), "Hoàn tiền thành công. Vui lòng kiểm tra lịch sử giao dịch",id,time);
                    }
                    databaseReference.setValue(currentAmount);
                    Toast.makeText(context, "Cập nhật thành công", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi truy vấn: " + error.getMessage());
            }
        });
    }
    public static void showQRDialog(Context context, String string) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        CustomDialogQrcodeBinding binding = CustomDialogQrcodeBinding.inflate(LayoutInflater.from(context));
        dialog.setContentView(binding.getRoot());

        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        window.getAttributes().windowAnimations = R.style.DialogAnimationDrop;
        window.setGravity(Gravity.CENTER);
        dialog.setCancelable(false);

        binding.imageView.setImageBitmap(Utils.convertQRCode(string));

        binding.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public static void showNotificationDialog(Context context, String imageUrl, String title, String content) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        CustomDialogNotificationBinding binding = CustomDialogNotificationBinding.inflate(LayoutInflater.from(context));
        dialog.setContentView(binding.getRoot());

        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        window.getAttributes().windowAnimations = R.style.DialogAnimationDrop;
        window.setGravity(Gravity.CENTER);
        dialog.setCancelable(false);

        binding.title.setText(title);
        binding.textview.setText(content);

        if (imageUrl == null || imageUrl.isEmpty()) {
            binding.cardView.setVisibility(View.GONE);
            binding.imageView.setVisibility(View.GONE);
        } else {
            Picasso.get().load(imageUrl).into(binding.imageView);
        }

        binding.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }


    public static void showVerificationDialog(Context context, String type, String title, String content, Runnable onSwipeCompleted) {
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        CustomDialogVerificationBinding binding = CustomDialogVerificationBinding.inflate(LayoutInflater.from(context));
        dialog.setContentView(binding.getRoot());

        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams win = window.getAttributes();
        win.gravity = Gravity.CENTER;
        window.setAttributes(win);
        dialog.setCancelable(false);

        // Gán text
        binding.headerTitle.setText(type);
        binding.title.setText(title);
        binding.text.setText(content);

        ImageView swipeButton = dialog.findViewById(R.id.swipeButton);
        View swipeBackground = dialog.findViewById(R.id.swipeBackground);
        TextView swipeText = dialog.findViewById(R.id.swipeText);

        swipeButton.setOnTouchListener(new View.OnTouchListener() {
            float dX;

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                switch (event.getAction()) {
                    case MotionEvent.ACTION_DOWN:
                        dX = v.getX() - event.getRawX();
                        break;
                    case MotionEvent.ACTION_MOVE:
                        v.setX(event.getRawX() + dX);
                        break;
                    case MotionEvent.ACTION_UP:
                        if (v.getX() > swipeText.getWidth() * 0.8f) {
                            onSwipeCompleted.run(); // Gọi chức năng khi vuốt thành công
                            dialog.dismiss();
                        } else {
                            v.animate().translationX(0).setDuration(300).start(); // Trả về vị trí ban đầu
                        }
                        break;
                }
                return true;
            }
        });

        binding.cancel.setOnClickListener(v -> dialog.dismiss());

        dialog.show();
    }

    public static void showError(Context context, String error){
        Dialog dialog = new Dialog(context);
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        CustomDialogErrorBinding binding = CustomDialogErrorBinding.inflate(LayoutInflater.from(context));
        dialog.setContentView(binding.getRoot());

        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));

        window.getAttributes().windowAnimations = R.style.DialogAnimationDrop;
        window.setGravity(Gravity.CENTER);
        dialog.setCancelable(false);

        binding.textview.setText(error);

        binding.cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dialog.dismiss();
            }
        });

        dialog.show();
    }

    public static void checkPhoneExists(String phone, OnPhoneCheckListener listener) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Users");

        databaseReference.orderByChild("phone").equalTo(phone).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists()) {
                    listener.onPhoneExists(true); // Số điện thoại đã tồn tại
                } else {
                    listener.onPhoneExists(false); // Số điện thoại chưa được sử dụng
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                listener.onPhoneExists(false);
            }
        });
    }

    // Interface callback để nhận kết quả
    public interface OnPhoneCheckListener {
        void onPhoneExists(boolean exists);
    }

    // Kiểm tra họ và tên (ít nhất 2 từ)
    public static boolean isValidFullName(String fullName) {
        return fullName != null && fullName.trim().split("\\s+").length >= 2;
    }

    // Kiểm tra email có hợp lệ không
    public static boolean isValidEmail(String email) {
        if (email == null || email.trim().isEmpty()) {
            return false;
        }

        String emailRegex = "^[a-zA-Z0-9._%+-]+@[a-zA-Z0-9.-]+\\.[a-zA-Z]{2,}$";
        return email.matches(emailRegex);
    }

    // Kiểm tra mật khẩu (Ít nhất 8 ký tự, có chữ hoa, chữ thường, số và ký tự đặc biệt)
    public static boolean isValidPassword(String password) {
        if (password == null || password.length() < 8) return false;

        Pattern pattern = Pattern.compile("^(?=.*[a-z])(?=.*[A-Z])(?=.*\\d)(?=.*[@#$%^&+=!]).{6,}$");
        return pattern.matcher(password).matches();
    }

    // Kiểm tra xác nhận mật khẩu có khớp không
    public static boolean isPasswordConfirmed(String password, String confirmPassword) {
        return password != null && password.equals(confirmPassword);
    }

    // Kiểm tra số điện thoại Việt Nam (10 số, bắt đầu bằng 0)
    public static boolean isValidPhoneNumber(String phone) {
        return phone != null && phone.matches("^(0[1-9][0-9]{8,9})$");
    }

    public static Bitmap convertQRCode(String text) {
        try {
            QRCodeWriter qrCodeWriter = new QRCodeWriter();
            BitMatrix bitMatrix = qrCodeWriter.encode(text, BarcodeFormat.QR_CODE, 512, 512);
            BarcodeEncoder barcodeEncoder = new BarcodeEncoder();
            Bitmap bitmap = barcodeEncoder.createBitmap(bitMatrix);
            return bitmap;
        } catch (WriterException e) {
            Log.e("KOS Plus", "Error generating QR code: " + e.getMessage());
            return null;
        }
    }
    public static String formatCurrencyVND(long amount) {
        DecimalFormat formatter = new DecimalFormat("#,###");
        return formatter.format(amount) + " VNĐ";
    }

    public static long parseCurrencyVND(String currency) {
        try {
            // Bỏ " VNĐ" và dấu chấm
            String clean = currency.replace(" VNĐ", "").replace(".", "").trim();
            return Long.parseLong(clean);
        } catch (NumberFormatException e) {
            e.printStackTrace();
            return 0;
        }
    }


    public static void logout(Context context) {
        // Xóa dữ liệu đăng nhập trong SharedPreferences
        SharedPreferences preferences = context.getSharedPreferences("MyPrefsFile", MODE_PRIVATE);
        SharedPreferences.Editor editor = preferences.edit();
        editor.clear(); // Xóa tất cả dữ liệu
        editor.apply();

        // Chuyển về màn hình đăng nhập
        Intent intent = new Intent(context, LoginActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK); // Xóa stack tránh quay lại
        context.startActivity(intent);
    }
}
