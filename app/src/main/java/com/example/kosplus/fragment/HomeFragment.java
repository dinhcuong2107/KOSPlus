package com.example.kosplus.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
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
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.example.kosplus.R;
import com.example.kosplus.adapter.BannerAdapter;
import com.example.kosplus.adapter.ProductAdapter;
import com.example.kosplus.adapter.ProductVerticalAdapter;
import com.example.kosplus.databinding.ActivityHomeFragmentBinding;
import com.example.kosplus.func.OneSignalNotification;
import com.example.kosplus.livedata.BannerLiveData;
import com.example.kosplus.livedata.ProductsOnSaleLiveData;
import com.example.kosplus.livedata.ProductsSuggestionLiveData;
import com.example.kosplus.livedata.ProductsTopRevenueLiveData;
import com.example.kosplus.livedata.ProductsTopWeeklyRevenueLiveData;
import com.example.kosplus.model.Banners;
import com.onesignal.OneSignal;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {
    ActivityHomeFragmentBinding binding;
    List<Banners> bannerList;
    BannerAdapter adapter;
    private Handler handler = new Handler();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        binding = DataBindingUtil.inflate(inflater, R.layout.activity_home_fragment, container, false);
        HomeFragmentVM viewModel = new ViewModelProvider(this).get(HomeFragmentVM.class);
        binding.setHomeFragment(viewModel);
        binding.setLifecycleOwner(this);
        binding.executePendingBindings();

        Log.d("KOS Plus", "OneSignalDebug Subscription ID (Player ID): " + OneSignal.getUser().getPushSubscription().getId());

        binding.test.setOnClickListener( view -> {

//            OneSignalNotification.sendNotificationToAllUsers( "Thông báo từ hệ thống", "Thông báo thử nghiệm");

            OneSignalNotification.sendNotificationToUser("a7d3e137-e51c-4fdb-8300-6ef150ec22f9", "Thông báo cá nhân thử nghiệm", "Thông báo cá nhân");
        });

        bannerList = new ArrayList<>();

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

        BannerLiveData bannerLiveData = ViewModelProviders.of(this).get(BannerLiveData.class);
        bannerLiveData.getLiveData().observe(this.getViewLifecycleOwner(), new Observer<List<Banners>>() {
            @Override
            public void onChanged(List<Banners> banners) {
                if (banners == null || banners.isEmpty()) {
                    return;
                }
                bannerList.clear();
                bannerList.addAll(banners);

                adapter = new BannerAdapter(bannerList);
                binding.viewpager.setAdapter(adapter);
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
        binding.recyclerViewSuggestion.setLayoutManager(new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false));
        binding.recyclerViewSuggestion.setHasFixedSize(true);
        ProductVerticalAdapter productAdapterSuggestion = new ProductVerticalAdapter(new ArrayList<>());
        binding.recyclerViewSuggestion.setAdapter(productAdapterSuggestion);

        // Quan sát dữ liệu từ LiveData
        ProductsSuggestionLiveData suggestionLiveData = ViewModelProviders.of(this).get(ProductsSuggestionLiveData.class);
        suggestionLiveData.getLiveData().observe(this.getViewLifecycleOwner(), key -> {
            if (key != null && !key.isEmpty()) {
                productAdapterSuggestion.updateData(key);
            }
        });

        // Cấu hình RecyclerView
        binding.recyclerViewOnSale.setLayoutManager(new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false));
        binding.recyclerViewOnSale.setHasFixedSize(true);
        ProductVerticalAdapter productAdapterOnSale = new ProductVerticalAdapter(new ArrayList<>());
        binding.recyclerViewOnSale.setAdapter(productAdapterOnSale);

        // Quan sát dữ liệu từ LiveData
        ProductsOnSaleLiveData onSaleLiveData = ViewModelProviders.of(this).get(ProductsOnSaleLiveData.class);
        onSaleLiveData.getLiveData().observe(this.getViewLifecycleOwner(), key -> {
            if (key != null && !key.isEmpty()) {
                productAdapterOnSale.updateData(key);
            }
        });

        // Cấu hình RecyclerView
        binding.recyclerViewTopWeekly.setLayoutManager(new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false));
        binding.recyclerViewTopWeekly.setHasFixedSize(true);
        ProductVerticalAdapter productAdapterWeekly = new ProductVerticalAdapter(new ArrayList<>());
        binding.recyclerViewTopWeekly.setAdapter(productAdapterWeekly);

        // Quan sát dữ liệu từ LiveData
        ProductsTopWeeklyRevenueLiveData topWeeklyRevenueLiveData = ViewModelProviders.of(this).get(ProductsTopWeeklyRevenueLiveData.class);
        topWeeklyRevenueLiveData.getLiveData().observe(this.getViewLifecycleOwner(), key -> {
            if (key != null && !key.isEmpty()) {
                productAdapterWeekly.updateData(key);
            }
        });

        // Cấu hình RecyclerView
        binding.recyclerViewTop.setLayoutManager(new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false));
        binding.recyclerViewTop.setHasFixedSize(true);
        ProductVerticalAdapter productAdapter = new ProductVerticalAdapter(new ArrayList<>());
        binding.recyclerViewTop.setAdapter(productAdapter);

        // Quan sát dữ liệu từ LiveData
        ProductsTopRevenueLiveData top10LiveData = ViewModelProviders.of(this).get(ProductsTopRevenueLiveData.class);
        top10LiveData.getLiveData().observe(this.getViewLifecycleOwner(), key -> {
            if (key != null && !key.isEmpty()) {
                productAdapter.updateData(key);
            }
        });

        return binding.getRoot();
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