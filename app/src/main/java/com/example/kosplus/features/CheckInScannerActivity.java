package com.example.kosplus.features;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.budiyev.android.codescanner.CodeScanner;
import com.example.kosplus.R;
import com.example.kosplus.adapter.DateAdapter;
import com.example.kosplus.databinding.ActivityCheckInScannerBinding;
import com.example.kosplus.func.Utils;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CheckInScannerActivity extends AppCompatActivity {
    private CodeScanner mCodeScanner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivityCheckInScannerBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_check_in_scanner);
        CheckInScannerVM viewModel = new ViewModelProvider(this).get(CheckInScannerVM.class);
        binding.setCheckinScanner(viewModel);
        binding.executePendingBindings();

        mCodeScanner = new CodeScanner(this, binding.scannerView);

        mCodeScanner.setDecodeCallback(result -> runOnUiThread(() -> {
            if (result.getText() == null || result.getText().trim().isEmpty()) {
                Toast.makeText(this, "Không quét được mã QR, vui lòng thử lại!", Toast.LENGTH_SHORT).show();
            } else {
                Log.d("Scan result", result.getText());
                mCodeScanner.stopPreview();
                viewModel.setCheckIn(binding.getRoot(), result.getText());
                binding.scannerView.setVisibility(View.GONE);
            }
        }));

        // Cấu hình RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        binding.recyclerView.setHasFixedSize(true);

        // Lấy thời gian mạng và xử lý
        new Thread(() -> {
            long internetTime = Utils.getInternetTimeMillis();
            if (internetTime <= 0) return;

            // Tạo list 50 ngày
            List<String> dateList = new ArrayList<>();
            SimpleDateFormat sdf = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
            Calendar calendar = Calendar.getInstance();
            calendar.setTimeInMillis(internetTime);

            for (int i = 0; i < 50; i++) {
                dateList.add(sdf.format(calendar.getTime()));
                calendar.add(Calendar.DAY_OF_YEAR, -1); // quay ngược 1 ngày
            }

            // Set vào UI thread
            new Handler(Looper.getMainLooper()).post(() -> {
                DateAdapter adapter = new DateAdapter(dateList);
                binding.recyclerView.setAdapter(adapter);
            });
        }).start();

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