package com.example.kosplus.features;

import android.graphics.Color;
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
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.formatter.ValueFormatter;

import java.time.YearMonth;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SalesSummaryActivity extends AppCompatActivity {
    ActivitySalesSummaryBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_sales_summary);
        SalesSummaryVM viewModel = new ViewModelProvider(this).get(SalesSummaryVM.class);
        binding.setSalessummary(viewModel);
        binding.setLifecycleOwner(this);
        binding.executePendingBindings();

        Calendar calendar = Calendar.getInstance();
        int currentMonth = calendar.get(Calendar.MONTH) + 1; // Java đếm từ 0
        int currentYear = calendar.get(Calendar.YEAR);

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

        // Lấy doanh thu 12 tháng năm hiện tại
        viewModel.getMonthlyRevenueInYear(currentYear, revenuePerMonth -> {
            runOnUiThread(() -> drawMonthlyRevenueChart(revenuePerMonth, currentYear));
        });

        binding.currentmonth.setText("Tháng " + currentMonth + ", " + currentYear);

        viewModel.getMonthlySalesQuantity(currentMonth, currentYear, new SalesSummaryVM.OnQuantityCallback() {
            @Override
            public void onResult(Map<String, Integer> data) {
                if (data == null || data.isEmpty()) {
                    Log.d("TOP_PRODUCT", "Không có dữ liệu.");
                    return;
                }

                // Sắp xếp theo số lượng giảm dần
                List<Map.Entry<String, Integer>> sortedList = new ArrayList<>(data.entrySet());
                Collections.sort(sortedList, (e1, e2) -> Integer.compare(e2.getValue(), e1.getValue()));

                List<String> idList = new ArrayList<>();
                List<Integer> quantityList = new ArrayList<>();

                for (Map.Entry<String, Integer> entry : sortedList) {
                    idList.add(entry.getKey());
                    quantityList.add(entry.getValue());
                    Log.d("TOP_PRODUCT", "SP: " + entry.getKey() + " | SL: " + entry.getValue());
                }

                ProductOrderAdapter adapter = new ProductOrderAdapter(idList, quantityList, new ArrayList<>());
                binding.recyclerView.setLayoutManager(new LinearLayoutManager(SalesSummaryActivity.this));
                binding.recyclerView.setAdapter(adapter);
            }

            @Override
            public void onError(String error) {
                Log.e("SALES_ERROR", error);
            }
        });

    }

    private void drawMonthlyRevenueChart(Map<Integer, Integer> revenuePerMonth, int year) {
        List<BarEntry> entries = new ArrayList<>();

        // Thêm dữ liệu theo tháng 1..12
        for (int month = 1; month <= 12; month++) {
            int revenue = revenuePerMonth.getOrDefault(month, 0);
            entries.add(new BarEntry(month, revenue));
        }

        BarDataSet dataSet = new BarDataSet(entries, "Doanh thu " + year);
        dataSet.setColor(Color.parseColor("#4CAF50")); // xanh lá
        dataSet.setValueTextSize(10f);

        BarData barData = new BarData(dataSet);
        barData.setBarWidth(0.9f);

        binding.monthlyRevenueChart.setData(barData);
        binding.monthlyRevenueChart.setFitBars(true);
        binding.monthlyRevenueChart.getDescription().setEnabled(false);

        // Trục X hiển thị tháng
        XAxis xAxis = binding.monthlyRevenueChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setGranularity(1f);
        xAxis.setLabelCount(12);
        xAxis.setValueFormatter(new ValueFormatter() {
            private final String[] months = {"", "T1", "T2", "T3", "T4", "T5", "T6",
                    "T7", "T8", "T9", "T10", "T11", "T12"};

            @Override
            public String getFormattedValue(float value) {
                if (value >= 1 && value <= 12) {
                    return months[(int) value];
                } else return "";
            }
        });

        // Trục Y bên trái
        YAxis yAxisLeft = binding.monthlyRevenueChart.getAxisLeft();
        yAxisLeft.setAxisMinimum(0f);
        binding.monthlyRevenueChart.getAxisRight().setEnabled(false);

        // Animation
        binding.monthlyRevenueChart.animateY(1000);
        binding.monthlyRevenueChart.invalidate();
    }
}