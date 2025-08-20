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
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kosplus.R;
import com.example.kosplus.adapter.CalendarRevenueAdapter;
import com.example.kosplus.adapter.ProductOrderAdapter;
import com.example.kosplus.databinding.ActivitySalesSummaryBinding;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SalesSummaryActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        ActivitySalesSummaryBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_sales_summary);
        SalesSummaryVM viewModel = new ViewModelProvider(this).get(SalesSummaryVM.class);
        binding.setSalessummary(viewModel);
        binding.setLifecycleOwner(this);
        binding.executePendingBindings();

        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH) + 1; // Java đếm từ 0
        int currentYear = calendar.get(Calendar.YEAR);

        binding.currentmonth.setText("Tháng " + currentMonth + ", " + currentYear);
        binding.lastmonth.setText("Tháng " + (currentMonth - 1) + ", " + currentYear);

        viewModel.getDailyRevenueInMonth(currentMonth, currentYear, data -> {
            int daysInMonthCount = YearMonth.of(currentYear, currentMonth).lengthOfMonth();

            List<Integer> daysInMonth = new ArrayList<>();
            List<Integer> revenuePerDay = new ArrayList<>();

            for (int day = 1; day <= daysInMonthCount; day++) {
                daysInMonth.add(day);
                int revenue = data.getOrDefault(day, 0);
                revenuePerDay.add(revenue);
            }


            binding.calendarRecyclerView.setLayoutManager(new GridLayoutManager(this, 7));
            binding.calendarRecyclerView.setAdapter(new CalendarRevenueAdapter(daysInMonth, revenuePerDay));

        });
        viewModel.getMonthlySalesWithRevenueRaw(currentMonth, currentYear, new SalesSummaryVM.OnResultCallback() {
            @Override
            public void onResult(Map<String, int[]> data) {
                if (data == null || data.isEmpty()) {
                    Log.d("TOP_PRODUCT", "Không có dữ liệu.");
                    return;
                }

                // Chuyển map thành list để sắp xếp
                List<Map.Entry<String, int[]>> sortedList = new ArrayList<>(data.entrySet());

                // Sắp xếp theo số lượng giảm dần (index 0 là quantity)
                Collections.sort(sortedList, (e1, e2) -> Integer.compare(e2.getValue()[0], e1.getValue()[0]));

                // Khởi tạo 3 danh sách
                List<String> idList = new ArrayList<>();
                List<Integer> quantityList = new ArrayList<>();
                List<Integer> revenueList = new ArrayList<>();

                // Lặp để lấy từng phần tử và thêm vào 3 list
                for (Map.Entry<String, int[]> entry : sortedList) {
                    String productId = entry.getKey();
                    int quantity = entry.getValue()[0];
                    int revenue = entry.getValue()[1];

                    Log.d("TOP_PRODUCT", "SP: " + productId + " | SL: " + quantity + " | Doanh thu: " + revenue);

                    idList.add(productId);
                    quantityList.add(quantity);
                    revenueList.add(revenue);
                }
                // salesAdapter.setData(idList, quantityList, revenueList);
                // Cấu hình RecyclerView
                binding.recyclerView.setLayoutManager(new LinearLayoutManager(SalesSummaryActivity.this, RecyclerView.VERTICAL, false));
                binding.recyclerView.setHasFixedSize(true);

                ProductOrderAdapter adapter = new ProductOrderAdapter(idList, quantityList, revenueList);
                binding.recyclerView.setAdapter(adapter);
            }

            @Override
            public void onError(String error) {
                Log.e("SALES_ERROR", error);
            }
        });

        viewModel.getMonthlySalesWithRevenueRaw(currentMonth-1, currentYear, new SalesSummaryVM.OnResultCallback() {
            @Override
            public void onResult(Map<String, int[]> data) {
                if (data == null || data.isEmpty()) {
                    Log.d("TOP_PRODUCT", "Không có dữ liệu.");
                    return;
                }

                // Chuyển map thành list để sắp xếp
                List<Map.Entry<String, int[]>> sortedList = new ArrayList<>(data.entrySet());

                // Sắp xếp theo số lượng giảm dần (index 0 là quantity)
                Collections.sort(sortedList, (e1, e2) -> Integer.compare(e2.getValue()[0], e1.getValue()[0]));

                // Khởi tạo 3 danh sách
                List<String> idList = new ArrayList<>();
                List<Integer> quantityList = new ArrayList<>();
                List<Integer> revenueList = new ArrayList<>();

                // Lặp để lấy từng phần tử và thêm vào 3 list
                for (Map.Entry<String, int[]> entry : sortedList) {
                    String productId = entry.getKey();
                    int quantity = entry.getValue()[0];
                    int revenue = entry.getValue()[1];

                    Log.d("TOP_PRODUCT", "SP: " + productId + " | SL: " + quantity + " | Doanh thu: " + revenue);

                    idList.add(productId);
                    quantityList.add(quantity);
                    revenueList.add(revenue);
                }
                // salesAdapter.setData(idList, quantityList, revenueList);
                // Cấu hình RecyclerView
                binding.recyclerView.setLayoutManager(new LinearLayoutManager(SalesSummaryActivity.this, RecyclerView.VERTICAL, false));
                binding.recyclerView.setHasFixedSize(true);

                ProductOrderAdapter adapter = new ProductOrderAdapter(idList, quantityList, revenueList);
                binding.recyclerView.setAdapter(adapter);
            }

            @Override
            public void onError(String error) {
                Log.e("SALES_ERROR", error);
            }
        });

    }
}