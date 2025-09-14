package com.example.kosplus;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.CountDownTimer;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.BindingAdapter;
import androidx.databinding.ObservableField;
import androidx.lifecycle.ViewModel;

import com.example.kosplus.databinding.CustomDialogErrorBinding;
import com.example.kosplus.datalocal.DataLocalManager;
import com.example.kosplus.func.OneSignalNotification;
import com.example.kosplus.func.Utils;
import com.example.kosplus.model.Notifications;
import com.example.kosplus.model.Users;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;
import com.squareup.picasso.Picasso;

public class MainVM extends ViewModel {
    public ObservableField<Users> users= new ObservableField<>();
    public ObservableField<String> token= new ObservableField<>();
    public MainVM() {
        token.set(OneSignal.getUser().getPushSubscription().getId());
        Log.d("MainVM", "MainVM: " + token.get());
       // OneSignalNotification.sendNotificationToUser(DataLocalManager.getUid(), "Đăng nhập", "Đăng nhập thành công");
    }

    public void checkUser(Context context) {
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Users").child(DataLocalManager.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Users users = snapshot.getValue(Users.class);
                if (!users.status){

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

                    binding.textview.setText("Tài khoản của bạn đã bị khóa và buộc đăng xuất sau 5s\n Vui lòng liên hệ chăm sóc khách hàng để được hỗ trợ");

                    binding.cancel.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            dialog.dismiss();
                        }
                    });

                    dialog.show();

                    new CountDownTimer(5000, 1000) {
                        int timeLeft = 5;

                        public void onTick(long millisUntilFinished) {
                            binding.textview.setText("Tài khoản của bạn đã bị khóa và buộc đăng xuất sau " + timeLeft + "s\n Vui lòng liên hệ chăm sóc khách hàng để được hỗ trợ");
                            timeLeft--;
                        }

                        public void onFinish() {
                            if (dialog.isShowing()) {
                                dialog.dismiss();
                                Utils.logout(context); // Gọi hàm đăng xuất
                            }
                        }
                    }.start();
                }else {
                    DataLocalManager.setRole(users.role);
                    if (token.get() == null || token.get().isEmpty())
                    {
                        Log.d("MainVM", "checkUser: Token rỗng");
                    } else if (users.token == null || users.token.isEmpty() || !users.token.equals(token.get())) {
                        databaseReference.child("token").setValue(token.get());
                    } else {
                        Log.d("MainVM", "checkUser: Token không thay đổi");
                    }
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @BindingAdapter({"android:src"})
    public static void setImageView(ImageView imageView, String imgUrl){
        if (imgUrl==null){
            imageView.setImageResource(R.drawable.ic_launcher_background);
        }
        else {
            Picasso.get().load(imgUrl).into(imageView);
        }
    }
}
