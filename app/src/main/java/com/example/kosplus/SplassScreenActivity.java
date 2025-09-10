package com.example.kosplus;

import android.content.Intent;
import android.database.DatabaseUtils;
import android.os.Bundle;
import android.os.Handler;
import android.view.animation.AccelerateDecelerateInterpolator;
import android.view.animation.DecelerateInterpolator;
import android.view.animation.OvershootInterpolator;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.databinding.DataBindingUtil;

import com.example.kosplus.databinding.ActivitySplassScreenBinding;
import com.example.kosplus.features.CartsActivity;
import com.example.kosplus.features.OrdersManageActivity;
import com.example.kosplus.features.ShopsManageActivity;
import com.example.kosplus.func.Utils;
import com.example.kosplus.model.Notifications;
import com.example.kosplus.model.OrderItems;
import com.example.kosplus.model.Orders;
import com.example.kosplus.model.ProductsRepository;
import com.example.kosplus.model.PromotionsRepository;
import com.example.kosplus.model.UsersRepository;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

public class SplassScreenActivity extends AppCompatActivity {

    private ProductsRepository repository;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        ActivitySplassScreenBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_splass_screen);


//        DatabaseReference ref = FirebaseDatabase.getInstance().getReference("KOS Plus").child("OrderItems");
//
//        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Orders");
//        databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
//            @Override
//            public void onDataChange(@NonNull DataSnapshot snapshot) {
//                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
//                    Orders orders = dataSnapshot.getValue(Orders.class);
//
//                    databaseReference.child(orders.id).child("productId").removeValue();
//                    databaseReference.child(orders.id).child("quantity").removeValue();
//                    databaseReference.child(orders.id).child("price").removeValue();
//                }
//            }
//
//            @Override
//            public void onCancelled(@NonNull DatabaseError error) {
//
//            }
//        });


        repository = new ProductsRepository(getApplication());

        // Preload dữ liệu Firebase → Room
        repository.preloadProducts();

        PromotionsRepository promotionsRepository = new PromotionsRepository(getApplication());
        promotionsRepository.preloadPromotions();

        // 1. Logo zoom nhẹ + fade-in
        binding.logo.setScaleX(0.5f);
        binding.logo.setScaleY(0.5f);
        binding.logo.setAlpha(0f);

        binding.logo.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setDuration(1000)
                .setInterpolator(new AccelerateDecelerateInterpolator())
                .start();

        // 2. appName: zoom nhẹ + fade + bounce
        binding.name.setScaleX(0.5f);
        binding.name.setScaleY(0.5f);
        binding.name.setAlpha(0f);
        binding.name.animate()
                .scaleX(1f)
                .scaleY(1f)
                .alpha(1f)
                .setStartDelay(900)
                .setDuration(800)
                .setInterpolator(new OvershootInterpolator()) // hiệu ứng nảy nhẹ
                .start();

        // 3. slogan: slide từ dưới lên + fade-in
        binding.slogan.setTranslationY(100);
        binding.slogan.setAlpha(0f);
        binding.slogan.animate()
                .translationY(0)
                .alpha(1f)
                .setStartDelay(1600)
                .setDuration(700)
                .setInterpolator(new DecelerateInterpolator())
                .start();
        // Chuyển sau 3 giây
        new Handler().postDelayed(() -> {
            startActivity(new Intent(this, MainActivity.class));
            finish();
        }, 3000);
    }
}