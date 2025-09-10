package com.example.kosplus.features;

import android.app.Application;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.ViewModel;

import com.example.kosplus.func.Utils;
import com.example.kosplus.model.Orders;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class SalesSummaryVM extends AndroidViewModel {

    public final ObservableField<String> todaySummary = new ObservableField<>();
    public final ObservableField<String> weekSummary = new ObservableField<>();
    public final ObservableField<String> monthSummary = new ObservableField<>();

    private final DatabaseReference ordersRef;
    private final SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault());

    public SalesSummaryVM(@NonNull Application application) {
        super(application);
        ordersRef = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Orders");
        loadSalesSummary();
    }

    /**
     * Load dữ liệu thống kê hôm nay/tuần này/tháng này
     */
    public void loadSalesSummary() {
        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Orders> orders = new ArrayList<>();
                for (DataSnapshot child : snapshot.getChildren()) {
                    Orders order = child.getValue(Orders.class);
                    if (order != null && order.completedTime != null && order.status) {
                        orders.add(order);
                    }
                }
                summarizeSales(orders);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    private void summarizeSales(List<Orders> orders) {
        int todayRevenue = 0, todayQty = 0;
        int weekRevenue = 0, weekQty = 0;
        int monthRevenue = 0, monthQty = 0;

        Calendar now = Calendar.getInstance();

        for (Orders order : orders) {
            try {
                Date completedDate = sdf.parse(Utils.longToTimeString(order.completedTime));
                Calendar orderCal = Calendar.getInstance();
                orderCal.setTime(completedDate);

                int revenue = order.total;

                // Hôm nay
                if (isSameDay(orderCal, now)) {
                    todayRevenue += revenue;
                }

                // Tuần này
                if (isSameWeek(orderCal, now)) {
                    weekRevenue += revenue;
                }

                // Tháng này
                if (orderCal.get(Calendar.MONTH) == now.get(Calendar.MONTH)
                        && orderCal.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
                    monthRevenue += revenue;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        todaySummary.set("Hôm nay: " + formatMoney(todayRevenue));
        weekSummary.set("Tuần này: " + formatMoney(weekRevenue));
        monthSummary.set("Tháng này: " + formatMoney(monthRevenue));
    }

    private boolean isSameDay(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    private boolean isSameWeek(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.WEEK_OF_YEAR) == c2.get(Calendar.WEEK_OF_YEAR);
    }

    private String formatMoney(int amount) {
        return String.format(Locale.getDefault(), "%,d VNĐ", amount);
    }

    // ---------------------- Thống kê thêm ----------------------

    /** Doanh thu từng ngày trong tháng */
    public void getDailyRevenueInMonth(int month, int year, OnDailyRevenueCallback callback) {
        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<Integer, Integer> revenuePerDay = new HashMap<>();

                for (DataSnapshot child : snapshot.getChildren()) {
                    Orders order = child.getValue(Orders.class);
                    if (order == null || order.completedTime == 0 || !order.status) continue;

                    try {
                        Date date = sdf.parse(Utils.longToTimeString(order.completedTime));
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(date);

                        if ((cal.get(Calendar.MONTH) + 1) == month && cal.get(Calendar.YEAR) == year) {
                            int day = cal.get(Calendar.DAY_OF_MONTH);
                            revenuePerDay.put(day, revenuePerDay.getOrDefault(day, 0) + order.total);
                        }
                    } catch (Exception ignored) {}
                }

                callback.onResult(revenuePerDay);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }

    /** Doanh thu từng tháng trong năm */
    public void getMonthlyRevenueInYear(int year, OnMonthlyRevenueCallback callback) {
        ordersRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<Integer, Integer> revenuePerMonth = new HashMap<>();

                for (int i = 1; i <= 12; i++) revenuePerMonth.put(i, 0);

                for (DataSnapshot child : snapshot.getChildren()) {
                    Orders order = child.getValue(Orders.class);
                    if (order == null || order.completedTime == 0 || !order.status) continue;

                    try {
                        Date date = sdf.parse(Utils.longToTimeString(order.completedTime));
                        Calendar cal = Calendar.getInstance();
                        cal.setTime(date);

                        if (cal.get(Calendar.YEAR) == year) {
                            int month = cal.get(Calendar.MONTH) + 1;
                            revenuePerMonth.put(month, revenuePerMonth.get(month) + order.total);
                        }
                    } catch (Exception ignored) {}
                }

                callback.onResult(revenuePerMonth);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) { }
        });
    }


    // ---------------------- Callback ----------------------

    public interface OnDailyRevenueCallback {
        void onResult(Map<Integer, Integer> revenuePerDay);
    }

    public interface OnMonthlyRevenueCallback {
        void onResult(Map<Integer, Integer> revenuePerMonth);
    }
}
