package com.example.kosplus;

import android.Manifest;
import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.viewpager2.widget.ViewPager2;

import com.example.kosplus.adapter.ViewPagerAdapter;
import com.example.kosplus.databinding.ActivityMainBinding;
import com.example.kosplus.func.DepthPageTransformer;
import com.example.kosplus.livedata.NotificationsLiveData;
import com.example.kosplus.model.Notifications;
import com.google.android.material.badge.BadgeDrawable;
import com.onesignal.OneSignal;

import org.json.JSONArray;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        askNotificationPermission();

        ActivityMainBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        MainVM viewModel = new MainVM();
        binding.setMain(viewModel);
        binding.executePendingBindings();
        binding.setLifecycleOwner(this);

        viewModel.checkUser(this);

        // Bắt buộc: Gọi init sớm nhất có thể
        OneSignal.initWithContext(this, "7859382a-ab69-4f54-817f-89e7e77e88ed");

        binding.bottomnavigation.setOnNavigationItemSelectedListener(item -> {
            View view = binding.bottomnavigation.findViewById(item.getItemId());
            if (view != null) {
                bounceAnimation(view); // Thêm hiệu ứng scale + sáng
            }
            if (item.getItemId() == R.id.item_home) {
                binding.viewpager.setCurrentItem(0);
            } else if (item.getItemId() == R.id.item_drinks) {
                binding.viewpager.setCurrentItem(1);
            } else if (item.getItemId() == R.id.item_foods) {
                binding.viewpager.setCurrentItem(2);
            } else if (item.getItemId() == R.id.item_notifications) {
                binding.viewpager.setCurrentItem(3);
            } else {
                binding.viewpager.setCurrentItem(4);
            }
            return true;
        });

// Gắn badge vào mục "Thông báo" (R.id.notifications)
        BadgeDrawable badge = binding.bottomnavigation.getOrCreateBadge(R.id.item_notifications);
        badge.setVisible(true);  // Hiển thị badge

        NotificationsLiveData liveData = ViewModelProviders.of(this).get(NotificationsLiveData.class);
        liveData.getLiveData().observe(this, new Observer<List<Notifications>>() {
            @Override
            public void onChanged(List<Notifications> notifications) {
                int i = 0;
                for (Notifications notification : notifications) {

                    if (notification.status) {
                        i++;
                    }
                }
                badge.setNumber(i);      // Gán số hiển thị (ví dụ: 5 thông báo)
            }
        });

// Tuỳ chỉnh màu sắc
        badge.setBackgroundColor(ContextCompat.getColor(this, R.color.color_a));
        badge.setBadgeTextColor(ContextCompat.getColor(this, R.color.white));

        ViewPagerAdapter adapter = new ViewPagerAdapter(this);
        binding.viewpager.setAdapter(adapter);
        binding.viewpager.setPageTransformer(new DepthPageTransformer());
        binding.viewpager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                switch (position) {
                    case 0:
                        binding.bottomnavigation.getMenu().findItem(R.id.item_home).setChecked(true);
                        break;
                    case 1:
                        binding.bottomnavigation.getMenu().findItem(R.id.item_drinks).setChecked(true);
                        break;
                    case 2:
                        binding.bottomnavigation.getMenu().findItem(R.id.item_foods).setChecked(true);
                        break;
                    case 3:
                        binding.bottomnavigation.getMenu().findItem(R.id.item_notifications).setChecked(true);
                        break;
                    case 4:
                        binding.bottomnavigation.getMenu().findItem(R.id.item_settings).setChecked(true);
                        break;
                }
            }
        });
    }

    private void bounceAnimation(View view) {
        ObjectAnimator scaleX = ObjectAnimator.ofFloat(view, "scaleX", 1.0f, 1.2f, 1.0f);
        ObjectAnimator scaleY = ObjectAnimator.ofFloat(view, "scaleY", 1.0f, 1.2f, 1.0f);
        ObjectAnimator translateY = ObjectAnimator.ofFloat(view, "translationY", 0, -20, 0);

        scaleX.setDuration(300);
        scaleY.setDuration(300);
        translateY.setDuration(300);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleX, scaleY, translateY);
        animatorSet.start();
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

    // Declare the launcher at the top of your Activity/Fragment:
    private final ActivityResultLauncher<String> requestPermissionLauncher =
            registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {
                if (isGranted) {
                    // FCM SDK (and your app) can post notifications.
                } else {
                    // TODO: Inform user that that your app will not show notifications.
                }
            });

    private void askNotificationPermission() {
        // This is only necessary for API level >= 33 (TIRAMISU)
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.POST_NOTIFICATIONS) ==
                    PackageManager.PERMISSION_GRANTED) {
                // FCM SDK (and your app) can post notifications.
            } else if (shouldShowRequestPermissionRationale(Manifest.permission.POST_NOTIFICATIONS)) {
                // TODO: display an educational UI explaining to the user the features that will be enabled
                //       by them granting the POST_NOTIFICATION permission. This UI should provide the user
                //       "OK" and "No thanks" buttons. If the user selects "OK," directly request the permission.
                //       If the user selects "No thanks," allow the user to continue without notifications.
            } else {
                // Directly ask for the permission
                requestPermissionLauncher.launch(Manifest.permission.POST_NOTIFICATIONS);
            }
        }
    }
}