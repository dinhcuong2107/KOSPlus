package com.example.kosplus.adapter;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentActivity;
import androidx.viewpager2.adapter.FragmentStateAdapter;

import com.example.kosplus.fragment.DrinksFragment;
import com.example.kosplus.fragment.FoodsFragment;
import com.example.kosplus.fragment.HomeFragment;
import com.example.kosplus.fragment.NotificationsFragment;
import com.example.kosplus.fragment.SettingFragment;

public class ViewPagerAdapter extends FragmentStateAdapter {
    public ViewPagerAdapter(@NonNull FragmentActivity fragmentActivity) {
        super(fragmentActivity);
    }

    @NonNull
    @Override
    public Fragment createFragment(int position) {
        switch (position){
            case 0: return new HomeFragment();
            case 1: return new DrinksFragment();
            case 2: return new FoodsFragment();
            case 3: return new NotificationsFragment();
            case 4: return new SettingFragment();
            default:return new HomeFragment();
        }
    }

    @Override
    public int getItemCount() {
        return 5;
    }
}
