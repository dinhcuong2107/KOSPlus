package com.example.kosplus.features;

import android.Manifest;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.budiyev.android.codescanner.CodeScanner;
import com.example.kosplus.R;
import com.example.kosplus.adapter.UserAdapter;
import com.example.kosplus.databinding.ActivityUsersManageBinding;
import com.example.kosplus.databinding.CustomDialogLoadingBinding;
import com.example.kosplus.datalocal.DataLocalManager;
import com.google.android.material.badge.BadgeDrawable;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;
import java.util.Map;

public class UsersManageActivity extends AppCompatActivity {
        private CodeScanner mCodeScanner;
        ActivityUsersManageBinding binding;
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            EdgeToEdge.enable(this);

            binding = DataBindingUtil.setContentView(this, R.layout.activity_users_manage);
            UsersManageVM viewModel = new ViewModelProvider(this).get(UsersManageVM.class);
            binding.setUsersmanage(viewModel);
            binding.executePendingBindings();

            Dialog dialog = new Dialog(this);
            dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
            CustomDialogLoadingBinding dialogLoadingBinding = CustomDialogLoadingBinding.inflate(LayoutInflater.from(this));
            dialog.setContentView(dialogLoadingBinding.getRoot());

            Window window = dialog.getWindow();
            window.setLayout(WindowManager.LayoutParams.MATCH_PARENT,WindowManager.LayoutParams.WRAP_CONTENT);
            window.setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
            WindowManager.LayoutParams win = window.getAttributes();
            win.gravity = Gravity.CENTER;
            window.setAttributes(win);
            dialog.setCancelable(false);

            dialog.show();

// Đóng sau 3 giây
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (dialog.isShowing()) {
                    dialog.dismiss();
                }
            }, 1000); // 1000ms = 1 giây

            // Cấu hình RecyclerView
            binding.recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
            binding.recyclerView.setHasFixedSize(true);

            UserAdapter adapter = new UserAdapter(new ArrayList<>());
            binding.recyclerView.setAdapter(adapter);

            viewModel.filteredUsers.observe(this, users -> {
                if (users == null || users.isEmpty()) {
                    binding.recyclerView.setVisibility(View.GONE);
                    binding.textview.setVisibility(View.VISIBLE);
                    Log.e("Users Manage", "Danh sách trống!");
                } else {
                    adapter.updateData(users); // cập nhật RecyclerView
                    binding.textview.setVisibility(View.GONE);
                    binding.recyclerView.setVisibility(View.VISIBLE);
                }

            });


            binding.tablayout.addTab(binding.tablayout.newTab().setText("Tất cả"));
            binding.tablayout.addTab(binding.tablayout.newTab().setText("Hoạt động"));
            binding.tablayout.addTab(binding.tablayout.newTab().setText("Khóa"));

            if (DataLocalManager.getRole().equals("Admin")) {
                binding.tablayout.addTab(binding.tablayout.newTab().setText("Manager"));
                binding.tablayout.addTab(binding.tablayout.newTab().setText("Staff"));
                binding.tablayout.addTab(binding.tablayout.newTab().setText("Customer"));

                viewModel.getCountByRole("Manager").observe(this, count -> {
                    TabLayout.Tab tab = binding.tablayout.getTabAt(3);
                    if (tab != null) {
                        BadgeDrawable badge = tab.getOrCreateBadge();
                        badge.setVisible(count > 0);
                        badge.setNumber(count);
                    }
                });

                viewModel.getCountByRole("Staff").observe(this, count -> {
                    TabLayout.Tab tab = binding.tablayout.getTabAt(4);
                    if (tab != null) {
                        BadgeDrawable badge = tab.getOrCreateBadge();
                        badge.setVisible(count > 0);
                        badge.setNumber(count);
                    }
                });

                viewModel.getCountByRole("Customer").observe(this, count -> {
                    TabLayout.Tab tab = binding.tablayout.getTabAt(5);
                    if (tab != null) {
                        BadgeDrawable badge = tab.getOrCreateBadge();
                        badge.setVisible(count > 0);
                        badge.setNumber(count);
                    }
                });

            }

            if (DataLocalManager.getRole().equals("Manager")) {
                binding.tablayout.addTab(binding.tablayout.newTab().setText("Staff"));
                binding.tablayout.addTab(binding.tablayout.newTab().setText("Customer"));

                viewModel.getCountByRole("Staff").observe(this, count -> {
                    TabLayout.Tab tab = binding.tablayout.getTabAt(3);
                    if (tab != null) {
                        BadgeDrawable badge = tab.getOrCreateBadge();
                        badge.setVisible(count > 0);
                        badge.setNumber(count);
                    }
                });

                viewModel.getCountByRole("Customer").observe(this, count -> {
                    TabLayout.Tab tab = binding.tablayout.getTabAt(4);
                    if (tab != null) {
                        BadgeDrawable badge = tab.getOrCreateBadge();
                        badge.setVisible(count > 0);
                        badge.setNumber(count);
                    }
                });
            }

            for (int i = 0; i < binding.tablayout.getTabCount(); i++) {
                TabLayout.Tab tab = binding.tablayout.getTabAt(i);
                if (tab != null) {
                    BadgeDrawable badge = tab.getOrCreateBadge();
                    badge.setVisible(true);
                    badge.setBackgroundColor(ContextCompat.getColor(this, R.color.color_a)); // màu badge
                    badge.setBadgeTextColor(Color.WHITE); // màu text
                    badge.setNumber(0); // mặc định
                }
            }

            viewModel.getCountAll().observe(this, count -> {
                TabLayout.Tab tab = binding.tablayout.getTabAt(0);
                if (tab != null) {
                    BadgeDrawable badge = tab.getOrCreateBadge();
                    badge.setVisible(count > 0);
                    badge.setNumber(count);
                }
            });

            viewModel.getCountActive().observe(this, count -> {
                TabLayout.Tab tab = binding.tablayout.getTabAt(1);
                if (tab != null) {
                    BadgeDrawable badge = tab.getOrCreateBadge();
                    badge.setVisible(count > 0);
                    badge.setNumber(count);
                }
            });

            viewModel.getCountBlocked().observe(this, count -> {
                TabLayout.Tab tab = binding.tablayout.getTabAt(2);
                if (tab != null) {
                    BadgeDrawable badge = tab.getOrCreateBadge();
                    badge.setVisible(count > 0);
                    badge.setNumber(count);
                }
            });

            binding.tablayout.getTabAt(0).select();

            binding.tablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    binding.searchView.setQuery("",false);
                    viewModel.setCategory(tab.getText().toString());
                }

                @Override
                public void onTabUnselected(TabLayout.Tab tab) {

                }

                @Override
                public void onTabReselected(TabLayout.Tab tab) {

                }
            });

            binding.searchView.clearFocus();
            binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
                @Override
                public boolean onQueryTextSubmit(String query) {
                    adapter.filter(query);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    adapter.filter(newText);
                    return false;
                }
            });

            mCodeScanner = new CodeScanner(this, binding.scannerView);

            mCodeScanner.setDecodeCallback(result -> runOnUiThread(() -> {
                if (result.getText() == null || result.getText().trim().isEmpty()) {
                    Toast.makeText(this, "Không quét được mã QR, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
                } else {
                    Log.d("Scan result", result.getText());
                    mCodeScanner.stopPreview();

                    binding.searchView.setQuery(result.getText(), false);

                    binding.scannerView.setVisibility(View.GONE);
                }
            }));

            binding.btnScan.setOnClickListener(v -> {
                if (ContextCompat.checkSelfPermission(this, Manifest.permission.CAMERA)
                        != PackageManager.PERMISSION_GRANTED) {
                    ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CAMERA}, 101);
                } else {
                    binding.scannerView.setVisibility(View.VISIBLE);
                    mCodeScanner.startPreview(); // Bắt đầu quét
                }
            });
        }
        @Override
        protected void onResume() {
            super.onResume();
            mCodeScanner.startPreview();
        }

        @Override
        protected void onPause() {
            mCodeScanner.releaseResources();
            super.onPause();
        }
    }