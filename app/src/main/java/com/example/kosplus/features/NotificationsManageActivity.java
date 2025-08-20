package com.example.kosplus.features;

import android.os.Bundle;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModel;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kosplus.R;
import com.example.kosplus.adapter.NotificationAdapter;
import com.example.kosplus.databinding.ActivityNotificationsManageBinding;
import com.example.kosplus.livedata.NotificationsLiveData;
import com.example.kosplus.model.Notifications;

import java.util.ArrayList;
import java.util.List;

public class NotificationsManageActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivityNotificationsManageBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_notifications_manage);
        NotificationsManageVM viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @Override
            public <T extends ViewModel> T create(Class<T> modelClass) {
                return (T) new NotificationsManageVM(getApplication(), getActivityResultRegistry());
            }
        }).get(NotificationsManageVM.class);

        binding.setNotificationsManage(viewModel);
        binding.executePendingBindings();
        binding.setLifecycleOwner(this);

        // Cấu hình RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        binding.recyclerView.setHasFixedSize(true);

        NotificationAdapter adapter = new NotificationAdapter(new ArrayList<>());
        binding.recyclerView.setAdapter(adapter);

        NotificationsLiveData liveData = ViewModelProviders.of(this).get(NotificationsLiveData.class);
        liveData.getLiveData().observe(this, new Observer<List<Notifications>>() {
            @Override
            public void onChanged(List<Notifications> notifications) {
                adapter.updateData(notifications);
            }
        });
    }
}