package com.example.kosplus.fragment;

import android.content.Intent;
import android.view.View;

import androidx.lifecycle.ViewModel;

import com.example.kosplus.features.CartsActivity;
import com.example.kosplus.features.LuckyWheelActivity;
import com.example.kosplus.features.OrdersManageActivity;

public class HomeFragmentVM extends ViewModel {

    public void onLuckyWheel(View view) {
        Intent intent = new Intent(view.getContext(), LuckyWheelActivity.class);
        view.getContext().startActivity(intent);
    }

    public void onItemCartManage(View view) {
        Intent intent = new Intent(view.getContext(), CartsActivity.class);
        view.getContext().startActivity(intent);
    }

    public void onOrderManage(View view) {
        Intent intent = new Intent(view.getContext(), OrdersManageActivity.class);
        view.getContext().startActivity(intent);
    }
}