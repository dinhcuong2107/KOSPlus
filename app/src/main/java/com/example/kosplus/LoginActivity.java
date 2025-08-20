package com.example.kosplus;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;

import com.example.kosplus.databinding.ActivityLoginBinding;
import com.example.kosplus.datalocal.DataLocalManager;
import com.example.kosplus.func.Utils;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        ActivityLoginBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_login);
        LoginVM viewModel = new LoginVM();
        binding.setLogin(viewModel);
        binding.setLifecycleOwner(this);
        binding.executePendingBindings();

        if (DataLocalManager.isFirstInstall()) {
            Utils.requestPermissions(this);
        }
    }

    @Override
    public void onBackPressed() {
        new AlertDialog.Builder(this)
                .setTitle("Thoát ứng dụng")
                .setMessage("Bạn có chắc chắn muốn thoát không?")
                .setPositiveButton("Có", (dialog, which) -> finishAffinity()) // Thoát app
                .setNegativeButton("Không", null) // Đóng dialog, không làm gì
                .show();
    }
}