package com.example.kosplus.features;

import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kosplus.R;
import com.example.kosplus.adapter.PromotionAdapter;
import com.example.kosplus.databinding.ActivityPromotionsManageBinding;
import com.example.kosplus.livedata.PromotionsLiveData;

import java.util.ArrayList;

public class PromotionsManageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivityPromotionsManageBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_promotions_manage);
        PromotionsManageVM viewModel = new PromotionsManageVM();
        binding.setPromotionsManage(viewModel);
        binding.executePendingBindings();

        // Cấu hình RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        binding.recyclerView.setHasFixedSize(true);

        PromotionAdapter adapter = new PromotionAdapter(new ArrayList<>());
        binding.recyclerView.setAdapter(adapter);

        // Quan sát dữ liệu từ LiveData
        PromotionsLiveData promotionLiveData = ViewModelProviders.of(this).get(PromotionsLiveData.class);
        promotionLiveData.getLiveData().observe(this, key -> {
            if (key != null && !key.isEmpty()) {
                adapter.updateData(key);
            } else {
                Log.e("PromotionAdapter", "Danh sách trống!");
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
    }
}