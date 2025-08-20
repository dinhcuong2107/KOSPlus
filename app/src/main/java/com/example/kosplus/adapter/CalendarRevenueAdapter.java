package com.example.kosplus.adapter;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kosplus.databinding.ItemRevenueBinding;

import java.util.List;

public class CalendarRevenueAdapter extends RecyclerView.Adapter<CalendarRevenueAdapter.ItemRevenueViewHolder> {
    private List<Integer> days;
    private List<Integer> revenues;


    public CalendarRevenueAdapter(List<Integer> days, List<Integer> revenues) {
        this.days = days;
        this.revenues = revenues;
        notifyDataSetChanged();
    }

    public void updateData(List<Integer> days, List<Integer> revenues) {
        this.days = days;
        this.revenues = revenues;
        notifyDataSetChanged();
    }


    @NonNull
    @Override
    public ItemRevenueViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemRevenueBinding binding = ItemRevenueBinding.inflate(LayoutInflater.from(parent.getContext()), parent, false);
        return new CalendarRevenueAdapter.ItemRevenueViewHolder(binding);
    }

    @Override
    public void onBindViewHolder(@NonNull ItemRevenueViewHolder holder, int position) {
        String day = days.get(position).toString();
        String revenue = revenues.get(position).toString();

        Log.d("CALENDAR_REVENUE", "Day: " + day + " | Revenue: " + revenue);

        holder.binding.textviewDay.setText("" + position);
        holder.binding.textviewRevenue.setText("" + revenue);

    }

    @Override
    public int getItemCount() {
        if (days != null) {
            return days.size();
        }
        return 0;
    }

    public class ItemRevenueViewHolder extends RecyclerView.ViewHolder {
        ItemRevenueBinding binding;

        public ItemRevenueViewHolder(@NonNull ItemRevenueBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }
    }
}
