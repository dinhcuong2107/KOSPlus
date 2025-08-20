package com.example.kosplus;

import android.content.Context;
import android.os.CountDownTimer;
import android.util.Log;
import android.widget.ImageView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import androidx.databinding.ObservableField;
import androidx.lifecycle.ViewModel;

import com.example.kosplus.datalocal.DataLocalManager;
import com.example.kosplus.func.OneSignalNotification;
import com.example.kosplus.func.Utils;
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
                    Utils.showError(context, "Tài khoản của bạn đã bị định chỉnh\n Bạn sẽ buộc đăng xuất trong vòng 5s");
                    new CountDownTimer(5000, 1000) { // Đếm ngược từ 5 giây
                        public void onTick(long millisUntilFinished) {
                            Toast.makeText(context, "Đăng xuất sau: " + millisUntilFinished / 1000 + "s", Toast.LENGTH_LONG).show();
                            Log.d("Countdown", "Đăng xuất sau: " + millisUntilFinished / 1000 + "s");
                        }

                        public void onFinish() {
                            Utils.logout(context); // Gọi hàm đăng xuất
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
