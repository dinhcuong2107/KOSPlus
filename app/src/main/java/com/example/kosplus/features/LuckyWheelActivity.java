package com.example.kosplus.features;

import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.ContextCompat;
import androidx.databinding.DataBindingUtil;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bluehomestudio.luckywheel.WheelItem;
import com.example.kosplus.R;
import com.example.kosplus.adapter.LuckyRewardAdapter;
import com.example.kosplus.adapter.RewardHistoryAdapter;
import com.example.kosplus.databinding.ActivityLuckyWheelBinding;
import com.example.kosplus.func.Utils;
import com.example.kosplus.livedata.LuckyRewardsLiveData;
import com.example.kosplus.livedata.RewardHistoriesLiveData;
import com.example.kosplus.livedata.UserRewardHistoriesLiveData;
import com.example.kosplus.model.LuckyRewards;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class LuckyWheelActivity extends AppCompatActivity {
    private List<WheelItem> wheelItems;
    private List<LuckyRewards> list;
    private int targetIndex;
    private List<String> colors = Arrays.asList("#FFB16E","#FF6F61", "#6EC6FF","#A3D977","#D1C4E9","#F5E79E");
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivityLuckyWheelBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_lucky_wheel);
        LuckyWheelVM viewModel = new LuckyWheelVM();
        binding.setLuckywheel(viewModel);
        binding.setLifecycleOwner(this);

        Drawable drawable = ContextCompat.getDrawable(this, R.drawable.giftcard_24);
        Bitmap icon;
        if (drawable != null) {
            if (drawable instanceof BitmapDrawable) {
                icon = ((BitmapDrawable) drawable).getBitmap();
            } else {
                Bitmap bitmap = Bitmap.createBitmap(
                        drawable.getIntrinsicWidth(),
                        drawable.getIntrinsicHeight(),
                        Bitmap.Config.ARGB_8888
                );
                Canvas canvas = new Canvas(bitmap);
                drawable.setBounds(0, 0, canvas.getWidth(), canvas.getHeight());
                drawable.draw(canvas);
                icon = bitmap;
            }
        } else {
            icon = null;
        }

        wheelItems = new ArrayList<>();

        wheelItems.add(new WheelItem(Color.parseColor("#FFB16E"), icon, "Chúc may mắn"));
        wheelItems.add(new WheelItem(Color.parseColor("#FF6F61"), icon, "Chúc may mắn"));
        wheelItems.add(new WheelItem(Color.parseColor("#6EC6FF"), icon, "Chúc may mắn"));
        wheelItems.add(new WheelItem(Color.parseColor("#A3D977"), icon, "Chúc may mắn"));
        wheelItems.add(new WheelItem(Color.parseColor("#D1C4E9"), icon, "Chúc may mắn"));
        wheelItems.add(new WheelItem(Color.parseColor("#F5E79E"), icon, "Chúc may mắn"));

        binding.luckyWheel.addWheelItems(wheelItems);


        // Cấu hình RecyclerView
        binding.recyclerViewRewardHistory.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        binding.recyclerViewRewardHistory.setHasFixedSize(true);

        RewardHistoryAdapter historyAdapter = new RewardHistoryAdapter(new ArrayList<>());
        binding.recyclerViewRewardHistory.setAdapter(historyAdapter);
        binding.recyclerViewRewardHistory.animate().alpha(0f).setDuration(150).withEndAction(() -> {
                    historyAdapter.notifyDataSetChanged();
                    binding.recyclerViewRewardHistory.setAlpha(0f);
                    binding.recyclerViewRewardHistory.animate().alpha(1f).setDuration(150).start();
                }).start();

        // Quan sát dữ liệu từ LiveData
        RewardHistoriesLiveData historyLiveData = ViewModelProviders.of(this).get(RewardHistoriesLiveData.class);
        historyLiveData.getLiveData().observe(this, key -> {
            historyAdapter.updateData(key);
        });

        // Cấu hình RecyclerView
        binding.recyclerViewUserRewardHistory.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        binding.recyclerViewUserRewardHistory.setHasFixedSize(true);

        RewardHistoryAdapter userHistoryAdapter = new RewardHistoryAdapter(new ArrayList<>());
        binding.recyclerViewUserRewardHistory.setAdapter(userHistoryAdapter);

        // Quan sát dữ liệu từ LiveData
        UserRewardHistoriesLiveData userHistoryLiveData = ViewModelProviders.of(this).get(UserRewardHistoriesLiveData.class);
        userHistoryLiveData.getLiveData().observe(this, key -> {
            userHistoryAdapter.updateData(key);
        });

        // Cấu hình RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        binding.recyclerView.setHasFixedSize(true);

        LuckyRewardAdapter adapter = new LuckyRewardAdapter(new ArrayList<>());
        binding.recyclerView.setAdapter(adapter);

        // Quan sát dữ liệu từ LiveData
        LuckyRewardsLiveData luckyRewardLiveData = ViewModelProviders.of(this).get(LuckyRewardsLiveData.class);
        luckyRewardLiveData.getLiveData().observe(this, key -> {
            if (key != null && !key.isEmpty()) {
                adapter.updateData(key);
                binding.recyclerView.setVisibility(View.VISIBLE);
                list = key;
                wheelItems = new ArrayList<>();
                for (int i=0; i< key.size(); i++) {
                    wheelItems.add(new WheelItem(Color.parseColor(""+colors.get(i)), icon, ""+key.get(i).reward));
                }
                binding.luckyWheel.addWheelItems(wheelItems);
            } else {
                binding.recyclerView.setVisibility(View.GONE);
            }
        });

        binding.spin.setOnClickListener(v -> {

            targetIndex = getWeightedIndex(list);
            Log.d("LuckyWheel", "Selected index: " + targetIndex + " - " + list.get(targetIndex).reward);
            binding.luckyWheel.rotateWheelTo(targetIndex+1);
        });

        binding.luckyWheel.setLuckyWheelReachTheTarget(() -> {
            String reward = list.get(targetIndex).reward;

            Utils.showNotificationDialog(this, "","Thông báo", "Bạn đã trúng " + reward);
            // TODO: Firebase
            viewModel.saveRewardToFirebase(reward);
        });

    }
    private int getWeightedIndex(List<LuckyRewards> list) {
        int totalWeight = 0;

        for (LuckyRewards reward : list) {
            totalWeight += reward.point;
        }

        int randomValue = new Random().nextInt(totalWeight);
        int cumulative = 0;

        for (int i = 0; i < list.size(); i++) {
            cumulative += list.get(i).point;
            if (randomValue < cumulative) {
                return i;
            }
        }

        return 0; // fallback nếu lỗi
    }
}