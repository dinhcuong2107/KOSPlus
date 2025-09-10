package com.example.kosplus.fragment;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.databinding.DataBindingUtil;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Observer;
import androidx.lifecycle.ViewModelProvider;
import androidx.lifecycle.ViewModelProviders;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.example.kosplus.R;
import com.example.kosplus.adapter.BannerAdapter;
import com.example.kosplus.adapter.ProductVerticalAdapter;
import com.example.kosplus.databinding.ActivityHomeFragmentBinding;
import com.example.kosplus.livedata.BannerLiveData;
import com.example.kosplus.livedata.ProductsLiveData;
import com.example.kosplus.model.Banners;
import com.example.kosplus.model.ProductSales;
import com.example.kosplus.model.Products;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.onesignal.OneSignal;

import java.util.ArrayList;
import java.util.Calendar;
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

        ProductsLiveData liveData = ViewModelProviders.of(this).get(ProductsLiveData.class);

        // Cấu hình RecyclerView
        binding.recyclerViewSuggestion.setLayoutManager(new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false));
        binding.recyclerViewSuggestion.setHasFixedSize(true);
        ProductVerticalAdapter productAdapterSuggestion = new ProductVerticalAdapter(new ArrayList<>(), false);
        binding.recyclerViewSuggestion.setAdapter(productAdapterSuggestion);

        // Quan sát dữ liệu từ LiveData
        liveData.getRandomProducts().observe(this.getViewLifecycleOwner(), key -> {
            if (key != null && !key.isEmpty()) {
                productAdapterSuggestion.updateData(key, false);
            } else {
                binding.layoutSuggestion.setVisibility(View.GONE);
            }
        });

        // Cấu hình RecyclerView
        binding.recyclerViewOnSale.setLayoutManager(new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false));
        binding.recyclerViewOnSale.setHasFixedSize(true);
        ProductVerticalAdapter productAdapterOnSale = new ProductVerticalAdapter(new ArrayList<>(),false);
        binding.recyclerViewOnSale.setAdapter(productAdapterOnSale);

        // Quan sát dữ liệu từ LiveData
        liveData.getActivePromotions().observe(this.getViewLifecycleOwner(), key -> {
            if (key != null && !key.isEmpty()) {
                productAdapterOnSale.updateData(key, false);
            } else {
                binding.layoutOnSale.setVisibility(View.GONE);
            }
        });

        // Cấu hình RecyclerView
        binding.recyclerViewTopWeekly.setLayoutManager(new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false));
        binding.recyclerViewTopWeekly.setHasFixedSize(true);
        ProductVerticalAdapter productAdapterWeekly = new ProductVerticalAdapter(new ArrayList<>(),true);
        binding.recyclerViewTopWeekly.setAdapter(productAdapterWeekly);

        // Thời gian hiện tại
        long now = System.currentTimeMillis();

        // 7 ngày trước
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(now);
        calendar.add(Calendar.DAY_OF_YEAR, -7);
        long sevenDaysAgo = calendar.getTimeInMillis();

        liveData.getTop10BestSellingProductsOfTime(sevenDaysAgo, now).observe(this.getViewLifecycleOwner(), key -> {
            if (key != null && !key.isEmpty()) {
                productAdapterWeekly.updateData(key, true);
            } else {
                binding.layoutTopWeekly.setVisibility(View.GONE);
            }
        });

        // Cấu hình RecyclerView
        binding.recyclerViewTop.setLayoutManager(new LinearLayoutManager(this.getContext(), LinearLayoutManager.VERTICAL, false));
        binding.recyclerViewTop.setHasFixedSize(true);
        ProductVerticalAdapter productAdapter = new ProductVerticalAdapter(new ArrayList<>(), true);
        binding.recyclerViewTop.setAdapter(productAdapter);

        liveData.getTopProducts().observe(this.getViewLifecycleOwner(), list -> {
            if (list != null && !list.isEmpty()) {
                productAdapter.updateData(list, true);
            } else {
                binding.layoutTop.setVisibility(View.GONE);
                Log.d("TOP_PRODUCT", "Không có dữ liệu");
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