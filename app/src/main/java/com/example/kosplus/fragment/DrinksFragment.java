package com.example.kosplus.fragment;

import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SearchView;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;

import com.example.kosplus.R;
import com.example.kosplus.adapter.ProductAdapter;
import com.example.kosplus.databinding.ActivityDrinksFragmentBinding;
import com.example.kosplus.livedata.DrinksLiveData;

import java.util.ArrayList;

public class DrinksFragment extends Fragment {
    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ActivityDrinksFragmentBinding binding = DataBindingUtil.inflate(inflater, R.layout.activity_drinks_fragment,container,false);
        DrinksFragmentVM viewModel = new DrinksFragmentVM();
        binding.setDrinksFragment(viewModel);
        binding.setLifecycleOwner(this);
        binding.executePendingBindings();

        // Cấu hình RecyclerView
        binding.recyclerView.setLayoutManager(new GridLayoutManager(this.getContext(), 2));
        binding.recyclerView.setHasFixedSize(true);

        ProductAdapter adapter = new ProductAdapter(new ArrayList<>());
        binding.recyclerView.setAdapter(adapter);

        // Quan sát dữ liệu từ LiveData
        DrinksLiveData drinkLiveData = ViewModelProviders.of(this).get(DrinksLiveData.class);
        drinkLiveData.getLiveData().observe(this.getViewLifecycleOwner(), key -> {
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
                adapter.filter(query);
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                return false;
            }
        });

        return binding.getRoot();
    }
}