package com.example.kosplus.features;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kosplus.R;
import com.example.kosplus.adapter.TransactionHistoryAdapter;
import com.example.kosplus.databinding.ActivityWalletBinding;
import com.example.kosplus.livedata.TransactionHistoriesLiveData;
import com.example.kosplus.model.TransactionHistory;

import java.util.ArrayList;
import java.util.List;

public class WalletActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivityWalletBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_wallet);
        binding.setWallet(new WalletVM());
        binding.setLifecycleOwner(this);
        binding.executePendingBindings();

        // Cấu hình RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        binding.recyclerView.setHasFixedSize(true);

        TransactionHistoryAdapter adapter = new TransactionHistoryAdapter(new ArrayList<>());
        binding.recyclerView.setAdapter(adapter);

        TransactionHistoriesLiveData liveData = ViewModelProviders.of(this).get(TransactionHistoriesLiveData.class);
        liveData.getLiveData().observe(this, new Observer<List<TransactionHistory>>() {
            @Override
            public void onChanged(List<TransactionHistory> transactions) {
                adapter.updateData(transactions);
            }
        });

    }
}