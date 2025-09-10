package com.example.kosplus.features;

import static androidx.recyclerview.widget.LinearLayoutManager.VERTICAL;

import android.graphics.Color;
import android.os.Bundle;
import android.util.Log;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.example.kosplus.R;
import com.example.kosplus.adapter.CalendarRevenueAdapter;
import com.example.kosplus.adapter.OrderItemAdapter;
import com.example.kosplus.databinding.ActivitySalesSummaryBinding;
import com.example.kosplus.livedata.OrdersLiveData;
import com.example.kosplus.model.OrderItems;
import com.example.kosplus.model.ProductSalesTotal;
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

        // Quan sát dữ liệu từ LiveData
        OrdersLiveData orderLiveData = ViewModelProviders.of(this).get(OrdersLiveData.class);
        // Tính thời gian tháng hiện tại
        calendar.set(Calendar.DAY_OF_MONTH, 1);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        long startOfMonth = calendar.getTimeInMillis();

        // Cuối tháng
        calendar.add(Calendar.MONTH, 1);
        calendar.add(Calendar.MILLISECOND, -1);
        long endOfMonth = calendar.getTimeInMillis();

        orderLiveData.getProductsAndRevenueInMonth(startOfMonth, endOfMonth)
                .observe(this, list -> {
                    if (list != null) {
                        List<OrderItems> orderItems = new ArrayList<>();
                        for (ProductSalesTotal pst : list) {
                            Log.d("REVENUE", "Product: " + pst.productId +
                                    " | Quantity: " + pst.totalQuantity +
                                    " | Revenue: " + pst.totalRevenue);
                            orderItems.add(new OrderItems("orderID", pst.productId, pst.totalQuantity, pst.totalRevenue));
                        }
                        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this, VERTICAL, false));
                        OrderItemAdapter orderItemAdapter = new OrderItemAdapter(orderItems);
                        binding.recyclerView.setAdapter(orderItemAdapter);
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