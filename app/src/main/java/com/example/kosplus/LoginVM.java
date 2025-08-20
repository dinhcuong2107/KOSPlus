package com.example.kosplus;

import static android.widget.Toast.LENGTH_LONG;

import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.lifecycle.ViewModel;

import com.example.kosplus.databinding.CustomDialogLoadingBinding;
import com.example.kosplus.datalocal.DataLocalManager;
import com.example.kosplus.func.Utils;
import com.example.kosplus.model.Users;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;

public class LoginVM extends ViewModel {
    public ObservableField<String> phone = new ObservableField<>();
    public ObservableField<String> name = new ObservableField<>();
    public ObservableField<String> password = new ObservableField<>();
    public LoginVM() {
        if (DataLocalManager.getUid() != ""){
            DatabaseReference reference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Users").child(DataLocalManager.getUid());
            reference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    Users users = snapshot.getValue(Users.class);
                    if (users != null && users.status) {
                        phone.set(users.phone);
                        name.set("Xin chào " + users.fullname);
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else {
            phone.set("");
            name.set("");
        }
    }
    public void onClickLogin(View view){
        Dialog dialog = new Dialog(view.getContext());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        CustomDialogLoadingBinding binding = CustomDialogLoadingBinding.inflate(LayoutInflater.from(view.getContext()));
        dialog.setContentView(binding.getRoot());

        Window window = dialog.getWindow();
        window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        WindowManager.LayoutParams win = window.getAttributes();
        win.gravity = Gravity.CENTER;
        window.setAttributes(win);
        dialog.setCancelable(false);

        dialog.show();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Users");
        databaseReference.orderByChild("phone").equalTo(phone.get())
                .addListenerForSingleValueEvent(new ValueEventListener() {
                    @Override
                    public void onDataChange(@NonNull DataSnapshot snapshot) {
                        if (snapshot.exists()) {
                            for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                                Users users = dataSnapshot.getValue(Users.class);
                                if (users != null && users.password.equals(password.get())) {

                                    if (users.status) {
                                        // Đăng nhập thành công
                                        dialog.dismiss();
                                        Toast.makeText(view.getContext(),"Thành công",LENGTH_LONG).show();
                                        DataLocalManager.setUid(dataSnapshot.getKey());
                                        DataLocalManager.setRole(users.role);
                                        DataLocalManager.setFirstInstall(false);
                                        onNextIntentMain(view);
                                        return;
                                    } else {
                                        dialog.dismiss();
                                        Utils.showError(view.getContext(), "Tài khoản đã bị khóa!");
                                    }

                                }
                            }
                            // Sai mật khẩu
                            dialog.dismiss();
                            Utils.showError(view.getContext(), "Mật khẩu không đúng!");
                        } else {
                            // Số điện thoại không tồn tại
                            dialog.dismiss();
                            Utils.showError(view.getContext(), "Số điện thoại chưa được đăng ký!");
                        }
                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError error) {
                        dialog.dismiss();
                        Utils.showError(view.getContext(), "Thử lại \n Lỗi: " + error.getMessage());
                    }
                });

    }
    public void onNextIntentMain(View view){
        Intent intent = new Intent(view.getContext(),MainActivity.class);
        view.getContext().startActivity(intent);
    }
    public void onNextIntentRegister(View view){
        Intent intent = new Intent(view.getContext(),RegisterActivity.class);
        view.getContext().startActivity(intent);
    }
}
