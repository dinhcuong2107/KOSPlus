package com.example.kosplus.features;

import android.util.Log;

import androidx.annotation.NonNull;
import androidx.databinding.ObservableField;
import androidx.lifecycle.ViewModel;

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

public class SalesSummaryVM extends ViewModel {
    public ObservableField<String> todaySummary = new ObservableField<>("");
    public ObservableField<String> weekSummary = new ObservableField<>("");
    public ObservableField<String> monthSummary = new ObservableField<>("");

    public SalesSummaryVM() {
        loadOrdersAndSummarize();
    }

    private void loadOrdersAndSummarize() {
        DatabaseReference ref = FirebaseDatabase.getInstance()
                .getReference("KOS Plus").child("Orders");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<Orders> completedOrders = new ArrayList<>();

                for (DataSnapshot snap : snapshot.getChildren()) {
                    Orders order = snap.getValue(Orders.class);
                    if (order != null && order.status) {
                        if (order.completedTime != null && !order.completedTime.isEmpty()) {
                            completedOrders.add(order);
                        }
                    }
                }

                summarizeSales(completedOrders);
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

        for (Orders order : orders)
            try {
                Date createdDate = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault())
                        .parse(order.createdTime);
                Calendar orderCal = Calendar.getInstance();
                orderCal.setTime(createdDate);

                int revenue = 0, quantity = 0;
                for (int i = 0; i < order.quantity.size(); i++) {
                    quantity += order.quantity.get(i);
                    revenue += order.quantity.get(i) * order.price.get(i);
                }

                // Kiểm tra theo ngày
                if (isSameDay(orderCal, now)) {
                    todayRevenue += revenue;
                    todayQty += quantity;
                }

                // Kiểm tra theo tuần
                if (isSameWeek(orderCal, now)) {
                    weekRevenue += revenue;
                    weekQty += quantity;
                }

                // Kiểm tra theo tháng
                if (orderCal.get(Calendar.MONTH) == now.get(Calendar.MONTH)
                        && orderCal.get(Calendar.YEAR) == now.get(Calendar.YEAR)) {
                    monthRevenue += revenue;
                    monthQty += quantity;
                }

            } catch (Exception e) {
                e.printStackTrace();
            }


        todaySummary.set("Hôm nay: " + todayQty + " sản phẩm, " + formatMoney(todayRevenue));
        weekSummary.set("Tuần này: " + weekQty + " sản phẩm, " + formatMoney(weekRevenue));
        monthSummary.set("Tháng này: " + monthQty + " sản phẩm, " + formatMoney(monthRevenue));
    }

    private boolean isSameDay(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    private boolean isSameWeek(Calendar c1, Calendar c2) {
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR)
                && c1.get(Calendar.WEEK_OF_YEAR) == c2.get(Calendar.WEEK_OF_YEAR);
    }

    private String formatMoney(int money) {
        return NumberFormat.getCurrencyInstance(new Locale("vi", "VN")).format(money);
    }

    public void getMonthlySalesWithRevenueRaw(int month, int year, OnResultCallback callback) {
        DatabaseReference orderRef = FirebaseDatabase.getInstance()
                .getReference("KOS Plus")
                .child("Orders");

        orderRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<String, int[]> salesMap = new HashMap<>(); // productId -> [quantity, revenue]
                SimpleDateFormat sdf = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault());

                for (DataSnapshot orderSnap : snapshot.getChildren()) {
                    Orders order = orderSnap.getValue(Orders.class);
                    if (order.completedTime != null && !order.completedTime.isEmpty() && order.status) {
                        if (order == null || order.createdTime == null) continue;

                        try {
                            Date date = sdf.parse(order.createdTime);
                            Calendar cal = Calendar.getInstance();
                            cal.setTime(date);
                            int orderMonth = cal.get(Calendar.MONTH) + 1;
                            int orderYear = cal.get(Calendar.YEAR);

                            if (orderMonth == month && orderYear == year) {
                                List<String> productIds = order.productId;
                                List<Integer> quantities = order.quantity;
                                List<Integer> prices = order.price;

                                for (int i = 0; i < productIds.size(); i++) {
                                    String pid = productIds.get(i);
                                    int qty = quantities.get(i);
                                    int price = prices.get(i);
                                    int revenue = qty * price;

                                    if (!salesMap.containsKey(pid)) {
                                        salesMap.put(pid, new int[]{qty, revenue});
                                    } else {
                                        int[] data = salesMap.get(pid);
                                        data[0] += qty;      // update quantity
                                        data[1] += revenue;  // update revenue
                                    }
                                }
                            }
                        } catch (ParseException e) {
                            e.printStackTrace();
                        }
                    }
                }

                callback.onResult(salesMap);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                callback.onError(error.getMessage());
            }
        });
    }
    public interface OnResultCallback {
        void onResult(Map<String, int[]> data);
        void onError(String error);
    }

    public void getDailyRevenueInMonth(int month, int year, OnDailyRevenueCallback callback) {
        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Orders");

        ref.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Map<Integer, Integer> revenuePerDay = new HashMap<>();

                for (DataSnapshot snap : snapshot.getChildren()) {
                    Orders order = snap.getValue(Orders.class);
                    if (order == null || order.createdTime == null || !order.status) continue;

                    try {
                        Date date = new SimpleDateFormat("HH:mm:ss dd/MM/yyyy", Locale.getDefault()).parse(order.createdTime);
                        Calendar calendar = Calendar.getInstance();
                        calendar.setTime(date);

                        int m = calendar.get(Calendar.MONTH) + 1;
                        int y = calendar.get(Calendar.YEAR);
                        int d = calendar.get(Calendar.DAY_OF_MONTH);

                        if (m == month && y == year) {
                            int oldRevenue = revenuePerDay.getOrDefault(d, 0);
                            revenuePerDay.put(d, oldRevenue + order.total);
                            Log.d("DAILY_REVENUE", "Day: " + d + ", Revenue: " + order.total);
                        }

                    } catch (ParseException e) {
                        e.printStackTrace();
                    }
                }

                callback.onResult(revenuePerDay);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {}
        });
    }
    public interface OnDailyRevenueCallback {
        void onResult(Map<Integer, Integer> data);
    }
}
