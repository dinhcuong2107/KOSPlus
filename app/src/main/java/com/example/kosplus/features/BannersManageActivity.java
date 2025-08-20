package com.example.kosplus.features;

import android.os.Bundle;
import android.os.Handler;
import android.view.View;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
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
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.example.kosplus.R;
import com.example.kosplus.adapter.BannerAdapter;
import com.example.kosplus.adapter.BannerManageAdapter;
import com.example.kosplus.databinding.ActivityBannersManageBinding;
import com.example.kosplus.livedata.BannerLiveData;
import com.example.kosplus.livedata.BannerManageLiveData;
import com.example.kosplus.model.Banners;

import java.util.ArrayList;
import java.util.List;

public class BannersManageActivity extends AppCompatActivity {
    ActivityBannersManageBinding binding;
    private Handler handler = new Handler();
    List<Banners> bannerList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);

        binding = DataBindingUtil.setContentView(this, R.layout.activity_banners_manage);
        BannersManageVM viewModel = new ViewModelProvider(this, new ViewModelProvider.Factory() {
            @Override
            public <T extends ViewModel> T create(Class<T> modelClass) {
                return (T) new BannersManageVM(getApplication(), getActivityResultRegistry());
            }
        }).get(BannersManageVM.class);

        binding.setBannersManage(viewModel);
        binding.executePendingBindings();



        binding.viewpager.setOffscreenPageLimit(3);
        binding.viewpager.setClipChildren(false);
        binding.viewpager.setClipToPadding(false);

        CompositePageTransformer compositePageTransformer = new CompositePageTransformer();
        compositePageTransformer.addTransformer(new MarginPageTransformer(40));
        compositePageTransformer.addTransformer(new ViewPager2.PageTransformer() {
            @Override
            public void transformPage(@NonNull View page, float position) {
                float r = 1 - Math.abs(position);
                page.setScaleY((float) (0.85 + r * 0.15f));
            }
        });
        binding.viewpager.setPageTransformer(compositePageTransformer);

        binding.viewpager.setCurrentItem(0, true);
        handler.postDelayed(runnable, 3000);

        bannerList = new ArrayList<>();
        BannerAdapter bannerAdapter = new BannerAdapter(bannerList);
        binding.viewpager.setAdapter(bannerAdapter);
        binding.circleIndicator.setViewPager(binding.viewpager);

        BannerLiveData bannerLiveData = ViewModelProviders.of(this).get(BannerLiveData.class);
        bannerLiveData.getLiveData().observe(this, new Observer<List<Banners>>() {
            @Override
            public void onChanged(List<Banners> banners) {
                if (banners == null || banners.isEmpty()) {
                    return;
                }
                bannerList.clear();
                bannerList.addAll(banners);

                bannerAdapter.updateData(bannerList);
                binding.viewpager.setAdapter(bannerAdapter);
                binding.circleIndicator.setViewPager(binding.viewpager);
            }
        });

        binding.viewpager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
//            @Override
//            public void onPageScrollStateChanged(int state) {
//                super.onPageScrollStateChanged(state);
//
//                if (state == ViewPager2.SCROLL_STATE_DRAGGING) {
//                    // Người dùng đang vuốt → tạm dừng auto-scroll
//                    handler.removeCallbacks(runnable);
//                } else if (state == ViewPager2.SCROLL_STATE_IDLE) {
//                    // Dừng vuốt → tiếp tục auto-scroll sau 3s
//                    handler.postDelayed(runnable, 3000);
//                }
//            }

            @Override
            public void onPageSelected(int position) {
                super.onPageSelected(position);
                // Optional: reset lại thời gian mỗi khi chọn trang mới
                handler.removeCallbacks(runnable);
                handler.postDelayed(runnable, 3000);
            }
        });


        // Cấu hình RecyclerView
        binding.recyclerView.setLayoutManager(new LinearLayoutManager(this, RecyclerView.VERTICAL, false));
        binding.recyclerView.setHasFixedSize(true);

        BannerManageAdapter adapter = new BannerManageAdapter(new ArrayList<>());
        binding.recyclerView.setAdapter(adapter);

        BannerManageLiveData bannerManageLiveData = ViewModelProviders.of(this).get(BannerManageLiveData.class);
        bannerManageLiveData.getLiveData().observe(this, new Observer<List<Banners>>() {
            @Override
            public void onChanged(List<Banners> banners) {
                adapter.updateData(banners);
            }
        });
    }

    private Runnable runnable = new Runnable() {
        @Override
        public void run() {
            if (binding.viewpager.getCurrentItem() == bannerList.size() - 1) {
                binding.viewpager.setCurrentItem(0);
            } else {
                binding.viewpager.setCurrentItem(binding.viewpager.getCurrentItem() + 1);
            }
        }
    };

    @Override
    public void onPause() {
        super.onPause();
        handler.removeCallbacks(runnable);
    }

    @Override
    public void onResume() {
        super.onResume();
        handler.postDelayed(runnable, 3000);
    }
}