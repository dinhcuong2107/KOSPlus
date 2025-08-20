package com.example.kosplus.features;

import android.Manifest;
import android.app.ProgressDialog;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
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
import com.example.kosplus.datalocal.DataLocalManager;
import com.google.android.material.tabs.TabLayout;

import java.util.ArrayList;

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

            ProgressDialog progressDialog = new ProgressDialog(this);
            progressDialog.setMessage("Đang xử lý...");
            progressDialog.setCancelable(false); // Không cho bấm ra ngoài để tắt
            progressDialog.show();

// Đóng sau 3 giây
            new Handler(Looper.getMainLooper()).postDelayed(() -> {
                if (progressDialog.isShowing()) {
                    progressDialog.dismiss();
                }
            }, 3000); // 3000ms = 3 giây

            // Cấu hình RecyclerView
            binding.recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
            binding.recyclerView.setHasFixedSize(true);

            UserAdapter adapter = new UserAdapter(new ArrayList<>());
            binding.recyclerView.setAdapter(adapter);

            // Quan sát dữ liệu từ ViewModel
            viewModel.getUserList().observe(this, users -> {
                if (users != null && !users.isEmpty()) {
                    adapter.updateData(users);
                    binding.textview.setVisibility(View.GONE);
                    binding.recyclerView.setVisibility(View.VISIBLE);
                } else {
                    binding.recyclerView.setVisibility(View.GONE);
                    binding.textview.setVisibility(View.VISIBLE);
                    Log.e("Users Manage", "Danh sách trống!");
                }
            });
            binding.tablayout.addTab(binding.tablayout.newTab().setText("All"));
            binding.tablayout.addTab(binding.tablayout.newTab().setText("Hoạt động"));
            binding.tablayout.addTab(binding.tablayout.newTab().setText("Khóa"));
            if (DataLocalManager.getRole().equals("Admin")) {
                binding.tablayout.addTab(binding.tablayout.newTab().setText("Manager"));
                binding.tablayout.addTab(binding.tablayout.newTab().setText("Staff"));
                binding.tablayout.addTab(binding.tablayout.newTab().setText("Customer"));
            }

            if (DataLocalManager.getRole().equals("Manager")) {
                binding.tablayout.addTab(binding.tablayout.newTab().setText("Staff"));
                binding.tablayout.addTab(binding.tablayout.newTab().setText("Customer"));
            }

            binding.tablayout.getTabAt(0).select();

            binding.tablayout.addOnTabSelectedListener(new TabLayout.OnTabSelectedListener() {
                @Override
                public void onTabSelected(TabLayout.Tab tab) {
                    binding.searchView.setQuery("",false);
                    viewModel.filterByCategory(tab.getText().toString());
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
                    adapter.getFilter().filter(query);
                    return false;
                }

                @Override
                public boolean onQueryTextChange(String newText) {
                    adapter.getFilter().filter(newText);
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