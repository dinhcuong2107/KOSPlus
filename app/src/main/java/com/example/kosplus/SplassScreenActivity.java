package com.example.kosplus;

import android.content.Intent;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;

import com.example.kosplus.databinding.ActivitySplassScreenBinding;

public class SplassScreenActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivitySplassScreenBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_splass_screen);

        // 1. Logo zoom nhẹ + fade-in
        binding.logo.setScaleX(0.5f);
        binding.logo.setScaleY(0.5f);
        binding.logo.setAlpha(0f);

        binding.logo.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(1000)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // 2. appName: zoom nhẹ + fade + bounce
        binding.name.setScaleX(0.5f);
        binding.name.setScaleY(0.5f);
        binding.name.setAlpha(0f);
        binding.name.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setStartDelay(900)
                .setDuration(800)
                .setInterpolator(new OvershootInterpolator()) // hiệu ứng nảy nhẹ
                .start();

        // 3. slogan: slide từ dưới lên + fade-in
        binding.slogan.setTranslationY(100);
        binding.slogan.setAlpha(0f);
        binding.slogan.animate()
                .translationY(0)
                .alpha(1f)
                .setStartDelay(1600)
                .setDuration(700)
                .setInterpolator(new DecelerateInterpolator())
                .start();
        // Chuyển sau 3 giây
        new Handler().postDelayed(() -> {
            startActivity(new Intent(this, LoginActivity.class));
            finish();
        }, 3000);

    }
}