package com.example.kosplus.features;

import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
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
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.kosplus.R;
import com.example.kosplus.adapter.DateAdapter;
import com.example.kosplus.databinding.ActivityCheckInCodeBinding;
import com.example.kosplus.func.Utils;
import com.example.kosplus.model.Shops;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;

public class CheckInCodeActivity extends AppCompatActivity {
    List<String> codeList = new ArrayList<>();
    List<Shops> shopList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivityCheckInCodeBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_check_in_code);
        CheckInCodeVM viewModel = new CheckInCodeVM();
        binding.setCheckinCode(viewModel);
        binding.setLifecycleOwner(this);

        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(CheckInCodeActivity.this, android.R.layout.simple_spinner_item, codeList);
        arrayAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.spinner.setAdapter(arrayAdapter);

        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Shops");
        ref.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                codeList.clear();
                if (snapshot.exists()) {
                    for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                        Shops shop = dataSnapshot.getValue(Shops.class);
                        if (shop != null) {
                            codeList.add(shop.id);
                            shopList.add(shop);
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
                Log.d("CheckInCode", "Mã: " + selectedCode);
                viewModel.setSelectedShop(selectedCode);

                String text = shopList.get(position).id + "\n" + shopList.get(position).address + "\n" + shopList.get(position).bankName + "\n" + shopList.get(position).bankNumber;
                Log.d("CheckInCode", "onDataChange: " + text);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
            }
        });

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
    }
}
