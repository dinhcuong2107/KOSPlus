package com.example.kosplus.features;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kosplus.R;
import com.example.kosplus.adapter.CartAdapter;
import com.example.kosplus.databinding.ActivityCartsBinding;
import com.example.kosplus.livedata.ItemCartsLiveData;
import com.example.kosplus.model.Shops;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.List;

public class CartsActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivityCartsBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_carts);

        CartsVM viewModel = new ViewModelProvider(this).get(CartsVM.class);
        binding.setItemcart(viewModel);
        binding.executePendingBindings();

        // Cấu hình RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        binding.recyclerView.setHasFixedSize(true);

        // Khởi tạo Adapter
        CartAdapter adapter = new CartAdapter(new ArrayList<>(), new CartAdapter.OnCartDataChangedListener() {
            @Override
            public void onCartDataChanged(List<String> productIds, List<Integer> quantities, List<Integer> finalPrices) {
                int total = 0;
                for (int p : finalPrices) total += p;

                binding.done.setText("Mua (" + total + "VNĐ)");

                // Nếu muốn dùng khi thanh toán
                viewModel.setData(productIds, quantities, finalPrices);

                // Nếu muốn dùng khi đặt hàng
//                this.productIdList = productIds;
//                this.quantityList = quantities;
//                this.finalPriceList = finalPrices;
            }
        });
        binding.recyclerView.setAdapter(adapter);

        // Quan sát dữ liệu từ LiveData
        ItemCartsLiveData liveData = ViewModelProviders.of(this).get(ItemCartsLiveData.class);
        liveData.getLiveData().observe(this, key -> {
            if (key != null && !key.isEmpty()) {
                adapter.updateData(key);
                binding.recyclerView.setVisibility(View.VISIBLE);
                binding.textview.setVisibility(View.GONE);

                Log.e("ItemCart Manage", "" + key.size());
            } else {
                binding.recyclerView.setVisibility(View.GONE);
                binding.textview.setVisibility(View.VISIBLE);
                Log.e("ItemCart Manage", "Danh sách trống!");
            }
        });

        List<String> codeList = new ArrayList<>();
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(CartsActivity.this, android.R.layout.simple_spinner_item, codeList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinner.setAdapter(arrayAdapter);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Shops");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                if (snapshot.exists())
                {
                    codeList.clear();
                    for (DataSnapshot dataSnapshot: snapshot.getChildren()) {
                        Shops shop = dataSnapshot.getValue(Shops.class);
                        if (shop != null && shop.status) {
                            codeList.add(shop.id);
                        }
                    }
                    arrayAdapter.notifyDataSetChanged();
                }

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
                viewModel.setAddress1(selectedCode);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {}
        });
    }
}