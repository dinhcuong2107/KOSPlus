package com.example.kosplus.features;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kosplus.R;
import com.example.kosplus.adapter.ShopAdapter;
import com.example.kosplus.databinding.ActivityShopsManageBinding;

import java.util.ArrayList;

public class ShopsManageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivityShopsManageBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_shops_manage);
        ShopsManageVM viewModel = new ViewModelProvider(this).get(ShopsManageVM.class);
        binding.setShopsMamage(viewModel);
        binding.executePendingBindings();

        // Cấu hình RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        binding.recyclerView.setHasFixedSize(true);

        ShopAdapter adapter = new ShopAdapter(new ArrayList<>());
        binding.recyclerView.setAdapter(adapter);

        // Quan sát dữ liệu từ ViewModel
        viewModel.getShopList().observe(this, key -> {
            if (key != null && !key.isEmpty()) {
                adapter.updateData(key);
            } else {
                Log.e("Store Manage", "Danh sách trống!");
            }
        });
    }
}