package com.example.kosplus.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.kosplus.R;
import com.example.kosplus.adapter.NotificationAdapter;
import com.example.kosplus.databinding.ActivityNotificationsFragmentBinding;
import com.example.kosplus.livedata.NotificationsLiveData;
import com.example.kosplus.model.Notifications;

import java.util.ArrayList;
import java.util.List;

public class NotificationsFragment extends Fragment {

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        ActivityNotificationsFragmentBinding binding = DataBindingUtil.inflate(inflater, R.layout.activity_notifications_fragment,container,true);
        binding.setNotificationsFragment(new NotificationsFragmentVM());
        binding.setLifecycleOwner(this);
        binding.executePendingBindings();

        // Cấu hình RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this.getContext(), RecyclerView.VERTICAL, false));
        binding.recyclerView.setHasFixedSize(true);

        NotificationAdapter adapter = new NotificationAdapter(new ArrayList<>());
        binding.recyclerView.setAdapter(adapter);

        NotificationsLiveData liveData = ViewModelProviders.of(this).get(NotificationsLiveData.class);
        liveData.getLiveData().observe(this.getViewLifecycleOwner(), new Observer<List<Notifications>>() {
            @Override
            public void onChanged(List<Notifications> notifications) {
                if (notifications == null || notifications.isEmpty()) {
                    binding.recyclerView.setVisibility(View.GONE);
                    binding.textview.setVisibility(View.VISIBLE);
                } else {
                    binding.recyclerView.setVisibility(View.VISIBLE);
                    binding.textview.setVisibility(View.GONE);
                }
                adapter.updateData(notifications);
            }
        });
        return binding.getRoot();
    }
}