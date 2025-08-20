package com.example.kosplus.fragment;

import android.app.Application;
import android.content.Intent;
import android.view.View;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.databinding.BindingAdapter;
import androidx.databinding.ObservableField;
import androidx.lifecycle.AndroidViewModel;

import com.example.kosplus.LoginActivity;
import com.example.kosplus.R;
import com.example.kosplus.datalocal.DataLocalManager;
import com.example.kosplus.features.BannersManageActivity;
import com.example.kosplus.features.CartsActivity;
import com.example.kosplus.features.CheckInCodeActivity;
import com.example.kosplus.features.CheckInScannerActivity;
import com.example.kosplus.features.NotificationsManageActivity;
import com.example.kosplus.features.OrdersManageActivity;
import com.example.kosplus.features.ProductsManageActivity;
import com.example.kosplus.features.ProfileEditActivity;
import com.example.kosplus.features.PromotionsManageActivity;
import com.example.kosplus.features.SalesSummaryActivity;
import com.example.kosplus.features.ShopsManageActivity;
import com.example.kosplus.features.UsersManageActivity;
import com.example.kosplus.features.WalletActivity;
import com.example.kosplus.func.Utils;
import com.example.kosplus.model.Users;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.squareup.picasso.Picasso;

public class SettingFragmentVM extends AndroidViewModel {
    public ObservableField<Users> users= new ObservableField<>();
    public SettingFragmentVM(@NonNull Application application) {
        super(application);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("KOS Plus").child("Users").child(DataLocalManager.getUid());
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                users.set(snapshot.getValue(Users.class));
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    @BindingAdapter({"android:src"})
    public static void setImageView(ImageView imageView, String imgUrl){
        if (imgUrl==null){
            imageView.setImageResource(R.drawable.ic_launcher_background);
        }
        else {
            Picasso.get().load(imgUrl).into(imageView);
        }
    }

    public void onShowQR(View view) {
        Utils.showQRDialog(view.getContext(),users.get().id);
    }

    public void onLogOut(View view){
        DataLocalManager.setUid("");
        Intent intent = new Intent(view.getContext(), LoginActivity.class);
        view.getContext().startActivity(intent);
    }

    public void onEditProfile  (View view) {
        Intent intent = new Intent(view.getContext(), ProfileEditActivity.class);
        intent.putExtra("ID", users.get().id);  // Thay bằng userID thực tế
        view.getContext().startActivity(intent);
    }

    public void onCartsManage  (View view) {
        Intent intent = new Intent(view.getContext(), CartsActivity.class);
        view.getContext().startActivity(intent);
    }

    public void onProductsManage (View view) {
        Intent intent = new Intent(view.getContext(), ProductsManageActivity.class);
        view.getContext().startActivity(intent);
    }
    public void onOrderManage (View view) {
        Intent intent = new Intent(view.getContext(), OrdersManageActivity.class);
        view.getContext().startActivity(intent);
    }

    public void onNotificationsManage (View view) {
        Intent intent = new Intent(view.getContext(), NotificationsManageActivity.class);
        view.getContext().startActivity(intent);
    }
    public void onPromotionsManage (View view) {
        Intent intent = new Intent(view.getContext(), PromotionsManageActivity.class);
        view.getContext().startActivity(intent);
    }
    public void onSalesSummary  (View view) {
        Intent intent = new Intent(view.getContext(), SalesSummaryActivity.class);
        view.getContext().startActivity(intent);
    }

    public void onShopsManage  (View view) {
        Intent intent = new Intent(view.getContext(), ShopsManageActivity.class);
        view.getContext().startActivity(intent);
    }

    public void onWallet  (View view) {
        Intent intent = new Intent(view.getContext(), WalletActivity.class);
        view.getContext().startActivity(intent);
    }

    public void onBannersManage  (View view) {
        Intent intent = new Intent(view.getContext(), BannersManageActivity.class);
        view.getContext().startActivity(intent);
    }
    public void onUsersManage  (View view) {
        Intent intent = new Intent(view.getContext(), UsersManageActivity.class);
        view.getContext().startActivity(intent);
    }
    public void onCheckInCode (View view) {
        Intent intent = new Intent(view.getContext(), CheckInCodeActivity.class);
        view.getContext().startActivity(intent);
    }
    public void onCheckInScanner (View view) {
        Intent intent = new Intent(view.getContext(), CheckInScannerActivity.class);
        view.getContext().startActivity(intent);
    }
}
