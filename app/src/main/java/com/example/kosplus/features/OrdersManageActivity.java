package com.example.kosplus.features;

import android.Manifest;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.budiyev.android.codescanner.CodeScanner;
import com.example.kosplus.MainActivity;
import com.example.kosplus.R;
import com.example.kosplus.adapter.OrderAdapter;
import com.example.kosplus.databinding.ActivityOrdersManageBinding;
import com.example.kosplus.datalocal.DataLocalManager;
import com.example.kosplus.livedata.OrdersLiveData;
import com.example.kosplus.model.Shops;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class OrdersManageActivity extends AppCompatActivity {
    private CodeScanner mCodeScanner;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivityOrdersManageBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_orders_manage);
        OrdersManageVM viewModel = new ViewModelProvider(this).get(OrdersManageVM.class);
        binding.setOrdersManage(viewModel);
        binding.executePendingBindings();

        if (DataLocalManager.getRole().equals("Customer")) {
            binding.btnScan.setVisibility(View.GONE);
            binding.scannerView.setVisibility(View.GONE);
        }

        // Cấu hình RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        binding.recyclerView.setHasFixedSize(true);

        OrderAdapter adapter = new OrderAdapter(new ArrayList<>());
        binding.recyclerView.setAdapter(adapter);

        // Quan sát dữ liệu từ LiveData
        OrdersLiveData orderLiveData = ViewModelProviders.of(this).get(OrdersLiveData.class);
        orderLiveData.getLiveData().observe(this, key -> {
            if (key != null && !key.isEmpty()) {
                adapter.updateData(key);
                binding.recyclerView.setVisibility(View.VISIBLE);
                binding.text.setVisibility(View.GONE);
            } else {
                binding.recyclerView.setVisibility(View.GONE);
                binding.text.setVisibility(View.VISIBLE);
                Log.e("Order Manage", "Danh sách trống!");
            }
        });

        List<String> codeList = new ArrayList<>();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, codeList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinner.setAdapter(arrayAdapter);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Shops");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                codeList.clear();
                for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                    Shops shop = dataSnapshot.getValue(Shops.class);
                    if (shop != null) {
                        codeList.add(shop.id);
                    }
                }
                arrayAdapter.notifyDataSetChanged();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Log.e("Firebase", "Lỗi tải cơ sở: " + error.getMessage());
            }
        });

        binding.spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedCode = codeList.get(position);
                Log.d("CODE_SELECTED", "Mã: " + selectedCode);
                orderLiveData.setCode(selectedCode);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
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
            Log.d("SCAN", "SCAN");
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

    @Override
    public void onBackPressed() {
        Intent intent = new Intent(this, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }
}