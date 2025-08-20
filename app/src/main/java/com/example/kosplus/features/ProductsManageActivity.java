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
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.kosplus.R;
import com.example.kosplus.adapter.ProductAdapter;
import com.example.kosplus.databinding.ActivityProductsManageBinding;
import com.example.kosplus.livedata.ProductsLiveData;

import java.util.ArrayList;

public class ProductsManageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivityProductsManageBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_products_manage);
        ProductsManageVM viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @Override
            public <T extends ViewModel> T create(Class<T> modelClass) {
                return (T) new ProductsManageVM(getApplication(), getActivityResultRegistry());
            }
        }).get(ProductsManageVM.class);

        binding.setProductsMamage(viewModel);
        binding.executePendingBindings();

        // Cấu hình RecyclerView
        binding.recyclerView.setLayoutManager(new GridLayoutManager(this, 2));
        binding.recyclerView.setHasFixedSize(true);

        ProductAdapter adapter = new ProductAdapter(new ArrayList<>());
        binding.recyclerView.setAdapter(adapter);

        // Quan sát dữ liệu từ LiveData
        ProductsLiveData productLiveData = ViewModelProviders.of(this).get(ProductsLiveData.class);
        productLiveData.getLiveData().observe(this, key -> {
            if (key != null && !key.isEmpty()) {
                adapter.updateData(key);
            } else {
                Log.e("Product Manage", "Danh sách trống!");
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
    }
}