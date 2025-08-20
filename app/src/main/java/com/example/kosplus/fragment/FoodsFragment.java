package com.example.kosplus.fragment;

import android.graphics.Rect;
import android.os.Bundle;
import android.util.Log;
import android.util.TypedValue;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.SearchView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import com.example.kosplus.R;
import com.example.kosplus.adapter.ProductAdapter;
import com.example.kosplus.databinding.ActivityFoodsFragmentBinding;
import com.example.kosplus.livedata.FoodsLiveData;

import java.util.ArrayList;

public class FoodsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ActivityFoodsFragmentBinding binding = DataBindingUtil.inflate(inflater, R.layout.activity_foods_fragment, container, false);
        binding.setFoodsFragment(new FoodsFragmentVM());
        binding.setLifecycleOwner(this);
        binding.executePendingBindings();

        // Cấu hình RecyclerView
        binding.recyclerView.setLayoutManager(new GridLayoutManager(this.getContext(), 2));
        binding.recyclerView.setHasFixedSize(true);

        ProductAdapter adapter = new ProductAdapter(new ArrayList<>());
        binding.recyclerView.setAdapter(adapter);

        // Quan sát dữ liệu từ LiveData
        FoodsLiveData foodLiveData = ViewModelProviders.of(this).get(FoodsLiveData.class);
        foodLiveData.getLiveData().observe(this.getViewLifecycleOwner(), key -> {
            if (key != null && !key.isEmpty()) {
                adapter.updateData(key);
                binding.recyclerView.setVisibility(View.VISIBLE);
                binding.textview.setVisibility(View.GONE);
            } else {
                binding.recyclerView.setVisibility(View.GONE);
                binding.textview.setVisibility(View.VISIBLE);
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

        return binding.getRoot();
    }
}